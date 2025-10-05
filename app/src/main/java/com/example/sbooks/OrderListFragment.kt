package com.example.sbooks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView

class OrderListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_order_list, container, false)

        // Xử lý click vào đơn hàng để mở OrderDetailFragment
        setupOrderClickListeners(root)

        // Xử lý click vào các tab filter
        setupTabClickListeners(root)

        return root
    }

    private fun setupOrderClickListeners(root: View) {
        // Lấy CardView của đơn hàng 1
        val orderLayout = root.findViewById<CardView>(R.id.orderLayout)

        orderLayout?.setOnClickListener {
            // Chuyển đến OrderDetailFragment khi click vào đơn hàng
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OrderDetailFragment())
                .addToBackStack(null)
                .commit()
        }

        // Có thể thêm click listener cho các đơn hàng khác nếu cần
        // setupOtherOrderClicks(root)
    }

    private fun setupTabClickListeners(root: View) {
        // Lấy các tab filter
        val tabAll = root.findViewById<TextView>(R.id.tabAll)
        val tabPending = root.findViewById<TextView>(R.id.tabPending)
        val tabProcessing = root.findViewById<TextView>(R.id.tabProcessing)
        val tabCompleted = root.findViewById<TextView>(R.id.tabCompleted)
        val tabCancelled = root.findViewById<TextView>(R.id.tabCancelled)

        // Xử lý click tab "Tất cả"
        tabAll?.setOnClickListener {
            selectTab(tabAll, listOf(tabPending, tabProcessing, tabCompleted, tabCancelled))
            // TODO: Lọc hiển thị tất cả đơn hàng
        }

        // Xử lý click tab "Chờ xác nhận"
        tabPending?.setOnClickListener {
            selectTab(tabPending, listOf(tabAll, tabProcessing, tabCompleted, tabCancelled))
            // TODO: Lọc hiển thị đơn hàng chờ xác nhận
        }

        // Xử lý click tab "Đang giao"
        tabProcessing?.setOnClickListener {
            selectTab(tabProcessing, listOf(tabAll, tabPending, tabCompleted, tabCancelled))
            // TODO: Lọc hiển thị đơn hàng đang giao
        }

        // Xử lý click tab "Hoàn tất"
        tabCompleted?.setOnClickListener {
            selectTab(tabCompleted, listOf(tabAll, tabPending, tabProcessing, tabCancelled))
            // TODO: Lọc hiển thị đơn hàng hoàn tất
        }

        // Xử lý click tab "Đã hủy"
        tabCancelled?.setOnClickListener {
            selectTab(tabCancelled, listOf(tabAll, tabPending, tabProcessing, tabCompleted))
            // TODO: Lọc hiển thị đơn hàng đã hủy
        }
    }

    private fun selectTab(selectedTab: TextView, otherTabs: List<TextView>) {
        // Highlight tab được chọn
        selectedTab.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, null))
        selectedTab.setTextColor(resources.getColor(android.R.color.white, null))

        // Reset các tab khác
        otherTabs.forEach { tab ->
            tab.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            tab.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }

    // Factory method để tạo instance mới
    companion object {
        fun newInstance(): OrderListFragment {
            return OrderListFragment()
        }

        // Có thể thêm factory method với parameters nếu cần
        fun newInstance(filterType: String): OrderListFragment {
            val fragment = OrderListFragment()
            val args = Bundle().apply {
                putString("filter_type", filterType)
            }
            fragment.arguments = args
            return fragment
        }
    }
}