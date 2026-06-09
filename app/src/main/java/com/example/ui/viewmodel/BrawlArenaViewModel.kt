package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.database.BrawlerMeta
import com.example.data.database.EsportMatch
import com.example.data.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BrawlArenaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MatchRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MatchRepository(database.matchDao(), database.brawlerDao(), database.matcherinoDao())
        
        // Populate standard brawlers & matcherinos if empty
        viewModelScope.launch {
            repository.prepopulateDefautBrawlersIfEmpty()
            repository.prepopulateMatcherinosIfEmpty()
        }
    }

    val matches: StateFlow<List<EsportMatch>> = repository.allMatches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val brawlers: StateFlow<List<BrawlerMeta>> = repository.allBrawlers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val matcherinos: StateFlow<List<com.example.data.database.Matcherino>> = repository.allMatcherinos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current operating states
    private val _selectedMatch = MutableStateFlow<EsportMatch?>(null)
    val selectedMatch: StateFlow<EsportMatch?> = _selectedMatch.asStateFlow()

    private val _selectedBrawler = MutableStateFlow<BrawlerMeta?>(null)
    val selectedBrawler: StateFlow<BrawlerMeta?> = _selectedBrawler.asStateFlow()

    private val _isGeneratingTactic = MutableStateFlow(false)
    val isGeneratingTactic: StateFlow<Boolean> = _isGeneratingTactic.asStateFlow()

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    fun selectMatch(match: EsportMatch?) {
        _selectedMatch.value = match
    }

    fun selectBrawler(brawler: BrawlerMeta?) {
        _selectedBrawler.value = brawler
    }

    fun clearStatus() {
        _operationStatus.value = null
    }

    // --- Database Operations ---

    fun addMatch(
        teamA: String,
        teamB: String,
        dateMillis: Long,
        gameMode: String,
        mapName: String = "",
        stage: String,
        generalNotes: String,
        draftPicksText: String,
        isScrim: Boolean = false,
        scrimDetails: String = "",
        scoreA: Int? = null,
        scoreB: Int? = null
    ) {
        viewModelScope.launch {
            val newMatch = EsportMatch(
                teamA = teamA.trim(),
                teamB = teamB.trim(),
                dateMillis = dateMillis,
                gameMode = gameMode,
                mapName = mapName.trim(),
                stage = stage,
                generalNotes = generalNotes.trim(),
                draftPicksText = draftPicksText.trim(),
                isScrim = isScrim,
                scrimDetails = scrimDetails.trim(),
                scoreA = scoreA,
                scoreB = scoreB
            )
            repository.insertMatch(newMatch)
            _operationStatus.value = if (isScrim) "Scrim başarıyla takvime eklendi!" else "Maç başarıyla takvime eklendi!"
        }
    }

    fun updateMatch(
        id: Int,
        teamA: String,
        teamB: String,
        dateMillis: Long,
        gameMode: String,
        mapName: String = "",
        stage: String,
        generalNotes: String,
        draftPicksText: String,
        aiTacticPlan: String,
        isScrim: Boolean = false,
        scrimDetails: String = "",
        scoreA: Int? = null,
        scoreB: Int? = null
    ) {
        viewModelScope.launch {
            val updated = EsportMatch(
                id = id,
                teamA = teamA.trim(),
                teamB = teamB.trim(),
                dateMillis = dateMillis,
                gameMode = gameMode,
                mapName = mapName.trim(),
                stage = stage,
                generalNotes = generalNotes.trim(),
                draftPicksText = draftPicksText.trim(),
                aiTacticPlan = aiTacticPlan,
                isScrim = isScrim,
                scrimDetails = scrimDetails.trim(),
                scoreA = scoreA,
                scoreB = scoreB
            )
            repository.updateMatch(updated)
            // Sync with current selection
            if (_selectedMatch.value?.id == id) {
                _selectedMatch.value = updated
            }
            _operationStatus.value = if (isScrim) "Scrim başarıyla güncellendi!" else "Maç başarıyla güncellendi!"
        }
    }

    fun deleteMatch(match: EsportMatch) {
        viewModelScope.launch {
            repository.deleteMatch(match)
            if (_selectedMatch.value?.id == match.id) {
                _selectedMatch.value = null
            }
            _operationStatus.value = "Maç/Scrim takvimden silindi."
        }
    }

    fun updateBrawlerNotes(
        brawlerId: String,
        name: String,
        role: String,
        bestModes: String,
        counterTips: String,
        userNotes: String,
        tier: String
    ) {
        viewModelScope.launch {
            val updatedBrawler = BrawlerMeta(
                brawlerId = brawlerId,
                brawlerName = name,
                brawlerRole = role,
                tier = tier,
                bestModes = bestModes,
                counterTips = counterTips.trim(),
                userNotes = userNotes.trim()
            )
            repository.insertBrawler(updatedBrawler) // Upsert so it works for all changes safely
            if (_selectedBrawler.value?.brawlerId == brawlerId) {
                _selectedBrawler.value = updatedBrawler
            }
            _operationStatus.value = "$name meta bilgileri güncellendi!"
        }
    }

    fun addCustomBrawler(
        name: String,
        role: String,
        bestModes: String,
        counterTips: String,
        userNotes: String,
        tier: String
    ) {
        viewModelScope.launch {
            val cleanId = name.trim().lowercase()
                .replace(" ", "_")
                .replace("[^a-z0-9_]".toRegex(), "")
            
            if (cleanId.isEmpty()) {
                _operationStatus.value = "Hata: Geçersiz savaşçı adı!"
                return@launch
            }

            val newBrawler = BrawlerMeta(
                brawlerId = cleanId,
                brawlerName = name.trim(),
                brawlerRole = role.trim(),
                tier = tier,
                bestModes = bestModes.trim(),
                counterTips = counterTips.trim(),
                userNotes = userNotes.trim()
            )
            repository.insertBrawler(newBrawler)
            _operationStatus.value = "$name metaya eklendi!"
        }
    }

    // --- Matcherino Operations ---

    fun addMatcherino(
        title: String,
        prizePool: String,
        url: String,
        status: String,
        notes: String,
        gameMode: String
    ) {
        viewModelScope.launch {
            val newM = com.example.data.database.Matcherino(
                title = title.trim(),
                prizePool = prizePool.trim(),
                url = url.trim(),
                status = status,
                notes = notes.trim(),
                gameMode = gameMode.trim()
            )
            repository.insertMatcherino(newM)
            _operationStatus.value = "Matcherino turnuvası başarıyla eklendi!"
        }
    }

    fun updateMatcherino(
        id: Int,
        title: String,
        prizePool: String,
        url: String,
        status: String,
        notes: String,
        gameMode: String
    ) {
        viewModelScope.launch {
            val updated = com.example.data.database.Matcherino(
                id = id,
                title = title.trim(),
                prizePool = prizePool.trim(),
                url = url.trim(),
                status = status,
                notes = notes.trim(),
                gameMode = gameMode.trim()
            )
            repository.updateMatcherino(updated)
            _operationStatus.value = "Matcherino turnuvası güncellendi!"
        }
    }

    fun deleteMatcherino(matcherino: com.example.data.database.Matcherino) {
        viewModelScope.launch {
            repository.deleteMatcherino(matcherino)
            _operationStatus.value = "Matcherino turnuvası silindi."
        }
    }


    // --- Gemini AI Actions ---

    fun generateAITacticPlay(match: EsportMatch) {
        viewModelScope.launch {
            _isGeneratingTactic.value = true
            _operationStatus.value = "Gemini taktik analizi hazırlıyor..."
            
            val analysisResult = GeminiClient.analyzeEsportTactics(
                teamA = match.teamA,
                teamB = match.teamB,
                gameMode = match.gameMode,
                mapName = match.mapName,
                extraNotes = "Roster/Notlar: ${match.generalNotes}\nDraft: ${match.draftPicksText}"
            )
            
            // Save result back into match
            val updatedMatch = match.copy(aiTacticPlan = analysisResult)
            repository.updateMatch(updatedMatch)
            
            // Update selected match details if opened
            if (_selectedMatch.value?.id == match.id) {
                _selectedMatch.value = updatedMatch
            }
            
            _isGeneratingTactic.value = false
            _operationStatus.value = "Gemini Savaş Planı Analizi Başarıyla Hazırlandı!"
        }
    }

    // --- Gemini AI Chat ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.COACH,
                text = "Merhaba asker! Ben Brawl Arena e-spor başantrenörün. 🎮🏆\n\nHangi haritada, hangi modda nasıl bir draft kurman gerektiğini veya meta brawlerları sormak istersen buradayım. Bana dilediğin soruyu sorabilir veya aşağıdaki hazır şablon butonları kullanabilirsin!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isSendingChatMessage = MutableStateFlow(false)
    val isSendingChatMessage: StateFlow<Boolean> = _isSendingChatMessage.asStateFlow()

    fun sendChatMessage(promptText: String) {
        if (promptText.isBlank()) return
        
        viewModelScope.launch {
            // 1. Add user message
            val userMsg = ChatMessage(sender = MessageSender.USER, text = promptText)
            val currentList = _chatMessages.value.toMutableList()
            currentList.add(userMsg)
            _chatMessages.value = currentList
            
            _isSendingChatMessage.value = true
            
            // 2. Build contextual chat history for Gemini
            val promptBuilder = StringBuilder()
            promptBuilder.append("Aşağıda kullanıcının seninle olan sohbet geçmişi ve yeni sorusu yer alıyor. Lütfen geçmişi dikkate alarak son derece profesyonel bir Brawl Stars koçu olarak yanıtla:\n\n")
            
            // We take only the last 16 messages to keep context short and clear
            val relevantHistory = currentList.takeLast(16)
            relevantHistory.forEach { msg ->
                val senderPrefix = if (msg.sender == MessageSender.USER) "Kullanıcı" else "Koç"
                promptBuilder.append("$senderPrefix: ${msg.text}\n")
            }
            promptBuilder.append("\nKoç:")
            
            // 3. Call api
            val reply = GeminiClient.chatWithCoach(promptBuilder.toString())
            
            // 4. Add reply message
            val coachMsg = ChatMessage(sender = MessageSender.COACH, text = reply)
            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(coachMsg)
            _chatMessages.value = updatedList
            
            _isSendingChatMessage.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.COACH,
                text = "Tüm sohbet geçmişi temizlendi! Yeni bir strateji planı yapmaya hazırız. Bana dilediğini sorabilirsin!"
            )
        )
    }
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    COACH
}

class BrawlArenaViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrawlArenaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BrawlArenaViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
