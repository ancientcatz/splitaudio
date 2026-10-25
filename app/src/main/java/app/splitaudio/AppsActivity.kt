package app.splitaudio

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplitAudioTheme {
                AppsScreen(onBack = { finish() })
            }
        }
    }
}

private data class AppEntry(
    val pkg: String,
    val label: String,
    val icon: Drawable,
)

// ──────────────────────────────────────────────────────────────────────────────
// Apps screen
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var allApps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var pickedApp by remember { mutableStateOf<AppEntry?>(null) }
    var changed by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load installed launcher apps off the main thread
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val list = pm.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
            )
                .map { it.activityInfo.applicationInfo }
                .distinctBy { it.packageName }
                .filter { it.packageName != context.packageName }
                .map {
                    AppEntry(
                        pkg = it.packageName,
                        label = pm.getApplicationLabel(it).toString(),
                        icon = pm.getApplicationIcon(it),
                    )
                }
            withContext(Dispatchers.Main) { allApps = list }
        }
    }

    // Flush changes when leaving the screen (mirrors the original onPause behavior)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && changed) {
                changed = false
                scope.launch {
                    withContext(Dispatchers.IO) { Route.apply(context.applicationContext) }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val shownApps = remember(allApps, query) {
        allApps
            .filter { query.isEmpty() || it.label.contains(query, true) || it.pkg.contains(query, true) }
            .sortedWith(
                compareBy(
                    { Outputs.get(context, it.pkg) == "default" },
                    { it.label.lowercase() },
                )
            )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.apps_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.apps_search)) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
            )

            Text(
                text = stringResource(R.string.apps_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(shownApps, key = { it.pkg }) { app ->
                    val output = Outputs.get(context, app.pkg)
                    ListItem(
                        leadingContent = {
                            Image(
                                bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                            )
                        },
                        headlineContent = { Text(app.label) },
                        trailingContent = { OutputChip(output) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickedApp = app },
                    )
                }
            }
        }
    }

    // Output picker — ModalBottomSheet
    val app = pickedApp
    if (app != null) {
        val currentOutput = Outputs.get(context, app.pkg)
        ModalBottomSheet(
            onDismissRequest = { pickedApp = null },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(16.dp))
                Outputs.KEYS.forEach { key ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Outputs.set(context, app.pkg, key)
                                changed = true
                                pickedApp = null
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = currentOutput == key,
                            onClick = null,
                        )
                        Spacer(Modifier.width(16.dp))
                        OutputIcon(key)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = Outputs.label(context, key),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Output chip + icon helpers
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun OutputChip(outputKey: String) {
    val (label, icon, container, content) = outputVisuals(outputKey)
    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun OutputIcon(outputKey: String) {
    val (_, icon, _, _) = outputVisuals(outputKey)
    Icon(
        icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun outputVisuals(key: String): VisualData {
    val c = MaterialTheme.colorScheme
    return when (key) {
        "speaker" -> VisualData(
            label = stringResource(R.string.out_speaker),
            icon = Icons.Filled.Smartphone,
            container = c.tertiaryContainer,
            content = c.onTertiaryContainer,
        )
        "bt" -> VisualData(
            label = stringResource(R.string.out_bt),
            icon = Icons.Filled.Bluetooth,
            container = c.primaryContainer,
            content = c.onPrimaryContainer,
        )
        "wired" -> VisualData(
            label = stringResource(R.string.out_wired),
            icon = Icons.Filled.Headphones,
            container = c.secondaryContainer,
            content = c.onSecondaryContainer,
        )
        else -> VisualData(
            label = stringResource(R.string.out_default),
            icon = Icons.Filled.Tune,
            container = c.surfaceVariant,
            content = c.onSurfaceVariant,
        )
    }
}

private data class VisualData(
    val label: String,
    val icon: ImageVector,
    val container: androidx.compose.ui.graphics.Color,
    val content: androidx.compose.ui.graphics.Color,
)
