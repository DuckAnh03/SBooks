package com.example.sbooks.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sbooks.R
import androidx.navigation.fragment.findNavController
import android.widget.Button


class AdminDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lấy button từ layout
        val btnAddBook = view.findViewById<Button>(R.id.btn_add_book)
        val btnAddUser = view.findViewById<Button>(R.id.btn_add_user)

        // Sự kiện click -> điều hướng tới Fragment khác
        btnAddBook.setOnClickListener {
            findNavController().navigate(R.id.nav_admin_books)
        }

        btnAddUser.setOnClickListener {
            findNavController().navigate(R.id.nav_admin_users)
        }
    }
}
