package com.thelatenightstudio.favi.voicerecording

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.media.ModelWithAudioRecord
import com.thelatenightstudio.favi.core.media.Recorder
import com.thelatenightstudio.favi.core.utils.DrawableHelper.getDrawableCompat
import com.thelatenightstudio.favi.core.utils.FileHelper.recordFile
import com.thelatenightstudio.favi.core.utils.NumberHelper.formatAsTime
import com.thelatenightstudio.favi.core.utils.PermissionHelper.checkAudioPermission
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivityVoiceRecordingBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.sqrt

class VoiceRecordingActivity : AppCompatActivity() {

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var binding: ActivityVoiceRecordingBinding
    private val recorder: Recorder by inject()
    private val modelRecorder: ModelWithAudioRecord by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceRecordingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAudioPermission(AUDIO_PERMISSION_REQUEST_CODE)

        initUI()
    }

    private fun initUI() = with(binding) {
        recordButton.setOnClickListener { modelRecorder.toggleRecording() }
        visualizer.ampNormalizer = { sqrt(it.toFloat()).toInt() }
    }

    override fun onStart() {
        super.onStart()
        listenOnRecorderStates()
    }

    private fun listenOnRecorderStates() = with(binding) {
        modelRecorder.init().apply {
            onStart = { recordButton.icon = getDrawableCompat(R.drawable.ic_stop_24) }
            onStop = {
                visualizer.clear()
                timelineTextView.text = 0L.formatAsTime()
                recordButton.icon = getDrawableCompat(R.drawable.ic_record_24)

                Log.d("Cek", "listenOnRecorderStates: ${applicationContext.recordFile}")
                lifecycleScope.launch { showToast("RECORD BERHASIL") }
            }
            onAmpListener = {
                runOnUiThread {
                    if (recorder.isRecording) {
                        timelineTextView.text = recorder.getCurrentTime().formatAsTime()
                        visualizer.addAmp(it, 1)
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