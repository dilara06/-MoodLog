package com.example.moodlogapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

// LoginScreen, kullanıcının e-posta ve şifre ile giriş yapmasını sağlayan ekrandır.
// Compose (kullanıcı arayüzü oluşturma yapısı) kullanılarak oluşturulmuştur.
@Composable
fun LoginScreen(navController: NavController) {
    // Kullanıcının girdiği email değeri
    var email       by remember { mutableStateOf("") }

    // Kullanıcının girdiği şifre değeri
    var password    by remember { mutableStateOf("") }

    // Hata mesajlarını göstermek için kullanılan değişken
    var errorMsg    by remember { mutableStateOf("") }

    // Giriş işlemi sırasında yüklenme durumunu gösteren değişken
    var isLoading   by remember { mutableStateOf(false) }

    // Arayüz düzeni için dikey bir sütun oluşturuluyor
    Column(
        modifier = Modifier
            .fillMaxSize() // Tüm ekranı kaplar
            .padding(32.dp), // Kenarlardan boşluk bırakır
        verticalArrangement = Arrangement.Center, // Ortalayarak dikey yerleştirir
        horizontalAlignment = Alignment.CenterHorizontally // Yatayda ortalar
    ) {
        // Başlık metni
        Text("MoodLog'a Hoş Geldin", fontSize = 24.sp)
        Spacer(Modifier.height(24.dp)) // Boşluk bırakır

        // E-posta girişi alanı
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it.trim() // Boşlukları temizler
                errorMsg = ""     // Yeni girişte hata mesajını sıfırlar
            },
            label = { Text("Email") }, // Giriş kutusunun etiketi
            isError = errorMsg.contains("email", ignoreCase = true), // Hata varsa kırmızı olur
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Klavye tipi
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Şifre girişi alanı
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMsg = ""
            },
            label = { Text("Şifre") },
            isError = errorMsg.contains("şifre", ignoreCase = true),
            visualTransformation = PasswordVisualTransformation(), // Şifreyi yıldızlarla gizler
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Hata mesajı görünürse ekrana yazdırılır
        if (errorMsg.isNotEmpty()) {
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error, // Tema rengine uygun hata rengi
                fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
        }

        // Yükleniyor animasyonu göster veya butonu göster
        if (isLoading) {
            CircularProgressIndicator() // Yükleniyor göstergesi
        } else {
            // Giriş yap butonu
            Button(
                onClick = {
                    // Basit boşluk kontrolü
                    when {
                        email.isBlank()    -> { errorMsg = "Email boş bırakılamaz."; return@Button }
                        password.isBlank() -> { errorMsg = "Şifre boş bırakılamaz."; return@Button }
                    }

                    isLoading = true
                    errorMsg = ""

                    // Firebase Authentication (kullanıcı kimlik doğrulama servisi) ile giriş yapılır
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            isLoading = false
                            // Giriş başarılıysa ruh hali giriş ekranına yönlendir
                            navController.navigate("mood_entry") {
                                popUpTo("login") { inclusive = true } // Geri tuşunda login ekranı gösterilmez
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMsg = e.message ?: "Giriş başarısız oldu."
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Giriş Yap") // Butonun üzerindeki yazı
            }
        }

        Spacer(Modifier.height(8.dp))

        // Kayıt ol butonu → kullanıcı kayıt ekranına yönlendirilir
        TextButton(onClick = { navController.navigate("register") }) {
            Text("Kayıt Ol", fontSize = 14.sp)
        }
    }
}
