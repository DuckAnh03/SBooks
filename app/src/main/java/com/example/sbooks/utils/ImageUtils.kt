package com.example.sbooks.utils
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import com.bumptech.glide.Glide // Add Glide dependency in build.gradle
import com.example.sbooks.R

object ImageUtils {

    const val REQUEST_CODE_PICK_IMAGE = 1001

    fun dispatchPickImageIntent(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // Alternative for broader compatibility / different UI:
        // val intent = Intent(Intent.ACTION_GET_CONTENT)
        // intent.type = "image/*"
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    // In a Fragment, you'd use registerForActivityResult for the modern approach.
    // This is a simplified version for Activity's onActivityResult.

    fun loadSelectedImageToView(context: Context, imageUri: Uri?, imageView: ImageView) {
        if (imageUri != null) {
            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.bg_book_placeholder) // Your placeholder
                .error(R.drawable.ic_book) // Your error placeholder
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.bg_book_placeholder)
        }
    }

    fun compressImage(context: Context, uri: Uri, maxWidth: Int = 800, maxHeight: Int = 600, quality: Int = 80): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val scaledBitmap = scaleBitmap(originalBitmap, maxWidth, maxHeight)
            scaledBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = minOf(scaleWidth, scaleHeight)

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, filename: String): String? {
        return try {
            val directory = File(context.filesDir, Constants.IMAGE_DIRECTORY)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, "$filename.jpg")
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun loadImageFromInternalStorage(imagePath: String): Bitmap? {
        return try {
            if (imagePath.isNotEmpty() && File(imagePath).exists()) {
                BitmapFactory.decodeFile(imagePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteImageFile(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            if (idx != -1) it.getString(idx) else null
        }
    }

    fun generateUniqueFileName(): String {
        return "img_${System.currentTimeMillis()}"
    }

    fun generateCategoryIconFileName(): String {
        return "category_icon_${System.currentTimeMillis()}"
    }

    fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 90): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    // Helper function to get default category icons based on category name
    fun getDefaultCategoryIcon(categoryName: String): Int {
        return when (categoryName.lowercase()) {
            "văn học", "van hoc" -> R.drawable.ic_book
            "khoa học", "khoa hoc" -> R.drawable.ic_category
            "nghệ thuật", "nghe thuat" -> R.drawable.ic_category
            "kinh tế", "kinh te" -> R.drawable.ic_category
            "lịch sử", "lich su" -> R.drawable.ic_book
            "tâm lý", "tam ly" -> R.drawable.ic_category
            else -> R.drawable.ic_category
        }
    }

    // Function to create circular bitmap for category icons
    fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)

        val paint = android.graphics.Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)

        val rect = android.graphics.Rect(0, 0, size, size)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }
}