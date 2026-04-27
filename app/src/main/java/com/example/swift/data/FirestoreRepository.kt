package com.example.swift.data

import com.example.swift.models.Coach
import com.example.swift.models.Seat
import com.example.swift.models.Station
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // e.g. "HALIM-TEGALLUAR-01Apr2026-06:00"
    fun getScheduleId(origin: Station, destination: Station, date: String, time: String): String {
        val formattedDate = date.replace(" ", "")
        val formattedTime = time.replace(":", "")
        return "${origin.name}-${destination.name}-$formattedDate-$formattedTime"
    }

    suspend fun checkAndSeedData(scheduleId: String, onComplete: () -> Unit) {
        val scheduleRef = db.collection("schedules").document(scheduleId)

        try {
            val snapshot = scheduleRef.get().await()
            if (!snapshot.exists()) {
                // Buat dummy data
                scheduleRef.set(mapOf("createdAt" to System.currentTimeMillis()))
                
                // Buat gerbong 1 sampai 8
                for (i in 1..8) {
                    val coachId = String.format("%02d", i)
                    val coachRef = scheduleRef.collection("coaches").document(coachId)
                    
                    val seats = mutableListOf<Seat>()
                    for (row in 1..13) {
                        listOf("A", "B", "C", "D", "F").forEach { letter ->
                            val seatId = "$row$letter"
                            seats.add(Seat(id = seatId, isAvailable = true))
                        }
                    }
                    
                    // Simpan objek List langsung tidak terlalu baik untuk realtime query
                    // Tapi karena struktur sederhana, simpan sebagai array object:
                    coachRef.set(mapOf(
                        "id" to coachId,
                        "seats" to seats
                    )).await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        onComplete()
    }

    // Menggunakan callback snapshot agar realtime perubahan kursi terlihat
    fun getCoachesFlow(scheduleId: String, onUpdate: (List<Coach>) -> Unit) {
        db.collection("schedules").document(scheduleId).collection("coaches")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                val coaches = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: return@mapNotNull null
                    val seatsList = doc.get("seats") as? List<Map<String, Any>> ?: emptyList()
                    val seats = seatsList.map { seatMap ->
                        Seat(
                            id = seatMap["id"] as? String ?: "",
                            isAvailable = seatMap["isAvailable"] as? Boolean ?: seatMap["available"] as? Boolean ?: false
                        )
                    }
                    Coach(id = id, seats = seats)
                }
                onUpdate(coaches.sortedBy { it.id })
            }
    }

    // Hanya menggunakan list coach yang dipilih untuk dipesan
    suspend fun bookSeatsTransaction(scheduleId: String, coachId: String, selectedSeatIds: List<String>): Boolean {
        val coachRef = db.collection("schedules").document(scheduleId)
            .collection("coaches").document(coachId)

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(coachRef)
                val seatsList = snapshot.get("seats") as? List<Map<String, Any>> ?: emptyList()
                
                // Validasi dulu apakah semua kursi yang dipilih masih available
                val isAllSelectedAvailable = seatsList.filter { (it["id"] as? String) in selectedSeatIds }
                    .all { (it["isAvailable"] as? Boolean ?: it["available"] as? Boolean) == true }
                    
                if (!isAllSelectedAvailable) {
                    throw Exception("Seat already booked")
                }

                // Update isAvailable = false
                val updatedSeatsList = seatsList.map { seatMap ->
                    val seatId = seatMap["id"] as? String ?: ""
                    if (seatId in selectedSeatIds) {
                        mapOf("id" to seatId, "isAvailable" to false, "available" to false)
                    } else {
                        seatMap
                    }
                }
                
                transaction.update(coachRef, "seats", updatedSeatsList)
                true
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
