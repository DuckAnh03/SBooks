package com.example.sbooks.fragments.customer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.sbooks.R
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.models.UserModel
import com.example.sbooks.utils.ImageUtils
import com.example.sbooks.utils.SharedPrefsHelper
import de.hdodenhof.circleimageview.CircleImageView

class ChangeUserInfoFragment : Fragment() {

    private lateinit var userDao: UserDao
    private lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var currentUser: UserModel? = null
    private var selectedImageUri: Uri? = null
    private var avatarBitmap: Bitmap? = null

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var ivAvatar: CircleImageView
    private lateinit var btnChangeAvatar: ImageButton
    private lateinit var tvUsername: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var layoutChangePassword: LinearLayout
    private lateinit var btnSaveChanges: Button

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                loadImageFromUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_change_user_info, container, false)

        // Initialize
        sharedPrefsHelper = SharedPrefsHelper(requireContext())
        val dbHelper = DatabaseHelper(requireContext())
        userDao = UserDao(dbHelper.writableDatabase)

        // Initialize views
        initViews(root)

        // Setup listeners
        setupListeners()

        // Load user data
        loadUserData()

        return root
    }

    private fun initViews(root: View) {
        btnBack = root.findViewById(R.id.btnBack)
        ivAvatar = root.findViewById(R.id.ivAvatar)
        btnChangeAvatar = root.findViewById(R.id.btnChangeAvatar)
        tvUsername = root.findViewById(R.id.tvUsername)
        tvUserRole = root.findViewById(R.id.tvUserRole)
        etFullName = root.findViewById(R.id.etFullName)
        etEmail = root.findViewById(R.id.etEmail)
        etPhone = root.findViewById(R.id.etPhone)
        etAddress = root.findViewById(R.id.etAddress)
        layoutChangePassword = root.findViewById(R.id.layoutChangePassword)
        btnSaveChanges = root.findViewById(R.id.btnSaveChanges)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnChangeAvatar.setOnClickListener {
            openImagePicker()
        }

        ivAvatar.setOnClickListener {
            openImagePicker()
        }

        layoutChangePassword.setOnClickListener {
            openChangePasswordFragment()
        }

        btnSaveChanges.setOnClickListener {
            saveUserInfo()
        }
    }

    private fun loadUserData() {
        val userId = sharedPrefsHelper.getUserId()
        if (userId == -1) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        Thread {
            try {
                val user = userDao.getUserById(userId)

                activity?.runOnUiThread {
                    if (user != null) {
                        currentUser = user
                        displayUserInfo(user)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Không tìm thấy thông tin người dùng",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun displayUserInfo(user: UserModel) {
        // Username and role
        tvUsername.text = user.username
        tvUserRole.text = user.getDisplayRole()

        // Form fields
        etFullName.setText(user.fullName)
        etEmail.setText(user.email)
        etPhone.setText(user.phone)
        etAddress.setText(user.address)

        // Avatar
        if (user.avatar.isNotEmpty()) {
            val bitmap = ImageUtils.loadImageFromInternalStorage(user.avatar)
            if (bitmap != null) {
                ivAvatar.setImageBitmap(bitmap)
            } else {
                setDefaultAvatar(user.username)
            }
        } else {
            setDefaultAvatar(user.username)
        }
    }

    private fun setDefaultAvatar(username: String) {
        // Set default avatar with first letter of username
        // You can create a custom drawable or use a library
        ivAvatar.setImageResource(R.drawable.ic_account_24)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                uri
            )
            avatarBitmap = bitmap
            ivAvatar.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Không thể tải ảnh: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateInput(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Validate full name
        if (fullName.isEmpty()) {
            etFullName.error = "Vui lòng nhập họ và tên"
            etFullName.requestFocus()
            return false
        }

        if (fullName.length < 2) {
            etFullName.error = "Họ và tên phải có ít nhất 2 ký tự"
            etFullName.requestFocus()
            return false
        }

        // Validate email
        if (email.isEmpty()) {
            etEmail.error = "Vui lòng nhập email"
            etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email không hợp lệ"
            etEmail.requestFocus()
            return false
        }

        // Validate phone
        if (phone.isEmpty()) {
            etPhone.error = "Vui lòng nhập số điện thoại"
            etPhone.requestFocus()
            return false
        }

        if (!Patterns.PHONE.matcher(phone).matches() || phone.length < 10) {
            etPhone.error = "Số điện thoại không hợp lệ"
            etPhone.requestFocus()
            return false
        }

        return true
    }

    private fun saveUserInfo() {
        if (!validateInput()) {
            return
        }

        val user = currentUser ?: return

        // Show loading
        btnSaveChanges.isEnabled = false
        btnSaveChanges.text = "Đang lưu..."

        Thread {
            try {
                // Save avatar if changed
                var avatarPath = user.avatar
                if (avatarBitmap != null) {
                    val filename = "avatar_${user.id}_${System.currentTimeMillis()}.jpg"
                    avatarPath = ImageUtils.saveImageToInternalStorage(
                        requireContext(),
                        avatarBitmap!!,
                        filename
                    ) ?: user.avatar
                }

                // Create updated user
                val updatedUser = user.copy(
                    fullName = etFullName.text.toString().trim(),
                    email = etEmail.text.toString().trim(),
                    phone = etPhone.text.toString().trim(),
                    address = etAddress.text.toString().trim(),
                    avatar = avatarPath
                )

                // Update in database
                val result = userDao.updateUser(updatedUser)

                activity?.runOnUiThread {
                    btnSaveChanges.isEnabled = true
                    btnSaveChanges.text = "Lưu thay đổi"

                    if (result > 0) {
                        // Update shared preferences
                        sharedPrefsHelper.updateUserInfo(updatedUser)

                        Toast.makeText(
                            requireContext(),
                            "Cập nhật thông tin thành công",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Go back
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Không thể cập nhật thông tin",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    btnSaveChanges.isEnabled = true
                    btnSaveChanges.text = "Lưu thay đổi"

                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun openChangePasswordFragment() {
        // Navigate to change password fragment
        val changePasswordFragment = ChangePasswordFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, changePasswordFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        avatarBitmap?.recycle()
        avatarBitmap = null
    }
}