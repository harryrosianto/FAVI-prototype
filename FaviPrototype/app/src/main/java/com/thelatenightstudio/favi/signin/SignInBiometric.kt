package com.thelatenightstudio.favi.signin

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.utils.InternetHelper.isConnected
import com.thelatenightstudio.favi.core.utils.ObserverHelper.getSignInObserver
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivitySignInBinding

class SignInBiometric {

    fun getBiometricPrompt(
        activity: AppCompatActivity,
        resources: Resources,
        binding: ActivitySignInBinding,
        viewModel: SignInViewModel
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                Log.d("Cek", "$errorCode :: $errString")

                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    showToast(activity, resources.getString(R.string.biometric_error))
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                showToast(activity, resources.getString(R.string.failed))
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                if (isConnected()) {
                    binding.progressBar.visibility = View.VISIBLE

                    viewModel.signInWithBiometric().observe(
                        activity, getSignInObserver(activity, resources, binding)
                    )
                } else {
                    showToast(activity, resources.getString(R.string.no_internet))
                }
            }
        }

        return BiometricPrompt(activity, executor, callback)
    }

    fun getPromptInfo(resources: Resources): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(resources.getString(R.string.sign_in_with_biometric))
            .setSubtitle(resources.getString(R.string.biometric_auth))

            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            // Authenticate without requiring the user to press a "confirm" button after satisfying the biometric check
            .setConfirmationRequired(false)
            .setNegativeButtonText(resources.getString(R.string.biometric_error_button))
            .build()
    }

    fun biometricAuthProcess(
        activity: AppCompatActivity,
        resources: Resources,
        viewModel: SignInViewModel,
        biometricPrompt: BiometricPrompt,
        promptInfo: BiometricPrompt.PromptInfo
    ) {
        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                if (viewModel.isBiometricActive())
                    biometricPrompt.authenticate(promptInfo)
                else showToast(
                    activity,
                    resources.getString(R.string.biometric_auth_is_off)
                )

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                showToast(
                    activity,
                    resources.getString(R.string.biometric_error_no_hardware)
                )

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                showToast(
                    activity,
                    resources.getString(R.string.biometric_error_hw_unavailable)
                )

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                showToast(
                    activity,
                    resources.getString(R.string.biometric_error_none_enrolled)
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent =
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                            )
                        }
                    activity.startActivityForResult(
                        enrollIntent,
                        SignInActivity.BIOMETRIC_SETUP_REQUEST_CODE
                    )
                } else {
                    activity.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }
    }

}