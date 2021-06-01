package com.thelatenightstudio.favi.addfundmenu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.jakewharton.rxbinding2.widget.RxTextView
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.utils.EditTextHelper.showEditTextExistAlert
import com.thelatenightstudio.favi.core.utils.HideKeyboardHelper.hideKeyboard
import com.thelatenightstudio.favi.core.utils.InternetHelper
import com.thelatenightstudio.favi.core.utils.LiveDataHelper.observeOnce
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivityAddFundBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AddFundActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFundBinding

    private val viewModel: AddFundViewModel by inject()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFundBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            lifecycleScope.launch {
                if (InternetHelper.isConnected()) {
                    binding.progressBar.visibility = View.VISIBLE

                    val requestAmount = binding.edRequestedFund.text.toString().toDouble()

                    (IO){ viewModel.increaseBalanceOfCurrentUser(requestAmount) }
                        .observeOnce(this@AddFundActivity, getIncreaseBalanceObserver())
                } else {
                    showToast(getString(R.string.no_internet))
                }
            }
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

}