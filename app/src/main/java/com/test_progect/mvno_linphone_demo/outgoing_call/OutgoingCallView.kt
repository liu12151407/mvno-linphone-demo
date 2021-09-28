package com.test_progect.mvno_linphone_demo.outgoing_call

import androidx.annotation.DrawableRes
import com.test_progect.mvno_linphone_demo.databinding.OutgouingCallFragmentBinding

interface OutgoingCallView {

    fun setOutgoingCallState(message: String)
    fun enableMicAndSpeaker()
    fun disableButtons()
    fun setMicIcon(@DrawableRes id: Int)
    fun setSpeakerIcon(@DrawableRes id: Int)

    interface Presenter {

        fun onCallEndButtonClicked()
        fun onSpeakerButtonClicked()
        fun onMicButtonClicked()

    }

}

class OutgoingCallViewImpl(
    private val binding: OutgouingCallFragmentBinding,
    private val presenter: OutgoingCallView.Presenter,
) : OutgoingCallView {

    init {
        binding.callEndButton.apply {
            isEnabled = true
            setOnClickListener {
                presenter.onCallEndButtonClicked()
            }
        }
        binding.micButton.apply {
            isEnabled = false
            setOnClickListener { presenter.onMicButtonClicked() }
        }
        binding.speakerButton.apply {
            isEnabled = false
            setOnClickListener { presenter.onSpeakerButtonClicked() }
        }
    }

    override fun setOutgoingCallState(message: String) {
        binding.outgoingCallStatusView.text = message
    }

    override fun enableMicAndSpeaker() {
        binding.apply {
            micButton.isEnabled = true
            speakerButton.isEnabled = true
        }
    }

    override fun disableButtons() {
        binding.apply {
            callEndButton.isEnabled = false
            micButton.isEnabled = false
            speakerButton.isEnabled = false
        }
    }

    override fun setMicIcon(id: Int) {
        binding.micButton.setImageResource(id)
    }

    override fun setSpeakerIcon(id: Int) {
        binding.speakerButton.setImageResource(id)
    }

}