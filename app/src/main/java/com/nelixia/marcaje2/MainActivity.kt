package com.nelixia.marcaje2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1001
    private val FILECHOOSER_REQUEST_CODE = 1002

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Activar la depuración del WebView (útil en desarrollo)
        WebView.setWebContentsDebuggingEnabled(true)

        // Verifica si se han concedido todos los permisos necesarios
        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE)
        } else {
            initWebview()
        }
    }

    private fun hasAllPermissions(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initWebview()
            } else {
                Log.e("MainActivity", "No se concedieron todos los permisos necesarios")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebview() {
        val webView = findViewById<WebView>(R.id.web)
        val webSettings = webView.settings

        // Habilitar funcionalidades avanzadas de JavaScript y otras configuraciones
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.loadsImagesAutomatically = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.setGeolocationEnabled(true)
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.setSupportMultipleWindows(true)

        // Permitir contenido mixto (HTTP y HTTPS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // Configurar el WebViewClient con manejo de errores y sin interceptar la navegación
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("WebView", "Finished loading: $url")
                super.onPageFinished(view, url)
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Log.e("WebView", "Error loading page: ${error?.description}")
                super.onReceivedError(view, request, error)
            }
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Devolver false para que el WebView maneje la URL internamente
                return false
            }
        }

        // Configurar el WebChromeClient para manejar permisos, consola, ventanas emergentes y carga de archivos
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.e("WebView", "Console: ${consoleMessage.message()}")
                return true
            }
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                callback.invoke(origin, true, false)
            }
            override fun onPermissionRequest(request: PermissionRequest?) {
                runOnUiThread {
                    request?.grant(request.resources)
                }
            }
            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                val newWebView = WebView(this@MainActivity).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            return false
                        }
                    }
                }
                val transport = resultMsg?.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent() ?: return false
                return try {
                    startActivityForResult(intent, FILECHOOSER_REQUEST_CODE)
                    true
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    false
                }
            }
        }

        // Cargar la URL; asegúrate de que el permiso INTERNET esté declarado en el manifest
        webView.loadUrl("https://hrmsgt.nelixia.net")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILECHOOSER_REQUEST_CODE) {
            filePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            filePathCallback = null
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
