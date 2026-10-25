package app.splitaudio

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.MusicVideo
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        }
        MonitorService.ensure(this)
        enableEdgeToEdge()
        setContent {
            SplitAudioTheme {
                HomeScreen()
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Home screen
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Reactive state
    var routeStatus by remember { mutableStateOf<String?>(null) }
    var shizukuAlive by remember { mutableStateOf(false) }
    var shizukuGranted by remember { mutableStateOf(false) }
    var accEnabled by remember { mutableStateOf(false) }
    var customMode by remember { mutableStateOf(Outputs.customFlag(context)) }
    var customAppCount by remember { mutableStateOf(Outputs.all(context).size) }
    var multiFocus by remember { mutableStateOf<String?>(null) }
    var lastStart by remember { mutableStateOf(Route.lastStart) }
    var isStarting by remember { mutableStateOf(false) }
    var isStopping by remember { mutableStateOf(false) }
    var isSwitchingMode by remember { mutableStateOf(false) }
    var isTogglingMf by remember { mutableStateOf(false) }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val acc = (Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "")
            .contains("${context.packageName}/")
        val s = Route.ask("state")
        val alive = Shz.alive()
        val granted = Shz.granted()
        val mf = if (granted) Route.multiFocusState() else null
        val custom = Outputs.customFlag(context)
        val nApps = Outputs.all(context).size
        withContext(Dispatchers.Main) {
            routeStatus = s
            shizukuAlive = alive
            shizukuGranted = granted
            accEnabled = acc
            customMode = custom
            customAppCount = nApps
            multiFocus = mf
            lastStart = Route.lastStart
        }
    }

    // Initial refresh + 2 s polling for live status updates
    LaunchedEffect(Unit) {
        refresh()
        while (isActive) {
            delay(2000)
            refresh()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Hero subtitle — explains the app at a glance
            item {
                Text(
                    text = stringResource(R.string.hero_sub),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            item {
                StatusCard(
                    status = routeStatus,
                    lastStart = lastStart,
                    customMode = customMode,
                    customAppCount = customAppCount,
                    isStarting = isStarting,
                    isStopping = isStopping,
                    onStart = {
                        scope.launch {
                            isStarting = true
                            val r = withContext(Dispatchers.IO) { Route.start(context) }
                            isStarting = false
                            refresh()
                            if (r.startsWith("deploy_fail") || r.startsWith("map_fail")) {
                                snackbarHostState.showSnackbar("Failed to start: $r")
                            }
                        }
                    },
                    onStop = {
                        scope.launch {
                            isStopping = true
                            withContext(Dispatchers.IO) { Route.stop() }
                            isStopping = false
                            refresh()
                        }
                    },
                )
            }

            item {
                SetupSection(
                    shizukuGranted = shizukuGranted,
                    shizukuAlive = shizukuAlive,
                    accEnabled = accEnabled,
                    onGrantShizuku = { Shz.request() },
                    onOpenAccessibility = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                )
            }

            item {
                CustomizeSection(
                    customMode = customMode,
                    customAppCount = customAppCount,
                    multiFocus = multiFocus,
                    shizukuGranted = shizukuGranted,
                    isSwitchingMode = isSwitchingMode,
                    isTogglingMf = isTogglingMf,
                    onModeChange = { custom ->
                        if (custom == Outputs.customFlag(context)) return@CustomizeSection
                        Outputs.setCustom(context, custom)
                        scope.launch {
                            isSwitchingMode = true
                            withContext(Dispatchers.IO) { Route.apply(context.applicationContext) }
                            isSwitchingMode = false
                            refresh()
                        }
                        if (custom && !Outputs.hasAny(context)) {
                            context.startActivity(Intent(context, AppsActivity::class.java))
                        }
                    },
                    onOpenApps = {
                        context.startActivity(Intent(context, AppsActivity::class.java))
                    },
                    onMultiFocusChange = { on ->
                        scope.launch {
                            isTogglingMf = true
                            withContext(Dispatchers.IO) {
                                Route.lastStart = context.getString(R.string.mf_result, Route.setMultiFocus(context, on))
                            }
                            isTogglingMf = false
                            refresh()
                        }
                    },
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Status card — prominent, state-colored
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusCard(
    status: String?,
    lastStart: String,
    customMode: Boolean,
    customAppCount: Int,
    isStarting: Boolean,
    isStopping: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    val data = when (status) {
        "on" -> StatusVisuals(
            text = stringResource(R.string.live_on),
            icon = Icons.Filled.GraphicEq,
            container = MaterialTheme.colorScheme.primaryContainer,
            onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        "off" -> StatusVisuals(
            text = stringResource(R.string.live_wait),
            icon = Icons.Filled.Pending,
            container = MaterialTheme.colorScheme.secondaryContainer,
            onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        else -> StatusVisuals(
            text = stringResource(R.string.live_none),
            icon = Icons.Filled.MusicOff,
            container = MaterialTheme.colorScheme.surfaceVariant,
            onContainer = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = data.container,
            contentColor = data.onContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = data.onContainer.copy(alpha = 0.12f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = data.icon,
                        transitionSpec = {
                            (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                        },
                        label = "statusIcon",
                    ) { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                AnimatedContent(
                    targetState = data.text,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "statusText",
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (customMode && customAppCount > 0)
                    stringResource(R.string.st_mode_custom, customAppCount)
                else
                    stringResource(R.string.st_mode_simple),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (lastStart.isNotBlank() && lastStart != "none") {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.st_last, lastStart),
                    style = MaterialTheme.typography.bodySmall,
                    color = data.onContainer.copy(alpha = 0.7f),
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.weight(1f),
                    enabled = !isStarting && !isStopping,
                    shape = MaterialTheme.shapes.large,
                ) {
                    if (isStarting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.btn_start))
                }
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    enabled = !isStarting && !isStopping,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = data.onContainer,
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, data.onContainer.copy(alpha = 0.5f)
                    ),
                ) {
                    if (isStopping) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(
                            Icons.Filled.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.btn_stop))
                }
            }
        }
    }
}

private data class StatusVisuals(
    val text: String,
    val icon: ImageVector,
    val container: Color,
    val onContainer: Color,
)

// ──────────────────────────────────────────────────────────────────────────────
// Setup section
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SetupSection(
    shizukuGranted: Boolean,
    shizukuAlive: Boolean,
    accEnabled: Boolean,
    onGrantShizuku: () -> Unit,
    onOpenAccessibility: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.sec_required),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(4.dp))

            SetupRow(
                icon = Icons.Outlined.Bluetooth,
                title = stringResource(R.string.row_shizuku),
                subtitle = stringResource(R.string.row_shizuku_d),
                statusText = when {
                    !shizukuAlive -> stringResource(R.string.v_not_running)
                    shizukuGranted -> stringResource(R.string.v_granted)
                    else -> stringResource(R.string.v_not_granted)
                },
                statusType = when {
                    !shizukuAlive -> StatusType.ERROR
                    shizukuGranted -> StatusType.OK
                    else -> StatusType.WARNING
                },
                actionLabel = if (shizukuGranted) null else stringResource(R.string.btn_grant),
                onAction = onGrantShizuku,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SetupRow(
                icon = Icons.Outlined.VolumeUp,
                title = stringResource(R.string.row_acc),
                subtitle = stringResource(R.string.row_acc_d),
                statusText = if (accEnabled) stringResource(R.string.v_on) else stringResource(R.string.chip_recommended),
                statusType = if (accEnabled) StatusType.OK else StatusType.WARNING,
                actionLabel = if (accEnabled) null else stringResource(R.string.btn_open),
                onAction = onOpenAccessibility,
            )
        }
    }
}

@Composable
private fun SetupRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    statusText: String,
    statusType: StatusType,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(text = statusText, type = statusType)
        }
        if (actionLabel != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

private enum class StatusType { OK, WARNING, ERROR }

@Composable
private fun StatusChip(text: String, type: StatusType) {
    val (container, content, leadingIcon) = when (type) {
        StatusType.OK -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Filled.CheckCircle,
        )
        StatusType.WARNING -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Filled.Pending,
        )
        StatusType.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Filled.RadioButtonUnchecked,
        )
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Customize section
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomizeSection(
    customMode: Boolean,
    customAppCount: Int,
    multiFocus: String?,
    shizukuGranted: Boolean,
    isSwitchingMode: Boolean,
    isTogglingMf: Boolean,
    onModeChange: (Boolean) -> Unit,
    onOpenApps: () -> Unit,
    onMultiFocusChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.sec_custom),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(12.dp))

            // Mode selection
            Text(
                text = stringResource(R.string.mode_title),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !customMode,
                    onClick = { onModeChange(false) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) {
                    Text(stringResource(R.string.mode_simple))
                }
                SegmentedButton(
                    selected = customMode,
                    onClick = { onModeChange(true) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) {
                    Text(stringResource(R.string.mode_custom))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (customMode && customAppCount == 0)
                    stringResource(R.string.mode_custom_empty)
                else if (customMode)
                    stringResource(R.string.mode_custom_d)
                else
                    stringResource(R.string.mode_simple_d),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // "Choose output per app" button — only in Custom mode
            AnimatedVisibility(
                visible = customMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = onOpenApps,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_apps))
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )

            // Multi-focus switch row
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Outlined.MusicVideo,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                headlineContent = {
                    Text(stringResource(R.string.mf_title))
                },
                supportingContent = {
                    Text(
                        text = when (multiFocus) {
                            "on" -> stringResource(R.string.mf_desc)
                            "off" -> stringResource(R.string.mf_off_auto)
                            "off_user" -> stringResource(R.string.mf_off_user)
                            else -> stringResource(R.string.mf_desc)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = multiFocus == "on",
                        onCheckedChange = onMultiFocusChange,
                        enabled = shizukuGranted && !isTogglingMf,
                    )
                },
            )
        }
    }
}
