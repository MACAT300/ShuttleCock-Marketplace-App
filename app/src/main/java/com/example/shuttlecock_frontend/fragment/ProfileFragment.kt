package com.example.shuttlecock_frontend.fragment

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import kotlin.jvm.java
import com.example.shuttlecock_frontend.activity.OrderHistoryActivity
import com.example.shuttlecock_frontend.activity.BrowsingHistoryActivity

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var tvProfileName: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var imgAvatarPlaceholder: ImageView

    private var pendingCameraUri: Uri? = null

    // 从相册选图 -> 先去裁剪
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) startCrop(uri)
        }
    }

    // 拍照 -> 先去裁剪
    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingCameraUri?.let { startCrop(it) }
        }
    }

    // 裁剪完成 -> 上传
    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val croppedUri = com.yalantis.ucrop.UCrop.getOutput(data)
                if (croppedUri != null) uploadAvatar(croppedUri)
            }
        }
    }

    // 相机权限请求
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvProfileName = view.findViewById(R.id.tvProfileName)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        imgAvatarPlaceholder = view.findViewById(R.id.imgAvatarPlaceholder)

        refreshProfileUi()

        view.findViewById<TextView>(R.id.btnEditProfile).setOnClickListener { showEditNameDialog() }
        view.findViewById<LinearLayout>(R.id.btnChangePassword).setOnClickListener { showChangePasswordDialog() }
        view.findViewById<FrameLayout>(R.id.avatarCircle).setOnClickListener {
            showAvatarViewer()
        }
        view.findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            UserSession.clear()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        view.findViewById<LinearLayout>(R.id.btnOrderHistory).setOnClickListener {
            startActivity(Intent(requireContext(), OrderHistoryActivity::class.java))
        }
        view.findViewById<LinearLayout>(R.id.btnBrowsingHistory).setOnClickListener {
            startActivity(Intent(requireContext(), BrowsingHistoryActivity::class.java))
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) refreshProfileUi()
    }

    private fun refreshProfileUi() {
        tvProfileName.text = UserSession.userName ?: ""
        view?.findViewById<TextView>(R.id.tvProfileEmail)?.text = UserSession.userEmail ?: ""

        val avatarUrl = UserSession.avatarUrl
        if (!avatarUrl.isNullOrBlank()) {
            imgAvatarPlaceholder.visibility = View.GONE
            imgAvatar.visibility = View.VISIBLE
            imgAvatar.load(RetrofitClient.baseUrlForImages() + avatarUrl + "?t=" + System.currentTimeMillis()) {
                placeholder(R.drawable.image_background)
                error(R.drawable.image_background)
            }
        } else {
            imgAvatar.visibility = View.GONE
            imgAvatarPlaceholder.visibility = View.VISIBLE
        }
    }

    private fun showAvatarViewer() {
        val avatarUrl = UserSession.avatarUrl

        val dialog = android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_avatar_viewer)

        val imgFullAvatar = dialog.findViewById<ImageView>(R.id.imgFullAvatar)

        if (!avatarUrl.isNullOrBlank()) {
            imgFullAvatar.load(RetrofitClient.baseUrlForImages() + avatarUrl + "?t=" + System.currentTimeMillis()) {
                placeholder(R.drawable.image_background)
                error(R.drawable.image_background)
                transformations(coil.transform.CircleCropTransformation())
            }
        } else {
            imgFullAvatar.setImageResource(R.drawable.ic_nav_profile)
        }

        imgFullAvatar.alpha = 0f
        imgFullAvatar.scaleX = 0.85f
        imgFullAvatar.scaleY = 0.85f
        imgFullAvatar.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start()

        dialog.findViewById<FrameLayout>(android.R.id.content).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<FrameLayout>(R.id.btnEditAvatar).setOnClickListener {
            dialog.dismiss()
            showAvatarSourceDialog()
        }

        dialog.show()
    }

    // ===== Edit name =====
    private fun showEditNameDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_field, null)
        val title = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val input1 = dialogView.findViewById<EditText>(R.id.etDialogInput1)

        title.text = "Edit Name"
        input1.hint = "Name"
        input1.setText(UserSession.userName)

        val dialog = AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.btnDialogSave).setOnClickListener {
            val newName = input1.text.toString().trim()
            if (newName.isNotEmpty()) updateName(newName)
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.btnDialogCancel).setOnClickListener { dialog.dismiss() }

        dialog.show()
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
        val title = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val input1 = dialogView.findViewById<EditText>(R.id.etDialogInput1)
        val input2 = dialogView.findViewById<EditText>(R.id.etDialogInput2)

        title.text = "Change Password"
        val isGoogle = UserSession.isGoogleAccount

        if (isGoogle) {
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

        val dialog = AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.btnDialogSave).setOnClickListener {
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
                    return@setOnClickListener
                }
            }
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.btnDialogCancel).setOnClickListener { dialog.dismiss() }

        dialog.show()
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

    // ===== Avatar: choose source =====
    private fun showAvatarSourceDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_avatar_source, null)

        val dialog = AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.btnTakePhoto).setOnClickListener {
            dialog.dismiss()
            checkCameraPermissionAndLaunch()
        }
        dialogView.findViewById<TextView>(R.id.btnChooseGallery).setOnClickListener {
            dialog.dismiss()
            pickFromGallery()
        }
        dialogView.findViewById<TextView>(R.id.btnDialogCancel).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        pickImageLauncher.launch(intent)
    }

    private fun checkCameraPermissionAndLaunch() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile("avatar_camera_", ".jpg", requireContext().cacheDir)
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        pendingCameraUri = photoUri

        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
        }
        takePhotoLauncher.launch(intent)
    }

    // ===== Crop =====
    private fun startCrop(sourceUri: Uri) {
        val destFile = File(requireContext().cacheDir, "avatar_cropped_${System.currentTimeMillis()}.jpg")
        val destUri = Uri.fromFile(destFile)

        val options = com.yalantis.ucrop.UCrop.Options().apply {
            setCircleDimmedLayer(true)
            setShowCropFrame(false)
            setShowCropGrid(false)
            setToolbarTitle("Adjust Photo")
            setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
        }

        val uCropIntent = com.yalantis.ucrop.UCrop.of(sourceUri, destUri)
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .getIntent(requireContext())

        cropLauncher.launch(uCropIntent)
    }

    // ===== Upload =====
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