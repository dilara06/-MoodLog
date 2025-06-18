package com.example.moodlogapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.moodlogapp.ui.navigation.NavGraph
import com.example.moodlogapp.ui.theme.MoodLogAppTheme

// MainActivity, uygulamanın başladığı ilk yerdir (giriş noktası).
// Burada Jetpack Compose (kullanıcı arayüzü oluşturma yapısı) ile arayüz tanımlanır
// ve navigation (ekranlar arası geçiş) sistemi başlatılır.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent bloğu, Compose (arayüz tanımı sistemi) ile kullanıcı arayüzünü (UI) tanımlar.
        setContent {
            // Uygulamanın genel görünüm teması burada uygulanır (renkler, yazı tipi vs.)
            MoodLogAppTheme {
                // Ekranı tamamen kaplayan bir yüzey (zemin) oluşturulur.
                Surface(modifier = Modifier.fillMaxSize()) {
                    // navigationController (ekranlar arası geçiş yöneticisi) tanımlanır.
                    val navController = rememberNavController()

                    // NavGraph (ekran geçiş yapısı) çağrılır.
                    // Bu yapı, hangi ekranın ne zaman gösterileceğini belirler.
                    NavGraph(navController)
                }
            }
        }
    }
}
