package com.example.sbooks.fragments.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.BestSellerAdapter
import com.example.sbooks.adapter.TopRatedAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.utils.DialogUtils

class ReportFragment : Fragment() {

    private companion object {
        private const val TAG = "ReportFragment"
    }

    // Views
    private lateinit var btnToday: Button
    private lateinit var btnThisWeek: Button
    private lateinit var btnThisMonth: Button
    private lateinit var btnCustomDateFrom: Button
    private lateinit var btnCustomDateTo: Button
    private lateinit var tvTotalRevenueAmount: TextView
    private lateinit var tvTotalOrdersCount: TextView
    private lateinit var tvAvgOrderValue: TextView
    private lateinit var rvBestsellingBooks: RecyclerView
    private lateinit var rvTopRatedBooks: RecyclerView
    private lateinit var btnExportPdf: Button
    private lateinit var btnExportExcel: Button

    // Data
    private lateinit var orderDao: OrderDao
    private lateinit var bookDao: BookDao
    private lateinit var userDao: UserDao
    private lateinit var bestSellerAdapter: BestSellerAdapter
    private lateinit var topRatedAdapter: TopRatedAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeViews(view)
            setupDatabase()
            setupRecyclerViews()
            setupClickListeners()
            loadReportData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
        }
    }

    private fun initializeViews(view: View) {
        btnToday = view.findViewById(R.id.btn_today)
        btnThisWeek = view.findViewById(R.id.btn_this_week)
        btnThisMonth = view.findViewById(R.id.btn_this_month)
        btnCustomDateFrom = view.findViewById(R.id.btn_custom_date_from)
        btnCustomDateTo = view.findViewById(R.id.btn_custom_date_to)
        tvTotalRevenueAmount = view.findViewById(R.id.tv_total_revenue_amount)
        tvTotalOrdersCount = view.findViewById(R.id.tv_total_orders_count)
        tvAvgOrderValue = view.findViewById(R.id.tv_avg_order_value)
        rvBestsellingBooks = view.findViewById(R.id.rv_bestselling_books)
        rvTopRatedBooks = view.findViewById(R.id.rv_top_rated_books)
        btnExportPdf = view.findViewById(R.id.btn_export_pdf)
        btnExportExcel = view.findViewById(R.id.btn_export_excel)
    }

    private fun setupDatabase() {
        val dbHelper = DatabaseHelper(requireContext())
        orderDao = OrderDao(dbHelper.writableDatabase)
        bookDao = BookDao(dbHelper.writableDatabase)
        userDao = UserDao(dbHelper.writableDatabase)
    }

    private fun setupRecyclerViews() {
        bestSellerAdapter = BestSellerAdapter { book ->
            DialogUtils.showToast(requireContext(), "Bestseller: ${book.title}")
        }

        topRatedAdapter = TopRatedAdapter { book ->
            DialogUtils.showToast(requireContext(), "Top rated: ${book.title}")
        }

        rvBestsellingBooks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bestSellerAdapter
        }

        rvTopRatedBooks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = topRatedAdapter
        }
    }

    private fun setupClickListeners() {
        btnToday.setOnClickListener {
            selectTimePeriod(btnToday)
            loadReportData("today")
        }

        btnThisWeek.setOnClickListener {
            selectTimePeriod(btnThisWeek)
            loadReportData("week")
        }

        btnThisMonth.setOnClickListener {
            selectTimePeriod(btnThisMonth)
            loadReportData("month")
        }

        btnCustomDateFrom.setOnClickListener {
            DialogUtils.showToast(requireContext(), "Chọn ngày bắt đầu")
        }

        btnCustomDateTo.setOnClickListener {
            DialogUtils.showToast(requireContext(), "Chọn ngày kết thúc")
        }

        btnExportPdf.setOnClickListener {
            DialogUtils.showToast(requireContext(), "Xuất PDF - Tính năng đang phát triển")
        }

        btnExportExcel.setOnClickListener {
            DialogUtils.showToast(requireContext(), "Xuất Excel - Tính năng đang phát triển")
        }

        // Set default selection
        selectTimePeriod(btnToday)
    }

    private fun selectTimePeriod(selectedButton: Button) {
        // Reset all buttons
        btnToday.setBackgroundResource(R.drawable.bg_button_secondary)
        btnThisWeek.setBackgroundResource(R.drawable.bg_button_secondary)
        btnThisMonth.setBackgroundResource(R.drawable.bg_button_secondary)

        // Set selected button
        selectedButton.setBackgroundResource(R.drawable.bg_button_primary)
    }

    private fun loadReportData(period: String = "today") {
        try {
            // Load basic statistics
            val allOrders = orderDao.getAllOrders()
            val completedOrders = allOrders.filter { it.status == com.example.sbooks.models.OrderModel.OrderStatus.DELIVERED }

            val totalRevenue = completedOrders.sumOf { it.finalAmount }
            val totalOrders = completedOrders.size
            val avgOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0

            tvTotalRevenueAmount.text = String.format("%,.0f VNĐ", totalRevenue)
            tvTotalOrdersCount.text = totalOrders.toString()
            tvAvgOrderValue.text = String.format("%,.0f VNĐ", avgOrderValue)

            // Load bestselling books
            val bestSellingBooks = bookDao.getBestSellingBooks(10)
            bestSellerAdapter.submitList(bestSellingBooks)

            // Load top rated books
            val topRatedBooks = bookDao.getTopRatedBooks(10)
            topRatedAdapter.submitList(topRatedBooks)

        } catch (e: Exception) {
            Log.e(TAG, "Error loading report data", e)
            // Set default values
            tvTotalRevenueAmount.text = "0 VNĐ"
            tvTotalOrdersCount.text = "0"
            tvAvgOrderValue.text = "0 VNĐ"
        }
    }
}