package com.test_progect.mvno_linphone_demo

class NoSimAccountInfo(
    val imsi: String,
    val msisdn: String,
    val domain: String,
    val password: String,
    val proxy: String,
    val transportType: Int = 0,
)