package com.thelatenightstudio.favi.signin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.utils.EditTextHelper.showEditTextExistAlert
import com.thelatenightstudio.favi.core.utils.InternetHelper.isConnected
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getEmailStream
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getInvalidFieldsStream
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getPasswordStream
import com.thelatenightstudio.favi.core.utils.ObserverHelper.getSignInObserver
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivitySignInBinding
import com.thelatenightstudio.favi.signin.SignInBiometric.biometricAuthProcess
import com.thelatenightstudio.favi.signin.SignInBiometric.getBiometricPrompt
import com.thelatenightstudio.favi.signin.SignInBiometric.getPromptInfo
import com.thelatenightstudio.favi.voicerecording.VoiceRecordingActivity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInActivity : AppCompatActivity() {

    companion object {
        const val BIOMETRIC_SETUP_REQUEST_CODE = 101
    }

    private lateinit var binding: ActivitySignInBinding

    private val viewModel: SignInViewModel by viewModel()

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.parentLayout.requestFocus()

        biometricPrompt = getBiometricPrompt(binding, viewModel)
        promptInfo = getPromptInfo()
        binding.btnBiometric.setOnClickListener {
            biometricAuthProcess(viewModel, biometricPrompt, promptInfo)
        }

        binding.btnVoice.setOnClickListener {
            val intent = Intent(this, VoiceRecordingActivity::class.java)
            startActivity(intent)
        }

        val emailStream = getEmailStream(binding.edEmail)
        emailStream.subscribe {
            showEditTextExistAlert(
                binding.edEmail,
                it,
                getString(R.string.email_not_valid)
            )
        }

        val passwordStream = getPasswordStream(binding.edPassword)
        passwordStream.subscribe {
            showEditTextExistAlert(
                binding.edPassword,
                it,
                getString(R.string.password_not_valid)
            )
        }

        val invalidFieldsStream = getInvalidFieldsStream(emailStream, passwordStream)
        invalidFieldsStream.subscribe { isValid -> binding.btnSignIn.isEnabled = isValid }

        binding.btnSignIn.setOnClickListener {
            lifecycleScope.launch {
                if (isConnected()) {
                    binding.progressBar.visibility = View.VISIBLE

                    val email = binding.edEmail.text.toString()
                    val password = binding.edPassword.text.toString()

                    (IO){ viewModel.signIn(email, password) }
                        .observe(this@SignInActivity, getSignInObserver(binding))
                } else {
                    showToast(getString(R.string.no_internet))
                }
            }
        }
    }

}