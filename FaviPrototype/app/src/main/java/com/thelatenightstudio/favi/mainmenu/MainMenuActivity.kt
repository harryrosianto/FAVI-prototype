package com.thelatenightstudio.favi.mainmenu

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.addfundmenu.AddFundActivity
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.model.User
import com.thelatenightstudio.favi.core.media.Recorder
import com.thelatenightstudio.favi.core.service.ResetPredictionService
import com.thelatenightstudio.favi.core.service.SignOutService
import com.thelatenightstudio.favi.core.utils.FileHelper.recordFile
import com.thelatenightstudio.favi.core.utils.InternetHelper.isConnected
import com.thelatenightstudio.favi.core.utils.LiveDataHelper.observeOnce
import com.thelatenightstudio.favi.core.utils.NumberHelper.formatAsBalance
import com.thelatenightstudio.favi.core.utils.PermissionHelper.checkAudioPermission
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivityMainMenuBinding
import com.thelatenightstudio.favi.transfermenu.TransferMenuActivity
import com.thelatenightstudio.favi.voicerecording.VoiceRecordingActivity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainMenuActivity : AppCompatActivity() {

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var binding: ActivityMainMenuBinding

    private val viewModel: MainMenuViewModel by viewModel()
    private val recorder: Recorder by inject()

    private var upKeyCount = 0
    private var waitingForPrediction = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAudioPermission(AUDIO_PERMISSION_REQUEST_CODE)
        val resetPredictionService = Intent(this, ResetPredictionService::class.java)
        ResetPredictionService.enqueueWork(this, resetPredictionService)

        lifecycleScope.launch {
            if (isConnected()) {
                (IO){ viewModel.getDataOfCurrentUser() }
                    .observeOnce(this@MainMenuActivity, getDataObserver())
            } else {
                showToast(getString(R.string.no_internet))
            }
        }

        binding.btnActivateBiometric.setOnClickListener {
            lifecycleScope.launch(IO) {
                val text =
                    if (viewModel.activateBiometric()) getString(R.string.biometric_activated)
                    else getString(R.string.biometric_deactivated)
                showToast(text)
            }
        }

        binding.btnAddFund.setOnClickListener {
            val intent = Intent(this, AddFundActivity::class.java)
            startActivity(intent)
        }
        binding.btnBalanceTransfer.setOnClickListener {
            val intent = Intent(this, TransferMenuActivity::class.java)
            startActivity(intent)
        }
        binding.btnVoiceRecording.setOnClickListener {
            val intent = Intent(this, VoiceRecordingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getDataObserver() =
        Observer<ApiResponse<User>> { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val data = response.data
                    with(binding) {
                        tvUserName.text = getString(R.string.username_format, data.email)
                        tvUserBalance.text = data.balance.formatAsBalance()
                        usernameLayout.visibility = VISIBLE
                    }

                    lifecycleScope.launch {
                        (IO){ viewModel.getRealtimeUpdatesOfCurrentUser() }
                            .observe(this@MainMenuActivity, getRealtimeObserver())
                    }
                }
                is ApiResponse.Error -> {
                    val text = response.errorMessage ?: getString(R.string.error)
                    lifecycleScope.launch { showToast(text) }
                }
                is ApiResponse.Empty -> {
                    val text = getString(R.string.empty)
                    lifecycleScope.launch { showToast(text) }
                }
            }
            binding.progressBar.visibility = GONE
        }

    private fun getRealtimeObserver() =
        Observer<ApiResponse<User>> { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val realtimeData = response.data
                    with(binding) {
                        tvUserName.text = getString(
                            R.string.username_format,
                            realtimeData.email
                        )
                        tvUserBalance.text =
                            realtimeData.balance.formatAsBalance()
                    }
                    if (waitingForPrediction)
                        lifecycleScope.launch {
                            waitingForPrediction = false
                            binding.voiceRecordingProgressBar.visibility = GONE

                            val resetPredictionService =
                                Intent(this@MainMenuActivity, ResetPredictionService::class.java)
                            ResetPredictionService.enqueueWork(
                                this@MainMenuActivity,
                                resetPredictionService
                            )

                            showToast(realtimeData.prediction)
                        }
                }
                is ApiResponse.Error -> {
                    val text =
                        response.errorMessage ?: getString(R.string.error)
                    lifecycleScope.launch { showToast(text) }
                }
                is ApiResponse.Empty -> {
                    val text = getString(R.string.empty)
                    lifecycleScope.launch { showToast(text) }
                }
            }
        }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            upKeyCount++
            val text = when (upKeyCount) {
                1 -> getString(R.string.up_key_pressed_once)
                2 -> getString(R.string.up_key_pressed_twice)
                else -> getString(R.string.voice_is_being_recorded)
            }
            lifecycleScope.launch {
                showToast(text)
                if (upKeyCount >= 3) {
                    recorder.toggleRecording()
                }
            }
            return true

        } else if (applicationInfo.targetSdkVersion >= Build.VERSION_CODES.ECLAIR) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event!!.isTracking
                && !event.isCanceled
            ) {
                onBackPressed()
                return true
            }
        }

        return false
    }

    override fun onStart() {
        listenOnRecorderStates()
        super.onStart()
    }

    private fun listenOnRecorderStates() {
        recorder.init().apply {
            onStart = {
                lifecycleScope.launch {
                    delay(900L)
                    toggleRecording()
                }
            }
            onStop = {
                lifecycleScope.launch {
                    showToast(getString(R.string.voice_processing))
                    binding.voiceRecordingProgressBar.visibility = VISIBLE
                    (IO){
                        val filePath = applicationContext.recordFile.toString()
                        viewModel.uploadFile(filePath)
                    }.observeOnce(this@MainMenuActivity, { response ->
                        when (response) {
                            is ApiResponse.Success -> {
                                waitingForPrediction = true
                            }
                            is ApiResponse.Error -> {
                                val text = response.errorMessage
                                    ?: getString(R.string.error)
                                lifecycleScope.launch { showToast(text) }
                            }
                            is ApiResponse.Empty -> {
                                val text = getString(R.string.empty)
                                lifecycleScope.launch { showToast(text) }
                            }
                        }
                    })
                }
            }
            onAmpListener = {}
        }
    }

    override fun onStop() {
        recorder.release()
        super.onStop()
    }

    override fun onDestroy() {
        val signOutService = Intent(this@MainMenuActivity, SignOutService::class.java)
        SignOutService.enqueueWork(this@MainMenuActivity, signOutService)
        super.onDestroy()
    }
}