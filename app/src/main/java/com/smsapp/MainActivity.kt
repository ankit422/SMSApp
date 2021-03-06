package com.smsapp

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsapp.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_ASK_PERMISSIONS: Int = 1
    private var messageList: List<Messages> = ArrayList()
    private lateinit var adapter: MessageAdapter
    private var kTimeStampFormat = "MMM dd, yyyy"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MessageAdapter()
        binding.listApp.addItemDecoration(
            DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL
            )
        )
        binding.listApp.adapter = adapter

        askForPermission()


    }

    private fun readSmsFromCP() {
        val cursor: Cursor? =
            contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)

        var lastDay = ""
        if (cursor?.moveToFirst()!!) { // must check the result to prevent exception
            Log.e("cursor", cursor.count.toString())
            do {
                val add = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val d2 = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))

                val cal = Calendar.getInstance() // creates calendar
                val temp = cal.timeInMillis
                val different = temp - d2?.toLong()!!
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
                else if (elapsedHours.toInt() > 47)
                    break
                else "${elapsedHours.toInt()} Hours ago"

                if (lastDay != dayToDis) {
                    messageList =
                        messageList.plus(Messages("header", add, body, d2, dayToDis))
                    lastDay = dayToDis
                }
                messageList =
                    messageList.plus(Messages("message", add, body, d2, elapsedHours.toString()))
                // use msgData
            } while (cursor.moveToNext())
        }
        if (messageList.isNotEmpty()) {
            binding.message.visibility = View.GONE
            adapter.setData(messageList)
        } else
            binding.message.visibility = View.VISIBLE

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun askForPermission() {
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_SMS), REQUEST_CODE_ASK_PERMISSIONS
            )
        } else {
            readSmsFromCP()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readSmsFromCP()
            } else {
                Toast.makeText(
                    this,
                    "Please Allow permission from Settings to proceed with  App.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}