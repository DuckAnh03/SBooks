package com.example.sbooks.fragments.customer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.sbooks.R
import com.example.sbooks.activities.customer.HomeActivity
import com.example.sbooks.activities.customer.LoginActivity
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.databinding.FragmentAccountOverviewBinding
import com.example.sbooks.utils.ImageUtils
import com.example.sbooks.utils.SharedPrefsHelper

class AccountOverviewFragment : Fragment() {

    private var _binding: FragmentAccountOverviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefsHelper: SharedPrefsHelper
    private lateinit var userDao: UserDao
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefsHelper = SharedPrefsHelper(requireContext())
        val dbHelper = com.example.sbooks.database.DatabaseHelper(requireContext()) // Giả định DatabaseHelper nằm ở đây
        userDao = UserDao(dbHelper.readableDatabase)
        loadUserData()
        setupListeners()
        setupBackButton()
    }

    private fun loadUserData() {

        val user = userDao.getUserById(sharedPrefsHelper.getUserId())
        // Get user data
        if(user != null){
            val fullName = user.fullName
            val username = user.username
            val email = user.email
            val phone = user.phone
            val address = user.address
            val role = user.role


            // Display user data
            binding.apply {
                // User header
                tvUserName.text = fullName.ifEmpty { username }
                tvUserEmail.text = email
                if (user.avatar.isNotEmpty()) {
                    val bitmap = ImageUtils.loadImageFromInternalStorage(user.avatar)
                    if (bitmap != null) {
                        ivAvatar.setImageBitmap(bitmap)
                    }
                }


                // Profile section
                tvProfileName.text = fullName.ifEmpty { "Chưa cập nhật" }
                tvProfileUsername.text = username
                tvProfileEmail.text = email.ifEmpty { "Chưa cập nhật" }
                tvProfilePhone.text = phone.ifEmpty { "Chưa cập nhật" }
                tvProfileAddress.text = address.ifEmpty { "Chưa cập nhật" }

            }
        }

    }

    private fun setupListeners() {
        binding.apply {
            // Edit profile
            btnEditProfile.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ChangeUserInfoFragment())
                    .addToBackStack(null)
                    .commit()
            }

            // My orders
            layoutMyOrders.setOnClickListener {
                // Navigate to orders fragment
                navigateToOrders()
            }

            // Settings
            layoutSettings.setOnClickListener {
                Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
            }

            // Change password
            layoutChangePassword.setOnClickListener {
                openChangePasswordFragment()
            }

            // Logout
            btnLogout.setOnClickListener {
                showLogoutConfirmation()
            }
        }
    }

    private fun navigateToOrders() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OrderListFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun performLogout() {
        // Clear session
        sharedPrefsHelper.clearUserSession()

        Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()

        // Navigate to login activity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            // Tạo Intent để quay lại HomeActivity
            val intent = Intent(requireContext(), HomeActivity::class.java)

            // 🔥 Cờ tối ưu: CLEAR_TOP và SINGLE_TOP
            // CLEAR_TOP: Xóa AccountActivity và bất kỳ Activity nào nằm trên HomeActivity.
            // SINGLE_TOP: Nếu HomeActivity đã là Activity đầu tiên trong Task, nó sẽ không tạo mới,
            //             mà gọi onNewIntent().
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Tùy chọn: Gửi một cờ hoặc dữ liệu để HomeActivity biết nên hiển thị Fragment nào.
            intent.putExtra(
                "navigate_to_home",
                true
            ) // Ví dụ: báo HomeActivity chuyển về HomeFragment

            startActivity(intent)
            requireActivity().finish() // Kết thúc AccountActivity
        }
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
        _binding = null
    }
}