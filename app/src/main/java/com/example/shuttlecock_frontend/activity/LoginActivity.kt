package com.example.shuttlecock_frontend.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.SignUpActivity
import com.example.shuttlecock_frontend.auth.RetrofitClient
import com.example.shuttlecock_frontend.data.UserSession
import com.example.shuttlecock_frontend.models.auth.LoginRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val WEB_CLIENT_ID = "855982182472-vbbefl4rk42l43ddjtivk952j8mq96ik.apps.googleusercontent.com"

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: TextView
    private lateinit var tvGoSignup: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnTogglePassword: ImageView

    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (UserSession.isLoggedIn) {
            startActivity(
                Intent(this, MainHostActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoSignup = findViewById(R.id.tvGoSignup)
        progressBar = findViewById(R.id.progressBar)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)

        setupTaglineRotation()


        btnLogin.setOnClickListener {
            doLogin()
        }

        tvGoSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnGoogle).setOnClickListener {
            signInWithGoogle()
        }
    }


    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        setLoading(true)

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    exchangeGoogleTokenWithBackend(googleIdTokenCredential.idToken)
                } else {
                    setLoading(false)
                    Toast.makeText(this@LoginActivity, "Unexpected credential type", Toast.LENGTH_SHORT).show()
                }
            } catch (e: GetCredentialException) {
                setLoading(false)
                Toast.makeText(this@LoginActivity, "Google sign-in cancelled or failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exchangeGoogleTokenWithBackend(idToken: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.loginWithGoogle(mapOf("idToken" to idToken))
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    RetrofitClient.authToken = body.token
                    UserSession.userId = body.user.id
                    UserSession.userName = body.user.name
                    UserSession.userEmail = body.user.email
                    UserSession.isGoogleAccount = body.user.isGoogleAccount
                    UserSession.avatarUrl = body.user.avatarUrl

                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                    startActivity(
                        Intent(this@LoginActivity, MainHostActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                } else {
                    val msg = response.errorBody()?.string() ?: "Google login failed"
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
        if (passwordVisible) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye)
        } else {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
        }
        etPassword.setSelection(etPassword.text.length)
    }

    private fun doLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()


        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        RetrofitClient.authToken = body.token
                        UserSession.userId = body.user.id
                        UserSession.userName = body.user.name
                        UserSession.userEmail = body.user.email
                        UserSession.isGoogleAccount = body.user.isGoogleAccount
                        UserSession.avatarUrl = body.user.avatarUrl

                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainHostActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Login failed"
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        btnLogin.isEnabled = !isLoading
    }

    private val taglines = listOf(
        "The Creation of Every Smash",
        "Where Every Game Begins",
        "Made for Every Rally",
        "Built for the Perfect Shot"
    )
    private var taglineIndex = 0
    private val taglineHandler = android.os.Handler(android.os.Looper.getMainLooper())

    private fun setupTaglineRotation() {
        val tvTagline = findViewById<TextView>(R.id.tvTagline)

        val rotate = object : Runnable {
            override fun run() {
                // 淡出 -> 换字 -> 淡入，比直接跳字更有质感
                tvTagline.animate().alpha(0f).setDuration(900).withEndAction {
                    taglineIndex = (taglineIndex + 1) % taglines.size
                    tvTagline.text = taglines[taglineIndex]
                    tvTagline.animate().alpha(1f).setDuration(900).start()
                }.start()

                taglineHandler.postDelayed(this, 5000) // 每3.5秒切换一句
            }
        }
        taglineHandler.postDelayed(rotate, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        taglineHandler.removeCallbacksAndMessages(null)
    }
}