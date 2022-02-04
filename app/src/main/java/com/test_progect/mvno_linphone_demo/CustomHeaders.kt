package com.test_progect.mvno_linphone_demo

import android.location.Location
import org.linphone.core.Account
import org.linphone.core.CallParams
import org.linphone.core.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

internal const val P_ASSOCIATED_URI_HEADER_KEY: String = "P-Associated-URI"
internal const val CONTACT_HEADER_KEY: String = "Contact"
internal const val FROM_HEADER_KEY: String = "From"
private const val TO_HEADER_KEY: String = "To"
private const val AUTHORIZATION_HEADER_KEY: String = "Authorization"
private const val X_LOCATION_HEADER_KEY: String = "XLocation"

private const val TIME_FORMAT = "dd-MM-yyyy'T'HH:mm:ss z"

internal fun Account.applyAuthorizationHeaders(
    accountInfo: NoSimAccountInfo,
    location: Location,
    deviceId: String
): Account =
    apply {
        setCustomHeader(TO_HEADER_KEY, getToHeaderValue(accountInfo.imsi, accountInfo.domain))
        setCustomHeader(FROM_HEADER_KEY, getFromHeaderValue(accountInfo, true))
        setCustomHeader(
            AUTHORIZATION_HEADER_KEY,
            getAuthorizationHeaderValue(accountInfo.imsi, accountInfo.domain)
        )
        setCustomHeader(CONTACT_HEADER_KEY, getContactHeaderValue(accountInfo.msisdn, deviceId))
        setLocationHeader(location)
    }

internal fun CallParams.applyHeaders(
    accountInfo: NoSimAccountInfo,
    location: Location,
    deviceId: String,
    pAssociatedURI: String,
): CallParams =
    apply {
        addCustomHeader(
            CONTACT_HEADER_KEY,
            getContactHeaderValue(accountInfo.msisdn, deviceId)
        )
        addCustomHeader(FROM_HEADER_KEY, getFromHeaderValue(accountInfo, false))
        addCustomHeader(P_ASSOCIATED_URI_HEADER_KEY, pAssociatedURI)
        addLocationHeader(location)
    }

internal fun CallParams.applyLocationHeader(location: Location): CallParams =
    apply { addCustomHeader(X_LOCATION_HEADER_KEY, getLocationHeaderValue(location)) }

internal fun ChatMessage.applyHeaders(
    accountInfo: NoSimAccountInfo,
    location: Location,
    deviceId: String,
    pAssociatedURI: String,
) {
    addCustomHeader(CONTACT_HEADER_KEY, getContactHeaderValue(accountInfo.msisdn, deviceId))
    addCustomHeader(FROM_HEADER_KEY, getFromHeaderValue(accountInfo, false))
    addCustomHeader(P_ASSOCIATED_URI_HEADER_KEY, pAssociatedURI)
    addCustomHeader(X_LOCATION_HEADER_KEY, getLocationHeaderValue(location))
}

internal fun CallParams.addLocationHeader(location: Location): CallParams =
    apply { addCustomHeader(X_LOCATION_HEADER_KEY, getLocationHeaderValue(location)) }

private fun Account.setLocationHeader(location: Location): Account =
    apply { setCustomHeader(X_LOCATION_HEADER_KEY, getLocationHeaderValue(location)) }

private fun getLocationHeaderValue(location: Location): String =
    "${getTime()};${location.longitude};${location.latitude};${location.accuracy}"

private fun getContactHeaderValue(msisdn: String, deviceId: String): String =
    ("<sip:$msisdn@172.30.147.209:43530;" +
            "transport=udp;" +
            "pn-provider=tinkoff;" +
            "pn-prid=$deviceId;>;" +
            "gr=urn;" +
            "+g.3gpp.smsip;" +
            "+sip.instance=\"<urn:uuid:d1644492-1103-00f8-a4eb-c7a87d3b41f7>\"")

private fun getFromHeaderValue(
    accountInfo: NoSimAccountInfo,
    isRegistration: Boolean
): String {
    val address = if (isRegistration) accountInfo.imsi else accountInfo.msisdn
    return "<sip:$address@${accountInfo.domain}>;tag=~UwXzKOlD\n"
}

private fun getToHeaderValue(imsi: String, domain: String): String =
    "sip:$imsi@$domain"

private fun getAuthorizationHeaderValue(imsi: String, domain: String): String =
    "Digest realm=\"$domain\", " +
            "nonce=\"9b4c5fedc08296985b586acee1f16218\", " +
            "algorithm=MD5, username=\"$imsi@$domain\", " +
            "uri=\"sip:$domain\", " +
            "response=\"365b94fb3ad933759f921d7d6d88d257\", " +
            "cnonce=\"HQlmMYSNfKlp66nk\", " +
            "nc=00000001, " +
            "qop=auth"

private fun getTime(): String {
    val calendar = Calendar.getInstance()
    val simpleDateFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
    return simpleDateFormat.format(calendar.time).toString()
}