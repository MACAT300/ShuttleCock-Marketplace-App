package com.example.shuttlecock_frontend.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.shuttlecock_frontend.R


class PaymentWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHECKOUT_URL = "checkout_url"

        // Must match stripe.success.url / stripe.cancel.url in application.properties
        private const val SUCCESS_URL_PREFIX = "https://yourapp.com/payment-success"
        private const val CANCEL_URL_PREFIX = "https://yourapp.com/payment-cancel"

        const val RESULT_PAYMENT_SUCCESS = Activity.RESULT_FIRST_USER + 1
        const val RESULT_PAYMENT_CANCELLED = Activity.RESULT_FIRST_USER + 2

        fun start(context: Context, checkoutUrl: String): Intent {
            return Intent(context, PaymentWebViewActivity::class.java)
                .putExtra(EXTRA_CHECKOUT_URL, checkoutUrl)
        }
    }

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_webview)

        webView = findViewById(R.id.paymentWebView)
        progressBar = findViewById(R.id.paymentProgressBar)

        val checkoutUrl = intent.getStringExtra(EXTRA_CHECKOUT_URL)
        if (checkoutUrl.isNullOrBlank()) {
            setResult(RESULT_PAYMENT_CANCELLED)
            finish()
            return
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return handleUrl(url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
            }
        }

        webView.loadUrl(checkoutUrl)
    }

    private fun handleUrl(url: String): Boolean {
        return when {
            url.startsWith(SUCCESS_URL_PREFIX) -> {
                setResult(RESULT_PAYMENT_SUCCESS)
                finish()
                true
            }
            url.startsWith(CANCEL_URL_PREFIX) -> {
                setResult(RESULT_PAYMENT_CANCELLED)
                finish()
                true
            }
            else -> false // let the WebView keep navigating normally (Stripe's own pages)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            setResult(RESULT_PAYMENT_CANCELLED)
            super.onBackPressed()
        }
    }
}
