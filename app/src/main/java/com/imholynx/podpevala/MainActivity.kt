package com.imholynx.podpevala

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import com.acrcloud.rec.sdk.ACRCloudClient
import com.acrcloud.rec.sdk.ACRCloudConfig
import com.acrcloud.rec.sdk.IACRCloudListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.File

class MainActivity : AppCompatActivity(), IACRCloudListener, ActivityCompat.OnRequestPermissionsResultCallback {

    val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101

    lateinit var config: ACRCloudConfig
    lateinit var client: ACRCloudClient

    var initState: Boolean = false
    var processing: Boolean = false

    var startTime: Long = 0
    var stopTime: Long = 0

    override fun onVolumeChanged(vlm: Double) {
        volume.text = vlm.toString()
        sound_button.setVolume(vlm.toFloat())
    }

    override fun onResult(json: String?) {
        result.text = json
        cancel()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start.setOnClickListener{ _ -> if(processing) stop() else start() }
        //sound_button.setOnClickListener{ view -> if(processing) stop() else start() }

        //TODO удалить
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                sound_button.setVolume(p1.toFloat()/100)
            }

        })

        config = ACRCloudConfig()
        config.acrcloudListener = this
        config.context = this
        config.host = "identify-eu-west-1.acrcloud.com"
        config.accessKey = "4db8d90f77f706fc96be20fafe5e6665"
        config.accessSecret = "nPkCxwyjVLxKAx9rQ0Rx5NAOgp2GbHpQjSfTSfNj"
        config.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTPS
        config.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE

        client = ACRCloudClient()
        initState = client.initWithConfig(config)

        if (initState) {
            if (checkPermissionsOrRequest())
                client.startPreRecord(3000)
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MY_PERMISSIONS_REQUEST_RECORD_AUDIO)
        Manifest.permission.RECORD_AUDIO
    }

    // return true if permission granted
    fun checkPermissionsOrRequest(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            return true
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
            showExplanationDialog()
            return false
        } else {
            makeRequest()
            return false
        }
    }

    fun showExplanationDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.micro_unavailable)
                .setMessage(R.string.message)
                .setPositiveButton(R.string.open_settings) { _, _ ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton(R.string.cancel) { _, _ -> checkPermissionsOrRequest() }
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_RECORD_AUDIO -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showExplanationDialog()
                } else {
                    client.startPreRecord(3000)
                }
            }
        }
    }

    fun start() {
        if (checkPermissionsOrRequest()) {
            if (!initState) {
                Toast.makeText(this, "please wait a second for init", Toast.LENGTH_SHORT).show()
                return;
            }
            if (!processing) {
                processing = true
                if (!::client.isInitialized || !client.startRecognize()) {
                    processing = false
                    result.text = "start error"
                }
                startTime = System.currentTimeMillis()
            }
        }
    }

    fun cancel() {
        if (processing && ::client.isInitialized) {
            processing = false
            client.cancel()
        }
    }

    fun stop() {
        if (processing && ::client.isInitialized) {
            client.stopRecordToRecognize()
        }
        processing = false
        stopTime = System.currentTimeMillis()
    }

    override fun onPause() {
        if (processing && ::client.isInitialized) {
            processing = false
            client.cancel()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if(::client.isInitialized) {
            client.cancel()
            client.release()
        }
        super.onDestroy()
    }

}
