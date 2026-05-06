package com.example.swift.utils

import android.util.Log
import com.example.swift.models.*

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

    private const val SENDER_EMAIL = "pradipta02032009@gmail.com"
    private const val SENDER_PASSWORD = "gprhailylhgvlvgs"
    private const val BRAND_RED = "#E31E24"
    private const val BRAND_DARK = "#1A1A2E"

    // ── SHARED ─────────────────────────────────────────────────────────────────

    private fun buildSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.writetimeout", "10000")
            put("mail.smtp.ssl.protocols", "TLSv1.2")
        }
        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() =
                PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
        })
    }

    private suspend fun sendHtml(to: String, subject: String, html: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val cleanTo = to.trim()
                Log.d("EmailSender", "Sending email to: '$cleanTo'")
                val session = buildSession()
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL, "Swift Express"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(cleanTo))
                    setSubject(subject, "utf-8")
                    setContent(html, "text/html; charset=utf-8")
                    saveChanges()
                }
                val transport = session.getTransport("smtp")
                transport.connect("smtp.gmail.com", 587, SENDER_EMAIL, SENDER_PASSWORD)
                transport.sendMessage(message, message.allRecipients)
                transport.close()
                true
            } catch (e: Exception) {
                Log.e("EmailSender", "Failed to send email to $to: ${e.message}", e)
                e.printStackTrace()
                false
            }
        }

    /** Reusable HTML wrapper — logo + colored header bar */
    private fun emailWrapper(
        headerColor: String,
        headerIcon: String,
        headerTitle: String,
        headerSubtitle: String,
        body: String
    ): String = """
        <!DOCTYPE html>
        <html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
        <style>
          body{margin:0;padding:0;background:#F4F4F4;font-family:Arial,sans-serif;}
          .container{max-width:600px;margin:32px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.10);}
          .header{background:$headerColor;padding:32px 28px 24px;color:#fff;}
          .header h1{margin:0 0 4px;font-size:22px;font-weight:800;letter-spacing:.5px;}
          .header p{margin:0;font-size:13px;opacity:.85;}
          .body{padding:28px 28px 8px;}
          .greeting{font-size:14px;color:#555;margin-bottom:8px;}
          .headline{font-size:20px;font-weight:700;color:#1A1A2E;margin:0 0 20px;}
          hr{border:none;border-top:1px dashed #ddd;margin:20px 0;}
          .detail-box{background:#F8F9FB;border-radius:12px;padding:20px;margin:16px 0;}
          .detail-row{display:flex;justify-content:space-between;padding:6px 0;font-size:14px;}
          .detail-label{color:#888;}
          .detail-value{font-weight:600;color:#1A1A2E;text-align:right;}
          .total-row{border-top:2px solid $headerColor;margin-top:12px;padding-top:12px;}
          .total-row .detail-label{font-weight:700;font-size:15px;color:#1A1A2E;}
          .total-row .detail-value{font-size:18px;color:$headerColor;}
          .badge{display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:700;letter-spacing:.5px;}
          .footer{background:#F8F9FB;padding:20px 28px;text-align:center;font-size:12px;color:#aaa;line-height:1.8;}
          .footer a{color:$headerColor;text-decoration:none;}
        </style></head>
        <body>
        <div class="container">
          <div class="header">
            <h1>$headerIcon Swift Express</h1>
            <p>$headerSubtitle</p>
          </div>
          <div class="body">$body</div>
          <div class="footer">
            This email was generated automatically, please do not reply.<br>
            For questions, contact us at <a href="mailto:support@swiftexpress.id">support@swiftexpress.id</a><br><br>
            <strong>Swift Express</strong> — Kereta Cepat Indonesia
          </div>
        </div>
        </body></html>
    """.trimIndent()

    // ── 1. PAYMENT CONFIRMATION ────────────────────────────────────────────────

    suspend fun sendTicketEmail(booking: BookingData, formattedTotalPrice: String): Boolean =
        withContext(Dispatchers.IO) {
            val passengerRows = booking.passengers.joinToString("") { p ->
                """
                <div class="detail-row">
                  <span class="detail-label">${p.name}</span>
                  <span class="detail-value">${p.passengerType.displayName} · ${p.identityNumber}</span>
                </div>
                """.trimIndent()
            }

            val body = """
                <p class="greeting">Hi, <strong>${booking.passengerName}</strong></p>
                <p class="headline">Ticket Confirmation Success</p>
                <div class="detail-box">
                  <div class="detail-row"><span class="detail-label">Booking Code</span><span class="detail-value" style="color:#1A6EDB;font-size:16px;">${booking.bookingCode}</span></div>
                  <div class="detail-row"><span class="detail-label">Route</span><span class="detail-value">${booking.origin.displayName} → ${booking.destination.displayName}</span></div>
                  <div class="detail-row"><span class="detail-label">Date</span><span class="detail-value">${booking.departureDate}</span></div>
                  <div class="detail-row"><span class="detail-label">Class</span><span class="detail-value">${booking.coachClass.displayName}</span></div>
                </div>
                <p style="font-size:13px;color:#888;margin-top:16px;">Your ticket is now active. Please check in at the station using your booking code.</p>
            """.trimIndent()

            Log.d("EmailSender", "Attempting to send Ticket email to: ${booking.passengerEmail}")
            val result = sendHtml(
                to = booking.passengerEmail,
                subject = "Ticket Updated — ${booking.bookingCode}",
                html = emailWrapper("#1A6EDB", "R", "Swift Express", "Ticket Confirmation", body)
            )
            Log.d("EmailSender", "Ticket email result for ${booking.bookingCode}: $result")
            result
        }

    // ── 2. CANCELLATION ───────────────────────────────────────────────────────

    suspend fun sendCancellationEmail(
        booking: BookingData,
        formattedTotalPrice: String,
        cancellationReason: String = "Cancelled by user"
    ): Boolean = withContext(Dispatchers.IO) {
        val body = """
            <p class="greeting">Hi, <strong>${booking.passengerName}</strong></p>
            <p class="headline">Your booking has been cancelled</p>
            <div class="detail-box">
              <div class="detail-row"><span class="detail-label">Booking Code</span><span class="detail-value" style="color:#E8A020;font-size:16px;">${booking.bookingCode}</span></div>
              <div class="detail-row"><span class="detail-label">Route</span><span class="detail-value">${booking.origin.displayName} → ${booking.destination.displayName}</span></div>
              <div class="detail-row"><span class="detail-label">Date</span><span class="detail-value">${booking.departureDate}</span></div>
              <div class="detail-row"><span class="detail-label">Departure</span><span class="detail-value">${booking.departureTime}</span></div>
              <div class="detail-row"><span class="detail-label">Class</span><span class="detail-value">${booking.coachClass.displayName}</span></div>
              <div class="detail-row"><span class="detail-label">Total Paid</span><span class="detail-value">$formattedTotalPrice</span></div>
            </div>
            <div class="detail-box" style="background:#FFF8E8;border-left:4px solid #E8A020;">
              <p style="margin:0;font-size:13px;color:#9A6A00;"><strong>Reason:</strong> $cancellationReason</p>
            </div>
            <p style="font-size:13px;color:#888;margin-top:16px;">Your refund (if applicable) will be processed within <strong>3–7 business days</strong>. You will receive a separate refund confirmation email.</p>
        """.trimIndent()

        val result = sendHtml(
            to = booking.passengerEmail,
            subject = "Booking Cancelled — ${booking.bookingCode}",
            html = emailWrapper("#E8A020", "C", "Swift Express", "Cancellation Notice", body)
        )
        Log.d("EmailSender", "Cancellation email to ${booking.passengerEmail} result: $result")
        result
    }

    // ── 3. RESCHEDULE ─────────────────────────────────────────────────────────

    suspend fun sendRescheduleEmail(
        booking: BookingData,
        oldDate: String,
        oldDeparture: String,
        oldArrival: String,
        newDate: String,
        newDeparture: String,
        newArrival: String,
        rescheduleReason: String = "Rescheduled by user"
    ): Boolean = withContext(Dispatchers.IO) {
        val body = """
            <p class="greeting">Hi, <strong>${booking.passengerName}</strong></p>
            <p class="headline">Ticket Reschedule Confirmation</p>
            <div class="detail-box">
              <div class="detail-row"><span class="detail-label">Booking Code</span><span class="detail-value" style="color:#1A6EDB;font-size:16px;">${booking.bookingCode}</span></div>
              <div class="detail-row"><span class="detail-label">Route</span><span class="detail-value">${booking.origin.displayName} → ${booking.destination.displayName}</span></div>
              <div class="detail-row"><span class="detail-label">Class</span><span class="detail-value">${booking.coachClass.displayName}</span></div>
            </div>
            <p style="font-size:13px;font-weight:600;color:#555;margin-bottom:8px;">SCHEDULE CHANGES</p>
            <div class="detail-box">
              <div class="detail-row" style="background:#FFF0F0;border-radius:8px;padding:8px;">
                <span class="detail-label" style="color:#E31E24;">Old Schedule</span>
                <span class="detail-value" style="color:#E31E24;">$oldDate · $oldDeparture – $oldArrival</span>
              </div>
              <div class="detail-row" style="background:#F0FFF4;border-radius:8px;padding:8px;margin-top:8px;">
                <span class="detail-label" style="color:#16A34A;">New Schedule</span>
                <span class="detail-value" style="color:#16A34A;">$newDate · $newDeparture – $newArrival</span>
              </div>
            </div>
            <div class="detail-box" style="background:#EFF6FF;border-left:4px solid #1A6EDB;">
              <p style="margin:0;font-size:13px;color:#1A4A8A;"><strong>Reason:</strong> $rescheduleReason</p>
            </div>
            <p style="font-size:13px;color:#888;margin-top:16px;">Please check-in at the station using your original Booking Code: ${booking.bookingCode}, no later than 30 minutes before the new departure time.</p>
        """.trimIndent()

        Log.d("EmailSender", "Attempting to send Reschedule email to: ${booking.passengerEmail}")
        val result = sendHtml(
            to = booking.passengerEmail,
            subject = "Ticket Rescheduled — ${booking.bookingCode}",
            html = emailWrapper("#1A6EDB", "R", "Swift Express", "Reschedule Confirmation", body)
        )
        Log.d("EmailSender", "Reschedule email result for ${booking.bookingCode}: $result")
        result
    }

    // ── 4. REFUND ─────────────────────────────────────────────────────────────

    suspend fun sendRefundEmail(
        booking: BookingData,
        formattedRefundAmount: String,
        refundMethod: String = "Original Payment Method",
        estimatedDays: Int = 5,
        refundReason: String = "Booking cancellation"
    ): Boolean = withContext(Dispatchers.IO) {
        val body = """
            <p class="greeting">Hi, <strong>${booking.passengerName}</strong></p>
            <p class="headline">Your refund is being processed</p>
            <div class="detail-box">
              <div class="detail-row"><span class="detail-label">Booking Code</span><span class="detail-value" style="color:#16A34A;font-size:16px;">${booking.bookingCode}</span></div>
              <div class="detail-row"><span class="detail-label">Route</span><span class="detail-value">${booking.origin.displayName} → ${booking.destination.displayName}</span></div>
              <div class="detail-row"><span class="detail-label">Departure Date</span><span class="detail-value">${booking.departureDate}</span></div>
              <div class="detail-row"><span class="detail-label">Class</span><span class="detail-value">${booking.coachClass.displayName}</span></div>
            </div>
            <p style="font-size:13px;font-weight:600;color:#555;margin-bottom:8px;">REFUND DETAILS</p>
            <div class="detail-box">
              <div class="detail-row"><span class="detail-label">Reason</span><span class="detail-value">$refundReason</span></div>
              <div class="detail-row"><span class="detail-label">Refund Method</span><span class="detail-value">$refundMethod</span></div>
              <div class="detail-row"><span class="detail-label">Estimated Arrival</span><span class="detail-value">$estimatedDays business days</span></div>
              <div class="detail-row total-row">
                <span class="detail-label" style="color:#16A34A;">REFUND AMOUNT</span>
                <span class="detail-value" style="color:#16A34A;">$formattedRefundAmount</span>
              </div>
            </div>
            <div class="detail-box" style="background:#F0FFF4;border-left:4px solid #16A34A;">
              <p style="margin:0;font-size:13px;color:#14532D;">Your refund has been submitted and will arrive in your account within <strong>$estimatedDays business days</strong>. If you have not received it after $estimatedDays days, please contact our support team.</p>
            </div>
            <p style="font-size:13px;color:#888;margin-top:16px;">Thank you for using Swift Express. We hope to serve you again on your next journey. 🚄</p>
        """.trimIndent()

        val result = sendHtml(
            to = booking.passengerEmail,
            subject = "Refund Processed — ${booking.bookingCode}",
            html = emailWrapper("#16A34A", "R", "Swift Express", "Refund Notification", body)
        )
        Log.d("EmailSender", "Refund email result: $result")
        result
    }
    // ── 5. OTP / PASSWORD RESET ───────────────────────────────────────────────

    suspend fun sendOtpEmail(toEmail: String, otp: String, userName: String = "User"): Boolean =
        withContext(Dispatchers.IO) {
            val body = """
                <p class="greeting">Hi, <strong>$userName</strong></p>
                <p class="headline">Password Reset Request</p>
                <p style="font-size:14px;color:#555;margin-bottom:20px;">
                  We received a request to reset the password for your Swift Express account.
                  Use the OTP code below to continue. This code is valid for <strong>10 minutes</strong>.
                </p>
                <div style="text-align:center;margin:28px 0;">
                  <div style="display:inline-block;background:#F8F9FB;border:2px dashed $BRAND_RED;border-radius:16px;padding:24px 40px;">
                    <p style="margin:0;font-size:13px;color:#888;letter-spacing:1px;text-transform:uppercase;">Your OTP Code</p>
                    <p style="margin:8px 0 0;font-size:42px;font-weight:900;letter-spacing:12px;color:$BRAND_RED;font-family:monospace;">$otp</p>
                  </div>
                </div>
                <div class="detail-box" style="background:#FFF8E8;border-left:4px solid #E8A020;">
                  <p style="margin:0;font-size:13px;color:#9A6A00;">
                    ⚠️ <strong>Do not share this code with anyone.</strong> Swift Express will never ask for your OTP.
                    If you did not request this, please ignore this email — your account remains secure.
                  </p>
                </div>
                <p style="font-size:13px;color:#888;margin-top:16px;">
                  This OTP will expire in <strong>10 minutes</strong>.
                </p>
            """.trimIndent()

            Log.d("EmailSender", "Sending OTP email to: $toEmail")
            val result = sendHtml(
                to = toEmail,
                subject = "[$otp] Swift Express — Password Reset OTP",
                html = emailWrapper(BRAND_RED, "🔐", "Swift Express", "Password Reset", body)
            )
            Log.d("EmailSender", "OTP email result: $result")
            result
        }
}
