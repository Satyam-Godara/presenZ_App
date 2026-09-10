//package com.presenz.app.ble
//
//import android.annotation.SuppressLint
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.le.AdvertiseCallback
//import android.bluetooth.le.AdvertiseData
//import android.bluetooth.le.AdvertiseSettings
//import android.bluetooth.le.BluetoothLeAdvertiser
//import android.util.Log
//
///**
// * Thin wrapper around BluetoothLeAdvertiser.
// * Broadcasts a small payload as "manufacturer specific data" - no GATT
// * connection needed, any nearby phone that's scanning can read it.
// */
//class BleAdvertiser(private val adapter: BluetoothAdapter) {
//
//    private var leAdvertiser: BluetoothLeAdvertiser? = null
//    private var callback: AdvertiseCallback? = null
//    var isAdvertising = false
//        private set
//
//    @SuppressLint("MissingPermission")
//    fun start(payload: ByteArray, onResult: (success: Boolean, message: String) -> Unit) {
//        val result = payload.decodeToString()
//        Log.e("payload",result)
//        if (!adapter.isEnabled) {
//            onResult(false, "Bluetooth is off")
//            return
//        }
//        if (adapter.bluetoothLeAdvertiser == null) {
//            onResult(false, "This device does not support BLE advertising")
//            return
//        }
//
//        stop() // ensure a clean slate
//
//        leAdvertiser = adapter.bluetoothLeAdvertiser
//        val settings = AdvertiseSettings.Builder()
//            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
//            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
//            .setConnectable(false)
//            .build()
//
//        val data = AdvertiseData.Builder()
//            .setIncludeDeviceName(false)
//            .setIncludeTxPowerLevel(false)
//            .addManufacturerData(BleProtocol.MANUFACTURER_ID, payload)
//            .build()
//        Log.e("data","$data")
//        callback = object : AdvertiseCallback() {
//            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
//                isAdvertising = true
//                onResult(true, "Advertising started")
//            }
//
//            override fun onStartFailure(errorCode: Int) {
//                isAdvertising = false
//                Log.e("BleAdvertiser", "Advertise failed: $errorCode")
//                onResult(false, "Advertise failed (code $errorCode)")
//            }
//        }
//
//        leAdvertiser?.startAdvertising(settings, data, callback)
//    }
//
//    @SuppressLint("MissingPermission")
//    fun stop() {
//        try {
//            callback?.let { leAdvertiser?.stopAdvertising(it) }
//        } catch (_: Exception) {
//            // adapter may already be off - safe to ignore for MVP
//        }
//        isAdvertising = false
//        callback = null
//    }
//}


package com.presenz.app.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.util.Log

/**
 * Thin wrapper around BluetoothLeAdvertiser.
 *
 * The payload is placed into manufacturer-specific advertising data.
 *
 * This class does not know whether the payload is:
 *
 *   SESSION
 *
 * or:
 *
 *   STUDENT_RESPONSE
 *
 * That responsibility belongs to BleProtocol.
 */
class BleAdvertiser(
    private val adapter: BluetoothAdapter
) {

    companion object {

        private const val TAG =
            "BleAdvertiser"

        /*
         * Legacy BLE advertising data has a limited payload.
         *
         * We keep an additional safety limit here.
         */
        private const val MAX_PAYLOAD_SIZE = 24
    }


    private var leAdvertiser:
            BluetoothLeAdvertiser? = null

    private var callback:
            AdvertiseCallback? = null


    var isAdvertising = false
        private set


    @SuppressLint("MissingPermission")
    fun start(
        payload: ByteArray,
        onResult: (
            success: Boolean,
            message: String
        ) -> Unit
    ) {

        if (!adapter.isEnabled) {

            onResult(
                false,
                "Bluetooth is off"
            )

            return
        }


        if (
            payload.isEmpty()
        ) {

            onResult(
                false,
                "BLE payload is empty"
            )

            return
        }


        if (
            payload.size > MAX_PAYLOAD_SIZE
        ) {

            onResult(
                false,
                "BLE payload is too large: ${payload.size} bytes"
            )

            return
        }


        val advertiser =
            adapter.bluetoothLeAdvertiser

        if (advertiser == null) {

            onResult(
                false,
                "This device does not support BLE advertising"
            )

            return
        }


        /*
         * Stop any previous advertisement.
         */
        stop()


        leAdvertiser =
            advertiser


        val settings =
            AdvertiseSettings.Builder()
                .setAdvertiseMode(
                    AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
                )
                .setTxPowerLevel(
                    AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
                )
                .setConnectable(false)
                .build()


        val data =
            try {

                AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .setIncludeTxPowerLevel(false)
                    .addManufacturerData(
                        BleProtocol.MANUFACTURER_ID,
                        payload
                    )
                    .build()

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Unable to build advertisement",
                    e
                )

                onResult(
                    false,
                    "Unable to build BLE advertisement"
                )

                return
            }


        Log.d(
            TAG,
            "Advertising payload: ${payload.toHexString()}"
        )


        callback =
            object : AdvertiseCallback() {

                override fun onStartSuccess(
                    settingsInEffect: AdvertiseSettings?
                ) {

                    isAdvertising = true

                    Log.d(
                        TAG,
                        "BLE advertising started"
                    )

                    onResult(
                        true,
                        "Advertising started"
                    )
                }


                override fun onStartFailure(
                    errorCode: Int
                ) {

                    isAdvertising = false

                    Log.e(
                        TAG,
                        "BLE advertising failed: $errorCode"
                    )

                    onResult(
                        false,
                        "Advertise failed (code $errorCode)"
                    )
                }
            }


        leAdvertiser?.startAdvertising(
            settings,
            data,
            callback
        )
    }


    @SuppressLint("MissingPermission")
    fun stop() {

        try {

            callback?.let {
                leAdvertiser?.stopAdvertising(it)
            }

        } catch (e: Exception) {

            Log.d(
                TAG,
                "Stopping BLE advertisement failed",
                e
            )
        }


        isAdvertising = false
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