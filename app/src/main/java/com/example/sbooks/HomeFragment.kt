package com.example.sbooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // Sửa: Lấy CardView thay vì LinearLayout
        val bookLayout = root.findViewById<CardView>(R.id.bookLayout)
        bookLayout.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookDetailFragment())
                .addToBackStack(null)
                .commit()
        }

        // Sửa: Lấy ImageButton thay vì ImageView
        val btnMore = root.findViewById<ImageButton>(R.id.btnMoreCategory)

        btnMore.setOnClickListener {
            val popup = PopupMenu(requireContext(), btnMore)
            popup.menuInflater.inflate(R.menu.menu_categories, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.cat_ai -> {
                        Toast.makeText(requireContext(), "Chọn AI", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.cat_science -> {
                        Toast.makeText(requireContext(), "Chọn Khoa học", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.cat_psychology -> {
                        Toast.makeText(requireContext(), "Chọn Tâm lý", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.cat_marketing -> {
                        Toast.makeText(requireContext(), "Chọn Marketing", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.cat_history -> {
                        Toast.makeText(requireContext(), "Chọn Lịch sử", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        return root
    }
}