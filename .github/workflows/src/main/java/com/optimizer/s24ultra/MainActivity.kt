package com.optimizer.s24ultra

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            S24UltraOptimizerApp(this)
        }
    }
}

@Composable
fun S24UltraOptimizerApp(context: Context) {
    var ramUsage by remember { mutableStateOf("") }
    var storageUsage by remember { mutableStateOf("") }
    var cacheSize by remember { mutableStateOf("0 MB") }
    var isOptimizing by remember { mutableStateOf(false) }
    var optimizationResult by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val optimizer = remember { SystemOptimizer(context) }

    LaunchedEffect(Unit) {
        updateStats(context, optimizer) { ram, storage, cache ->
            ramUsage = ram
            storageUsage = storage
            cacheSize = cache
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1B2A))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "S24 Ultra Optimizer",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BCD4),
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                "Dein System zum Laufen bringen 🚀",
                fontSize = 14.sp,
                color = Color(0xFF90CAF9)
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatsCard("RAM-Nutzung", ramUsage)
            StatsCard("Speicher", storageUsage)
            StatsCard("Cache", cacheSize)

            Spacer(modifier = Modifier.height(8.dp))

            OptimizeButton(
                text = "⚡ 1-KLICK OPTIMIEREN",
                isLoading = isOptimizing,
                onClick = {
                    isOptimizing = true
                    scope.launch(Dispatchers.Default) {
                        optimizationResult = optimizer.quickOptimize()
                        updateStats(context, optimizer) { ram, storage, cache ->
                            ramUsage = ram
                            storageUsage = storage
                            cacheSize = cache
                        }
                        isOptimizing = false
                    }
                }
            )

            OptimizeButton(
                text = "🧹 Cache Löschen",
                isLoading = false,
                onClick = {
                    scope.launch(Dispatchers.Default) {
                        optimizer.clearCache()
                        updateStats(context, optimizer) { ram, storage, cache ->
                            cacheSize = cache
                        }
                    }
                }
            )

            OptimizeButton(
                text = "🔄 System Refresh",
                isLoading = false,
                onClick = {
                    scope.launch(Dispatchers.Default) {
                        optimizer.killBackgroundApps()
                        updateStats(context, optimizer) { ram, storage, cache ->
                            ramUsage = ram
                        }
                    }
                }
            )

            if (optimizationResult.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        optimizationResult,
                        fontSize = 12.sp,
                        color = Color(0xFF81C784),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 Tipps:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64B5F6))
                    Text("• Cache regelmäßig löschen", fontSize = 11.sp, color = Color(0xFF90CAF9))
                    Text("• Animationen in Einstellungen reduzieren", fontSize = 11.sp, color = Color(0xFF90CAF9))
                    Text("• Hintergrund-Apps deaktivieren", fontSize = 11.sp, color = Color(0xFF90CAF9))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatsCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3557)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, color = Color(0xFF90CAF9), fontWeight = FontWeight.Medium)
            Text(value, fontSize = 16.sp, color = Color(0xFF00BCD4), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OptimizeButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00BCD4),
            disabledContainerColor = Color(0xFF4DB8E8)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

fun updateStats(
    context: Context,
    optimizer: SystemOptimizer,
    onUpdate: (ram: String, storage: String, cache: String) -> Unit
) {
    val runtime = Runtime.getRuntime()

    val maxMemory = runtime.maxMemory() / 1048576L
    val totalMemory = runtime.totalMemory() / 1048576L
    val freeMemory = runtime.freeMemory() / 1048576L
    val usedMemory = totalMemory - freeMemory
    val ramPercent = (usedMemory * 100 / maxMemory).toInt()
    val ramStr = "$usedMemory MB / $maxMemory MB ($ramPercent%)"

    val stats = StatFs(Environment.getDataDirectory().absolutePath)
    val storageTotal = stats.totalBlockCount * stats.blockSize / 1073741824L
    val storageFree = stats.availableBlocks * stats.blockSize / 1073741824L
    val storageUsed = storageTotal - storageFree
    val storageStr = "$storageUsed GB / $storageTotal GB"

    val cacheSize = getCacheSizeInMB(context)
    val cacheStr = "$cacheSize MB"

    onUpdate(ramStr, storageStr, cacheStr)
}

fun getCacheSizeInMB(context: Context): Long {
    var size = 0L
    context.cacheDir?.let { size += getDirSize(it) }
    context.externalCacheDir?.let { size += getDirSize(it) }
    return size / 1048576L
}

fun getDirSize(dir: File): Long {
    var size = 0L
    dir.listFiles()?.forEach { file ->
        size += if (file.isDirectory) getDirSize(file) else file.length()
    }
    return size
}
