//package com.presenz.app.student
//
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothManager
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.presenz.app.ble.BleAdvertiser
//import com.presenz.app.ble.BleProtocol
//import com.presenz.app.ble.BleScanner
//import com.presenz.app.data.AppDatabase
//import com.presenz.app.data.AttendanceRecord
//import com.presenz.app.databinding.ActivityStudentScanBinding
//import com.presenz.app.network.ApiClient
//import com.presenz.app.util.Prefs
//import kotlinx.coroutines.launch
//
///**
// * Student side of the BLE handshake.
// *
// * 1. Scan for a teacher's session advert (TYPE_SESSION -> token).
// * 2. Resolve that token into a real sessionId via the backend.
// * 3. Advertise our own response (token + rollNo) so the teacher's phone can
// *    pick it up and mark us present.
// * 4. Poll the backend until the teacher has actually marked us present, then
// *    save the record locally and finish.
// */
//class StudentScanActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityStudentScanBinding
//    private var bleScanner: BleScanner? = null
//    private var bleAdvertiser: BleAdvertiser? = null
//    private val handler = Handler(Looper.getMainLooper())
//
//    private var lockedToken: String? = null
//    private var resolvedSessionId: String? = null
//    private var resolvedSubject: String? = null
//    private var pollAttempts = 0
//    private var finished = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityStudentScanBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.btnCancel.setOnClickListener { finish() }
//
//        startScanning()
//    }
//
//    private fun startScanning() {
//        val manager = getSystemService(BluetoothManager::class.java)
//        val adapterBt: BluetoothAdapter? = manager?.adapter
//        if (adapterBt == null || !adapterBt.isEnabled) {
//            binding.tvStatus.text = "Please turn on Bluetooth and try again"
//            return
//        }
//
//        bleScanner = BleScanner(adapterBt)
//        bleScanner?.start(
//            onPayload = { payload, _ ->
//                if (lockedToken != null) return@start // already handling one session
//                val session = BleProtocol.decodeSession(payload) ?: return@start
//                lockedToken = session.token
//                runOnUiThread { onSessionFound(session.token, adapterBt) }
//            },
//            onError = { msg ->
//                runOnUiThread { binding.tvStatus.text = "Scan issue: $msg" }
//            }
//        )
//    }
//
//    private fun onSessionFound(token: String, adapterBt: BluetoothAdapter) {
//        binding.tvStatus.text = "Session found - checking in..."
//        bleScanner?.stop()
//
//        lifecycleScope.launch {
//            try {
//                val resp = ApiClient.service().getSessionByToken(token)
//                if (resp.isSuccessful && resp.body() != null) {
//                    val body = resp.body()!!
//                    resolvedSessionId = body.sessionId
//                    resolvedSubject = body.subject
//                    binding.tvSubject.text = body.subject
//                    startRespondingAndPolling(token, adapterBt)
//                } else {
//                    binding.tvStatus.text = "Session expired, resuming scan..."
//                    resetAndRescan()
//                }
//            } catch (e: Exception) {
//                binding.tvStatus.text = "Network error: ${e.message}"
//                resetAndRescan()
//            }
//        }
//    }
//
//    private fun startRespondingAndPolling(token: String, adapterBt: BluetoothAdapter) {
//        val rollNo = Prefs.studentRollNo ?: return
//
//        bleAdvertiser = BleAdvertiser(adapterBt)
//        bleAdvertiser?.start(BleProtocol.encodeStudentPayload(token, rollNo)) { success, message ->
//            runOnUiThread {
//                binding.tvStatus.text = if (success) "Broadcasting your roll no..." else "Advertise issue: $message"
//            }
//        }
//
//        pollAttempts = 0
//        pollStatus()
//    }
//
//    private fun pollStatus() {
//        val sid = resolvedSessionId ?: return
////        val rollNo = Prefs.studentRollNo ?: return
//
//        lifecycleScope.launch {
//            try {
////                val resp = ApiClient.service().getAttendanceStatus(sid, rollNo)
//                val resp = ApiClient.service().getAttendanceStatus(
//                    ApiClient.studentBearer(),
//                    sid
//                )
//                if (resp.isSuccessful && resp.body()?.present == true) {
//                    onMarkedPresent(sid, resp.body()!!.subject ?: resolvedSubject ?: "Session")
//                    return@launch
//                }
//            } catch (_: Exception) {
//                // ignore transient errors, we'll retry
//            }
//
//            pollAttempts++
//            if (pollAttempts < 30 && !finished) {
//                handler.postDelayed({ pollStatus() }, 2000)
//            } else if (!finished) {
//                binding.tvStatus.text = "Still waiting on the teacher's phone..."
//                handler.postDelayed({ pollStatus() }, 2000)
//            }
//        }
//    }
//
//    private fun onMarkedPresent(sessionId: String, subject: String) {
//        if (finished) return
//        finished = true
//        bleAdvertiser?.stop()
//        bleScanner?.stop()
//
//        binding.progress.visibility = android.view.View.GONE
//        binding.tvStatus.text = "You're marked present ✓"
//        binding.tvSubject.text = subject
//        binding.btnCancel.text = "Done"
//
//        lifecycleScope.launch {
//            AppDatabase.get(this@StudentScanActivity).attendanceDao().insert(
//                AttendanceRecord(
//                    role = "STUDENT",
//                    sessionId = sessionId,
//                    subject = subject,
//                    rollNo = Prefs.studentRollNo,
//                    studentName = Prefs.studentName,
//                    timestampMillis = System.currentTimeMillis()
//                )
//            )
//        }
//    }
//
//    private fun resetAndRescan() {
//        lockedToken = null
//        resolvedSessionId = null
//        bleAdvertiser?.stop()
//        handler.postDelayed({ if (!finished) startScanning() }, 1500)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        finished = true
//        handler.removeCallbacksAndMessages(null)
//        bleScanner?.stop()
//        bleAdvertiser?.stop()
//    }
//}

package com.presenz.app.student

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.presenz.app.ble.BleAdvertiser
import com.presenz.app.ble.BleProtocol
import com.presenz.app.ble.BleScanner
import com.presenz.app.data.AppDatabase
import com.presenz.app.data.AttendanceRecord
import com.presenz.app.databinding.ActivityStudentScanBinding
import com.presenz.app.network.ApiClient
import com.presenz.app.util.Prefs
import kotlinx.coroutines.launch
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
class StudentScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentScanBinding

    private var bleScanner: BleScanner? = null
    private var bleAdvertiser: BleAdvertiser? = null

    private val handler = Handler(Looper.getMainLooper())

    private var lockedToken: String? = null
    private var resolvedSessionId: String? = null
    private var resolvedSubject: String? = null

    private var pollAttempts = 0
    private var finished = false
    private var biometricPrompt: BiometricPrompt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener {
            finish()
        }

        /*
         * Student must already be registered/logged in.
         */
        if (!Prefs.isStudentLoggedIn()) {
            binding.tvStatus.text = "Please login first"
            binding.btnCancel.text = "Back"
            return
        }

        startScanning()
    }

    // ============================================================
    // BLE SCANNING
    // ============================================================

    private fun startScanning() {

        val manager = getSystemService(BluetoothManager::class.java)
        val adapterBt = manager?.adapter

        if (adapterBt == null) {
            binding.tvStatus.text = "Bluetooth is not available"
            return
        }

        if (!adapterBt.isEnabled) {
            binding.tvStatus.text =
                "Please turn on Bluetooth and try again"
            return
        }

        /*
         * Student's registered group.
         */
        val studentGroupCode =
            Prefs.studentGroupName?.trim()?.uppercase()

        if (studentGroupCode.isNullOrBlank()) {
            binding.tvStatus.text =
                "Student group information is missing"

            return
        }

        binding.tvStatus.text =
            "Looking for a session for $studentGroupCode..."

        bleScanner = BleScanner(adapterBt)

        bleScanner?.start(

            onPayload = { payload, _ ->

                /*
                 * Ignore anything that is not a session advert.
                 */
                val sessionAdvert =
                    BleProtocol.decodeSession(payload)
                        ?: return@start

                /*
                 * Already processing a session.
                 */
                if (lockedToken != null) {
                    return@start
                }

                /*
                 * IMPORTANT:
                 *
                 * Teacher's advert now contains:
                 *
                 * token + groupCode
                 *
                 * Only students belonging to the same group
                 * are allowed to continue.
                 */
                if (
                    !sessionAdvert.groupCode.equals(
                        studentGroupCode,
                        ignoreCase = true
                    )
                ) {

                    return@start
                }

                /*
                 * Correct group.
                 */
                lockedToken = sessionAdvert.token

                runOnUiThread {
                    onSessionFound(
                        token = sessionAdvert.token,
                        groupCode = sessionAdvert.groupCode,
                        adapterBt = adapterBt
                    )
                }
            },

            onError = { message ->

                runOnUiThread {
                    binding.tvStatus.text =
                        "Scan issue: $message"
                }
            }
        )
    }


    // ============================================================
    // SESSION FOUND
    // ============================================================

    private fun onSessionFound(
        token: String,
        groupCode: String,
        adapterBt: BluetoothAdapter
    ) {

        binding.tvStatus.text =
            "Session found · checking in..."

        bleScanner?.stop()

        lifecycleScope.launch {

            try {

                /*
                 * Resolve the short BLE token through backend.
                 */
                val response =
                    ApiClient.service()
                        .getSessionByToken(token)

                if (
                    response.isSuccessful &&
                    response.body() != null
                ) {

                    val body = response.body()!!

                    /*
                     * Backend should return the session's group.
                     *
                     * This gives us a second safety check.
                     */
                    if (
                        !body.groupCode.equals(
                            Prefs.studentGroupName,
                            ignoreCase = true
                        )
                    ) {

                        binding.tvStatus.text =
                            "This session is for another group"

                        resetAndRescan()

                        return@launch
                    }

                    resolvedSessionId = body.sessionId
                    resolvedSubject = body.subject

                    binding.tvSubject.text =
                        body.subject

//                    startRespondingAndPolling(
//                        token = token,
//                        adapterBt = adapterBt
//                    )
                    // replace for biometric validation before marking attendance
                    authenticateBeforeMarkingAttendance(token, adapterBt)

                } else {

                    binding.tvStatus.text =
                        "Session expired, searching again..."

                    resetAndRescan()
                }

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Network error: ${e.message}"

                resetAndRescan()
            }
        }
    }



    // ============================================================
// BIOMETRIC GATE
// ============================================================

    private fun authenticateBeforeMarkingAttendance(
        token: String,
        adapterBt: BluetoothAdapter
    ) {
        val biometricManager = BiometricManager.from(this)

        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            binding.tvStatus.text = when (canAuthenticate) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    "This device has no biometric hardware"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    "No biometrics enrolled — set up fingerprint/face unlock first"
                else ->
                    "Biometric authentication is unavailable"
            }
            resetAndRescan()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    startRespondingAndPolling(token, adapterBt)
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    binding.tvStatus.text = "Authentication cancelled: $errString"
                    resetAndRescan()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Fingerprint/face not recognized — prompt stays open,
                    // system shows its own retry UI. No action needed here.
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirm your identity")
            .setSubtitle("Verify it's really you before marking attendance")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt?.authenticate(promptInfo)
    }
    // ============================================================
    // STUDENT RESPONSE
    // ============================================================

    private fun startRespondingAndPolling(
        token: String,
        adapterBt: BluetoothAdapter
    ) {

        val rollNo =
            Prefs.studentRollNo

        val groupCode =
            Prefs.studentGroupName

        if (rollNo.isNullOrBlank()) {

            binding.tvStatus.text =
                "Student roll number is missing"

            return
        }

        if (groupCode.isNullOrBlank()) {

            binding.tvStatus.text =
                "Student group is missing"

            return
        }

        /*
         * Student now responds with:
         *
         * TOKEN + GROUP_CODE + ROLL_NO
         *
         * The teacher will additionally verify the group.
         */
        bleAdvertiser =
            BleAdvertiser(adapterBt)

        bleAdvertiser?.start(
            BleProtocol.encodeStudentPayload(
                token = token,
                groupCode = groupCode,
                rollNo = rollNo
            )
        ) { success, message ->

            runOnUiThread {

                binding.tvStatus.text =
                    if (success) {
                        "Broadcasting attendance response..."
                    } else {
                        "Advertise issue: $message"
                    }
            }
        }

        pollAttempts = 0
        pollStatus()
    }


    // ============================================================
    // WAIT FOR ATTENDANCE
    // ============================================================

    private fun pollStatus() {

        val sessionId =
            resolvedSessionId
                ?: return

        val rollNo =
            Prefs.studentRollNo
                ?: return
        val token = Prefs.studentToken?:return
        lifecycleScope.launch {

            try {

                val response =
                    ApiClient.service()
                        .getAttendanceStatus(
                            sessionId = sessionId,
                            bearer = "Bearer $token",
                        )

                if (
                    response.isSuccessful &&
                    response.body()?.present == true
                ) {

                    val subject =
                        response.body()?.subject
                            ?: resolvedSubject
                            ?: "Session"

                    onMarkedPresent(
                        sessionId = sessionId,
                        subject = subject
                    )

                    return@launch
                }

            } catch (_: Exception) {

                // Ignore temporary network errors.
            }

            pollAttempts++

            if (
                pollAttempts < 30 &&
                !finished
            ) {

                handler.postDelayed(
                    {
                        pollStatus()
                    },
                    2000
                )

            } else if (!finished) {

                binding.tvStatus.text =
                    "Still waiting on the teacher..."

                handler.postDelayed(
                    {
                        pollStatus()
                    },
                    2000
                )
            }
        }
    }


    // ============================================================
    // ATTENDANCE CONFIRMED
    // ============================================================

    private fun onMarkedPresent(
        sessionId: String,
        subject: String
    ) {

        if (finished) {
            return
        }

        finished = true

        bleAdvertiser?.stop()
        bleScanner?.stop()

        binding.progress.visibility =
            View.GONE

        binding.tvStatus.text =
            "You're marked present ✓"

        binding.tvSubject.text =
            subject

        binding.btnCancel.text =
            "Done"

        lifecycleScope.launch {

            AppDatabase
                .get(this@StudentScanActivity)
                .attendanceDao()
                .insert(

                    AttendanceRecord(
                        role = "STUDENT",
                        sessionId = sessionId,
                        subject = subject,
                        rollNo = Prefs.studentRollNo,
                        studentName = Prefs.studentName,
                        timestampMillis =
                            System.currentTimeMillis()
                    )
                )
        }
    }


    // ============================================================
    // RESET SCANNER
    // ============================================================

    private fun resetAndRescan() {

        lockedToken = null
        resolvedSessionId = null
        resolvedSubject = null

        bleAdvertiser?.stop()

        handler.postDelayed(
            {
                if (!finished) {
                    startScanning()
                }
            },
            1500
        )
    }


    // ============================================================
    // CLEANUP
    // ============================================================

    override fun onDestroy() {

        super.onDestroy()

        finished = true

        handler.removeCallbacksAndMessages(null)

        bleScanner?.stop()
        bleAdvertiser?.stop()
        biometricPrompt?.cancelAuthentication()
    }
}