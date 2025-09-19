package com.example.sbooks.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {

    const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val CALL_PHONE = Manifest.permission.CALL_PHONE

    val STORAGE_PERMISSIONS = arrayOf(
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE
    )

    val CAMERA_PERMISSIONS = arrayOf(
        CAMERA_PERMISSION,
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE
    )

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }

    fun hasStoragePermissions(context: Context): Boolean {
        return hasPermissions(context, STORAGE_PERMISSIONS)
    }

    fun hasCameraPermissions(context: Context): Boolean {
        return hasPermissions(context, CAMERA_PERMISSIONS)
    }

    fun hasCallPermission(context: Context): Boolean {
        return hasPermission(context, CALL_PHONE)
    }
}
