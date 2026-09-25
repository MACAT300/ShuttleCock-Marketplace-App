package com.example.shuttlecock_frontend.activity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.auth.RetrofitClient
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnSendCode: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etEmail = findViewById(R.id.etEmail)
        btnSendCode = findViewById(R.id.btnSendCode)
        progressBar = findViewById(R.id.progressBar)

        btnSendCode.setOnClickListener { sendCode() }

        findViewById<TextView>(R.id.tvBackToLogin).setOnClickListener {
            finish()
        }
    }

    private fun sendCode() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.requestPasswordReset(mapOf("email" to email))
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, "Verification code sent", Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                            .putExtra("email", email)
                    )
                    finish()
                } else {
                    val msg = response.errorBody()?.string() ?: "Failed to send code"
                    Toast.makeText(this@ForgotPasswordActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ForgotPasswordActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        btnSendCode.isEnabled = !isLoading
    }
}