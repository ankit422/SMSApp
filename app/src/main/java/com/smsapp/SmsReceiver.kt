package com.smsapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.telephony.SmsMessage
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class SmsReceiver : BroadcastReceiver() {

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

                val sender: String? = messages[0]?.originatingAddress
                val message = sb.toString()
                createNotification(context, message, sender)
            }
        }
    }

    private fun createNotification(context: Context?, message: String?, sender: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("sender", sender)
        intent.putExtra("message", message)
        intent.data = (Uri.parse("foobar://" + SystemClock.elapsedRealtime()));

        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(message)
        bigText.setBigContentTitle("$sender sent you a message")
        bigText.setSummaryText("New Message")

        val id = Math.random().toInt()
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, id, intent, 0)
        val builder = NotificationCompat.Builder(context!!, context.getString(R.string.app_name)!!)
            .setSmallIcon(R.drawable.bb)
            .setContentTitle(sender)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setStyle(bigText)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "channel_id"
                val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH
                )
                createNotificationChannel(channel)
                builder.setChannelId(channelId)
            }
            notify(id, builder.build())
        }
    }

}