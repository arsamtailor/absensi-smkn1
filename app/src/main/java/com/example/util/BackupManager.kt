package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    suspend fun exportFullBackupJson(db: AppDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        // Classes
        val classes = db.classGroupDao().getAllClasses().first()
        val classesArray = JSONArray()
        classes.forEach { cg ->
            val obj = JSONObject().apply {
                put("id", cg.id)
                put("name", cg.name)
                put("academicYear", cg.academicYear)
                put("major", cg.major)
                put("description", cg.description)
            }
            classesArray.put(obj)
        }
        root.put("classes", classesArray)

        // Students
        val students = db.studentDao().getAllStudents().first()
        val studentsArray = JSONArray()
        students.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("classId", s.classId)
                put("nisn", s.nisn)
                put("name", s.name)
                put("gender", s.gender)
                put("phone", s.phone)
            }
            studentsArray.put(obj)
        }
        root.put("students", studentsArray)

        // Sessions
        val sessions = db.attendanceDao().getAllSessions().first()
        val sessionsArray = JSONArray()
        sessions.forEach { sess ->
            val obj = JSONObject().apply {
                put("id", sess.id)
                put("classId", sess.classId)
                put("date", sess.date)
                put("subject", sess.subject)
                put("notes", sess.notes)
                put("timestamp", sess.timestamp)
            }
            sessionsArray.put(obj)
        }
        root.put("sessions", sessionsArray)

        // Records
        val records = db.attendanceDao().getAllRecords().first()
        val recordsArray = JSONArray()
        records.forEach { rec ->
            val obj = JSONObject().apply {
                put("id", rec.id)
                put("sessionId", rec.sessionId)
                put("studentId", rec.studentId)
                put("status", rec.status)
                put("note", rec.note)
            }
            recordsArray.put(obj)
        }
        root.put("records", recordsArray)

        // Schedules
        val schedules = db.scheduleDao().getAllSchedules().first()
        val schedulesArray = JSONArray()
        schedules.forEach { sch ->
            val obj = JSONObject().apply {
                put("id", sch.id)
                put("dayOfWeek", sch.dayOfWeek)
                put("className", sch.className)
                put("subject", sch.subject)
                put("startTime", sch.startTime)
                put("endTime", sch.endTime)
                put("room", sch.room)
                put("isAlarmEnabled", sch.isAlarmEnabled)
            }
            schedulesArray.put(obj)
        }
        root.put("schedules", schedulesArray)

        root.put("exportedAt", System.currentTimeMillis())
        root.put("appVersion", "1.0.0")

        root.toString(2)
    }

    suspend fun importFullBackupJson(db: AppDatabase, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            if (root.has("classes")) {
                val classesArray = root.getJSONArray("classes")
                for (i in 0 until classesArray.length()) {
                    val obj = classesArray.getJSONObject(i)
                    db.classGroupDao().insertClass(
                        ClassGroup(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            academicYear = obj.getString("academicYear"),
                            major = obj.optString("major", "AKL"),
                            description = obj.optString("description", "")
                        )
                    )
                }
            }

            if (root.has("students")) {
                val studentsArray = root.getJSONArray("students")
                for (i in 0 until studentsArray.length()) {
                    val obj = studentsArray.getJSONObject(i)
                    db.studentDao().insertStudent(
                        Student(
                            id = obj.optLong("id", 0L),
                            classId = obj.getLong("classId"),
                            nisn = obj.optString("nisn", ""),
                            name = obj.getString("name"),
                            gender = obj.optString("gender", "L"),
                            phone = obj.optString("phone", "")
                        )
                    )
                }
            }

            if (root.has("sessions")) {
                val sessionsArray = root.getJSONArray("sessions")
                for (i in 0 until sessionsArray.length()) {
                    val obj = sessionsArray.getJSONObject(i)
                    db.attendanceDao().insertSession(
                        AttendanceSession(
                            id = obj.optLong("id", 0L),
                            classId = obj.getLong("classId"),
                            date = obj.getString("date"),
                            subject = obj.optString("subject", "Absensi Harian"),
                            notes = obj.optString("notes", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("records")) {
                val recordsArray = root.getJSONArray("records")
                val recordsList = mutableListOf<AttendanceRecord>()
                for (i in 0 until recordsArray.length()) {
                    val obj = recordsArray.getJSONObject(i)
                    recordsList.add(
                        AttendanceRecord(
                            id = obj.optLong("id", 0L),
                            sessionId = obj.getLong("sessionId"),
                            studentId = obj.getLong("studentId"),
                            status = obj.getString("status"),
                            note = obj.optString("note", "")
                        )
                    )
                }
                db.attendanceDao().insertRecords(recordsList)
            }

            if (root.has("schedules")) {
                val schedulesArray = root.getJSONArray("schedules")
                for (i in 0 until schedulesArray.length()) {
                    val obj = schedulesArray.getJSONObject(i)
                    db.scheduleDao().insertSchedule(
                        TeachingSchedule(
                            id = obj.optLong("id", 0L),
                            dayOfWeek = obj.getString("dayOfWeek"),
                            className = obj.getString("className"),
                            subject = obj.getString("subject"),
                            startTime = obj.getString("startTime"),
                            endTime = obj.getString("endTime"),
                            room = obj.optString("room", "R. Kelas"),
                            isAlarmEnabled = obj.optBoolean("isAlarmEnabled", true)
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareBackupText(context: Context, backupJson: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, backupJson)
            putExtra(Intent.EXTRA_TITLE, "Backup_Absensi_SMKN1Cirinten.json")
            type = "application/json"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Cadangkan / Kirim File Backup Data"))
    }
}
