package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.ClassGroupDao
import com.example.data.dao.ScheduleDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.ClassGroup
import com.example.data.model.Student
import com.example.data.model.TeachingSchedule

@Database(
    entities = [
        ClassGroup::class,
        Student::class,
        AttendanceSession::class,
        AttendanceRecord::class,
        TeachingSchedule::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classGroupDao(): ClassGroupDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "absensi_siswa_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
