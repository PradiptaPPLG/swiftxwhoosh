package com.example.swift.utils

import com.example.swift.models.BookingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    // === PERHATIAN: GANTI DENGAN EMAIL DAN APP PASSWORD ANDA ===
    private const val SENDER_EMAIL = "pradipta02032009@gmail.com" 
    private const val SENDER_PASSWORD = "gprhailylhgvlvgs" 
    // ==========================================================

    suspend fun sendTicketEmail(booking: BookingData, formattedTotalPrice: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
               val props = Properties()
                props.put("mail.smtp.auth", "true")
                props.put("mail.smtp.starttls.enable", "true")
                props.put("mail.smtp.host", "smtp.gmail.com")
                props.put("mail.smtp.port", "587")

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                    }
                })

                val html = """
                    <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
                        <div style="background:#DB1A1A;padding:20px;color:white;border-radius:12px 12px 0 0;">
                            <h1 style="margin:0;">🚄 Swift - E-Tiket</h1>
                            <p style="margin:4px 0 0;">Kereta Cepat Indonesia</p>
                        </div>
                        <div style="background:#fff;padding:20px;border:1px solid #eee;">
                            <h2 style="color:#DB1A1A;">Kode Booking: ${booking.bookingCode}</h2>
                            <table style="width:100%;border-collapse:collapse;">
                                <tr><td style="padding:8px 0;color:#666;">Penumpang</td><td style="padding:8px 0;font-weight:bold;">${booking.passengers.joinToString(", ") { "${it.name} (${it.passengerType.displayName})" }}</td></tr>
                                <tr><td style="padding:8px 0;color:#666;">Rute</td><td style="padding:8px 0;font-weight:bold;">${booking.origin.displayName} → ${booking.destination.displayName}</td></tr>
                                <tr><td style="padding:8px 0;color:#666;">Tanggal</td><td style="padding:8px 0;font-weight:bold;">${booking.departureDate}</td></tr>
                                <tr><td style="padding:8px 0;color:#666;">Jam</td><td style="padding:8px 0;font-weight:bold;">${booking.departureTime} - ${booking.arrivalTime}</td></tr>
                                <tr><td style="padding:8px 0;color:#666;">Durasi</td><td style="padding:8px 0;font-weight:bold;">${booking.travelDuration} menit</td></tr>
                                <tr><td style="padding:8px 0;color:#666;">Gerbong</td><td style="padding:8px 0;font-weight:bold;">${booking.coachClass.displayName}</td></tr>
                                <tr><td style="padding:8px 0;color:#666;">Jumlah Tiket</td><td style="padding:8px 0;font-weight:bold;">${booking.ticketCount}</td></tr>
                                <tr style="border-top:2px solid #DB1A1A;"><td style="padding:12px 0;font-weight:bold;font-size:16px;">TOTAL BAYAR</td><td style="padding:12px 0;font-weight:bold;font-size:18px;color:#DB1A1A;">$formattedTotalPrice</td></tr>
                            </table>
                        </div>
                    </div>
                """.trimIndent()

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(booking.passengerEmail))
                    subject = "E-Tiket Swift - Kode Booking: ${booking.bookingCode}"
                    setContent(html, "text/html; charset=utf-8")
                }

                Transport.send(message)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
