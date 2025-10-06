package com.example.sbooks.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.example.sbooks.models.BestSellerBookModel
import com.example.sbooks.models.TopRatedBookModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportExportUtils {

    companion object {
        private const val PDF_WIDTH = 595 // A4 width in points
        private const val PDF_HEIGHT = 842 // A4 height in points

        /**
         * Export report data to PDF
         */
        fun exportToPdf(
            context: Context,
            totalRevenue: Double,
            totalOrders: Int,
            avgOrderValue: Double,
            bestSellerBooks: List<BestSellerBookModel>,
            topRatedBooks: List<TopRatedBookModel>,
            period: String = "Hôm nay"
        ): Boolean {
            try {
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(PDF_WIDTH, PDF_HEIGHT, 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                // Setup paint for text
                val titlePaint = Paint().apply {
                    textSize = 24f
                    color = Color.BLACK
                    isAntiAlias = true
                    isFakeBoldText = true
                }

                val headerPaint = Paint().apply {
                    textSize = 18f
                    color = Color.BLACK
                    isAntiAlias = true
                    isFakeBoldText = true
                }

                val normalPaint = Paint().apply {
                    textSize = 12f
                    color = Color.BLACK
                    isAntiAlias = true
                }

                val smallPaint = Paint().apply {
                    textSize = 10f
                    color = Color.GRAY
                    isAntiAlias = true
                }

                var yPosition = 50f
                val margin = 50f

                // Title
                canvas.drawText("BÁO CÁO DOANH THU", margin, yPosition, titlePaint)
                yPosition += 40f

                // Period
                canvas.drawText("Thời gian: $period", margin, yPosition, normalPaint)
                yPosition += 30f

                // Date generated
                val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                canvas.drawText("Ngày tạo: $currentDate", margin, yPosition, smallPaint)
                yPosition += 40f

                // Revenue Statistics
                canvas.drawText("THỐNG KÊ DOANH THU", margin, yPosition, headerPaint)
                yPosition += 30f

                canvas.drawText("Tổng doanh thu: ${String.format("%,.0f VNĐ", totalRevenue)}", margin, yPosition, normalPaint)
                yPosition += 20f

                canvas.drawText("Tổng đơn hàng: $totalOrders", margin, yPosition, normalPaint)
                yPosition += 20f

                canvas.drawText("Giá trị đơn hàng TB: ${String.format("%,.0f VNĐ", avgOrderValue)}", margin, yPosition, normalPaint)
                yPosition += 40f

                // Best Selling Books
                if (bestSellerBooks.isNotEmpty()) {
                    canvas.drawText("TOP SÁCH BÁN CHẠY", margin, yPosition, headerPaint)
                    yPosition += 30f

                    bestSellerBooks.take(5).forEachIndexed { index, book ->
                        val text = "${index + 1}. ${book.title} - ${book.author}"
                        canvas.drawText(text, margin, yPosition, normalPaint)
                        yPosition += 15f

                        val details = "   Đã bán: ${book.soldQuantity} | Doanh thu: ${book.getFormattedRevenue()}"
                        canvas.drawText(details, margin, yPosition, smallPaint)
                        yPosition += 20f
                    }
                    yPosition += 20f
                }

                // Top Rated Books
                if (topRatedBooks.isNotEmpty()) {
                    canvas.drawText("TOP SÁCH ĐÁNH GIÁ CAO", margin, yPosition, headerPaint)
                    yPosition += 30f

                    topRatedBooks.take(5).forEachIndexed { index, book ->
                        val text = "${index + 1}. ${book.title} - ${book.author}"
                        canvas.drawText(text, margin, yPosition, normalPaint)
                        yPosition += 15f

                        val details = "   Đánh giá: ${String.format("%.1f", book.rating)}/5.0 | Giá: ${book.getFormattedPrice()}"
                        canvas.drawText(details, margin, yPosition, smallPaint)
                        yPosition += 20f
                    }
                }

                document.finishPage(page)

                // Save file
                val fileName = "BaoCao_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
                val (outputStream, uri) = createOutputStream(context, fileName, "application/pdf")

                if (outputStream != null) {
                    outputStream.use {
                        document.writeTo(it)
                    }
                    document.close()

                    // Show success message
                    Toast.makeText(context, "Đã xuất PDF: $fileName", Toast.LENGTH_LONG).show()

                    // Try to open the file
                    uri?.let { openFile(context, it, "application/pdf") }

                    return true
                } else {
                    document.close()
                    Toast.makeText(context, "Không thể tạo file PDF", Toast.LENGTH_SHORT).show()
                    return false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Lỗi khi xuất PDF: ${e.message}", Toast.LENGTH_LONG).show()
                return false
            }
        }

        /**
         * Export report data to Excel (CSV format for simplicity)
         */
        fun exportToExcel(
            context: Context,
            totalRevenue: Double,
            totalOrders: Int,
            avgOrderValue: Double,
            bestSellerBooks: List<BestSellerBookModel>,
            topRatedBooks: List<TopRatedBookModel>,
            period: String = "Hôm nay"
        ): Boolean {
            try {
                val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val csvContent = StringBuilder()

                // Title and basic info
                csvContent.append("BÁO CÁO DOANH THU\n")
                csvContent.append("Thời gian:,$period\n")
                csvContent.append("Ngày tạo:,$currentDate\n")
                csvContent.append("\n")

                // Statistics
                csvContent.append("THỐNG KÊ DOANH THU\n")
                csvContent.append("Tổng doanh thu:,${String.format("%.0f", totalRevenue)} VNĐ\n")
                csvContent.append("Tổng đơn hàng:,$totalOrders\n")
                csvContent.append("Giá trị đơn hàng TB:,${String.format("%.0f", avgOrderValue)} VNĐ\n")
                csvContent.append("\n")

                // Best selling books
                if (bestSellerBooks.isNotEmpty()) {
                    csvContent.append("TOP SÁCH BÁN CHẠY\n")
                    csvContent.append("Hạng,Tên sách,Tác giả,Giá,Số lượng bán,Doanh thu\n")

                    bestSellerBooks.forEach { book ->
                        csvContent.append("${book.rank},${book.title},${book.author},${book.price.toLong()},${book.soldQuantity},${book.revenue.toLong()}\n")
                    }
                    csvContent.append("\n")
                }

                // Top rated books
                if (topRatedBooks.isNotEmpty()) {
                    csvContent.append("TOP SÁCH ĐÁNH GIÁ CAO\n")
                    csvContent.append("STT,Tên sách,Tác giả,Giá,Đánh giá,Số đánh giá,Tồn kho\n")

                    topRatedBooks.forEachIndexed { index, book ->
                        csvContent.append("${index + 1},${book.title},${book.author},${book.price.toLong()},${book.rating},${book.reviewCount},${book.stock}\n")
                    }
                }

                // Save file
                val fileName = "BaoCao_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
                val (outputStream, uri) = createOutputStream(context, fileName, "text/csv")

                if (outputStream != null) {
                    outputStream.use {
                        it.write(csvContent.toString().toByteArray(Charsets.UTF_8))
                    }

                    // Show success message
                    Toast.makeText(context, "Đã xuất CSV: $fileName", Toast.LENGTH_LONG).show()

                    // Try to open the file
                    uri?.let { openFile(context, it, "text/csv") }

                    return true
                } else {
                    Toast.makeText(context, "Không thể tạo file CSV", Toast.LENGTH_SHORT).show()
                    return false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Lỗi khi xuất CSV: ${e.message}", Toast.LENGTH_LONG).show()
                return false
            }
        }

        /**
         * Create output stream based on Android version
         * Returns Pair of (OutputStream, Uri)
         */
        private fun createOutputStream(
            context: Context,
            fileName: String,
            mimeType: String
        ): Pair<OutputStream?, Uri?> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ use MediaStore
                createOutputStreamMediaStore(context, fileName, mimeType)
            } else {
                // Android 9 and below use traditional file system
                createOutputStreamLegacy(context, fileName)
            }
        }

        /**
         * Create output stream using MediaStore (Android 10+)
         */
        @RequiresApi(Build.VERSION_CODES.Q)
        private fun createOutputStreamMediaStore(
            context: Context,
            fileName: String,
            mimeType: String
        ): Pair<OutputStream?, Uri?> {
            return try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                uri?.let {
                    val outputStream = context.contentResolver.openOutputStream(it)
                    Pair(outputStream, it)
                } ?: Pair(null, null)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(null, null)
            }
        }

        /**
         * Create output stream using legacy file system (Android 9 and below)
         */
        private fun createOutputStreamLegacy(
            context: Context,
            fileName: String
        ): Pair<OutputStream?, Uri?> {
            return try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)
                val outputStream = FileOutputStream(file)
                val uri = Uri.fromFile(file)

                Pair(outputStream, uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(null, null)
            }
        }

        /**
         * Open file with appropriate app
         */
        private fun openFile(context: Context, uri: Uri, mimeType: String) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(intent, "Mở file"))
                } else {
                    Toast.makeText(context, "File đã được lưu trong thư mục Downloads", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "File đã được lưu trong thư mục Downloads", Toast.LENGTH_SHORT).show()
            }
        }
    }
}