package com.thelatenightstudio.favi.mainmenu

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.addfundmenu.AddFundActivity
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.model.User
import com.thelatenightstudio.favi.core.utils.InternetHelper
import com.thelatenightstudio.favi.core.utils.NumberHelper.formatAsBalance
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivityMainMenuBinding
import com.thelatenightstudio.favi.transfermenu.TransferMenuActivity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    private val viewModel: MainMenuViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            if (InternetHelper.isConnected()) {
                (IO){ viewModel.getDataOfCurrentUser() }
                    .observe(this@MainMenuActivity, getDataObserver())
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

    override fun onDestroy() {
        lifecycleScope.launch(IO) {
            viewModel.signOut()
        }
        super.onDestroy()
    }
}