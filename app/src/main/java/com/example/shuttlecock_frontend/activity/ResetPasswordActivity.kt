package com.example.shuttlecock_frontend.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.auth.RetrofitClient
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etCode: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnTogglePassword: ImageView
    private lateinit var btnResetPassword: TextView
    private lateinit var progressBar: ProgressBar

    private var passwordVisible = false
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        email = intent.getStringExtra("email") ?: ""

        etCode = findViewById(R.id.etCode)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        progressBar = findViewById(R.id.progressBar)

        findViewById<TextView>(R.id.tvSentTo).text = "Enter the code sent to $email"

        btnTogglePassword.setOnClickListener { togglePasswordVisibility() }
        btnResetPassword.setOnClickListener { doReset() }
    }

    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
        if (passwordVisible) {
            etNewPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye)
        } else {
            etNewPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
        }
        etNewPassword.setSelection(etNewPassword.text.length)
    }

    private fun doReset() {
        val code = etCode.text.toString().trim()
        val newPassword = etNewPassword.text.toString()

        if (code.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.confirmPasswordReset(
                    mapOf("email" to email, "code" to code, "newPassword" to newPassword)
                )
                if (response.isSuccessful) {
                    Toast.makeText(this@ResetPasswordActivity, "Password reset successful", Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                } else {
                    val msg = response.errorBody()?.string() ?: "Failed to reset password"
                    Toast.makeText(this@ResetPasswordActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ResetPasswordActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        btnResetPassword.isEnabled = !isLoading
    }
}