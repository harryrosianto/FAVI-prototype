package com.thelatenightstudio.favi.transfermenu

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
import com.thelatenightstudio.favi.core.utils.InternetHelper.isConnected
import com.thelatenightstudio.favi.core.utils.LiveDataHelper.observeOnce
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getEmailStream
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getInvalidFieldsStream
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivityTransferMenuBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransferMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferMenuBinding

    private val viewModel: TransferMenuViewModel by viewModel()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requestAmountStream =
            RxTextView.textChanges(binding.edReceiverGetBalance)
                .skipInitialValue()
                .map {
                    it.isEmpty()
                }
        requestAmountStream.subscribe { isNotValid ->
            showEditTextExistAlert(
                binding.edReceiverGetBalance,
                isNotValid,
                getString(R.string.transfer_fund_error)
            )
        }

        val receiverIdStream = getEmailStream(binding.edReceiverId)
        receiverIdStream.subscribe {
            showEditTextExistAlert(
                binding.edReceiverId,
                it,
                getString(R.string.email_not_valid)
            )
        }

        val invalidFieldsStream = getInvalidFieldsStream(requestAmountStream, receiverIdStream)
        invalidFieldsStream.subscribe { isValid -> binding.btnConfirmTransfer.isEnabled = isValid }

        binding.btnConfirmTransfer.setOnClickListener {
            it.hideKeyboard()
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                if (isConnected()) {
                    val receiverId = binding.edReceiverId.text.toString()
                    val requestAmount = binding.edReceiverGetBalance.text.toString().toDouble()

                    (IO){ viewModel.transferBalanceToAnotherUser(receiverId, requestAmount) }
                        .observeOnce(this@TransferMenuActivity, getTransferBalanceObserver())
                } else {
                    binding.progressBar.visibility = View.GONE
                    showToast(getString(R.string.no_internet))
                }
            }
        }
    }

    private fun getTransferBalanceObserver(): Observer<ApiResponse<Boolean>> =
        Observer<ApiResponse<Boolean>> { response ->
            val toastText = when (response) {
                is ApiResponse.Success -> {
                    getString(R.string.transfer_successful)
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