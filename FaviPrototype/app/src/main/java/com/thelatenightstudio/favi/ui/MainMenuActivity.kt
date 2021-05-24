package com.thelatenightstudio.favi.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.security.SharedPreferencesManager
import com.thelatenightstudio.favi.core.utils.ToastHelper
import com.thelatenightstudio.favi.databinding.ActivityMainMenuBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainMenuActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STRING = "extra_string"
    }

    private lateinit var binding: ActivityMainMenuBinding

    private val viewModel: MainMenuViewModel by viewModel()
    private val spmanager: SharedPreferencesManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras != null) {
            val text = intent.getStringExtra(EXTRA_STRING)
            binding.textView.text = text
        }

        binding.btnSaveToken.setOnClickListener {
            spmanager.activateBiometricAuth()
            val text = getString(R.string.successful)
            ToastHelper.showToast(this, text)
        }
    }

    override fun onDestroy() {
        viewModel.signOut()
        spmanager.signOut()
        super.onDestroy()
    }
}