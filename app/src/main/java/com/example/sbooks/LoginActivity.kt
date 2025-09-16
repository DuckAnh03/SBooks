package com.example.sbooks

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sbooks.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý nút Đăng nhập
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                binding.tvError.text = "Vui lòng nhập đầy đủ thông tin"
                binding.tvError.visibility = android.view.View.VISIBLE
            } else {
                // Kiểm tra dữ liệu tạm thời
                if (username == "admin" && password == "1234") {
                    binding.tvError.visibility = android.view.View.GONE

                    // Tạo Intent để chuyển sang HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)

                    // Kết thúc LoginActivity để bấm back không quay lại
                    finish()
                } else {
                    binding.tvError.text = "Tên đăng nhập hoặc mật khẩu không đúng"
                    binding.tvError.visibility = android.view.View.VISIBLE
                }
            }
        }

        // Xử lý nút Đăng ký (chưa triển khai)
        binding.btnRegister.setOnClickListener {
            Toast.makeText(this, "Chức năng đăng ký chưa được triển khai", Toast.LENGTH_SHORT).show()
        }
    }
}
