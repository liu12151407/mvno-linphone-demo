package com.test_progect.mvno_linphone_demo.call.incoming_call

import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.databinding.IncomingCallFragmentBinding

interface IncomingCallView {

    fun updateIncomingCallState(message: String)
    fun disableCallAnswerButton()
    fun disableCallEndButton()

    interface Presenter {

        fun onCallAnswerButtonClicked()
        fun onCallEndButtonClicked()

    }

}

class IncomingCallViewImpl(
    private val binding: IncomingCallFragmentBinding,
    presenter: IncomingCallView.Presenter
) : IncomingCallView {

    private val context = binding.root.context

    init {
        binding.callAnswerButton.setOnClickListener {
            presenter.onCallAnswerButtonClicked()
        }
        binding.callEndButton.setOnClickListener {
            presenter.onCallEndButtonClicked()
        }
        binding.incomingCallStateView.text = context.getString(R.string.incoming_call_initial_state)
    }

    override fun disableCallAnswerButton() {
        binding.callAnswerButton.isEnabled = false
        binding.callAnswerButton.setBackgroundColor(context.getColor(R.color.grey))
    }

    override fun disableCallEndButton() {
        binding.callEndButton.isEnabled = false
        binding.callEndButton.setBackgroundColor(context.getColor(R.color.grey))
    }

    override fun updateIncomingCallState(message: String) {
        binding.incomingCallStateView.text = message
    }


}