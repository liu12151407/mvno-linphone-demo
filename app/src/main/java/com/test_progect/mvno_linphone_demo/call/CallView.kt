package com.test_progect.mvno_linphone_demo.call

import android.view.MenuItem
import androidx.annotation.DrawableRes
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.databinding.CallFragmentBinding
import org.linphone.core.Call
import org.linphone.core.RegistrationState

interface CallView {

    fun onCallStateChanged(state: Call.State?, message: String)
    fun onRegistrationStateChanged(state: RegistrationState, message: String)
    fun setMicIcon(@DrawableRes id: Int)
    fun setSpeakerIcon(@DrawableRes id: Int)

    interface Presenter {

        fun onCallButtonClicked()
        fun onCallEndButtonClicked()
        fun onSpeakerButtonClicked()
        fun onMicButtonClicked()
        fun onMenuItemClicked(item: MenuItem): Boolean
    }

}

class CallViewImpl(
    private val binding: CallFragmentBinding,
    private val presenter: CallView.Presenter,
) : CallView {

    private val context = binding.root.context

    init {
        initView()
    }

    override fun onCallStateChanged(state: Call.State?, message: String) {
        binding.callStatusView.text = message
        when (state) {
            Call.State.IncomingReceived -> {
                binding.apply {
                    callEndButton.isEnabled = true
                    callButton.isEnabled = true
//                        findViewById<EditText>(R.id.remote_address).setText(call.remoteAddress.asStringUriOnly())
                }
            }
            Call.State.Connected -> {
                binding.apply {
                    micButton.isEnabled = true
                    speakerButton.isEnabled = true
                }
            }
            Call.State.Released -> {
                binding.apply {
                    callEndButton.isEnabled = false
                    callButton.isEnabled = false
                    micButton.isEnabled = false
                    speakerButton.isEnabled = false
//                    findViewById<EditText>(R.id.remote_address).text.clear()
                }
            }
            else -> {
                // empty
            }
        }
    }

    override fun onRegistrationStateChanged(state: RegistrationState, message: String) {
        when (state) {
            RegistrationState.Failed, RegistrationState.Cleared -> {
                binding.registrationStateView.text =
                    context.getString(R.string.linphone_registration_failed, message)
            }
            RegistrationState.Ok -> {
                binding.registrationStateView.text =
                    context.getString(R.string.linphone_registration_ok)
            }
            else -> {
                // empty
            }
        }
    }

    override fun setMicIcon(id: Int) {
        binding.micButton.setImageResource(id)
    }

    override fun setSpeakerIcon(id: Int) {
        binding.speakerButton.setImageResource(id)
    }

    private fun initView() {
        initToolbar()
        binding.apply {
            registrationStateView.text = context.getString(R.string.linphone_registration_ok)
            callEndButton.isEnabled = false
            callButton.isEnabled = false
            micButton.isEnabled = false
            speakerButton.isEnabled = false
            callEndButton.setOnClickListener {
                presenter.onCallEndButtonClicked()
            }
            callButton.setOnClickListener {
                presenter.onCallButtonClicked()
            }
            micButton.setOnClickListener {
                presenter.onMicButtonClicked()
            }
            speakerButton.setOnClickListener {
                presenter.onSpeakerButtonClicked()
            }
        }
    }

    private fun initToolbar() {
        binding.toolbar.apply {
            inflateMenu(R.menu.call_menu)
            setOnMenuItemClickListener {
                presenter.onMenuItemClicked(it)
            }
        }
    }

}