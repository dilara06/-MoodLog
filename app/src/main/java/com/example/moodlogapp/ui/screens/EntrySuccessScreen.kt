package com.example.moodlogapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

// EntrySuccessScreen, ruh hali kaydı başarılı olduğunda gösterilen ekrandır.
// Jetpack Compose (kullanıcı arayüzü oluşturma yapısı) kullanılarak tanımlanmıştır.
@Composable
fun EntrySuccessScreen(navController: NavController) {
    // Ekran düzeni: dikey hizalanmış, ortalanmış bir kolon (Column)
    Column(
        modifier = Modifier
            .fillMaxSize() // Tüm ekranı kaplar
            .padding(32.dp), // Kenarlardan boşluk bırakır
        verticalArrangement = Arrangement.Center, // Dikeyde ortalanır
        horizontalAlignment = Alignment.CenterHorizontally // Yatayda ortalanır
    ) {
        // Başlık metni
        Text(
            text = "Ruh hâli başarıyla kaydedildi!",
            fontSize = 24.sp, // Yazı boyutu
            color = MaterialTheme.colorScheme.primary // Temanın birincil rengi (renk şeması)
        )

        Spacer(Modifier.height(24.dp)) // 24dp boşluk ekler

        // Çıkış yapma butonu
        Button(
            onClick = {
                // Firebase Authentication (kimlik doğrulama) üzerinden çıkış yapılır
                FirebaseAuth.getInstance().signOut()

                // Navigation (yönlendirme) ile login ekranına dönülür
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true } // Geriye tüm stack temizlenir
                }
            },
            modifier = Modifier.fillMaxWidth() // Buton tam genişlikte olur
        ) {
            Text("Çıkış Yap") // Buton etiketi
        }

        Spacer(Modifier.height(16.dp)) // 16dp boşluk ekler

        // Tekrar ruh hali ekleme butonu
        Button(
            onClick = {
                // Kullanıcıyı mood_entry (ruh hâli girişi) ekranına yönlendirir
                navController.navigate("mood_entry") {
                    popUpTo("entry_success") { inclusive = true } // Bu ekran geri yığından silinir
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tekrar Ruh Hali Ekle")
        }
    }
}
