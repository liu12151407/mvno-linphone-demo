package com.test_progect.mvno_linphone_demo.call.incoming_call

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.LinphoneManager
import com.test_progect.mvno_linphone_demo.LocationProvider
import com.test_progect.mvno_linphone_demo.MainActivity
import com.test_progect.mvno_linphone_demo.call.CallRouter
import com.test_progect.mvno_linphone_demo.core_ui.CoroutineJobDelegate
import com.test_progect.mvno_linphone_demo.core_ui.CoroutineJobDelegateImpl
import com.test_progect.mvno_linphone_demo.databinding.IncomingCallFragmentBinding
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import ru.tcsbank.mvno.coroutines.onError
import ru.tcsbank.mvno.coroutines.onFinish
import ru.tcsbank.mvno.coroutines.onLaunch

class IncomingCallFragment : Fragment(), IncomingCallView.Presenter,
    CoroutineJobDelegate by CoroutineJobDelegateImpl() {

    private lateinit var view: IncomingCallView
    private var uncheckedBinding: IncomingCallFragmentBinding? = null
    private val binding: IncomingCallFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: CallRouter by lazy { parentFragment as CallRouter }
    private val linphoneManager: LinphoneManager by lazy {
        (requireActivity() as MainActivity).linphoneManager
    }
    private val locationProvider: LocationProvider by lazy {
        (requireActivity() as MainActivity).locationProvider
    }
    private var location: Location? = null
    private val coreListener = object : CoreListenerStub() {

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            view.updateIncomingCallState(message)
            if (state == Call.State.Released || state == Call.State.Error) {
                router.closeChildFragment(this@IncomingCallFragment)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // empty
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initializeCoroutineJob()
        uncheckedBinding = IncomingCallFragmentBinding.inflate(inflater, container, false)
        view = IncomingCallViewImpl(binding, this)
        linphoneManager.addCoreListenerStub(coreListener)
        return binding.root
    }

    override fun onDestroyView() {
        cancelCoroutineJob()
        linphoneManager.terminateCall()
        linphoneManager.removeCoreListenerStub(coreListener)
        super.onDestroyView()
    }

    override fun onCallAnswerButtonClicked() {
        view.disableCallAnswerButton()
        onLaunch { location = locationProvider.getLocation() }
            .onError {
                // TODO MVNO-15956
            }
            .onFinish {
                if (location != null) linphoneManager.acceptCall(checkNotNull(location))
            }
    }

    override fun onCallEndButtonClicked() {
        view.disableCallEndButton()
        linphoneManager.terminateCall()
        router.closeChildFragment(this@IncomingCallFragment)
    }

}