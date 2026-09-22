package com.example.shuttlecock_frontend.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.activity.LoginActivity
import com.example.shuttlecock_frontend.auth.RetrofitClient
import com.example.shuttlecock_frontend.data.UserSession
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var tvProfileName: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var imgAvatarPlaceholder: ImageView

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) uploadAvatar(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvProfileName = view.findViewById(R.id.tvProfileName)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        imgAvatarPlaceholder = view.findViewById(R.id.imgAvatarPlaceholder)

        refreshProfileUi()

        view.findViewById<TextView>(R.id.btnEditName).setOnClickListener { showEditNameDialog() }
        view.findViewById<TextView>(R.id.btnChangePassword).setOnClickListener { showChangePasswordDialog() }
        view.findViewById<FrameLayout>(R.id.btnChangeAvatar).setOnClickListener { pickImage() }

        view.findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            UserSession.clear()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) refreshProfileUi()
    }

    private fun refreshProfileUi() {
        tvProfileName.text = UserSession.userName ?: ""

        val avatarUrl = UserSession.avatarUrl
        if (!avatarUrl.isNullOrBlank()) {
            imgAvatarPlaceholder.visibility = View.GONE
            imgAvatar.visibility = View.VISIBLE
            imgAvatar.load(RetrofitClient.baseUrlForImages() + avatarUrl) {
                placeholder(R.drawable.image_background)
                error(R.drawable.image_background)
            }
        } else {
            imgAvatar.visibility = View.GONE
            imgAvatarPlaceholder.visibility = View.VISIBLE
        }
    }

    // ===== Edit name =====
    private fun showEditNameDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_field, null)
        val input1 = dialogView.findViewById<EditText>(R.id.etDialogInput1)
        input1.hint = "Name"
        input1.setText(UserSession.userName)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Name")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = input1.text.toString().trim()
                if (newName.isNotEmpty()) updateName(newName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateName(newName: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateUserName(
                    UserSession.userId,
                    mapOf("name" to newName)
                )
                if (response.isSuccessful) {
                    UserSession.userName = newName
                    refreshProfileUi()
                    Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update name", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===== Change password =====
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_field, null)
        val input1 = dialogView.findViewById<EditText>(R.id.etDialogInput1)
        val input2 = dialogView.findViewById<EditText>(R.id.etDialogInput2)

        val isGoogle = UserSession.isGoogleAccount

        if (isGoogle) {
            // Google账号没有真正的密码，跳过"输入现在密码"这一步，直接设新密码
            input1.hint = "New password"
            input1.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            input2.visibility = View.GONE
        } else {
            input1.hint = "Current password"
            input1.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            input2.hint = "New password"
            input2.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            input2.visibility = View.VISIBLE
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                if (isGoogle) {
                    val newPassword = input1.text.toString()
                    if (newPassword.isNotEmpty()) changePassword(null, newPassword)
                } else {
                    val currentPassword = input1.text.toString()
                    val newPassword = input2.text.toString()
                    if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                        changePassword(currentPassword, newPassword)
                    } else {
                        Toast.makeText(requireContext(), "Please fill in both fields", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changePassword(currentPassword: String?, newPassword: String) {
        lifecycleScope.launch {
            try {
                val body = mutableMapOf("newPassword" to newPassword)
                if (currentPassword != null) body["currentPassword"] = currentPassword

                val response = RetrofitClient.apiService.changePassword(UserSession.userId, body)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()
                } else {
                    val msg = response.errorBody()?.string() ?: "Failed to update password"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===== Avatar upload =====
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        pickImageLauncher.launch(intent)
    }

    private fun uploadAvatar(uri: Uri) {
        lifecycleScope.launch {
            try {
                val file = uriToTempFile(uri) ?: run {
                    Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = RetrofitClient.apiService.uploadAvatar(UserSession.userId, part)
                if (response.isSuccessful) {
                    val updatedUser = response.body()
                    UserSession.avatarUrl = updatedUser?.avatarUrl
                    refreshProfileUi()
                    Toast.makeText(requireContext(), "Avatar updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to upload avatar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToTempFile(uri: Uri): File? {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("avatar_", ".jpg", requireContext().cacheDir)
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        return tempFile
    }
}