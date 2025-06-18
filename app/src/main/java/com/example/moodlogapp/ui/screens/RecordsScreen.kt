package com.example.moodlogapp.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(navController: NavController) {
    /* ---------------------------- STATE ---------------------------- */
    val db = Firebase.firestore
    var allEntries by remember { mutableStateOf(listOf<Entry>()) }

    var datePreset by remember { mutableStateOf(DatePreset.TODAY) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var nameFilter by remember { mutableStateOf("Tümü") }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // load once
    LaunchedEffect(Unit) {
        db.collection("mood_entries").get().addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { d ->
                val ts = d.getTimestamp("timestamp")?.toDate() ?: return@mapNotNull null
                Entry(
                    id = d.id,
                    firstName = d.getString("firstName").orEmpty(),
                    lastName = d.getString("lastName").orEmpty(),
                    moodLabel = d.getString("moodLabel").orEmpty(),
                    moodEmoji = d.getString("moodEmoji").orEmpty(),
                    moodScore = d.getLong("moodScore")?.toInt() ?: 0,
                    note = d.getString("note"),
                    date = ts
                )
            }
            allEntries = list
        }
    }

    /* ----------------------- FILTERED DATA ------------------------ */
    val filtered = remember(allEntries, datePreset, startDate, endDate, nameFilter) {
        allEntries
            .filter { e ->
                when (datePreset) {
                    DatePreset.TODAY -> isSameDay(e.date, Date())
                    DatePreset.WEEK -> isWithinDays(e.date, 7)
                    DatePreset.MONTH -> isWithinDays(e.date, 30)
                    DatePreset.ALL -> true
                }
            }
            .filter { e ->
                val afterStart = startDate?.let { e.date >= it } ?: true
                val beforeEnd = endDate?.let { e.date <= it } ?: true
                afterStart && beforeEnd
            }
            .filter { e ->
                if (nameFilter == "Tümü") true else "${e.firstName} ${e.lastName}" == nameFilter
            }
    }

    val scores = remember(filtered) {
        filtered.groupBy { it.firstName + " " + it.lastName }
            .map { (name, list) -> name to list.sumOf { it.moodScore } }
            .sortedByDescending { it.second }
    }

    /* ----------------------- UI ------------------------ */
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("KAYITLAR") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            /* Zaman çipleri */
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                listOf("Bugün", "Bu Hafta", "Bu Ay", "Hepsi").forEach { label ->
                    val selected = when (label) {
                        "Bugün" -> datePreset == DatePreset.TODAY
                        "Bu Hafta" -> datePreset == DatePreset.WEEK
                        "Bu Ay" -> datePreset == DatePreset.MONTH
                        else -> datePreset == DatePreset.ALL
                    }
                    FilterChip(
                        selected = selected,
                        onClick = {
                            datePreset = when (label) {
                                "Bugün" -> DatePreset.TODAY
                                "Bu Hafta" -> DatePreset.WEEK
                                "Bu Ay" -> DatePreset.MONTH
                                else -> DatePreset.ALL
                            }
                        },
                        label = { Text(label) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            /* Tarih aralığı */
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { showStartPicker = true }) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(startDate?.let { sdf.format(it) } ?: "Başlangıç")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { showEndPicker = true }) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(endDate?.let { sdf.format(it) } ?: "Bitiş")
                }
            }
            Spacer(Modifier.height(8.dp))

            /* İsim filtresi */
            val names = listOf("Tümü") + allEntries.map { it.firstName + " " + it.lastName }.distinct().sorted()
            var expand by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expand, onExpandedChange = { expand = !expand }) {
                OutlinedTextField(
                    value = nameFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("İsim seç") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expand) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(expanded = expand, onDismissRequest = { expand = false }) {
                    names.forEach { n ->
                        DropdownMenuItem(text = { Text(n) }, onClick = {
                            nameFilter = n
                            expand = false
                        })
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            /* İki sütun – yarı yarıya */
            Row(Modifier.fillMaxSize()) {
                /* Kayıtlar */
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                ) {
                    items(filtered) { entry ->
                        Card(Modifier.padding(vertical = 6.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${entry.firstName} ${entry.lastName}", fontWeight = FontWeight.Bold)
                                Text("${entry.moodEmoji} ${entry.moodLabel} — Puan: ${entry.moodScore}")
                                entry.note?.let { Text(it, fontSize = 12.sp) }
                                Text(sdf.format(entry.date), fontSize = 12.sp)
                            }
                        }
                    }
                }
                /* Puan tablosu */
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    item {
                        Text(
                            "Puan Toplamları",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(scores) { (name, total) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name)
                            Text(total.toString())
                        }
                    }
                }
            }
        }
    }

    /* ---------------- Date pickers ---------------- */
    val ctx = LocalContext.current
    if (showStartPicker) {
        val cal = Calendar.getInstance()
        DatePickerDialog(ctx, { _: DatePicker, y: Int, m: Int, d: Int ->
            startDate = Calendar.getInstance().apply {
                set(y, m, d, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            showStartPicker = false
        }, cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]).show()
    }
    if (showEndPicker) {
        val cal = Calendar.getInstance()
        DatePickerDialog(ctx, { _: DatePicker, y: Int, m: Int, d: Int ->
            endDate = Calendar.getInstance().apply {
                set(y, m, d, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.time
            showEndPicker = false
        }, cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]).show()
    }
}

/* ---------------- Helpers & models ---------------- */
private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

private fun isSameDay(d1: Date, d2: Date): Boolean {
    val c1 = Calendar.getInstance().apply { time = d1 }
    val c2 = Calendar.getInstance().apply { time = d2 }
    return c1[Calendar.YEAR] == c2[Calendar.YEAR] && c1[Calendar.DAY_OF_YEAR] == c2[Calendar.DAY_OF_YEAR]
}

private fun isWithinDays(date: Date, days: Int): Boolean {
    val diff = Date().time - date.time
    return diff <= days * 24 * 60 * 60 * 1000L
}

enum class DatePreset { TODAY, WEEK, MONTH, ALL }

data class Entry(
    val id: String,
    val firstName: String,
    val lastName: String,
    val moodLabel: String,
    val moodEmoji: String,
    val moodScore: Int,
    val note: String?,
    val date: Date
)
