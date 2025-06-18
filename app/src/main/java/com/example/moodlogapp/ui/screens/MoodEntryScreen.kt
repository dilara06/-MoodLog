package com.example.moodlogapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodEntryScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    // Firebase
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = auth.currentUser?.uid

    // UI state
    var selectedMood by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf<String?>(null) }
    var hasEntryToday by remember { mutableStateOf(false) }

    // Emoji → (etiket, skor)
    val moodMap = mapOf(
        "😃" to ("Mutlu" to 1),
        "😐" to ("Kararsız" to 2),
        "😠" to ("Öfkeli" to 3),
        "😰" to ("Kaygılı" to 4),
        "😢" to ("Üzgün" to 5)
    )

    // Rol & günlük kayıt kontrolü
    LaunchedEffect(uid) {
        uid?.let { id ->
            // Rolü çek
            db.collection("users").document(id).get().addOnSuccessListener { snap ->
                userRole = snap.getString("role") ?: "student"
            }

            // Bugün kayıt var mı?
            db.collection("mood_entries")
                .whereEqualTo("userId", id)
                .get()
                .addOnSuccessListener { qs ->
                    val cal = Calendar.getInstance()
                    val year = cal.get(Calendar.YEAR)
                    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                    for (doc in qs) {
                        val ts = doc.getTimestamp("timestamp") ?: continue
                        val entryCal = Calendar.getInstance().apply { time = ts.toDate() }
                        if (entryCal.get(Calendar.YEAR) == year && entryCal.get(Calendar.DAY_OF_YEAR) == dayOfYear) {
                            hasEntryToday = true
                            break
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bugün Nasılsın?") },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("mood_entry") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Çıkış"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Bugünkü ruh halini seç", fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))

            // Emoji butonları
            moodMap.forEach { (emoji, pair) ->
                val (label, _) = pair
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedMood = emoji
                            errorMsg = ""
                        },
                        enabled = !hasEntryToday,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMood == emoji)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.width(64.dp)
                    ) {
                        Text(emoji, fontSize = 24.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(label, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it; errorMsg = "" },
                label = { Text("Kısa bir not eklemek ister misin?") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !hasEntryToday
            )

            Spacer(Modifier.height(24.dp))

            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }
            if (hasEntryToday) {
                Text("Bugün zaten bir ruh hali kaydetmişsiniz!")
                Spacer(Modifier.height(12.dp))
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (hasEntryToday) return@Button
                        if (auth.currentUser == null) {
                            errorMsg = "Önce giriş yapmalısınız."
                            return@Button
                        }
                        if (selectedMood.isBlank()) {
                            errorMsg = "Lütfen bir ruh hâli seç."
                            return@Button
                        }

                        isLoading = true
                        errorMsg = ""

                        db.collection("users").document(uid!!).get()
                            .addOnSuccessListener { snap ->
                                val firstName = snap.getString("firstName").orEmpty()
                                val lastName = snap.getString("lastName").orEmpty()
                                val username = snap.getString("username").orEmpty()
                                val email = snap.getString("email") ?: auth.currentUser!!.email.orEmpty()
                                val score = moodMap[selectedMood]?.second ?: 0

                                val entry = hashMapOf(
                                    "moodEmoji" to selectedMood,
                                    "moodLabel" to moodMap[selectedMood]?.first,
                                    "moodScore" to score,
                                    "note" to note.ifBlank { null },
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "username" to username,
                                    "email" to email,
                                    "timestamp" to FieldValue.serverTimestamp(),
                                    "userId" to uid
                                )

                                db.collection("mood_entries")
                                    .add(entry)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        navController.navigate("entry_success") {
                                            popUpTo("mood_entry") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMsg = e.message ?: "Kaydetme hatası."
                                    }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMsg = e.message ?: "Profil okunamadı."
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !hasEntryToday
                ) {
                    Text("Kaydet")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (userRole == "teacher") {
                Button(
                    onClick = { navController.navigate("records") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kayıtları Görüntüle")
                }
            }
        }
    }
}
