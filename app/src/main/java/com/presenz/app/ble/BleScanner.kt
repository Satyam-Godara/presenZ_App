//package com.presenz.app.ble
//
//import android.annotation.SuppressLint
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.le.BluetoothLeScanner
//import android.bluetooth.le.ScanCallback
//import android.bluetooth.le.ScanResult
//import android.bluetooth.le.ScanSettings
//import android.util.Log
//
///**
// * Thin wrapper around BluetoothLeScanner. Scans continuously and hands any
// * presenZ manufacturer-data payload it sees back to the caller, which then
// * decides (via BleProtocol) whether it's a session advert or a student
// * response advert.
// */
//class BleScanner(private val adapter: BluetoothAdapter) {
//
//    private var leScanner: BluetoothLeScanner? = null
//    private var callback: ScanCallback? = null
//    var isScanning = false
//        private set
//
//    @SuppressLint("MissingPermission")
//    fun start(onPayload: (ByteArray, rssi: Int) -> Unit, onError: (String) -> Unit) {
//        if (!adapter.isEnabled) {
//            onError("Bluetooth is off")
//            return
//        }
//        leScanner = adapter.bluetoothLeScanner
//        if (leScanner == null) {
//            onError("BLE scanning unavailable on this device")
//            return
//        }
//
//        stop()
//
//        val settings = ScanSettings.Builder()
//            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//            .build()
//
//        callback = object : ScanCallback() {
//            override fun onScanResult(callbackType: Int, result: ScanResult) {
//                handle(result)
//            }
//
//            override fun onBatchScanResults(results: MutableList<ScanResult>) {
//                results.forEach { handle(it) }
//            }
//
//            override fun onScanFailed(errorCode: Int) {
//                Log.e("BleScanner", "Scan failed: $errorCode")
//                onError("Scan failed (code $errorCode)")
//            }
//
//            private fun handle(result: ScanResult) {
//                val record = result.scanRecord ?: return
//                val payload = record.getManufacturerSpecificData(BleProtocol.MANUFACTURER_ID) ?: return
//                onPayload(payload, result.rssi)
//            }
//        }
//
//        // No ScanFilter applied here for simplicity/broad compatibility -
//        // we just ignore any advert that isn't ours (manufacturer ID null above).
//        leScanner?.startScan(null, settings, callback)
//        isScanning = true
//    }
//
//    @SuppressLint("MissingPermission")
//    fun stop() {
//        try {
//            callback?.let { leScanner?.stopScan(it) }
//        } catch (_: Exception) {
//            // ignore - adapter may already be off
//        }
//        isScanning = false
//        callback = null
//    }
//}

package com.presenz.app.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log

/**
 * Thin wrapper around BluetoothLeScanner.
 *
 * This class only finds presenZ manufacturer advertisements.
 *
 * It does NOT decide:
 *
 *   - which group the advertisement belongs to
 *   - whether the token is valid
 *   - whether a student should respond
 *   - whether attendance should be marked
 *
 * Those decisions belong to the respective Activity/business logic.
 */
class BleScanner(
    private val adapter: BluetoothAdapter
) {

    companion object {

        private const val TAG =
            "BleScanner"
    }


    private var leScanner:
            BluetoothLeScanner? = null

    private var callback:
            ScanCallback? = null


    var isScanning = false
        private set


    @SuppressLint("MissingPermission")
    fun start(
        onPayload: (
            payload: ByteArray,
            rssi: Int
        ) -> Unit,

        onError: (
            message: String
        ) -> Unit
    ) {

        if (!adapter.isEnabled) {

            onError(
                "Bluetooth is off"
            )

            return
        }


        val scanner =
            adapter.bluetoothLeScanner

        if (scanner == null) {

            onError(
                "BLE scanning unavailable on this device"
            )

            return
        }


        /*
         * Stop previous scanner before starting another.
         */
        stop()


        leScanner =
            scanner


        val settings =
            ScanSettings.Builder()
                .setScanMode(
                    ScanSettings.SCAN_MODE_LOW_LATENCY
                )
                .build()


        callback =
            object : ScanCallback() {

                override fun onScanResult(
                    callbackType: Int,
                    result: ScanResult
                ) {

                    handleResult(
                        result,
                        onPayload
                    )
                }


                override fun onBatchScanResults(
                    results: MutableList<ScanResult>
                ) {

                    results.forEach { result ->

                        handleResult(
                            result,
                            onPayload
                        )
                    }
                }


                override fun onScanFailed(
                    errorCode: Int
                ) {

                    isScanning = false

                    Log.e(
                        TAG,
                        "BLE scan failed: $errorCode"
                    )

                    onError(
                        "Scan failed (code $errorCode)"
                    )
                }
            }


        try {

            /*
             * No ScanFilter.
             *
             * We filter using manufacturer ID after receiving
             * the ScanResult. This preserves your existing behavior
             * and broad device compatibility.
             */
            scanner.startScan(
                null,
                settings,
                callback
            )

            isScanning = true

            Log.d(
                TAG,
                "BLE scanning started"
            )

        } catch (e: Exception) {

            isScanning = false

            Log.e(
                TAG,
                "Unable to start BLE scan",
                e
            )

            onError(
                "Unable to start BLE scan: ${e.message}"
            )
        }
    }


    @SuppressLint("MissingPermission")
    private fun handleResult(
        result: ScanResult,
        onPayload: (
            ByteArray,
            Int
        ) -> Unit
    ) {

        val record =
            result.scanRecord
                ?: return


        val payload =
            record.getManufacturerSpecificData(
                BleProtocol.MANUFACTURER_ID
            )
                ?: return


        if (payload.isEmpty()) {
            return
        }


        Log.d(
            TAG,
            "presenZ advert: " +
                    "RSSI=${result.rssi}, " +
                    "payload=${payload.toHexString()}"
        )


        onPayload(
            payload,
            result.rssi
        )
    }


    @SuppressLint("MissingPermission")
    fun stop() {

        try {

            callback?.let {
                leScanner?.stopScan(it)
            }

        } catch (e: Exception) {

            Log.d(
                TAG,
                "Stopping BLE scanner failed",
                e
            )
        }


        isScanning = false
        callback = null
    }


    private fun ByteArray.toHexString(): String {

        return joinToString(
            separator = " "
        ) {
            "%02X".format(it)
        }
    }
}