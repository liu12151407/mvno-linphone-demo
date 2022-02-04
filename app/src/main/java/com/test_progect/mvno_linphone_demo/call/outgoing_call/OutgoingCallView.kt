package com.test_progect.mvno_linphone_demo.call.outgoing_call

import androidx.annotation.DrawableRes
import com.test_progect.mvno_linphone_demo.databinding.OutgouingCallFragmentBinding

interface OutgoingCallView {

    fun setOutgoingCallState(message: String)
    fun enableMicAndSpeaker()
    fun disableButtons()
    fun setMuteIcon(@DrawableRes id: Int)
    fun setSpeakerIcon(@DrawableRes id: Int)

    interface Presenter {

        fun onCallEndButtonClicked()
        fun onSpeakerButtonClicked()
        fun onMuteButtonClicked()

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
        binding.muteButton.apply {
            isEnabled = false
            setOnClickListener { presenter.onMuteButtonClicked() }
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
            muteButton.isEnabled = true
            speakerButton.isEnabled = true
        }
    }

    override fun disableButtons() {
        binding.apply {
            callEndButton.isEnabled = false
            muteButton.isEnabled = false
            speakerButton.isEnabled = false
        }
    }

    override fun setMuteIcon(id: Int) {
        binding.muteButton.setImageResource(id)
    }

    override fun setSpeakerIcon(id: Int) {
        binding.speakerButton.setImageResource(id)
    }

}