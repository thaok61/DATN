package com.thao.bluetoothapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.*
import android.media.AudioAttributes.*
import android.media.AudioFormat.*
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.thao.bluetoothapp.databinding.FragmentDeviceControlPanelBinding
import com.thao.bluetoothapp.utils.PERMISSION_REQUEST_CODE
import com.thao.bluetoothapp.utils.TAG


class DeviceControlPanelFragment : Fragment() {

    private lateinit var binding: FragmentDeviceControlPanelBinding

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    private var intBufferSize = 0
    private var shortAudioData: ShortArray? = null

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
            Log.d(TAG, "onViewCreated: CLICKED")
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
    }

    private fun threadLoop() {
        val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)

        intBufferSize = AudioRecord.getMinBufferSize(
            intRecordSampleRate / 4,
            CHANNEL_IN_STEREO,
            ENCODING_PCM_16BIT
        ) / 4

        shortAudioData = ShortArray(size = intBufferSize)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            intRecordSampleRate,
            CHANNEL_IN_STEREO,
            ENCODING_PCM_16BIT,
            intBufferSize
        )


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

        while (isActive) {
            audioRecord?.read(shortAudioData!!, 0, shortAudioData!!.size)

            for (i in shortAudioData!!.indices) {
                shortAudioData!![i] =
                    (shortAudioData!![i] * intGain).coerceAtMost(Short.MAX_VALUE.toInt()).toShort()
            }

            audioTrack?.write(shortAudioData!!, 0, shortAudioData!!.size)
        }

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
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSION_REQUEST_CODE
                    )
                }
                return
            }
        }
    }

}