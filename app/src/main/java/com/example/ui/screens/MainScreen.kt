package com.example.ui.screens

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.MainActivity
import com.example.data.database.BrawlerMeta
import com.example.data.database.EsportMatch
import com.example.ui.viewmodel.BrawlArenaViewModel
import com.example.ui.viewmodel.BrawlArenaViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

// --- Custom Palette for Esports Theme ---
object ArenaColors {
    val Background = Color(0xFF090A10)
    val SurfaceDark = Color(0xFF131522)
    val SurfaceMedium = Color(0xFF1E2136)
    
    val AccGold = Color(0xFFFFB300)       // S-Tier & Legendary Trophy
    val AccCyan = Color(0xFF00E5FF)       // Esport Tech / Stats Glow
    val AccCrimson = Color(0xFFFF3D00)    // A-Tier / Health Bar Saturated Red
    val AccPurple = Color(0xFFD0BCFF)     // B-Tier
    
    val TextMain = Color(0xFFF3F4F6)
    val TextMuted = Color(0xFF9CA3AF)
    
    val GradientMain = Brush.horizontalGradient(
        colors = listOf(Color(0xFF1A1F38), Color(0xFF101326))
    )
    val GoldGlow = Brush.linearGradient(
        colors = listOf(Color(0xFFFFA000), Color(0xFFFFD54F))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: BrawlArenaViewModel = viewModel(
        factory = BrawlArenaViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val brawlers by viewModel.brawlers.collectAsStateWithLifecycle()
    val matcherinos by viewModel.matcherinos.collectAsStateWithLifecycle()
    
    val selectedMatch by viewModel.selectedMatch.collectAsStateWithLifecycle()
    val selectedBrawler by viewModel.selectedBrawler.collectAsStateWithLifecycle()
    val isGeneratingTactic by viewModel.isGeneratingTactic.collectAsStateWithLifecycle()
    val statusMessage by viewModel.operationStatus.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Maç Takvimi, 1: Karakter Metası, 2: Matcherinolar, 3: Brawl AI Koçu
    var showAddMatchForm by remember { mutableStateOf(false) }
    var matchToEdit by remember { mutableStateOf<EsportMatch?>(null) }
    var showAddBrawlerForm by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe status messages to trigger Snackbar feedback
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Brawl Logo",
                            tint = ArenaColors.AccGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "BRAWL ARENA TAKTİK",
                            fontWeight = FontWeight.Bold,
                            color = ArenaColors.TextMain,
                            letterSpacing = 1.sp,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArenaColors.Background,
                    titleContentColor = ArenaColors.TextMain
                ),
                actions = {
                    if (activeTab == 0) {
                        IconButton(
                            onClick = { showAddMatchForm = true },
                            modifier = Modifier.testTag("add_match_fab_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Yeni Maç Ekle",
                                tint = ArenaColors.AccCyan
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ArenaColors.SurfaceDark,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Maç Takvimi") },
                    label = { Text("Maçlar & Scrim") },
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ArenaColors.AccCyan,
                        selectedTextColor = ArenaColors.AccCyan,
                        unselectedIconColor = ArenaColors.TextMuted,
                        unselectedTextColor = ArenaColors.TextMuted,
                        indicatorColor = ArenaColors.SurfaceMedium
                    ),
                    modifier = Modifier.testTag("nav_item_calendar")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Meta & Karakterler") },
                    label = { Text("Karakter Metası") },
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ArenaColors.AccGold,
                        selectedTextColor = ArenaColors.AccGold,
                        unselectedIconColor = ArenaColors.TextMuted,
                        unselectedTextColor = ArenaColors.TextMuted,
                        indicatorColor = ArenaColors.SurfaceMedium
                    ),
                    modifier = Modifier.testTag("nav_item_meta")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Matcherinolar") },
                    label = { Text("Matcherinolar") },
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ArenaColors.AccCyan,
                        selectedTextColor = ArenaColors.AccCyan,
                        unselectedIconColor = ArenaColors.TextMuted,
                        unselectedTextColor = ArenaColors.TextMuted,
                        indicatorColor = ArenaColors.SurfaceMedium
                    ),
                    modifier = Modifier.testTag("nav_item_matcherino")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Face, contentDescription = "Brawl AI Koçu") },
                    label = { Text("AI Koç") },
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ArenaColors.AccGold,
                        selectedTextColor = ArenaColors.AccGold,
                        unselectedIconColor = ArenaColors.TextMuted,
                        unselectedTextColor = ArenaColors.TextMuted,
                        indicatorColor = ArenaColors.SurfaceMedium
                    ),
                    modifier = Modifier.testTag("nav_item_aicoach")
                )
            }
        },
        containerColor = ArenaColors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ArenaColors.Background)
        ) {
            // Main views based on active tab
            Crossfade(targetState = activeTab, label = "tabTransition") { tab ->
                when (tab) {
                    0 -> MatchCalendarTab(
                        matches = matches,
                        onMatchClick = { viewModel.selectMatch(it) },
                        onEditClick = { matchToEdit = it },
                        onDeleteClick = { viewModel.deleteMatch(it) },
                        onRecommendClick = { viewModel.generateAITacticPlay(it) },
                        isGenerating = isGeneratingTactic
                    )
                    1 -> BrawlerMetaTab(
                        brawlers = brawlers,
                        onBrawlerClick = { viewModel.selectBrawler(it) },
                        onAddBrawlerClick = { showAddBrawlerForm = true }
                    )
                    2 -> MatcherinosTab(
                        matcherinos = matcherinos,
                        onAddMatcherino = { title, prize, url, status, notes, mode ->
                            viewModel.addMatcherino(title, prize, url, status, notes, mode)
                        },
                        onUpdateMatcherino = { id, title, prize, url, status, notes, mode ->
                            viewModel.updateMatcherino(id, title, prize, url, status, notes, mode)
                        },
                        onDeleteMatcherino = { viewModel.deleteMatcherino(it) }
                    )
                    3 -> BrawlAICoachTab(viewModel = viewModel)
                }
            }

            // Bottom Sheets / Overlays to support features

            // 1. ADD / EDIT MATCH DIALOG
            if (showAddMatchForm || matchToEdit != null) {
                MatchFormDialog(
                    match = matchToEdit,
                    onDismiss = {
                        showAddMatchForm = false
                        matchToEdit = null
                    },
                    onSave = { teamA, teamB, dateMillis, gameMode, stage, notes, draft, isScrim, scrimDetails, scoreA, scoreB ->
                        if (matchToEdit != null) {
                            viewModel.updateMatch(
                                id = matchToEdit!!.id,
                                teamA = teamA,
                                teamB = teamB,
                                dateMillis = dateMillis,
                                gameMode = gameMode,
                                mapName = "", // harita kaldırıldı
                                stage = stage,
                                generalNotes = notes,
                                draftPicksText = draft,
                                aiTacticPlan = matchToEdit!!.aiTacticPlan,
                                isScrim = isScrim,
                                scrimDetails = scrimDetails,
                                scoreA = scoreA,
                                scoreB = scoreB
                            )
                            matchToEdit = null
                        } else {
                            viewModel.addMatch(
                                teamA = teamA,
                                teamB = teamB,
                                dateMillis = dateMillis,
                                gameMode = gameMode,
                                mapName = "", // harita kaldırıldı
                                stage = stage,
                                generalNotes = notes,
                                draftPicksText = draft,
                                isScrim = isScrim,
                                scrimDetails = scrimDetails,
                                scoreA = scoreA,
                                scoreB = scoreB
                            )
                            showAddMatchForm = false
                        }
                    }
                )
            }

            // 2. MATCH DETAILS & GEMINI PLAN SCREEN
            selectedMatch?.let { match ->
                MatchDetailsOverlay(
                    match = match,
                    onDismiss = { viewModel.selectMatch(null) },
                    onEdit = {
                        viewModel.selectMatch(null)
                        matchToEdit = match
                    },
                    onTriggerGemini = { viewModel.generateAITacticPlay(match) },
                    isGenerating = isGeneratingTactic
                )
            }

            // 3. BRAWLER DETAILS SHEET / EDITOR
            selectedBrawler?.let { brawler ->
                BrawlerDetailsOverlay(
                    brawler = brawler,
                    onDismiss = { viewModel.selectBrawler(null) },
                    onSaveMeta = { name, role, bestModes, counterTips, notes, tier ->
                        viewModel.updateBrawlerNotes(
                            brawlerId = brawler.brawlerId,
                            name = name,
                            role = role,
                            bestModes = bestModes,
                            counterTips = counterTips,
                            userNotes = notes,
                            tier = tier
                        )
                    }
                )
            }

            // 4. ADD CUSTOM BRAWLER DIALOG
            if (showAddBrawlerForm) {
                BrawlerFormDialog(
                    onDismiss = { showAddBrawlerForm = false },
                    onSave = { name, role, bestModes, counterTips, notes, tier ->
                        viewModel.addCustomBrawler(
                            name = name,
                            role = role,
                            bestModes = bestModes,
                            counterTips = counterTips,
                            userNotes = notes,
                            tier = tier
                        )
                        showAddBrawlerForm = false
                    }
                )
            }
        }
    }
}

// ==========================================
// DATE RIBBON & MONTH CALENDAR INDICATOR
// ==========================================
@Composable
fun DateRibbon(
    matches: List<EsportMatch>,
    selectedDate: Date?,
    onDateSelect: (Date?) -> Unit
) {
    val dates = remember {
        (0 until 21).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, offset)
            cal.time
        }
    }
    
    val dayFormat = remember { SimpleDateFormat("EEE", Locale("tr")) }
    val dayNumberFormat = remember { SimpleDateFormat("d", Locale("tr")) }
    val sdfKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📅 ANTRENMAN & MAÇ TAKVİMİ",
                color = ArenaColors.TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            if (selectedDate != null) {
                TextButton(
                    onClick = { onDateSelect(null) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Tüm Günleri Göster ✖", color = ArenaColors.AccCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dates) { date ->
                val isSelected = selectedDate != null && 
                    sdfKey.format(date) == sdfKey.format(selectedDate)
                
                val dateStr = sdfKey.format(date)
                val dayMatches = matches.filter { sdfKey.format(Date(it.dateMillis)) == dateStr }
                val hasMatch = dayMatches.any { !it.isScrim }
                val hasScrim = dayMatches.any { it.isScrim }

                Card(
                    modifier = Modifier
                        .width(62.dp)
                        .clickable {
                            if (isSelected) onDateSelect(null) else onDateSelect(date)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) ArenaColors.AccCyan else ArenaColors.SurfaceMedium
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(ArenaColors.SurfaceDark)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = dayFormat.format(date).uppercase(),
                            color = if (isSelected) Color.Black else ArenaColors.TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayNumberFormat.format(date),
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Indicators inside card for active types on that day
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasMatch) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (isSelected) Color.Black else ArenaColors.AccCyan)
                                )
                            }
                            if (hasScrim) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (isSelected) Color.Black else ArenaColors.AccPurple)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 1: MATCH CALENDAR
// ==========================================
@Composable
fun MatchCalendarTab(
    matches: List<EsportMatch>,
    onMatchClick: (EsportMatch) -> Unit,
    onEditClick: (EsportMatch) -> Unit,
    onDeleteClick: (EsportMatch) -> Unit,
    onRecommendClick: (EsportMatch) -> Unit,
    isGenerating: Boolean
) {
    var selectedDateFilter by remember { mutableStateOf<Date?>(null) }
    var selectedTypeFilter by remember { mutableStateOf("Hepsi") } // "Hepsi", "Turnuvalar", "Scrimler"

    val sdfKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val filteredMatches = remember(matches, selectedDateFilter, selectedTypeFilter) {
        matches.filter { match ->
            val matchesDate = if (selectedDateFilter == null) true else {
                sdfKey.format(Date(match.dateMillis)) == sdfKey.format(selectedDateFilter!!)
            }
            val matchesType = when (selectedTypeFilter) {
                "Scrimler" -> match.isScrim
                "Turnuvalar" -> !match.isScrim
                else -> true
            }
            matchesDate && matchesType
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stats/Banner Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Turnuva & Scrim Sistemi",
                        color = ArenaColors.AccCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (matches.isEmpty()) "Takvimde Etkinlik Yok" else "${matches.size} Planlı Antrenman / Maç",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Calendar",
                    tint = ArenaColors.AccCyan,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // 1. DATE RIBBON FOR HIGHLIGHTING SCRIMS
        DateRibbon(
            matches = matches,
            selectedDate = selectedDateFilter,
            onDateSelect = { selectedDateFilter = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. FILTER SCRIM VS ESPOR TOURNAMENTS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Hepsi", "Turnuvalar", "Scrimler").forEach { type ->
                val isSelected = selectedTypeFilter == type
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTypeFilter = type },
                    label = { 
                        Text(
                            text = when(type) {
                                "Turnuvalar" -> "🏆 Turnuva Maçları"
                                "Scrimler" -> "📋 Scrimler"
                                else -> "🔍 Hepsi"
                            }
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (type == "Scrimler") ArenaColors.AccPurple else ArenaColors.AccCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = ArenaColors.SurfaceDark,
                        labelColor = ArenaColors.TextMuted
                    ),
                    modifier = Modifier.testTag("type_filter_chip_$type")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty Matches",
                        tint = ArenaColors.SurfaceMedium,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Etkinlik Seçilmedi veya Yok",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belirtilen gün veya filtrelere uygun planlanmış etkinlik bulunmamaktadır. Sağ üstteki '+' butonuna basarak yeni bir antrenman/scrim günü işaretleyebilirsiniz.",
                        color = ArenaColors.TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("match_list")
            ) {
                items(filteredMatches) { match ->
                    MatchItemCard(
                        match = match,
                        onClick = { onMatchClick(match) },
                        onEdit = { onEditClick(match) },
                        onDelete = { onDeleteClick(match) },
                        onRecommend = { onRecommendClick(match) },
                        isGenerating = isGenerating
                    )
                }
            }
        }
    }
}

@Composable
fun MatchItemCard(
    match: EsportMatch,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRecommend: () -> Unit,
    isGenerating: Boolean
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("tr")) }
    val formattedDate = dateFormatter.format(Date(match.dateMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("match_item_${match.id}"),
        colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (match.isScrim) ArenaColors.AccPurple else if (match.aiTacticPlan.isNotEmpty()) ArenaColors.AccCyan else ArenaColors.SurfaceMedium)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header: Stage pill, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (match.isScrim) ArenaColors.AccPurple.copy(alpha = 0.2f) else ArenaColors.SurfaceMedium,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = if (match.isScrim) "📋 SCRIM (ANTRENMAN)" else match.stage,
                        color = if (match.isScrim) ArenaColors.AccPurple else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = formattedDate,
                    color = ArenaColors.TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main: Team Names & Conditional Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = match.teamA,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (match.scoreA != null && match.scoreB != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "${match.scoreA} - ${match.scoreB}",
                            color = ArenaColors.AccCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                        )
                        Text(
                            text = "SKOR",
                            color = ArenaColors.TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                } else {
                    Text(
                        text = "VS",
                        color = ArenaColors.AccGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                Text(
                    text = match.teamB,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meta badging (Game Mode & Map removed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Mode (Shown only if gameMode is specified)
                if (match.gameMode.isNotEmpty()) {
                    Surface(
                        color = Color(0xFF1E2A38),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Mode",
                                tint = ArenaColors.AccCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = match.gameMode,
                                color = ArenaColors.AccCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Scrim Goals short indicator
                if (match.isScrim && match.scrimDetails.isNotEmpty()) {
                    Text(
                        text = "🎯 ${match.scrimDetails}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = ArenaColors.SurfaceMedium, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Match Sil",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Manual Edit icon/button
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = ArenaColors.TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Yaz", color = ArenaColors.TextMuted, fontSize = 13.sp)
                    }

                    // AI Strategize Button
                    Button(
                        onClick = onRecommend,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (match.aiTacticPlan.isNotEmpty()) ArenaColors.SurfaceMedium else ArenaColors.AccCyan,
                            contentColor = if (match.aiTacticPlan.isNotEmpty()) ArenaColors.AccCyan else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, // Can act as general action glow
                            contentDescription = "Yapay Zeka Taktik",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (match.aiTacticPlan.isNotEmpty()) "Taktik Plan 🤖" else "Gemini Analiz",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// TAB 2: BRAWLER META TABLE
// ==========================================
@Composable
fun BrawlerMetaTab(
    brawlers: List<BrawlerMeta>,
    onBrawlerClick: (BrawlerMeta) -> Unit,
    onAddBrawlerClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf("Hepsi") }

    val filteredBrawlers = remember(brawlers, searchQuery, selectedTierFilter) {
        brawlers.filter { brawler ->
            val matchesSearch = brawler.brawlerName.contains(searchQuery, ignoreCase = true) ||
                    brawler.brawlerRole.contains(searchQuery, ignoreCase = true)
            val matchesTier = selectedTierFilter == "Hepsi" || brawler.tier == selectedTierFilter
            matchesSearch && matchesTier
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Karakter veya rol ara...", color = ArenaColors.TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ArenaColors.TextMuted) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("brawler_search_bar"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ArenaColors.SurfaceDark,
                    unfocusedContainerColor = ArenaColors.SurfaceDark,
                    focusedBorderColor = ArenaColors.AccCyan,
                    unfocusedBorderColor = ArenaColors.SurfaceMedium,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Add Custom Character Button
            IconButton(
                onClick = onAddBrawlerClick,
                modifier = Modifier
                    .background(ArenaColors.SurfaceDark, RoundedCornerShape(12.dp))
                    .size(48.dp)
                    .testTag("add_custom_brawler_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Yeni Karakter",
                    tint = ArenaColors.AccGold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tier Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Hepsi", "S", "A", "B").forEach { tier ->
                val isSelected = selectedTierFilter == tier
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTierFilter = tier },
                    label = { Text(if (tier == "Hepsi") "Tüm Meta" else "$tier Tier") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when(tier) {
                            "S" -> ArenaColors.AccGold
                            "A" -> ArenaColors.AccCrimson
                            "B" -> ArenaColors.AccPurple
                            else -> ArenaColors.AccCyan
                        },
                        selectedLabelColor = Color.Black,
                        containerColor = ArenaColors.SurfaceDark,
                        labelColor = ArenaColors.TextMuted
                    ),
                    modifier = Modifier.testTag("tier_chip_$tier")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredBrawlers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aranan karakter veya meta bulunamadı.",
                    color = ArenaColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("brawler_list")
            ) {
                items(filteredBrawlers) { brawler ->
                    BrawlerItemCard(
                        brawler = brawler,
                        onClick = { onBrawlerClick(brawler) }
                    )
                }
            }
        }
    }
}

@Composable
fun BrawlerItemCard(
    brawler: BrawlerMeta,
    onClick: () -> Unit
) {
    val tierColor = when (brawler.tier.uppercase()) {
        "S" -> ArenaColors.AccGold
        "A" -> ArenaColors.AccCrimson
        "B" -> ArenaColors.AccPurple
        else -> ArenaColors.AccCyan
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("brawler_card_${brawler.brawlerId}"),
        colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stylized Avatar representation
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tierColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = brawler.brawlerName.take(2).uppercase(),
                    color = tierColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = brawler.brawlerName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    // Small Tier badge
                    Surface(
                        color = tierColor.copy(alpha = 0.2f),
                        contentColor = tierColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${brawler.tier} TIER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = brawler.brawlerRole,
                    color = ArenaColors.TextMuted,
                    fontSize = 12.sp
                )
                if (brawler.userNotes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📝 Not: ${brawler.userNotes}",
                        color = ArenaColors.AccCyan,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Send, // acts as a details pointer
                contentDescription = "Details",
                tint = ArenaColors.SurfaceMedium,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


// ==========================================
// OVERLAYS / FORMS / DETAIL VIEWS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchFormDialog(
    match: EsportMatch?,
    onDismiss: () -> Unit,
    onSave: (
        teamA: String,
        teamB: String,
        dateMillis: Long,
        gameMode: String,
        stage: String,
        notes: String,
        draft: String,
        isScrim: Boolean,
        scrimDetails: String,
        scoreA: Int?,
        scoreB: Int?
    ) -> Unit
) {
    var teamA by remember { mutableStateOf(match?.teamA ?: "") }
    var teamB by remember { mutableStateOf(match?.teamB ?: "") }
    var gameMode by remember { mutableStateOf(match?.gameMode ?: "") }
    var stage by remember { mutableStateOf(match?.stage ?: "Grup Aşaması") }
    var generalNotes by remember { mutableStateOf(match?.generalNotes ?: "") }
    var draftPicksText by remember { mutableStateOf(match?.draftPicksText ?: "") }
    var isScrim by remember { mutableStateOf(match?.isScrim ?: false) }
    var scrimDetails by remember { mutableStateOf(match?.scrimDetails ?: "") }

    var scoreAText by remember { mutableStateOf(match?.scoreA?.toString() ?: "") }
    var scoreBText by remember { mutableStateOf(match?.scoreB?.toString() ?: "") }

    val tournamentStages = listOf("Grup Aşaması", "Son 16", "Çeyrek Final", "Yarı Final", "Büyük Final")
    var expandedStage by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = match?.dateMillis ?: System.currentTimeMillis()
    )

    val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val isPastDate = selectedMillis < System.currentTimeMillis()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Seç", color = ArenaColors.AccCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("İptal", color = ArenaColors.TextMuted)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = ArenaColors.SurfaceDark
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = ArenaColors.TextMuted,
                    subheadContentColor = ArenaColors.TextMuted,
                    navigationContentColor = Color.White,
                    yearContentColor = Color.White,
                    selectedYearContentColor = Color.Black,
                    selectedYearContainerColor = ArenaColors.AccCyan,
                    dayContentColor = Color.White,
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = ArenaColors.AccCyan,
                    todayContentColor = ArenaColors.AccCyan,
                    todayDateBorderColor = ArenaColors.AccCyan
                )
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (match == null) "YENİ PLANLAMA EKLE" else "ETKİNLİK DÜZENLE",
                color = if (isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        containerColor = ArenaColors.SurfaceDark,
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scrim Toggling Switch Card
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ArenaColors.SurfaceMedium, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isScrim) Icons.Default.Info else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isScrim) "Scrim (Antrenman)" else "Turnuva Maçı",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Switch(
                            checked = isScrim,
                            onCheckedChange = { isScrim = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ArenaColors.AccPurple,
                                checkedTrackColor = ArenaColors.AccPurple.copy(alpha = 0.5f),
                                uncheckedThumbColor = ArenaColors.AccCyan,
                                uncheckedTrackColor = ArenaColors.AccCyan.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                item {
                    // Team inputs
                    OutlinedTextField(
                        value = teamA,
                        onValueChange = { teamA = it },
                        label = { Text(if (isScrim) "Kendi Takımımız (Örn: Takım A)" else "1. Takım Adı", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("input_team_a"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = teamB,
                        onValueChange = { teamB = it },
                        label = { Text(if (isScrim) "Scrim Rakibi" else "2. Takım Adı (Rakip)", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("input_team_b"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // Date Picker trigger field (replaces Game Mode selection)
                item {
                    val formattedDate = remember(selectedMillis) {
                        SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(Date(selectedMillis))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = formattedDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Maç Tarihi", color = ArenaColors.TextMuted) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Tarih Seç",
                                    tint = ArenaColors.AccCyan
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.White,
                                disabledBorderColor = ArenaColors.TextMuted,
                                disabledLabelColor = ArenaColors.TextMuted
                            ),
                            enabled = false
                        )
                    }
                }

                // Conditional Score entry if match has passed
                if (isPastDate) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ArenaColors.SurfaceMedium, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🏆 Maç Skorunu Girin",
                                color = ArenaColors.AccGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = scoreAText,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() }) scoreAText = newValue
                                    },
                                    label = { Text(teamA.ifEmpty { "1. Takım" }, color = ArenaColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier.weight(1f).testTag("input_score_a"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Text(
                                    text = "-",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                OutlinedTextField(
                                    value = scoreBText,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() }) scoreBText = newValue
                                    },
                                    label = { Text(teamB.ifEmpty { "2. Takım" }, color = ArenaColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier.weight(1f).testTag("input_score_b"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }

                if (isScrim) {
                    item {
                        OutlinedTextField(
                            value = scrimDetails,
                            onValueChange = { scrimDetails = it },
                            label = { Text("Scrim Amaçları & Detayları", color = ArenaColors.AccPurple) },
                            placeholder = { Text("Örn: Roster denemeleri, counters, özel kompozisyonlar...", color = ArenaColors.TextMuted) },
                            modifier = Modifier.fillMaxWidth().testTag("input_scrim_details"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            maxLines = 4
                        )
                    }
                } else {
                    item {
                        // Stage drop-down (only for tournament matches)
                        ExposedDropdownMenuBox(
                            expanded = expandedStage,
                            onExpandedChange = { expandedStage = !expandedStage }
                        ) {
                            OutlinedTextField(
                                value = stage,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Turnuva Aşaması", color = ArenaColors.TextMuted) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStage) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("dropdown_stage"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedStage,
                                onDismissRequest = { expandedStage = false }
                            ) {
                                tournamentStages.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = {
                                            stage = s
                                            expandedStage = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Global Bans draft input
                    OutlinedTextField(
                        value = draftPicksText,
                        onValueChange = { draftPicksText = it },
                        label = { Text("Global Bans (Global Yasaklar)", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("input_draft"),
                        placeholder = { Text("Örn: Yasak: Mortis, Piper, Larry & Lawrie") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    // Notes
                    OutlinedTextField(
                        value = generalNotes,
                        onValueChange = { generalNotes = it },
                        label = { Text("Genel Taktik Notları", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("input_notes"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 3
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = ArenaColors.TextMuted)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (teamA.isNotEmpty() && teamB.isNotEmpty()) {
                        val sA = scoreAText.toIntOrNull()
                        val sB = scoreBText.toIntOrNull()
                        onSave(teamA, teamB, selectedMillis, gameMode, stage, generalNotes, draftPicksText, isScrim, scrimDetails, sA, sB)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan, contentColor = Color.Black),
                enabled = teamA.isNotEmpty() && teamB.isNotEmpty()
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun MatchDetailsOverlay(
    match: EsportMatch,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onTriggerGemini: () -> Unit,
    isGenerating: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("match_details_pane"),
        colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceDark),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (match.isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (match.isScrim) "📋 SCRIM (ANTRENMAN)" else match.stage,
                    color = if (match.isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Team matchups & Conditional Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.teamA,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                if (match.scoreA != null && match.scoreB != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "${match.scoreA} - ${match.scoreB}",
                            color = ArenaColors.AccCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "SKOR",
                            color = ArenaColors.TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                } else {
                    Text(
                        text = "VS",
                        color = ArenaColors.AccGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Text(
                    text = match.teamB,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (match.gameMode.isNotEmpty()) {
                    Surface(
                        color = ArenaColors.SurfaceMedium,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Oyun Modu", color = ArenaColors.TextMuted, fontSize = 11.sp)
                            Text(match.gameMode, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                Surface(
                    color = ArenaColors.SurfaceMedium,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Etkinlik Türü", color = ArenaColors.TextMuted, fontSize = 11.sp)
                        Text(
                            text = if (match.isScrim) "Scrim" else "Turnuva Maçı",
                            color = if (match.isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Core Scrollable Details container
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Scrim Details
                if (match.isScrim) {
                    item {
                        Text("🎯 ANTRENMAN AMACI & DETAYLARI", color = ArenaColors.AccPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = ArenaColors.SurfaceMedium,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (match.scrimDetails.isNotEmpty()) match.scrimDetails else "Detaylı scrim hedefi belirtilmemiş.",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Global Yasaklar details
                item {
                    Text("⚔️ GLOBAL YASAKLAMALAR (GLOBAL BANS)", color = ArenaColors.AccGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = ArenaColors.SurfaceMedium,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (match.draftPicksText.isNotEmpty()) match.draftPicksText else "Henüz bir global bans planlanmadı.",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // General notes
                item {
                    Text("📝 GENEL NOTLAR & STRATEJİ", color = ArenaColors.TextMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFF161928),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (match.generalNotes.isNotEmpty()) match.generalNotes else "Ekstra not eklenmemiş.",
                            color = ArenaColors.TextMain,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Gemini Strategy section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F2236))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = if (match.isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GEMINI SAVAŞ PLANI",
                                    color = if (match.isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }

                            if (match.aiTacticPlan.isNotEmpty()) {
                                TextButton(
                                    onClick = onTriggerGemini,
                                    enabled = !isGenerating,
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Yenile 🔄", color = if (match.isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isGenerating) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = if (match.isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan)
                            }
                        } else {
                            if (match.aiTacticPlan.isNotEmpty()) {
                                Text(
                                    text = match.aiTacticPlan,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Uzak Sunucu Gemini Yapay Zekası ile bu scrim veya turnuva maçına özel draft analizi yapıp zafere giden bir plan oluşturmak ister misin?",
                                        color = ArenaColors.TextMuted,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = onTriggerGemini,
                                        colors = ButtonDefaults.buttonColors(containerColor = if (match.isScrim) ArenaColors.AccPurple else ArenaColors.AccCyan, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Gemini Savaş Planı Oluştur", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(ArenaColors.SurfaceMedium))
                ) {
                    Text("Bilgileri Düzenle")
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.SurfaceMedium, contentColor = Color.White)
                ) {
                    Text("Kapat")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrawlerDetailsOverlay(
    brawler: BrawlerMeta,
    onDismiss: () -> Unit,
    onSaveMeta: (
        name: String,
        role: String,
        bestModes: String,
        counterTips: String,
        notes: String,
        tier: String
    ) -> Unit
) {
    var nameText by remember { mutableStateOf(brawler.brawlerName) }
    var roleText by remember { mutableStateOf(brawler.brawlerRole) }
    var bestModesText by remember { mutableStateOf(brawler.bestModes) }
    var counterTipsText by remember { mutableStateOf(brawler.counterTips) }
    var notesText by remember { mutableStateOf(brawler.userNotes) }
    var selectedTier by remember { mutableStateOf(brawler.tier) }

    val tiers = listOf("S", "A", "B", "C")
    val tierColor = when (selectedTier.uppercase()) {
        "S" -> ArenaColors.AccGold
        "A" -> ArenaColors.AccCrimson
        "B" -> ArenaColors.AccPurple
        else -> ArenaColors.AccCyan
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("brawler_details_pane"),
        colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceDark),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(tierColor)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = tierColor.copy(alpha = 0.2f),
                        contentColor = tierColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "$selectedTier TIER",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "METAYI KENDİN GÜNCELLE",
                        color = ArenaColors.AccGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Brawler Title Input
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Karakter Adı", color = ArenaColors.TextMuted) },
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = Color.White),
                modifier = Modifier.fillMaxWidth().testTag("brawler_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArenaColors.SurfaceMedium,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable specifications
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = roleText,
                        onValueChange = { roleText = it },
                        label = { Text("Karakter Sınıfı / Rolü", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("brawler_role_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    Text("🏆 EN İYİ OLDUĞU OYUN MODLARI", color = ArenaColors.AccGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = bestModesText,
                        onValueChange = { bestModesText = it },
                        modifier = Modifier.fillMaxWidth().testTag("brawler_best_modes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    Text("🛡️ META KARŞILAŞTIRMA VE COUTERS", color = ArenaColors.AccCrimson, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = counterTipsText,
                        onValueChange = { counterTipsText = it },
                        modifier = Modifier.fillMaxWidth().testTag("brawler_counters_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    // Interactive Meta Tier Selection
                    Text("📊 METADAKİ GÜCÜ (TIER SEÇİMİ)", color = ArenaColors.TextMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tiers.forEach { t ->
                            val isSelected = selectedTier == t
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTier = t },
                                color = if (isSelected) {
                                    when(t) {
                                        "S" -> ArenaColors.AccGold
                                        "A" -> ArenaColors.AccCrimson
                                        "B" -> ArenaColors.AccPurple
                                        else -> ArenaColors.AccCyan
                                    }
                                } else ArenaColors.SurfaceMedium,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$t Ligi",
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    // User editable notes
                    Text("📝 KİŞİSEL STRATEJİ VE NOTLARINIZ", color = ArenaColors.AccCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier.fillMaxWidth().testTag("brawler_notes_input"),
                        placeholder = { Text("Seçim stratejinizi, bu karakterle yaptığınız özel kombinasyonları not alın...", color = ArenaColors.TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ArenaColors.SurfaceMedium,
                            unfocusedContainerColor = ArenaColors.SurfaceMedium,
                            focusedBorderColor = ArenaColors.AccCyan,
                            unfocusedBorderColor = ArenaColors.SurfaceMedium,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(ArenaColors.SurfaceMedium))
                ) {
                    Text("Vazgeç")
                }

                Button(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            onSaveMeta(nameText, roleText, bestModesText, counterTipsText, notesText, selectedTier)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.AccCyan, contentColor = Color.Black),
                    enabled = nameText.isNotBlank()
                ) {
                    Text("Değişiklikleri Kaydet", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// COMPOSABLE: BRAWLER FORM DIALOG FOR NEW CHARACTERS
// ==========================================
@Composable
fun BrawlerFormDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        role: String,
        bestModes: String,
        counterTips: String,
        notes: String,
        tier: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Ateş Edici") }
    var bestModes by remember { mutableStateOf("Savaş Topu, Nakavt") }
    var counterTips by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var tier by remember { mutableStateOf("A") }

    val tiers = listOf("S", "A", "B", "C")
    val roles = listOf("Ateş Edici", "Suikastçı", "Tank", "Destek", "Savaşçı", "Kontrol")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "YENİ METAYA KARAKTER EKLE",
                color = ArenaColors.AccGold,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        containerColor = ArenaColors.SurfaceDark,
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Karakter Adı", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("new_brawler_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Sınıf / Rol (Örn: Suikastçı, Tank)", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("new_brawler_role_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = bestModes,
                        onValueChange = { bestModes = it },
                        label = { Text("En İyi Olduğu Oyun Modları", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("new_brawler_modes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = counterTips,
                        onValueChange = { counterTips = it },
                        label = { Text("Meta Karşılaştırmaları (Counters)", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("new_brawler_counters_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    Text("Metadaki Gücü (Tier)", color = ArenaColors.TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tiers.forEach { t ->
                            val isSelected = tier == t
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tier = t },
                                color = if (isSelected) ArenaColors.AccGold else ArenaColors.SurfaceMedium,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = t,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Özel Taktiksel Notlar", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("new_brawler_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = ArenaColors.TextMuted)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, role, bestModes, counterTips, notes, tier)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.AccGold, contentColor = Color.Black),
                enabled = name.isNotBlank()
            ) {
                Text("Ekle", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ==========================================
// TAB 3: MATCHERINOS (COMMUNITY CROWDFUNDED EVENTS)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatcherinosTab(
    matcherinos: List<com.example.data.database.Matcherino>,
    onAddMatcherino: (title: String, prizePool: String, url: String, status: String, notes: String, gameMode: String) -> Unit,
    onUpdateMatcherino: (id: Int, title: String, prizePool: String, url: String, status: String, notes: String, gameMode: String) -> Unit,
    onDeleteMatcherino: (com.example.data.database.Matcherino) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var editingEntity by remember { mutableStateOf<com.example.data.database.Matcherino?>(null) }

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Matcherinolar Sistemi",
                        color = ArenaColors.AccCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Topluluk Destekli Turnuvalar",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ek kupon kodları girerek ödül havuzlarını büyütebileceğiniz resmi platform aktiviteleri.",
                        color = ArenaColors.TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Add Matcherino Action Button
        Button(
            onClick = {
                editingEntity = null
                showForm = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("add_matcherino_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.AccCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Yeni Matcherino Turnuvası Ekle", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (matcherinos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Kayıtlı Matcherino Bulunmuyor.",
                    color = ArenaColors.TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matcherinos) { m ->
                    val statusColor = when (m.status) {
                        "Sürüyor" -> ArenaColors.AccCyan
                        "Yakında" -> ArenaColors.AccGold
                        else -> ArenaColors.TextMuted
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("matcherino_item_${m.id}"),
                        colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceMedium),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = statusColor.copy(alpha = 0.15f),
                                    contentColor = statusColor,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = m.status.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            editingEntity = m
                                            showForm = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = ArenaColors.TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteMatcherino(m) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = ArenaColors.AccCrimson,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = m.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🏆 Ödül Havuzu: ${m.prizePool}",
                                    color = ArenaColors.AccGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🎮 Mod: ${m.gameMode}",
                                    color = ArenaColors.TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            if (m.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = m.notes,
                                    color = ArenaColors.TextMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ArenaColors.SurfaceDark, RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                )
                            }

                            if (m.url.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        try {
                                            uriHandler.openUri(m.url)
                                        } catch (e: Exception) {
                                            // Handle invalid url safely
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.SurfaceDark, contentColor = ArenaColors.AccCyan),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Turnuva Sayfasına Git", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Update Popup Dialog Dialog Form
    if (showForm) {
        var titleText by remember { mutableStateOf(editingEntity?.title ?: "") }
        var prizeText by remember { mutableStateOf(editingEntity?.prizePool ?: "") }
        var urlText by remember { mutableStateOf(editingEntity?.url ?: "") }
        var statusText by remember { mutableStateOf(editingEntity?.status ?: "Sürüyor") }
        var notesText by remember { mutableStateOf(editingEntity?.notes ?: "") }
        var modeText by remember { mutableStateOf(editingEntity?.gameMode ?: "Savaş Topu") }

        val statuses = listOf("Sürüyor", "Yakında", "Tamamlandı")

        androidx.compose.ui.window.Dialog(onDismissRequest = { showForm = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("matcherino_form_dialog"),
                colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceMedium),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = if (editingEntity == null) "Yeni Turnuva Ekle" else "Turnuvayı Düzenle",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Turnuva Adı", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("m_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = prizeText,
                        onValueChange = { prizeText = it },
                        label = { Text("Ödül Havuzu ($ USD)", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("m_prize_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = modeText,
                        onValueChange = { modeText = it },
                        label = { Text("Oyun Modu", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("m_mode_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text("Matcherino URL", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("m_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Turnuva Notları / Kupon Kodları", color = ArenaColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("m_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Durum", color = ArenaColors.TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statuses.forEach { s ->
                            val isSel = statusText == s
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { statusText = s },
                                color = if (isSel) ArenaColors.AccCyan else ArenaColors.SurfaceDark,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = s,
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showForm = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.SurfaceDark, contentColor = Color.White),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Vazgeç")
                        }

                        Button(
                            onClick = {
                                if (titleText.isNotBlank()) {
                                    val currentEditing = editingEntity
                                    if (currentEditing == null) {
                                        onAddMatcherino(titleText, prizeText, urlText, statusText, notesText, modeText)
                                    } else {
                                        onUpdateMatcherino(currentEditing.id, titleText, prizeText, urlText, statusText, notesText, modeText)
                                    }
                                    showForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.AccCyan, contentColor = Color.Black),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Kaydet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrawlAICoachTab(
    viewModel: com.example.ui.viewmodel.BrawlArenaViewModel
) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isSending by viewModel.isSendingChatMessage.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    // Auto-scroll to bottom of chat when new message arrives
    LaunchedEffect(chatMessages.size, isSending) {
        if (chatMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }
    
    val quickPrompts = listOf(
        "🏆 Meta Analizi" to "Bana şu anki Brawl Stars e-spor meta karakterlerini ve S-tier brawlerları analiz eder misin?",
        "⚔️ Sinerjiler" to "Savaş Topu haritalarında en iyi brawler sinerjileri ve pasif yetenek kombinasyonları hangileridir?",
        "🛡️ Karşı Seçim" to "Meta karakterlerden Cordelius ve Fang'e karşı durabilecek güçlü counter-pick brawlerları listeler misin?",
        "💡 Taktikler" to "E-spor düzeyinde haritada alan kontrolü (laning) kurmak ve baskı yapmak için kilit tüyolar nelerdir?"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Brawl AI Başantrenörü",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Gemini Destekli Profesyonel E-spor Analisti",
                    color = ArenaColors.TextMuted,
                    fontSize = 12.sp
                )
            }
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("button_clear_chat")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Temizle",
                    tint = ArenaColors.AccCrimson
                )
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // Chat History Scroll Areas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ArenaColors.SurfaceDark, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            if (chatMessages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz mesaj yok. Bir soru sorarak başlayın!",
                        color = ArenaColors.TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(chatMessages) { message ->
                        val isCoach = message.sender == com.example.ui.viewmodel.MessageSender.COACH
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = if (isCoach) Alignment.Start else Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = if (isCoach) Arrangement.Start else Arrangement.End,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                if (isCoach) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = "Coach Avatar",
                                        tint = ArenaColors.AccGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "E-Spor Koçu 🤖",
                                        color = ArenaColors.AccGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text(
                                        text = "Kullanıcı (Sen)",
                                        color = ArenaColors.AccCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Avatar",
                                        tint = ArenaColors.AccCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            
                            Surface(
                                color = if (isCoach) ArenaColors.SurfaceMedium else Color(0xFF0F2633),
                                shape = RoundedCornerShape(
                                    topStart = if (isCoach) 2.dp else 12.dp,
                                    topEnd = if (isCoach) 12.dp else 2.dp,
                                    bottomStart = 12.dp,
                                    bottomEnd = 12.dp
                                ),
                                border = if (isCoach) null else androidx.compose.foundation.BorderStroke(1.dp, ArenaColors.AccCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = message.text,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                    
                    if (isSending) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = ArenaColors.AccGold,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Başantrenör stratejiyi hazırlıyor...",
                                    color = ArenaColors.TextMuted,
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Quick Action Template Row
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts.size) { index ->
                val (title, prompt) = quickPrompts[index]
                Surface(
                    color = ArenaColors.SurfaceMedium,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArenaColors.AccGold.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable(enabled = !isSending) {
                        viewModel.sendChatMessage(prompt)
                    }
                ) {
                    Text(
                        text = title,
                        color = ArenaColors.AccGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Custom Text Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Koça bir şey sor...", color = ArenaColors.TextMuted, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ArenaColors.AccCyan,
                    unfocusedBorderColor = ArenaColors.SurfaceMedium
                ),
                maxLines = 3,
                singleLine = false,
                enabled = !isSending
            )
            
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .background(ArenaColors.AccCyan, RoundedCornerShape(12.dp))
                    .size(48.dp)
                    .testTag("chat_send_button"),
                enabled = !isSending && inputText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Gönder",
                    tint = Color.Black
                )
            }
        }
    }
}
