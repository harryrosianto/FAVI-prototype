package com.thelatenightstudio.favi

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.thelatenightstudio.favi.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    companion object {
        private val TAG = HomeActivity::class.java.simpleName

        private const val REQUEST_CODE = 101
    }

    private lateinit var binding: ActivityHomeBinding

    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        biometricPrompt = createBiometricPrompt()

        binding.btnBiometric.setOnClickListener {
            val promptInfo = createPromptInfo()

//            if (BiometricManager.from(context)
//                    .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
//            ) {
//                biometricPrompt.authenticate(promptInfo)
//            } else {
//                loginWithPassword()
//            }

            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    Log.d(TAG, "App can authenticate using biometrics.")

                    biometricPrompt.authenticate(promptInfo)
                }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    Log.e(TAG, "No biometric features available on this device.")

                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    Log.e(TAG, "Biometric features are currently unavailable.")

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    // Prompts the user to create credentials that your app accepts.

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val enrollIntent =
                            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BIOMETRIC_STRONG
                                )
                            }
                        startActivityForResult(enrollIntent, REQUEST_CODE)
                    } else {
                        startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }
            }
        }
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                Log.d(TAG, "$errorCode :: $errString")

                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {

                    Log.d(TAG, "onAuthenticationError: negative button is clicked")
                    //loginWithPassword()
                    // Because in this app, the negative button allows the user to enter an account password. This is completely optional and your app doesn’t have to do it.
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                Log.d(TAG, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                Log.d(TAG, "Authentication was successful")
                // Proceed with viewing the private encrypted message.
                //showEncryptedMessage(result.cryptoObject)

                val intent = Intent(this@HomeActivity, SecondActivity::class.java)
                startActivity(intent)
            }
        }

        return BiometricPrompt(this, executor, callback)

    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.prompt_info_title))
            .setSubtitle(getString(R.string.prompt_info_subtitle))
            .setDescription(getString(R.string.prompt_info_description))
            // Authenticate without requiring the user to press a "confirm"
            // button after satisfying the biometric check
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.prompt_info_use_app_password))
            .build()
    }

}