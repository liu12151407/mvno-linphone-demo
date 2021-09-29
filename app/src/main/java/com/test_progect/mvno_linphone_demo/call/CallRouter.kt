package com.test_progect.mvno_linphone_demo.call

import androidx.fragment.app.Fragment

interface CallRouter {

    fun openOutgoingCall(phoneNumber: String)
    fun openIncomingCall()
    fun closeChildFragment(fragment: Fragment)

}