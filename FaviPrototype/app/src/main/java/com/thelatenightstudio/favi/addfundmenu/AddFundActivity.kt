package com.thelatenightstudio.favi.addfundmenu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.jakewharton.rxbinding2.widget.RxTextView
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.model.User
import com.thelatenightstudio.favi.core.media.Recorder
import com.thelatenightstudio.favi.core.service.ResetPredictionService
import com.thelatenightstudio.favi.core.utils.EditTextHelper.showEditTextExistAlert
import com.thelatenightstudio.favi.core.utils.FileHelper.recordFile
import com.thelatenightstudio.favi.core.utils.HideKeyboardHelper.hideKeyboard
import com.thelatenightstudio.favi.core.utils.InternetHelper.isConnected
import com.thelatenightstudio.favi.core.utils.LiveDataHelper.observeOnce
import com.thelatenightstudio.favi.core.utils.PermissionHelper.checkAudioPermission
import com.thelatenightstudio.favi.core.utils.StringHelper.NOT_A_NUMBER
import com.thelatenightstudio.favi.core.utils.StringHelper.toNumber
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivityAddFundBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddFundActivity : AppCompatActivity() {

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 100

        private const val TAMBAH = "tambah"
        private const val TRANSFER = "transfer"
    }

    private lateinit var binding: ActivityAddFundBinding

    private val viewModel: AddFundViewModel by viewModel()
    private val recorder: Recorder by inject()

    private var upKeyCount = 0
    private var waitingForPrediction = false

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFundBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAudioPermission(AUDIO_PERMISSION_REQUEST_CODE)
        val resetPredictionService = Intent(this, ResetPredictionService::class.java)
        ResetPredictionService.enqueueWork(this, resetPredictionService)

        val requestAmountStream =
            RxTextView.textChanges(binding.edRequestedFund)
                .skipInitialValue()
                .map {
                    it.isEmpty()
                }
        requestAmountStream.subscribe { isNotValid ->
            showEditTextExistAlert(
                binding.edRequestedFund,
                isNotValid,
                getString(R.string.requested_fund_error)
            )
            binding.btnRequestFund.isEnabled = !isNotValid
        }

        binding.btnRequestFund.setOnClickListener {
            it.hideKeyboard()
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                if (isConnected()) {
                    val requestAmount = binding.edRequestedFund.text.toString().toDouble()

                    (IO){ viewModel.increaseBalanceOfCurrentUser(requestAmount) }
                        .observeOnce(this@AddFundActivity, getIncreaseBalanceObserver())
                } else {
                    binding.progressBar.visibility = View.GONE
                    showToast(getString(R.string.no_internet))
                }
            }
        }

        lifecycleScope.launch {
            (IO){ viewModel.getRealtimeUpdatesOfCurrentUser() }
                .observe(this@AddFundActivity, getRealtimeObserver())
        }

    }

    private fun getIncreaseBalanceObserver() = Observer<ApiResponse<Boolean>> { response ->
        val toastText = when (response) {
            is ApiResponse.Success -> {
                getString(R.string.requested_fund_success)
            }
            is ApiResponse.Error -> {
                response.errorMessage
                    ?: getString(R.string.error)
            }
            is ApiResponse.Empty -> {
                getString(R.string.empty)
            }
        }
        lifecycleScope.launch {
            showToast(toastText)
            binding.progressBar.visibility = View.GONE

            delay(1000)
            if (response is ApiResponse.Success) {
                onBackPressed()
            }
        }
    }

    private fun getRealtimeObserver() =
        Observer<ApiResponse<User>> { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val realtimeData = response.data
                    if (waitingForPrediction)
                        lifecycleScope.launch {
                            waitingForPrediction = false
                            binding.progressBar.visibility = View.GONE

                            val resetPredictionService =
                                Intent(this@AddFundActivity, ResetPredictionService::class.java)
                            ResetPredictionService.enqueueWork(
                                this@AddFundActivity,
                                resetPredictionService
                            )

                            val convertedPrediction = realtimeData.prediction.toNumber()
                            if (convertedPrediction == NOT_A_NUMBER)
                                showToast(getString(R.string.voice_error))
                            else {
                                with(binding.edRequestedFund) {
                                    val newText = "${text.toString()}$convertedPrediction"
                                    setText(newText)
                                }
                            }
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
                    binding.progressBar.visibility = View.VISIBLE
                    (IO){
                        val filePath = applicationContext.recordFile.toString()
                        viewModel.uploadFile(filePath)
                    }.observeOnce(this@AddFundActivity, { response ->
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

}