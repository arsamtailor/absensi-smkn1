package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfGuideGenerator {
    fun generateAndOpenManualPdf(context: Context, teacherName: String, schoolName: String) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1B365D")
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val sectionPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            typeface = Typeface.DEFAULT
        }
        val boldBodyPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // PAGE 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 40f

        // Draw header background bar
        val bgPaint = Paint().apply { color = Color.parseColor("#F1F5F9") }
        canvas.drawRect(20f, 20f, pageWidth - 20f, 95f, bgPaint)

        canvas.drawText("BUKU PANDUAN PENGGUNAAN APLIKASI", 35f, 45f, titlePaint)
        canvas.drawText("Presensi & Absensi Siswa Digital - $schoolName", 35f, 65f, subTitlePaint)
        canvas.drawText("Dokumen Panduan Operasional Guru & Wali Kelas", 35f, 82f, bodyPaint)

        y = 105f

        // Draw Banner Illustration
        try {
            val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.img_guide_banner)
            if (bitmap != null) {
                val destRect = android.graphics.RectF(35f, y, pageWidth - 35f, y + 80f)
                canvas.drawBitmap(bitmap, null, destRect, null)
                y += 90f
            }
        } catch (e: Exception) {
            // fallback if bitmap reading fails
        }

        canvas.drawText("1. PETUNJUK INSTALASI APK DI HP ANDROID", 35f, y, sectionPaint)
        y += 18f

        val step1 = listOf(
            "• Langkah 1: Simpan file 'app-release.apk' ke penyimpanan HP Android Anda.",
            "• Langkah 2: Buka file APK melalui Manajer File (File Manager) di HP.",
            "• Langkah 3: Jika muncul peringatan 'Instal dari Sumber Tidak Dikenal',",
            "  pilih 'Izinkan' / 'Allow' pada setelan Chrome atau File Manager.",
            "• Langkah 4: Tekan 'Instal' dan tunggu hingga proses instalasi selesai.",
            "• Langkah 5: Buka aplikasi 'Absensi Siswa' dari layar utama HP Anda."
        )
        step1.forEach { line ->
            canvas.drawText(line, 40f, y, bodyPaint)
            y += 16f
        }

        y += 15f
        canvas.drawText("2. PANDUAN ALUR PENGGUNAAN HARIAN (STEP BY STEP)", 35f, y, sectionPaint)
        y += 20f

        val step2 = listOf(
            "LANGKAH A: ATUR MASTER DATA UTAMA (Cukup 1 Kali di Awal)",
            "1. Buka menu 'Pengaturan & Master Data' di pojok kanan atas layar utama.",
            "2. Tab 'Profil Guru': Isi Nama Guru, NIP, Nama Sekolah, dan Kode PIN Pengaman (4 Digit).",
            "3. Tab 'Master Kelas': Tambahkan daftar Kelas yang diajar (Contoh: X AKL 1, XI MPLB 2).",
            "4. Tab 'Master Mata Pelajaran': Pilih dari Dropdown Mapel Cepat (AKL/MPLB) atau ketik manual.",
            "5. Tab 'Master Siswa': Tambah siswa per kelas atau gunakan fitur 'Impor Masal Siswa'.",
            "",
            "LANGKAH B: CARA PENGINPUTAN PRESENSI HARIAN",
            "1. Di Halaman Utama, tekan tombol '+ TAMBAH PRESENSI' atau icon Absen di kartu kelas.",
            "2. Pilih Kelas, Tanggal, Sesi/Jam Ke, dan Mata Pelajaran yang diajarkan.",
            "3. Pilih Status Kehadiran untuk tiap siswa: [H] Hadir, [I] Izin, [S] Sakit, [A] Alpa.",
            "4. Berikan Catatan / Jurnal KBM jika ada, lalu tekan 'SIMPAN PRESENSI'.",
            "",
            "LANGKAH C: MELIHAT & MENGEKSPOR LAPORAN",
            "1. Buka tab 'Laporan' di menu navigasi bawah.",
            "2. Pilih Kelas dan Periode Laporan (Mingguan, Bulanan, Semester).",
            "3. Tekan icon 'FileDownload' untuk Mengunduh File Excel/CSV Rekap Presensi Lengkap.",
            "4. Tekan icon 'Share' untuk membagikan ringkasan laporan langsung ke WhatsApp Orang Tua."
        )
        step2.forEach { line ->
            if (line.startsWith("LANGKAH")) {
                canvas.drawText(line, 40f, y, boldBodyPaint)
            } else {
                canvas.drawText(line, 40f, y, bodyPaint)
            }
            y += 16f
        }

        // Footer page 1
        canvas.drawText("Halaman 1 dari 2", pageWidth / 2f - 30f, pageHeight - 30f, bodyPaint)
        pdfDocument.finishPage(page)

        // PAGE 2
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        y = 40f

        canvas.drawText("3. FITUR-FITUR UNGGULAN & PENJELASAN TEKNIS", 35f, y, sectionPaint)
        y += 20f

        val features = listOf(
            "A. BYPASS PIN & KEAMANAN FLEKSIBEL",
            "   Aplikasi dilengkapi sistem PIN 4 Digit untuk melindungi Master Data.",
            "   Jika guru lupa PIN, tersedia tombol 'Lupa PIN / Bypass Pengaman' yang dapat membuka kunci",
            "   master data secara langsung tanpa resiko terkunci selamanya.",
            "",
            "B. IMPOR MASAL SISWA (PASTE TEXT / CSV)",
            "   Guru dapat memasukkan 30-40 nama siswa dalam hitungan detik.",
            "   Cukup salin (copy) daftar nama siswa dari Excel atau WhatsApp, lalu tempel (paste) ke kotak dialog.",
            "   Format fleksibel: NAMA, NISN, L/P, NO HP.",
            "",
            "C. DROPDOWN MAPEL STANDAR JURUSAN (AKL & MPLB)",
            "   Pengaturan mata pelajaran dibuat sangat praktis menggunakan Dropdown Menu.",
            "   Tersedia preset cepat untuk Jurusan Akuntansi (AKL), Perkantoran (MPLB), serta Mapel Umum.",
            "",
            "D. ASISTEN ANALISIS BERBASIS AI (INTELLIGENT PRESENCE ANALYSIS)",
            "   Fitur AI di tab Laporan menganalisis secara otomatis tingkat kehadiran kelas,",
            "   mendeteksi siswa rawan alpa (>=3 Alpa), serta memberikan saran tindakan konkrit untuk Wali Kelas.",
            "",
            "E. PENGARSIPAN SEMESTER & TAHUN AJARAN BARU",
            "   Saat pergantian semester/tahun ajaran, gunakan menu 'Arsip & Periode' di beranda utama.",
            "   Data semester lama tersimpan rapi di database arsip dan dapat diakses di Laporan kapan saja.",
            "",
            "F. DETEKSI ALPA & SURAT PERINGATAN (SP)",
            "   Sistem mendeteksi siswa dengan Alpa >= 3x secara otomatis.",
            "   Guru/Wali Kelas dapat mencetak atau mengirimkan Surat Panggilan Orang Tua (SP) via WhatsApp."
        )
        features.forEach { line ->
            if (line.startsWith("A.") || line.startsWith("B.") || line.startsWith("C.") || line.startsWith("D.") || line.startsWith("E.") || line.startsWith("F.")) {
                canvas.drawText(line, 40f, y, boldBodyPaint)
            } else {
                canvas.drawText(line, 40f, y, bodyPaint)
            }
            y += 16f
        }

        y += 25f
        canvas.drawText("4. DUKUNGAN BANTUAN & INFORMASI", 35f, y, sectionPaint)
        y += 20f
        canvas.drawText("Aplikasi dikembangkan khusus untuk mendukung efisiensi Administrasi Guru & Wali Kelas.", 40f, y, bodyPaint)
        y += 16f
        canvas.drawText("Jika terjadi kendala teknis, gunakan fitur Backup Database di menu Pengaturan.", 40f, y, bodyPaint)

        y += 40f
        canvas.drawRect(35f, y, pageWidth - 35f, y + 60f, bgPaint)
        canvas.drawText("Dokumen ini dihasilkan secara otomatis oleh Aplikasi Absensi Siswa Mobile.", 50f, y + 25f, boldBodyPaint)
        canvas.drawText("Dibuat untuk: $teacherName | $schoolName", 50f, y + 42f, bodyPaint)

        // Footer page 2
        canvas.drawText("Halaman 2 dari 2", pageWidth / 2f - 30f, pageHeight - 30f, bodyPaint)
        pdfDocument.finishPage(page)

        try {
            val pdfFile = File(context.cacheDir, "Buku_Panduan_Absensi_Siswa.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Buku Panduan Aplikasi Absensi Siswa")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Buka / Simpan Buku Panduan PDF")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
