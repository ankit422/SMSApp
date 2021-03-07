package com.smsapp

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import java.util.*


class SMSViewModel(application: Application) : AndroidViewModel(application) {
    val messages: MutableLiveData<List<Messages>> = MutableLiveData()

    fun getMessages() {
        readSmsFromCP()
    }

    private fun readSmsFromCP() {
        //=====use existing data on config changes====
        if (!messages.value.isNullOrEmpty()) {
            messages.value = messages.value
            return
        }

        var messageList: List<Messages> = ArrayList()
        val cal = Calendar.getInstance() // creates calendar object
        val projection =
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE_SENT)

        //==== adding date condition so that it only fetch last 48 hours of data===
        val cursor: Cursor? =
            getApplication<Application>().applicationContext.contentResolver.query(
                Uri.parse("content://sms/inbox"), projection,
                Telephony.Sms.DATE_SENT + ">" + (cal.timeInMillis - 172800000),
                null, null
            )

        var lastDay = ""
        if (cursor?.moveToFirst()!!) { // must check the result to prevent exception
            do {
                val add = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val created =
                    cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))

                val temp = cal.timeInMillis
                val different = temp - created?.toLong()!!
                val hoursInMilli = 1000 * 60 * 60
                val elapsedHours = different / hoursInMilli

                val dayToDis = if (elapsedHours.toInt() in 24..47)
                    "1 Day ago"
                else if (elapsedHours.toInt() in 12..23)
                    "12 Hours ago"
                else if (elapsedHours.toInt() in 6..11)
                    "6 Hours ago"
                else if (elapsedHours.toInt() in 3..5)
                    "2 Hours ago"
                else "${elapsedHours.toInt()} Hours ago"

                if (lastDay != dayToDis) {
                    messageList =
                        messageList.plus(Messages("header", add, body, created, dayToDis))
                    lastDay = dayToDis
                }
                messageList =
                    messageList.plus(
                        Messages("message", add, body, created, elapsedHours.toString())
                    )
            } while (cursor.moveToNext())
        }

        messages.value = messageList
    }

}

