package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Moshi Serialized Data Models for Gemini ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Retrofit Client & Manager ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Sends a custom prompt for Esport Draft and Tactical Planning to Gemini.
     */
    suspend fun analyzeEsportTactics(
        teamA: String,
        teamB: String,
        gameMode: String,
        mapName: String,
        extraNotes: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Hata: Gemini API Anahtarı bulunamadı! Lütfen AI Studio Secrets panelinden 'GEMINI_API_KEY' anahtarını ayarlayın."
        }

        val systemPrompt = """
            Sen Brawl Stars e-spor koçu ve analistisin. Kullanıcının verdiği e-spor maçı bilgilerine (Takım adları, Oyun modu, Harita ismi, varsa özel notlar ve brawler taslakları) göre bir Taktik Analizi ve Savaş Planı ("Savaş Planı") oluşturmalısın.
            
            Aşağıdaki yapıya sadık kalarak, Türkçe dilinde son derece net, heyecanlı ve e-spor jargonuna uygun bir analiz yap:
            1. ⚔️ DRAFT ÖNERİLERİ: Bu harita ve mod için (S, A, B tier brawlerlardan) seçilmesi gereken en iyi brawler sinerjileri ve yasaklanması (ban) gereken kritik karakterler.
            2. 🛡️ TAKTİK & KORİDOR KONTROLÜ (Laning): Hangi brawlerin hangi lasede (orta, sol, sağ) kalması gerektiği, alan kontrolü taktikleri.
            3. 🔥 STRATEJİ & MAÇ PLANI: Bu oyun modunda ve haritada zafere ulaşmak için kilit oyun aşamaları (örn: baskı yapma, can yenilemeyi geciktirme, kasayı eritmeye ne zaman gidileceği vb.).
            
            Kısa, net ve doğrudan okunabilir maddeler halinde yaz, gereksiz uzun cümlelerden kaçın.
        """.trimIndent()

        val prompt = """
            Maç Detayları:
            Takımlar: $teamA vs $teamB
            Oyun Modu: $gameMode
            Harita: $mapName
            Kullanıcı Notları ve Draft Durumu:
            ${if (extraNotes.isNotEmpty()) extraNotes else "Özel bir not belirtilmedi."}
            
            Lütfen bu detaylara göre stratejimi oluştur.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            )
        )

        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Analiz oluşturulamadı. Yanıt boş döndü."
        } catch (e: Exception) {
            "Bağlantı Hatası: ${e.localizedMessage ?: "Gemini API isteği başarısız oldu."}"
        }
    }

    /**
     * General chat with Brawl Stars Esports AI Advisor.
     */
    suspend fun chatWithCoach(
        fullPrompt: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Hata: Gemini API Anahtarı bulunamadı! Lütfen AI Studio Secrets panelinden 'GEMINI_API_KEY' anahtarını ayarlayın."
        }

        val systemPrompt = """
            Sen Brawl Stars e-spor arenasına yıllarını vermiş duayen bir e-spor başantrenörüsün (Head Coach). Oyun mekanikleri, brawler sinerjileri, karşı koyma (counter-picks) dinamikleri, harita geometrisi, oyun modlarının temelleri ve takım kompozisyonları (draft) konularında mükemmel ve derinlemesine bilgiye sahipsin.
            
            Kullanıcılara e-spor düzeyinde tavsiyeler ver. Cevapların her zaman motive edici, oyun jargonuna aşina (aşırı resmi olmayan ancak profesyonel) ve kilit maddeler içerecek şekilde düzenlenmiş olsun.
            
            Cevaplarını her zaman anlaşılır Türkçe ile formüle et ve e-sporcuların (örneğin "laning", "tier list", "banning", "drafting", "utility", "gadget/star power" vb.) sıkça kullandığı kavramları yerinde kullan.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = fullPrompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            )
        )

        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Cevap üretilemedi. Yanıt boş döndü."
        } catch (e: Exception) {
            "Bağlantı Hatası: ${e.localizedMessage ?: "Gemini API isteği başarısız oldu."}"
        }
    }
}
