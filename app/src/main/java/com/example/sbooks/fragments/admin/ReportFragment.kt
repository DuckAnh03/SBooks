package com.example.sbooks.fragments.admin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.example.sbooks.models.BestSellerBookModel
import com.example.sbooks.models.TopRatedBookModel
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.ReportExportUtils
import com.example.sbooks.utils.DatePickerUtils
import java.util.*

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

    // Current data for export
    private var currentTotalRevenue = 0.0
    private var currentTotalOrders = 0
    private var currentAvgOrderValue = 0.0
    private var currentBestSellerBooks = listOf<BestSellerBookModel>()
    private var currentTopRatedBooks = listOf<TopRatedBookModel>()
    private var currentPeriod = "Hôm nay"

    // Custom date range
    private var customFromDate: Calendar? = null
    private var customToDate: Calendar? = null

    // Export type tracker
    private var pendingExportType: ExportType? = null

    private enum class ExportType {
        PDF, CSV
    }

    // Permission launcher using the modern API
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with export
            when (pendingExportType) {
                ExportType.PDF -> exportToPdf()
                ExportType.CSV -> exportToCSV()
                null -> {}
            }
            pendingExportType = null
        } else {
            // Permission denied
            DialogUtils.showToast(requireContext(), "Cần quyền truy cập để xuất file")
            pendingExportType = null
        }
    }

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
            currentPeriod = "Hôm nay"
            loadReportData("today")
        }

        btnThisWeek.setOnClickListener {
            selectTimePeriod(btnThisWeek)
            currentPeriod = "Tuần này"
            loadReportData("week")
        }

        btnThisMonth.setOnClickListener {
            selectTimePeriod(btnThisMonth)
            currentPeriod = "Tháng này"
            loadReportData("month")
        }

        btnCustomDateFrom.setOnClickListener {
            DatePickerUtils.showDatePicker(
                requireContext(),
                btnCustomDateFrom,
                customFromDate
            ) { selectedDate ->
                customFromDate = selectedDate
                checkCustomDateRange()
            }
        }

        btnCustomDateTo.setOnClickListener {
            DatePickerUtils.showDatePicker(
                requireContext(),
                btnCustomDateTo,
                customToDate
            ) { selectedDate ->
                customToDate = selectedDate
                checkCustomDateRange()
            }
        }

        btnExportPdf.setOnClickListener {
            handleExport(ExportType.PDF)
        }

        btnExportExcel.setOnClickListener {
            handleExport(ExportType.CSV)
        }

        // Set default selection
        selectTimePeriod(btnToday)
    }

    private fun handleExport(exportType: ExportType) {
        if (needsStoragePermission()) {
            // Request permission
            pendingExportType = exportType
            requestStoragePermission()
        } else {
            // No permission needed or already granted
            when (exportType) {
                ExportType.PDF -> exportToPdf()
                ExportType.CSV -> exportToCSV()
            }
        }
    }

    private fun needsStoragePermission(): Boolean {
        // Android 10 (API 29) and above don't need WRITE_EXTERNAL_STORAGE for app-specific directories
        // or when using MediaStore API
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, we typically don't need this permission if saving to app-specific storage
            // or using MediaStore. But check if your ReportExportUtils requires it.
            false
        } else {
            // For Android 9 and below, check if permission is granted
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun selectTimePeriod(selectedButton: Button) {
        // Reset all buttons
        btnToday.setBackgroundResource(R.drawable.bg_button_secondary)
        btnThisWeek.setBackgroundResource(R.drawable.bg_button_secondary)
        btnThisMonth.setBackgroundResource(R.drawable.bg_button_secondary)

        // Set selected button
        selectedButton.setBackgroundResource(R.drawable.bg_button_primary)
    }

    private fun checkCustomDateRange() {
        if (customFromDate != null && customToDate != null) {
            // Validate date range
            if (customFromDate!!.after(customToDate)) {
                DialogUtils.showToast(requireContext(), "Ngày bắt đầu không thể sau ngày kết thúc")
                return
            }

            // Update period text and load data
            val fromText = DatePickerUtils.formatDate(customFromDate!!)
            val toText = DatePickerUtils.formatDate(customToDate!!)
            currentPeriod = "$fromText - $toText"

            // Reset button selection
            btnToday.setBackgroundResource(R.drawable.bg_button_secondary)
            btnThisWeek.setBackgroundResource(R.drawable.bg_button_secondary)
            btnThisMonth.setBackgroundResource(R.drawable.bg_button_secondary)

            loadReportData("custom")
        }
    }

    private fun parseOrderDate(dateString: String): Calendar {
        // Parse the order date string to Calendar
        // Format: "2025-11-21 13:16:20" (yyyy-MM-dd HH:mm:ss)
        val calendar = Calendar.getInstance()
        try {
            val datePart = dateString.split(" ")[0] // Get "2025-11-21"
            val parts = datePart.split("-")
            if (parts.size == 3) {
                calendar.set(Calendar.YEAR, parts[0].toInt())
                calendar.set(Calendar.MONTH, parts[1].toInt() - 1) // Month is 0-based
                calendar.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing order date: $dateString", e)
        }
        return calendar
    }

    private fun loadReportData(period: String = "today") {
        try {
            // Load basic statistics based on period
            val allOrders = orderDao.getAllOrders()
            val filteredOrders = when (period) {
                "today" -> {
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val todayEnd = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }

                    allOrders.filter { order ->
                        val orderDate = parseOrderDate(order.orderDate)
                        !orderDate.before(today) && !orderDate.after(todayEnd)
                    }
                }
                "week" -> {
                    val weekStart = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val weekEnd = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }

                    allOrders.filter { order ->
                        val orderDate = parseOrderDate(order.orderDate)
                        !orderDate.before(weekStart) && !orderDate.after(weekEnd)
                    }
                }
                "month" -> {
                    val monthStart = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val monthEnd = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }

                    allOrders.filter { order ->
                        val orderDate = parseOrderDate(order.orderDate)
                        !orderDate.before(monthStart) && !orderDate.after(monthEnd)
                    }
                }
                "custom" -> {
                    if (customFromDate != null && customToDate != null) {
                        val fromDate = customFromDate!!.clone() as Calendar
                        fromDate.set(Calendar.HOUR_OF_DAY, 0)
                        fromDate.set(Calendar.MINUTE, 0)
                        fromDate.set(Calendar.SECOND, 0)
                        fromDate.set(Calendar.MILLISECOND, 0)

                        val toDate = customToDate!!.clone() as Calendar
                        toDate.set(Calendar.HOUR_OF_DAY, 23)
                        toDate.set(Calendar.MINUTE, 59)
                        toDate.set(Calendar.SECOND, 59)
                        toDate.set(Calendar.MILLISECOND, 999)

                        allOrders.filter { order ->
                            val orderDate = parseOrderDate(order.orderDate)
                            !orderDate.before(fromDate) && !orderDate.after(toDate)
                        }
                    } else allOrders
                }
                else -> allOrders
            }

            val completedOrders = filteredOrders.filter { it.status == com.example.sbooks.models.OrderModel.OrderStatus.DELIVERED }

            currentTotalRevenue = completedOrders.sumOf { it.finalAmount }
            currentTotalOrders = completedOrders.size
            currentAvgOrderValue = if (currentTotalOrders > 0) currentTotalRevenue / currentTotalOrders else 0.0

            tvTotalRevenueAmount.text = String.format("%,.0f VNĐ", currentTotalRevenue)
            tvTotalOrdersCount.text = currentTotalOrders.toString()
            tvAvgOrderValue.text = String.format("%,.0f VNĐ", currentAvgOrderValue)

            // Load bestselling books
            currentBestSellerBooks = bookDao.getBestSellingBooks(10)
            bestSellerAdapter.submitList(currentBestSellerBooks)

            // Load top rated books
            currentTopRatedBooks = bookDao.getTopRatedBooks(10)
            topRatedAdapter.submitList(currentTopRatedBooks)

        } catch (e: Exception) {
            Log.e(TAG, "Error loading report data", e)
            // Set default values
            currentTotalRevenue = 0.0
            currentTotalOrders = 0
            currentAvgOrderValue = 0.0
            currentBestSellerBooks = emptyList()
            currentTopRatedBooks = emptyList()

            tvTotalRevenueAmount.text = "0 VNĐ"
            tvTotalOrdersCount.text = "0"
            tvAvgOrderValue.text = "0 VNĐ"
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun exportToPdf() {
        try {
            btnExportPdf.isEnabled = false
            btnExportPdf.text = "Đang xuất PDF..."

            val success = ReportExportUtils.exportToPdf(
                requireContext(),
                currentTotalRevenue,
                currentTotalOrders,
                currentAvgOrderValue,
                currentBestSellerBooks,
                currentTopRatedBooks,
                currentPeriod
            )

            if (success) {
                DialogUtils.showToast(requireContext(), "Xuất PDF thành công!")
            } else {
                DialogUtils.showToast(requireContext(), "Lỗi khi xuất PDF")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error exporting PDF", e)
            DialogUtils.showToast(requireContext(), "Lỗi khi xuất PDF: ${e.message}")
        } finally {
            btnExportPdf.isEnabled = true
            btnExportPdf.text = "Xuất PDF"
        }
    }

    private fun exportToCSV() {
        try {
            btnExportExcel.isEnabled = false
            btnExportExcel.text = "Đang xuất CSV..."

            val success = ReportExportUtils.exportToExcel(
                requireContext(),
                currentTotalRevenue,
                currentTotalOrders,
                currentAvgOrderValue,
                currentBestSellerBooks,
                currentTopRatedBooks,
                currentPeriod
            )

            if (success) {
                DialogUtils.showToast(requireContext(), "Xuất CSV thành công!")
            } else {
                DialogUtils.showToast(requireContext(), "Lỗi khi xuất CSV")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error exporting CSV", e)
            DialogUtils.showToast(requireContext(), "Lỗi khi xuất CSV: ${e.message}")
        } finally {
            btnExportExcel.isEnabled = true
            btnExportExcel.text = "Xuất Excel"
        }
    }
}