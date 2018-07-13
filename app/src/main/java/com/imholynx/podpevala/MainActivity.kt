package com.imholynx.podpevala

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.acrcloud.rec.sdk.ACRCloudClient
import com.acrcloud.rec.sdk.ACRCloudConfig
import com.acrcloud.rec.sdk.IACRCloudListener
import java.io.File
import android.net.Uri.fromParts
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.support.v4.view.accessibility.AccessibilityEventCompat.setAction
import android.content.Intent
import android.net.Uri
import android.provider.Settings


class MainActivity : AppCompatActivity(),IACRCloudListener {

    lateinit var config:ACRCloudConfig
    lateinit var client:ACRCloudClient

    var volume: TextView? = null
    var result: TextView? = null
    var tv_time: TextView? = null

    var initState:Boolean = false
    var processing:Boolean = false

    var startTime:Long = 0
    var stopTime:Long = 0

    override fun onVolumeChanged(p0: Double) {
        volume?.text  = p0.toString()
    }

    override fun onResult(p0: String?) {
        result?.text = p0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var path = Environment.getExternalStorageDirectory().toString() + "/acrcloud/model"

        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }

        volume = findViewById(R.id.volume)
        result = findViewById(R.id.result)
        tv_time = findViewById(R.id.tv_time)

        val startBtn: Button = findViewById(R.id.start)
        val cancelBtn: Button = findViewById(R.id.cancel)
        val stopBtn: Button = findViewById(R.id.stop)
        startBtn.setOnClickListener{view -> start(); }
        cancelBtn.setOnClickListener{view -> cancel(); }
        stopBtn.setOnClickListener{view -> stop(); }

        config = ACRCloudConfig()
        config.acrcloudListener = this
        config.context = this
        config.host = "identify-eu-west-1.acrcloud.com"
        config.dbPath = path
        config.accessKey = "4db8d90f77f706fc96be20fafe5e6665"
        config.accessSecret = "nPkCxwyjVLxKAx9rQ0Rx5NAOgp2GbHpQjSfTSfNj"
        config.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTPS
        config.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE

        client = ACRCloudClient()

        this.initState = client.initWithConfig(config)

        if (initState) {
            //TODO переделать
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {

                client.startPreRecord(3000)
                //Start recognition

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                Log.d(this@MainActivity.localClassName,"ask for permission")
                makeRequest()
            }
        }
    }



    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                101)
    }

    fun start(){
        if(!initState){
            Toast.makeText(this,"please wait a second for init",Toast.LENGTH_SHORT).show()
            return;
        }

        if(!processing){
            processing = true
            if(!::client.isInitialized || !client.startRecognize()){
                processing = false
                result?.text = "start error"
            }
            startTime = System.currentTimeMillis()
        }
    }
    fun cancel(){
        if(processing && ::client.isInitialized)
        {
            processing = false
            client.cancel()
            result?.text = ""
            tv_time?.text = ""


        }
    }
    fun stop(){
        if(processing && ::client.isInitialized){
            client.stopRecordToRecognize()
        }
        processing = false
        stopTime = System.currentTimeMillis()
    }
}
