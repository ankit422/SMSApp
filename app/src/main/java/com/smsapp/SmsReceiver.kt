package com.smsreader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import java.util.regex.Pattern


class SmsReceiver(private val listener: Listener) : BroadcastReceiver() {

    private val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == SMS_RECEIVED) {
            val bundle = intent.extras
            if (bundle != null) {
                // get sms objects
                val pdus = bundle["pdus"] as Array<Any>?
                if (pdus!!.isEmpty()) {
                    return
                }
                // large message might be broken into many
                val messages: Array<SmsMessage?> = arrayOfNulls(pdus.size)
                val sb = StringBuilder()
                for (i in pdus.indices) {
                    messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                    sb.append(messages[i]?.messageBody)
                }

                //====todo we can add check here to read messages that we send and ignore others===
                val sender: String? = messages[0]?.originatingAddress
                val message = sb.toString()
                val datePattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4}")
                val dateMatcher = datePattern.matcher(message)

                val pricePattern = Pattern.compile("\\$(\\d*)")
                val priceMatcher = pricePattern.matcher(message)

                if (dateMatcher.find() && priceMatcher.find())
                    listener.onSMSReceived(message, dateMatcher.group(0), priceMatcher.group(0))
            }
        }
    }

    interface Listener {
        fun onSMSReceived(message: String?, date: String?, price: String?)
    }
}