package com.test_progect.mvno_linphone_demo

interface Router {

    fun openAccount()

    fun openCall()

    fun openOutgoingCall(phoneNumber: String)

    fun openIncomingCall()

}