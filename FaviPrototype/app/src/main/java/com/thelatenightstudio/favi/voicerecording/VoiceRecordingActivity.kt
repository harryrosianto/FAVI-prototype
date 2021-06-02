package com.thelatenightstudio.favi.voicerecording

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.media.Recorder
import com.thelatenightstudio.favi.core.utils.DrawableHelper.getDrawableCompat
import com.thelatenightstudio.favi.core.utils.FileHelper.recordFile
import com.thelatenightstudio.favi.core.utils.NumberHelper.formatAsTime
import com.thelatenightstudio.favi.core.utils.PermissionHelper.checkAudioPermission
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivityVoiceRecordingBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.sqrt

class VoiceRecordingActivity : AppCompatActivity() {

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var binding: ActivityVoiceRecordingBinding
    private val recorder: Recorder by inject()
    private val viewModel: VoiceRecordingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceRecordingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAudioPermission(AUDIO_PERMISSION_REQUEST_CODE)

        initUI()
    }

    private fun initUI() = with(binding) {
        recordButton.setOnClickListener { recorder.toggleRecording() }
        visualizer.ampNormalizer = { sqrt(it.toFloat()).toInt() }
    }

    override fun onStart() {
        super.onStart()
        listenOnRecorderStates()
    }

    private fun listenOnRecorderStates() = with(binding) {
        recorder.init().apply {
            onStart = {
                recordButton.icon = getDrawableCompat(R.drawable.ic_stop_24)
            }
            onStop = {
                visualizer.clear()
                timelineTextView.text = 0L.formatAsTime()
                recordButton.icon = getDrawableCompat(R.drawable.ic_record_24)

                lifecycleScope.launch {
                    showToast(getString(R.string.voice_uploading))
                    (IO){
                        val filePath = applicationContext.recordFile.toString()
                        viewModel.uploadFile(filePath)
                    }.observe(this@VoiceRecordingActivity, { response ->
                        val text = when (response) {
                            is ApiResponse.Success -> {
                                getString(R.string.voice_complete)
                            }
                            is ApiResponse.Error -> {
                                response.errorMessage
                                    ?: getString(R.string.error)
                            }
                            is ApiResponse.Empty -> {
                                getString(R.string.empty)
                            }
                        }
                        lifecycleScope.launch { showToast(text) }
                    })
                }
            }
            onAmpListener = {
                runOnUiThread {
                    if (recorder.isRecording) {
                        timelineTextView.text = recorder.getCurrentTime().formatAsTime()
                        visualizer.addAmp(it, tickDuration)
                    }
                }
            }
        }
    }

    override fun onStop() {
        recorder.release()
        super.onStop()
    }

}