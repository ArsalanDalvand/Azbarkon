package com.arsalandalvand.azbarkon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionScreen(
    onBack: () -> Unit,
    onAddPoem: (Beyt) -> Unit // تابعی که شعر را به دیتابیس اصلی می‌فرستد
) {
    val context = LocalContext.current
    // بارگذاری اشعار فقط یکبار انجام شود
    val suggestions = remember { PoemImporter.loadFromAssets(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("اشعار پیشنهادی (سعدی)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(suggestions) { beyt ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = beyt.beyt1, style = MaterialTheme.typography.bodyMedium)
                            Text(text = beyt.beyt2, style = MaterialTheme.typography.bodyMedium)
                        }
                        // دکمه افزودن سریع
                        IconButton(onClick = { onAddPoem(beyt) }) {
                            Icon(Icons.Default.Add, "افزودن به لیست من")
                        }
                    }
                }
            }
        }
    }
}