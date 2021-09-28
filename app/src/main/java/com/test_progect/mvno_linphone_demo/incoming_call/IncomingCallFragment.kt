package com.test_progect.mvno_linphone_demo.incoming_call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.MainActivity
import com.test_progect.mvno_linphone_demo.Router
import com.test_progect.mvno_linphone_demo.databinding.IncomingCallFragmentBinding
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub

class IncomingCallFragment : Fragment(), IncomingCallView.Presenter {

    private lateinit var view: IncomingCallView
    private var uncheckedBinding: IncomingCallFragmentBinding? = null
    private val binding: IncomingCallFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: Router by lazy { requireActivity() as Router }
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val coreListener = object : CoreListenerStub() {

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            view.updateIncomingCallState(message)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = IncomingCallFragmentBinding.inflate(inflater, container, false)
        view = IncomingCallViewImpl(binding, this)
        core.addListener(coreListener)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        core.removeListener(coreListener)
    }

    override fun onCallAnswerButtonClicked() {
        view.disableCallAnswerButton()
        core.currentCall?.accept()
    }

    override fun onCallEndButtonClicked() {
        view.disableCallEndButton()
        core.currentCall?.terminate()
        router.openCall()
    }

}