package com.presenz.app.util

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class BluetoothEnabler(
    private val activity: AppCompatActivity,
    private val onResult: (enabled: Boolean) -> Unit
) {
    // Holds whatever action was waiting on the dialog result.
    private var pendingAction: (() -> Unit)? = null

    private val launcher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val enabled = result.resultCode == Activity.RESULT_OK

            onResult(enabled)

            if (enabled) {
                pendingAction?.invoke()
            }
            pendingAction = null
        }

    fun isBluetoothEnabled(): Boolean {
        val manager = activity.getSystemService(BluetoothManager::class.java)
        return manager?.adapter?.isEnabled == true
    }

    fun requestEnable() {
        launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    fun ensureEnabledThen(block: () -> Unit) {
        if (isBluetoothEnabled()) {
            block()
        } else {
            pendingAction = block
            requestEnable()
        }
    }
}