package com.example.sbooks.fragments.customer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.CustomerOrderAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.models.OrderModel
import com.example.sbooks.utils.SharedPrefsHelper

class OrderListFragment : Fragment() {

    private lateinit var orderDao: OrderDao
    private lateinit var orderAdapter: CustomerOrderAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPrefsHelper:SharedPrefsHelper
    private var currentStatus: OrderModel.OrderStatus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_order_list, container, false)

        // Initialize database
        val dbHelper = DatabaseHelper(requireContext())
        orderDao = OrderDao(dbHelper.readableDatabase)

        //User
        sharedPrefsHelper = SharedPrefsHelper(requireContext())

        // Setup RecyclerView
        recyclerView = root.findViewById(R.id.recyclerViewOrders)
        setupRecyclerView()

        // Setup filter tabs
        setupFilterTabs(root)

        // Setup filter button
        val btnFilter = root.findViewById<ImageButton>(R.id.btnFilter)
        btnFilter.setOnClickListener {
            // TODO: Show filter dialog
        }

        // Load orders
        loadOrders()

        return root
    }

    private fun setupRecyclerView() {
        orderAdapter = CustomerOrderAdapter { order ->
            navigateToOrderDetail(order)
        }

        recyclerView.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

    }

    private fun setupFilterTabs(root: View) {
        val tabAll = root.findViewById<TextView>(R.id.tabAll)
        val tabPending = root.findViewById<TextView>(R.id.tabPending)
        val tabProcessing = root.findViewById<TextView>(R.id.tabProcessing)
        val tabCompleted = root.findViewById<TextView>(R.id.tabCompleted)
        val tabCancelled = root.findViewById<TextView>(R.id.tabCancelled)

        val tabs = listOf(
            tabAll to null,
            tabPending to OrderModel.OrderStatus.PENDING,
            tabProcessing to OrderModel.OrderStatus.PROCESSING,
            tabCompleted to OrderModel.OrderStatus.DELIVERED,
            tabCancelled to OrderModel.OrderStatus.CANCELLED
        )

        tabs.forEach { (tab, status) ->
            tab.setOnClickListener {
                selectTab(tabs.map { it.first }, tab, status)
            }
        }
    }

    private fun selectTab(allTabs: List<TextView>, selectedTab: TextView, status: OrderModel.OrderStatus?) {
        // Reset all tabs
        allTabs.forEach { tab ->
            tab.apply {
                setBackgroundColor(Color.parseColor("#F0F0F0"))
                setTextColor(Color.parseColor("#666666"))
            }
        }

        // Highlight selected
        selectedTab.apply {
            setBackgroundColor(Color.parseColor("#FF5722"))
            setTextColor(Color.WHITE)
        }

        currentStatus = status
        loadOrders()
    }

    private fun loadOrders() {
        val currentUserId = sharedPrefsHelper.getUserId()

        val orders = if (currentStatus != null) {
            // Filter by status
            orderDao.getOrdersByStatus(currentStatus!!)
                .filter { it.customerId == currentUserId }
        } else {
            // Get all orders for current user
            orderDao.getAllOrders()
                .filter { it.customerId == currentUserId }
        }

        orderAdapter.submitList(orders)
    }

    private fun navigateToOrderDetail(order: OrderModel) {
        val bundle = Bundle().apply {
            putInt("order_id", order.id)
        }
        val detailFragment = OrderDetailFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openOrderDetail(orderId: Int) {
        val detailFragment = OrderDetailFragment.newInstance(orderId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Reload orders when returning to this fragment
        loadOrders()
    }
}