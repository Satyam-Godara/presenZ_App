//package com.presenz.app.teacher
//
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothManager
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.presenz.app.ble.BleAdvertiser
//import com.presenz.app.ble.BleProtocol
//import com.presenz.app.ble.BleScanner
//import com.presenz.app.data.AppDatabase
//import com.presenz.app.data.AttendanceRecord
//import com.presenz.app.databinding.ActivitySessionBinding
//import com.presenz.app.network.ApiClient
//import com.presenz.app.network.MarkAttendanceRequest
//import com.presenz.app.network.StartSessionRequest
//import kotlinx.coroutines.launch
//
///**
// * The heart of the app.
// *
// * Flow:
// *  1. Ask the backend to start a session -> get sessionId + short token.
// *  2. Continuously BLE-advertise that token so nearby student phones see it.
// *  3. Continuously BLE-scan for student "response" adverts (token + rollNo).
// *  4. For each new, valid response, call the backend to mark attendance and
// *     show it in the live list.
// *  5. On "End Session", stop BLE work, close the session on the backend, and
// *     save a summary row into local history (Room).
// */
//class SessionActivity : AppCompatActivity() {
//
//    companion object {
//
//        const val EXTRA_SUBJECT = "extra_subject"
//        const val EXTRA_GROUP_ID = "extra_group_id"
//        const val EXTRA_GROUP_NAME = "extra_group_name"
//    }
//    private lateinit var binding: ActivitySessionBinding
//    private lateinit var adapter: PresentStudentAdapter
//
//    private var bleAdvertiser: BleAdvertiser? = null
//    private var bleScanner: BleScanner? = null
//
//    private var sessionId: String? = null
//    private var sessionToken: String? = null
//    private var groupId: String? = null
//    private var groupName: String = ""
//    private var subject: String = "Session"
//    private val markedRollNos = mutableSetOf<String>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySessionBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
////        subject = intent.getStringExtra(EXTRA_SUBJECT) ?: "Session"
//        subject = intent.getStringExtra(EXTRA_SUBJECT) ?: "Session"
//
//        groupId = intent.getStringExtra(EXTRA_GROUP_ID)
//
//        groupName = intent.getStringExtra(EXTRA_GROUP_NAME) ?: ""
//        if (groupId.isNullOrBlank()) {
//            finish()
//            return
//        }
//        binding.tvSubject.text = "$subject · $groupName"
//        binding.tvSubject.text = subject
//
//        adapter = PresentStudentAdapter()
//        binding.rvPresent.layoutManager = LinearLayoutManager(this)
//        binding.rvPresent.adapter = adapter
//
//        binding.btnEndSession.setOnClickListener { endSession() }
//
//        startSessionOnBackend()
//    }
//
//    private fun startSessionOnBackend() {
//        binding.tvStatus.text = "Creating session..."
//        lifecycleScope.launch {
//            try {
////                val resp = ApiClient.service().startSession(ApiClient.teacherBearer(), StartSessionRequest(subject, groupId)
//                val selectedGroupId = groupId ?: return@launch
//
//                val resp = ApiClient.service().startSession(
//                    ApiClient.teacherBearer(),
//                    StartSessionRequest(
//                        subject = subject,
//                        groupId = selectedGroupId
//                    )
//                )
//                if (resp.isSuccessful && resp.body() != null) {
//                    val body = resp.body()!!
//                    sessionId = body.sessionId
//                    sessionToken = body.token
//                    groupId = body.groupId
//                    groupName = body.groupName
//
//                    binding.tvSubject.text =
//                        "${body.subject} · ${body.groupName}"
//
//                    binding.tvStatus.text =
//                        "Broadcasting session · ${body.groupName}"
//                    binding.tvStatus.text = "Broadcasting token ${body.token} · keep this screen open"
//                    startBle()
//                } else {
//                    binding.tvStatus.text = "Failed to start session: ${resp.code()}"
//                }
//            } catch (e: Exception) {
//                binding.tvStatus.text = "Network error: ${e.message}"
//            }
//        }
//    }
//
//    private fun startBle() {
//        val manager = getSystemService(BluetoothManager::class.java)
//        val adapterBt: BluetoothAdapter? = manager?.adapter
//        if (adapterBt == null || !adapterBt.isEnabled) {
//            binding.tvStatus.text = "Please enable Bluetooth and restart the session"
//            return
//        }
//
//        val token = sessionToken ?: return
//
//        // 1. Advertise our session token continuously.
//        bleAdvertiser = BleAdvertiser(adapterBt)
//        bleAdvertiser?.start(BleProtocol.encodeSessionPayload(token, groupCode = groupId)) { success, message ->
//            runOnUiThread {
//                if (!success) binding.tvStatus.text = "Advertising issue: $message"
//            }
//        }
//
//        // 2. Scan for student response adverts and verify+mark them.
//        bleScanner = BleScanner(adapterBt)
//        bleScanner?.start(
//            onPayload = { payload, _ ->
//                val advert = BleProtocol.decodeStudent(payload) ?: return@start
//                if (!advert.token.equals(token, ignoreCase = true)) return@start // stale/foreign token
//                if (markedRollNos.contains(advert.rollNo)) return@start // already handled
//                markedRollNos.add(advert.rollNo)
//                markStudentPresent(advert.rollNo)
//            },
//            onError = { msg ->
//                runOnUiThread { binding.tvStatus.text = "Scan issue: $msg" }
//            }
//        )
//    }
//
//    private fun markStudentPresent(rollNo: String) {
//        val sid = sessionId ?: return
//        val token = sessionToken ?: return
//        lifecycleScope.launch {
//            try {
//                val resp = ApiClient.service().markAttendance(
//                    MarkAttendanceRequest(sessionId = sid, token = token, rollNo = rollNo, name = null)
//                )
//                if (resp.isSuccessful) {
//                    runOnUiThread {
//                        if (adapter.addIfNew(rollNo)) {
//                            binding.tvCount.text = adapter.count().toString()
//                        }
//                    }
//                } else {
//                    markedRollNos.remove(rollNo) // allow retry on transient failure
//                }
//            } catch (e: Exception) {
//                markedRollNos.remove(rollNo)
//            }
//        }
//    }
//
//    private fun endSession() {
//        bleAdvertiser?.stop()
//        bleScanner?.stop()
//        binding.tvStatus.text = "Ending session..."
//        binding.btnEndSession.isEnabled = false
//
//        val sid = sessionId
//        if (sid == null) {
//            finish()
//            return
//        }
//
//        lifecycleScope.launch {
//            try {
//                ApiClient.service().endSession(ApiClient.teacherBearer(), sid)
//            } catch (_: Exception) {
//                // still save locally even if the network call fails
//            }
//
//            AppDatabase.get(this@SessionActivity).attendanceDao().insert(
//                AttendanceRecord(
//                    role = "TEACHER",
//                    sessionId = sid,
//                    subject = subject,
//                    presentCount = adapter.count(),
//                    timestampMillis = System.currentTimeMillis()
//                )
//            )
//            finish()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        bleAdvertiser?.stop()
//        bleScanner?.stop()
//    }
//}



package com.presenz.app.teacher

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.presenz.app.ble.BleAdvertiser
import com.presenz.app.ble.BleProtocol
import com.presenz.app.ble.BleScanner
import com.presenz.app.data.AppDatabase
import com.presenz.app.data.AttendanceRecord
import com.presenz.app.databinding.ActivitySessionBinding
import com.presenz.app.network.ApiClient
import com.presenz.app.network.MarkAttendanceRequest
import com.presenz.app.network.StartSessionRequest
import com.presenz.app.util.BluetoothEnabler
import kotlinx.coroutines.launch

/**
 * Teacher's active attendance session.
 *
 * Flow:
 *
 * 1. Receive the selected subject + group from TeacherHomeActivity.
 * 2. Create a session on the backend.
 * 3. Receive sessionId + token + group information.
 * 4. Advertise:
 *
 *      [SESSION TYPE][TOKEN][GROUP CODE]
 *
 * 5. Scan for student responses:
 *
 *      [STUDENT TYPE][TOKEN][ROLL NO]
 *
 * 6. Verify the token.
 * 7. Ask the backend to mark the student present.
 * 8. Display the student in the live attendance list.
 * 9. Stop BLE and close the session when the teacher ends it.
 */
class SessionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SUBJECT = "extra_subject"
        const val EXTRA_GROUP_ID = "extra_group_id"
        const val EXTRA_GROUP_NAME = "extra_group_name"
    }

    private lateinit var binding: ActivitySessionBinding
    private lateinit var presentAdapter: PresentStudentAdapter

    private var bleAdvertiser: BleAdvertiser? = null
    private var bleScanner: BleScanner? = null

    private var sessionId: String? = null
    private var sessionToken: String? = null

    /**
     * This is the short code that is broadcast over BLE.
     *
     * Do NOT use a MongoDB ObjectId here.
     */
    private var groupId: String? = null
    private var groupCode: String? = null

    private var groupName: String = ""
    private var subject: String = "Session"

    /**
     * Prevents sending the same roll number to the backend repeatedly
     * while the student keeps advertising.
     */
    private val markedRollNos = mutableSetOf<String>()

    private val bluetoothEnabler = BluetoothEnabler(this) { enabled ->
        if (enabled) {
            startSessionOnBackend()
        } else {
            showStatus("Bluetooth is required to start a session")
            binding.btnEndSession.isEnabled = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        readIntentData()

        setupAttendanceList()

        binding.btnEndSession.setOnClickListener {
            endSession()
        }
        // Gate the whole flow on Bluetooth being on.
        bluetoothEnabler.ensureEnabledThen {
            startSessionOnBackend()
        }
    }

    // ---------------------------------------------------------------------
    // INITIALIZATION
    // ---------------------------------------------------------------------

//    private fun readIntentData() {
//
//        subject =
//            intent.getStringExtra(EXTRA_SUBJECT)
//                ?.trim()
//                ?.ifEmpty { "Session" }
//                ?: "Session"
//
//        /*
//         * At the moment your TeacherHomeActivity passes EXTRA_GROUP_ID.
//         *
//         * This should eventually become EXTRA_GROUP_CODE once the backend
//         * exposes a short BLE-safe group code.
//         */
//
//        groupId =
//            intent.getStringExtra(EXTRA_GROUP_ID)
//                ?.trim()
//
//        groupName =
//            intent.getStringExtra(EXTRA_GROUP_NAME)
//                ?.trim()
//                ?: ""
//
//        groupCode =
//            groupName
//                .trim()
//                .uppercase()
//
//        if (groupId.isNullOrBlank()) {
//            finish()
//            return
//        }
//
//        updateSessionTitle()
//    }
//private fun readIntentData() {
//
//    Log.e("SESSION_DEBUG", "Intent = $intent")
//    Log.e("SESSION_DEBUG", "Extras = ${intent.extras}")
//
//    subject =
//        intent.getStringExtra(EXTRA_SUBJECT)
//            ?.trim()
//            ?.ifEmpty { "Session" }
//            ?: "Session"
//
//    groupId =
//        intent.getStringExtra(EXTRA_GROUP_ID)
//            ?.trim()
//
//    groupName =
//        intent.getStringExtra(EXTRA_GROUP_NAME)
//            ?.trim()
//            ?: ""
//
//    Log.e("SESSION_DEBUG", "subject = $subject")
//    Log.e("SESSION_DEBUG", "groupId = $groupId")
//    Log.e("SESSION_DEBUG", "groupName = $groupName")
//
//    groupCode =
//        groupName
//            .trim()
//            .uppercase()
//
//    if (groupId.isNullOrBlank()) {
//        Log.e("SESSION_DEBUG", "❌ GROUP ID IS NULL")
//        finish()
//        return
//    }
//
//    updateSessionTitle()
//}
//
    private fun setupAttendanceList() {

        presentAdapter = PresentStudentAdapter()

        binding.rvPresent.layoutManager =
            LinearLayoutManager(this)

        binding.rvPresent.adapter =
            presentAdapter

        binding.tvCount.text = "0"
    }
//
    private fun updateSessionTitle() {

        binding.tvSubject.text =
            if (groupName.isNotBlank()) {
                "$subject · $groupName"
            } else {
                subject
            }
    }
//
//    // ---------------------------------------------------------------------
//    // BACKEND SESSION
//    // ---------------------------------------------------------------------
//
//    private fun startSessionOnBackend() {
//
//        val selectedGroupCode =
//            groupCode;
//        Log.e("groupId", "$selectedGroupCode");
//
//        if (selectedGroupCode.isNullOrBlank()) {
//            showStatus("Group information is missing")
//            return
//        }
//
//        showStatus("Creating session...")
//        binding.btnEndSession.isEnabled = true
//
//        lifecycleScope.launch {
//
//            try {
//
//                val response =
//                    ApiClient.service().startSession(
//                        ApiClient.teacherBearer(),
//                        StartSessionRequest(
//                            subject = subject,
//                            groupId = selectedGroupCode
//                        )
//                    )
//
//                if (
//                    response.isSuccessful &&
//                    response.body() != null
//                ) {
//
//                    val body =
//                        response.body()!!
//
//                    sessionId =
//                        body.sessionId
//
//                    sessionToken =
//                        body.token
//
//                    /*
//                     * Backend is authoritative.
//                     *
//                     * If your StartSessionResponse later contains
//                     * groupCode, use that here instead of groupId.
//                     */
//                    groupCode =
//                        body.groupId
//                            .trim()
//                            .lowercase()
//                    Log.e("BODy","$body");
//                    Log.e("groupCdeide","$groupCode");
//                    groupName =
//                        body.groupName
//
//                    subject =
//                        body.subject
//
//                    updateSessionTitle()
//
//                    showStatus(
//                        "Starting attendance for $groupName..."
//                    )
//
//                    startBle()
//
//                } else {
//
//                    showStatus(
//                        "Failed to start session: ${response.code()}"
//                    )
//                }
//
//            } catch (e: Exception) {
//
//                showStatus(
//                    "Network error: ${e.message ?: "Unknown error"}"
//                )
//            }
//        }
//    }
private fun readIntentData() {
    subject = intent.getStringExtra(EXTRA_SUBJECT)?.trim()?.ifEmpty { "Session" } ?: "Session"
    groupId = intent.getStringExtra(EXTRA_GROUP_ID)?.trim()
    groupName = intent.getStringExtra(EXTRA_GROUP_NAME)?.trim() ?: ""

    if (groupId.isNullOrBlank()) {
        Log.e("SESSION_DEBUG", "❌ GROUP ID IS NULL")
        finish()
        return
    }

    // Short BLE-safe code, derived from the name — never the Mongo id.
    groupCode = groupName.uppercase()

    updateSessionTitle()
}

    private fun startSessionOnBackend() {
        val currentGroupId = groupId
        if (currentGroupId.isNullOrBlank()) {
            showStatus("Group information is missing")
            return
        }

        showStatus("Creating session...")
        binding.btnEndSession.isEnabled = true

        lifecycleScope.launch {
            try {
                val response = ApiClient.service().startSession(
                    ApiClient.teacherBearer(),
                    StartSessionRequest(
                        subject = subject,
                        groupId = currentGroupId   // ✅ real id, not groupCode
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    sessionId = body.sessionId
                    sessionToken = body.token
                    groupId = body.groupId          // backend is authoritative for the real id
                    groupName = body.groupName
                    subject = body.subject

                    // Rebuild the short BLE code from the (possibly updated) name.
                    // If your backend later returns a dedicated body.groupCode, use that instead.
                    groupCode = groupName.uppercase()

                    updateSessionTitle()
                    showStatus("Starting attendance for $groupName...")
                    startBle()
                } else {
                    showStatus("Failed to start session: ${response.code()}")
                }
            } catch (e: Exception) {
                showStatus("Network error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    // ---------------------------------------------------------------------
    // BLE
    // ---------------------------------------------------------------------

    private fun startBle() {

        val manager = getSystemService(BluetoothManager::class.java)

        val bluetoothAdapter = manager?.adapter

        if ( bluetoothAdapter == null ) {
            showStatus("Bluetooth is not available on this device")
            return
        }

        if (
            !bluetoothAdapter.isEnabled
        ) {
            showStatus(
                "Please enable Bluetooth and restart the session"
            )
            return
        }

        val token =
            sessionToken

        if (token.isNullOrBlank()) {
            showStatus(
                "Session token is missing"
            )
            return
        }

        val selectedGroupCode =
            groupCode

        if (selectedGroupCode.isNullOrBlank()) {
            showStatus(
                "Session group code is missing"
            )
            return
        }

        /*
         * -------------------------------------------------------------
         * 1. ADVERTISE TEACHER SESSION
         * -------------------------------------------------------------
         *
         * Packet:
         *
         * [TYPE_SESSION][TOKEN][GROUP_CODE]
         */
        startSessionAdvertisement(
            bluetoothAdapter,
            token,
            selectedGroupCode
        )

        /*
         * -------------------------------------------------------------
         * 2. SCAN FOR STUDENTS
         * -------------------------------------------------------------
         *
         * Packet:
         *
         * [TYPE_STUDENT_RESPONSE][TOKEN][ROLL_NO]
         */
        startStudentScanner(
            bluetoothAdapter,
            token
        )
    }

    private fun startSessionAdvertisement(
        bluetoothAdapter: BluetoothAdapter,
        token: String,
        groupCode: String
    ) {

        val payload =
            BleProtocol.encodeSessionPayload(
                token = token,
                groupCode = groupCode
            )

        bleAdvertiser =
            BleAdvertiser(bluetoothAdapter)

        bleAdvertiser?.start(
            payload = payload
        ) { success, message ->

            runOnUiThread {

                if (success) {

                    showStatus(
                        "Broadcasting session · $groupName"
                    )

                } else {

                    showStatus(
                        "Advertising issue: $message"
                    )
                }
            }
        }
    }

    private fun startStudentScanner(
        bluetoothAdapter: BluetoothAdapter,
        token: String
    ) {

        bleScanner =
            BleScanner(bluetoothAdapter)

        bleScanner?.start(

            onPayload = { payload, _ ->

                /*
                 * Decode the student packet.
                 */
                val advert =
                    BleProtocol.decodeStudent(payload)
                        ?: return@start

                /*
                 * Ignore responses belonging to another session.
                 */
                if (
                    !advert.token.equals(
                        token,
                        ignoreCase = true
                    )
                ) {
                    return@start
                }

                /*
                 * Ignore students already processed.
                 */
                if (
                    markedRollNos.contains(
                        advert.rollNo
                    )
                ) {
                    return@start
                }

                /*
                 * Temporarily lock this roll number.
                 *
                 * If backend marking fails, markStudentPresent()
                 * removes it so that the student can retry.
                 */
                markedRollNos.add(
                    advert.rollNo
                )

                markStudentPresent(
                    advert.rollNo
                )
            },

            onError = { message ->

                runOnUiThread {

                    showStatus(
                        "Scan issue: $message"
                    )
                }
            }
        )
    }

    // ---------------------------------------------------------------------
    // ATTENDANCE
    // ---------------------------------------------------------------------

    private fun markStudentPresent(
        rollNo: String
    ) {

        val currentSessionId =
            sessionId
                ?: return

        val currentToken =
            sessionToken
                ?: return

        lifecycleScope.launch {

            try {

                val response =
                    ApiClient.service().markAttendance(
                        MarkAttendanceRequest(
                            sessionId = currentSessionId,
                            token = currentToken,
                            rollNo = rollNo,
                            name = null
                        )
                    )

                if (response.isSuccessful) {

                    runOnUiThread {

                        if (
                            presentAdapter.addIfNew(
                                rollNo
                            )
                        ) {

                            binding.tvCount.text =
                                presentAdapter
                                    .count()
                                    .toString()
                        }
                    }

                } else {

                    /*
                     * Backend rejected the request.
                     *
                     * Allow this student to try again.
                     */
                    markedRollNos.remove(
                        rollNo
                    )
                }

            } catch (_: Exception) {

                /*
                 * Network failure.
                 *
                 * Allow retry.
                 */
                markedRollNos.remove(
                    rollNo
                )
            }
        }
    }

    // ---------------------------------------------------------------------
    // END SESSION
    // ---------------------------------------------------------------------

    private fun endSession() {

        /*
         * Stop BLE immediately.
         */
        stopBle()

        showStatus(
            "Ending session..."
        )

        binding.btnEndSession.isEnabled =
            false

        val currentSessionId =
            sessionId

        if (currentSessionId.isNullOrBlank()) {
            finish()
            return
        }

        lifecycleScope.launch {

            try {

                ApiClient.service()
                    .endSession(
                        ApiClient.teacherBearer(),
                        currentSessionId
                    )

            } catch (_: Exception) {

                /*
                 * Even if the backend request fails,
                 * save the local history record.
                 */
            }

            saveLocalHistory(
                currentSessionId
            )

            finish()
        }
    }

    private suspend fun saveLocalHistory(
        currentSessionId: String
    ) {

        AppDatabase
            .get(this@SessionActivity)
            .attendanceDao()
            .insert(
                AttendanceRecord(
                    role = "TEACHER",
                    sessionId = currentSessionId,
                    subject = subject,
                    presentCount = presentAdapter.count(),
                    timestampMillis =
                        System.currentTimeMillis()
                )
            )
    }

    // ---------------------------------------------------------------------
    // CLEANUP
    // ---------------------------------------------------------------------

    private fun stopBle() {

        bleAdvertiser?.stop()
        bleScanner?.stop()

        bleAdvertiser = null
        bleScanner = null
    }

    override fun onDestroy() {

        stopBle()

        super.onDestroy()
    }

    // ---------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------

    private fun showStatus(
        message: String
    ) {

        binding.tvStatus.text =
            message
    }
}