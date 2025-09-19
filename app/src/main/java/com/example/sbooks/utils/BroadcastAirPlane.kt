package com.example.sbooks.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BroadcastAirPlane: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val isAirplaneModeEnabled = intent?.getBooleanExtra("state", false) ?: return
        if (isAirplaneModeEnabled) {
            Toast.makeText(context, "Chế dộ máy bay được bật", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Chế dộ máy bay bị tắt", Toast.LENGTH_SHORT).show()
        }
            
    }
}