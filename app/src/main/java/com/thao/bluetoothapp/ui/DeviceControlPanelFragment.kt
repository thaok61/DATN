package com.thao.bluetoothapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.*
import android.media.AudioAttributes.CONTENT_TYPE_SPEECH
import android.media.AudioAttributes.USAGE_MEDIA
import android.media.AudioFormat.*
import android.media.audiofx.NoiseSuppressor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.paramsen.noise.Noise
import com.thao.bluetoothapp.databinding.FragmentDeviceControlPanelBinding
import com.thao.bluetoothapp.utils.PERMISSION_REQUEST_CODE
import com.thao.bluetoothapp.utils.TAG
import com.thao.bluetoothapp.viewmodel.DeviceViewModel
import kotlin.math.pow


class DeviceControlPanelFragment : Fragment() {

    private lateinit var binding: FragmentDeviceControlPanelBinding
    private val deviceViewModel: DeviceViewModel by activityViewModels()

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    private var intBufferSize = 0
    private var shortAudioData: ShortArray? = null
    private var shortDenoisedAudioData: FloatArray? = null

    private var intGain = 1
    private var isActive = true

    private var thread: Thread? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDeviceControlPanelBinding.inflate(layoutInflater)

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                // You can use the API that requires the permission.
                Toast.makeText(requireContext(), "GRANTED RECORD AUDIO", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnMic.setOnClickListener {
            if (binding.btnMic.text == "ON") {
                thread = Thread {
                    threadLoop()
                }
                thread?.start()
                isActive = true
                binding.btnMic.text = "OFF"
            } else {
                isActive = false
                Log.d(TAG, "onViewCreated: ${thread?.state}")
                binding.btnMic.text = "ON"
                audioTrack?.stop()
                audioRecord?.stop()
                Log.d(TAG, "onViewCreated: ${thread?.isAlive}")
            }
        }


        deviceViewModel.isConnected.observe(viewLifecycleOwner, {
            if (it == false) {
                isActive = false
                Log.d(TAG, "onViewCreated: ${thread?.state}")
                binding.btnMic.text = "ON"
                audioTrack?.stop()
                audioRecord?.stop()
                Log.d(TAG, "onViewCreated: ${thread?.isAlive}")
            }
        })

    }

    private fun threadLoop() {
        val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)

        intBufferSize = AudioRecord.getMinBufferSize(
            intRecordSampleRate / 4,
            CHANNEL_IN_STEREO,
            ENCODING_PCM_16BIT
        )
        if (intBufferSize % 2 != 0) intBufferSize -= 1

        shortAudioData = ShortArray(size = intBufferSize)
        shortDenoisedAudioData = FloatArray(size = intBufferSize)
        val PSD = FloatArray(size = intBufferSize)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            intRecordSampleRate,
            CHANNEL_IN_STEREO,
            ENCODING_PCM_16BIT,
            intBufferSize
        )
        NoiseSuppressor.create(audioRecord!!.audioSessionId)
        Log.d(TAG, "threadLoop: ${NoiseSuppressor.isAvailable()}")


        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(USAGE_MEDIA)
                    .setContentType(CONTENT_TYPE_SPEECH).build()
            )
            .setAudioFormat(
                AudioFormat.Builder().setEncoding(ENCODING_PCM_16BIT)
                    .setSampleRate(intRecordSampleRate)
                    .setChannelMask(CHANNEL_IN_STEREO).build()
            ).build()


        audioTrack!!.playbackRate = intRecordSampleRate
        audioRecord!!.startRecording()
        audioTrack!!.play()

        val noise = Noise.imaginary(intBufferSize * 2)

        while (isActive) {
            audioRecord?.read(shortAudioData!!, 0, shortAudioData!!.size)

            val floatShortAudio = FloatArray(intBufferSize * 2) {
                0F
            }

            for (i in 0 until floatShortAudio.size / 2) {
                floatShortAudio[i * 2] = shortAudioData!![i].toFloat()
            }
            val fft: FloatArray =
                noise.fft(floatShortAudio, FloatArray(intBufferSize * 2))

            for (i in 0 until fft.size / 2) {
                val real = fft[i * 2]
                val imaginary = fft[i * 2 + 1]
                PSD[i] = real.pow(2) + imaginary.pow(2)

                fft[i * 2 + 1] = -fft[i * 2 + 1]

            }
            val deFFT = noise.fft(fft, FloatArray(intBufferSize * 2))
            for (i in 0 until deFFT.size / 2) {
                shortDenoisedAudioData!![i] = (deFFT[i * 2] * 2 / (deFFT.size))
//                if (PSD[i] < 1E8) {
//                    shortDenoisedAudioData!![i] = 0F
//                }
            }

            Log.d(
                TAG,
                "threadLoop: ${shortDenoisedAudioData!![0]} - ${shortAudioData!![0]} - ${PSD[0]}"
            )
//            audioTrack?.write(
//                shortDenoisedAudioData!!,
//                0,
//                shortDenoisedAudioData!!.size,
//                WRITE_NON_BLOCKING
//            )
            audioTrack?.write(shortMe(shortDenoisedAudioData!!), 0, shortDenoisedAudioData!!.size)
        }
    }

    fun shortMe(floats: FloatArray): ShortArray {
        val out = ShortArray(floats.size) // will drop last byte if odd number
        for (i in out.indices) {
            out[i] = floats[i].toInt().toShort()
        }
        return out
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Toast.makeText(requireContext(), "Granted All", Toast.LENGTH_SHORT).show()
//                    thread = Thread { threadLoop() }
                } else {
                    requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSION_REQUEST_CODE
                    )
                }
                return
            }
        }
    }

}