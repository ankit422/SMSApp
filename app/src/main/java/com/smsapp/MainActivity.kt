package com.smsapp

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_ASK_PERMISSIONS: Int = 1
    private lateinit var adapter: MessageAdapter
    val model: SMSViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        checkForNotification()
        askForPermission()
    }

    private fun init() {
        adapter = MessageAdapter()
        binding.listApp.addItemDecoration(
            DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL
            )
        )
        binding.listApp.adapter = adapter

        model.messages.observe(this, Observer<List<Messages>> { messages ->
            if (messages.isNotEmpty()) {
                binding.message.visibility = View.GONE
                adapter.setData(messages)
            } else
                binding.message.visibility = View.VISIBLE
        })
    }

    private fun checkForNotification() {
        //=====check if intent from notification, open popup to show data ====
        if (intent.hasExtra("sender") && intent.hasExtra("message"))
            AlertDialog.Builder(this).setTitle("${intent.getStringExtra("sender")}")
                .setMessage(intent.getStringExtra("message"))
                .setPositiveButton("Got It", null).show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun askForPermission() {
        var list = arrayOf<String>()
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            list = list.plus(Manifest.permission.READ_SMS)
        } else
            model.getMessages()

        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            list = list.plus(Manifest.permission.RECEIVE_SMS)
        } else
            registerSmsListener()

        if (list.isNotEmpty())
            requestPermissions(list, REQUEST_CODE_ASK_PERMISSIONS)
    }

    private fun registerSmsListener() {
        val filter = IntentFilter()
        filter.addAction("android.provider.Telephony.SMS_RECEIVED")
        filter.priority = 2147483647
        val receiver = SmsReceiver()
        registerReceiver(receiver, filter)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerSmsListener()
                model.getMessages()
            } else {
                Toast.makeText(
                    this, "Please Allow permission from Settings to proceed with  App.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}