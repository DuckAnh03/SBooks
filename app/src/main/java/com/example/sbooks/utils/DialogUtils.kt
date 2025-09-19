package com.example.sbooks.utils
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object DialogUtils {

    fun showConfirmDialog(
        context: Context,
        title: String,
        message: String,
        positiveAction: () -> Unit,
        negativeAction: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Xác nhận") { _, _ -> positiveAction() }
            .setNegativeButton("Hủy") { _, _ -> negativeAction?.invoke() }
            .setCancelable(false)
            .show()
    }

    fun showDeleteConfirmDialog(
        context: Context,
        itemName: String,
        deleteAction: () -> Unit
    ) {
        showConfirmDialog(
            context,
            "Xác nhận xóa",
            "Bạn có chắc chắn muốn xóa \"$itemName\"?\nHành động này không thể hoàn tác.",
            deleteAction
        )
    }

    fun showInfoDialog(context: Context, title: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun showErrorDialog(context: Context, error: String, function: () -> Unit) {
        showInfoDialog(context, "Lỗi", error)
    }

    fun showSuccessDialog(context: Context, message: String) {
        showInfoDialog(context, "Thành công", message)
    }

    fun showChoiceDialog(
        context: Context,
        title: String,
        items: Array<String>,
        selectedAction: (Int) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setItems(items) { _, which -> selectedAction(which) }
            .show()
    }

    fun showSingleChoiceDialog(
        context: Context,
        title: String,
        items: Array<String>,
        selectedIndex: Int,
        selectedAction: (Int) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setSingleChoiceItems(items, selectedIndex) { dialog, which ->
                selectedAction(which)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    fun showLongToast(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_LONG)
    }
}