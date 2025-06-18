package com.example.moodlogapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.moodlogapp.ui.screens.LoginScreen
import com.example.moodlogapp.ui.screens.MoodEntryScreen
import com.example.moodlogapp.ui.screens.RegistrationScreen
import com.example.moodlogapp.ui.screens.EntrySuccessScreen
import com.example.moodlogapp.ui.screens.RecordsScreen

/**
 * Uygulamadaki ekranlar arası geçişleri (navigation) tanımlayan yapı.
 * Her ekran bir "route" (yol) olarak belirlenir.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable // Composable (arayüz oluşturulabilir) bir fonksiyondur
fun NavGraph(navController: NavHostController) {
    // NavHost (navigasyon ana taşıyıcısı), başlangıç ekranını ve ekran geçişlerini tanımlar
    NavHost(navController = navController, startDestination = "login") {

        // "login" route'u LoginScreen (giriş ekranı) ile eşleşir
        composable("login") { LoginScreen(navController) }

        // "register" route'u RegistrationScreen (kayıt ekranı) ile eşleşir
        composable("register") { RegistrationScreen(navController) }

        // "entry_success" route'u EntrySuccessScreen (başarı ekranı) ile eşleşir
        composable("entry_success")  { EntrySuccessScreen(navController) }

        // "mood_entry" route'u MoodEntryScreen (ruh hali giriş ekranı) ile eşleşir
        composable("mood_entry") { MoodEntryScreen(navController) }

        // "records" route'u RecordsScreen (kayıt görüntüleme ekranı) ile eşleşir
        composable("records")       { RecordsScreen(navController) }
    }
}
