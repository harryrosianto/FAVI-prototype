package com.thelatenightstudio.favi.signin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.utils.EditTextHelper.showEditTextExistAlert
import com.thelatenightstudio.favi.core.utils.InternetHelper.isConnected
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getEmailStream
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getInvalidFieldsStream
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getPasswordStream
import com.thelatenightstudio.favi.core.utils.ObserverHelper.getSignInObserver
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivitySignInBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInActivity : AppCompatActivity() {

    companion object {
        const val BIOMETRIC_SETUP_REQUEST_CODE = 101
    }

    private lateinit var binding: ActivitySignInBinding

    private val viewModel: SignInViewModel by viewModel()
    private val biometric: SignInBiometric by inject()

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.parentLayout.requestFocus()

        biometricPrompt = biometric.getBiometricPrompt(
            this,
            resources,
            binding,
            viewModel
        )
        promptInfo = biometric.getPromptInfo(resources)

        binding.btnBiometric.setOnClickListener {
            biometric.biometricAuthProcess(
                this,
                resources,
                viewModel,
                biometricPrompt,
                promptInfo
            )
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
        invalidFieldsStream.subscribe { isValid -> binding.btnSignUp.isEnabled = isValid }

        binding.btnSignUp.setOnClickListener {
            if (isConnected()) {
                binding.progressBar.visibility = View.VISIBLE

                val email = binding.edEmail.text.toString()
                val password = binding.edPassword.text.toString()

                viewModel.signIn(email, password).observe(
                    this, getSignInObserver(this, resources, binding)
                )
            } else {
                showToast(this, getString(R.string.no_internet))
            }
        }
    }

}