package com.thelatenightstudio.favi.core.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.securepreferences.SecurePreferences

class SharedPreferencesManager(context: Context) {

    companion object {
        private const val FILE_NAME = "shared_preferences_manager"
        const val TOKEN = "token"
        const val BIOMETRIC_AUTH = "biometric_auth"
        const val USERNAME = "username"
        const val TEMPORARY_USERNAME = "temporary_username"
        const val PASSWORD = "password"
        const val TEMPORARY_PASSWORD = "temporary_password"
    }

    private var sharedPref: SharedPreferences =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val spec = KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            val masterKey = MasterKey.Builder(context)
                .setKeyGenParameterSpec(spec)
                .build()
            EncryptedSharedPreferences
                .create(
                    context,
                    FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
        } else {
            SecurePreferences(context)
        }

    private var editor: SharedPreferences.Editor = sharedPref.edit()

    private fun saveToPreference(key: String, value: String) =
        editor.putString(key, value).commit()

    private fun saveToPreference(key: String, value: Boolean) =
        editor.putBoolean(key, value).commit()

    fun getString(key: String): String = sharedPref.getString(key, "") as String
    fun getBoolean(key: String): Boolean = sharedPref.getBoolean(key, false)

    fun activateBiometricAuth() {
        saveToPreference(USERNAME, getString(TEMPORARY_USERNAME))
        saveToPreference(PASSWORD, getString(TEMPORARY_PASSWORD))
        saveToPreference(BIOMETRIC_AUTH, true)
    }

    fun signIn(username: String, password: String) {
        saveToPreference(TEMPORARY_USERNAME, username)
        saveToPreference(TEMPORARY_PASSWORD, password)
    }

    fun signOut() {
        saveToPreference(TEMPORARY_USERNAME, "")
        saveToPreference(TEMPORARY_PASSWORD, "")
    }

}
