package com.nelixia.marcaje2

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebview() {
        val visor = findViewById<WebView>(R.id.web)

        visor.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.e("WebView - Logger", "${consoleMessage.messageLevel()} : ${consoleMessage.lineNumber()} : ${consoleMessage.message()}")
                return true
            }
        }

        val webSettings: WebSettings = visor.settings
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.loadsImagesAutomatically = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true

        visor.loadUrl("https://hrmsgt.nelixia.net/admin")
    }
}
