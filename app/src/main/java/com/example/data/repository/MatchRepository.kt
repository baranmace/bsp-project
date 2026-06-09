package com.example.data.repository

import com.example.data.database.BrawlerDao
import com.example.data.database.BrawlerMeta
import com.example.data.database.EsportMatch
import com.example.data.database.MatchDao
import kotlinx.coroutines.flow.Flow

import com.example.data.database.Matcherino
import com.example.data.database.MatcherinoDao
import kotlinx.coroutines.flow.first

class MatchRepository(
    private val matchDao: MatchDao,
    private val brawlerDao: BrawlerDao,
    private val matcherinoDao: MatcherinoDao
) {
    val allMatches: Flow<List<EsportMatch>> = matchDao.getAllMatches()
    val allBrawlers: Flow<List<BrawlerMeta>> = brawlerDao.getAllBrawlers()
    val allMatcherinos: Flow<List<Matcherino>> = matcherinoDao.getAllMatcherinos()

    suspend fun getMatchById(id: Int): EsportMatch? = matchDao.getMatchById(id)

    suspend fun insertMatch(match: EsportMatch): Long = matchDao.insertMatch(match)

    suspend fun updateMatch(match: EsportMatch) = matchDao.updateMatch(match)

    suspend fun deleteMatch(match: EsportMatch) = matchDao.deleteMatch(match)

    suspend fun updateBrawler(brawler: BrawlerMeta) = brawlerDao.updateBrawler(brawler)

    suspend fun insertBrawler(brawler: BrawlerMeta) = brawlerDao.insertBrawler(brawler)

    suspend fun insertMatcherino(matcherino: Matcherino): Long = matcherinoDao.insertMatcherino(matcherino)

    suspend fun updateMatcherino(matcherino: Matcherino) = matcherinoDao.updateMatcherino(matcherino)

    suspend fun deleteMatcherino(matcherino: Matcherino) = matcherinoDao.deleteMatcherino(matcherino)

    suspend fun prepopulateMatcherinosIfEmpty() {
        if (allMatcherinos.first().isEmpty()) {
            val defaultMatcherinos = listOf(
                Matcherino(
                    title = "Brawl Stars Championship Monthly Finals",
                    prizePool = "$50,000 USD",
                    url = "https://matcherino.com/t/bwc-monthly-finals",
                    status = "Sürüyor",
                    notes = "Kod girişi yaparak ücretsiz katkıda bulunun! Matcherino kodunu turnuva günü canlı yayından alabilirsiniz.",
                    gameMode = "Savaş Topu & Nakavt"
                ),
                Matcherino(
                    title = "Queso Cup EMEA Season 5",
                    prizePool = "$15,000 USD",
                    url = "https://matcherino.com/t/queso-cup-emea",
                    status = "Sürüyor",
                    notes = "EMEA bölgesinin en prestijli topluluk destekli turnuvası. Sponsorlu ödül havuzu aktiftir.",
                    gameMode = "Elmas Kapmaca & Soygun"
                ),
                Matcherino(
                    title = "SPS Masters Season 4 Qualifiers",
                    prizePool = "$25,000 USD",
                    url = "https://matcherino.com/t/sps-masters-s4",
                    status = "Yakında",
                    notes = "Gelecek hafta başlayacak espor elemeleri. Matcherino üzerinden destek olabilirsiniz.",
                    gameMode = "Sıcak Bölge & Savaş Topu"
                ),
                Matcherino(
                    title = "Brawl Stars Community Cup #42",
                    prizePool = "$2,500 USD",
                    url = "https://matcherino.com/t/comm-cup-42",
                    status = "Tamamlandı",
                    notes = "Tüm topluluğa açık kupa. Ödül ödemeleri Matcherino cüzdanlarına aktarılmıştır.",
                    gameMode = "Nakavt"
                )
            )
            for (m in defaultMatcherinos) {
                matcherinoDao.insertMatcherino(m)
            }
        }
    }

    suspend fun prepopulateDefautBrawlersIfEmpty() {
        val currentCount = brawlerDao.getBrawlerCount()
        if (currentCount < 103) {
            val list = mutableListOf<BrawlerMeta>()
            
            // Add real brawlers
            val realBrawlers = listOf(
                Pair("moe", "Moe"), Pair("clancy", "Clancy"), Pair("kenji", "Kenji"),
                Pair("piper", "Piper"), Pair("colette", "Colette"), Pair("colt", "Colt"),
                Pair("spike", "Spike"), Pair("mortis", "Mortis"), Pair("edgar", "Edgar"),
                Pair("crow", "Crow"), Pair("leon", "Leon"), Pair("dynamike", "Dynamike"),
                Pair("byron", "Byron"), Pair("shelly", "Shelly"), Pair("el_primo", "El Primo"),
                Pair("nita", "Nita"), Pair("bull", "Bull"), Pair("brock", "Brock"),
                Pair("barley", "Barley"), Pair("poco", "Poco"), Pair("rosa", "Rosa"),
                Pair("jessie", "Jessie"), Pair("tick", "Tick"), Pair("eight_bit", "8-Bit"),
                Pair("rico", "Rico"), Pair("darryl", "Darryl"), Pair("penny", "Penny"),
                Pair("carl", "Carl"), Pair("jacky", "Jacky"), Pair("gus", "Gus"),
                Pair("bo", "Bo"), Pair("emz", "Emz"), Pair("stu", "Stu"),
                Pair("pam", "Pam"), Pair("frank", "Frank"), Pair("bibi", "Bibi"),
                Pair("bea", "Bea"), Pair("nani", "Nani"), Pair("griff", "Griff"),
                Pair("grom", "Grom"), Pair("bonnie", "Bonnie"), Pair("gale", "Gale"),
                Pair("lola", "Lola"), Pair("ash", "Ash"), Pair("belle", "Belle"),
                Pair("buzz", "Buzz"), Pair("fang", "Fang"), Pair("eve", "Eve"),
                Pair("janet", "Janet"), Pair("otis", "Otis"), Pair("sam", "Sam"),
                Pair("buster", "Buster"), Pair("chester", "Chester"), Pair("gray", "Gray"),
                Pair("mandy", "Mandy"), Pair("rt", "R-T"), Pair("willow", "Willow"),
                Pair("maisie", "Maisie"), Pair("hank", "Hank"), Pair("cordelius", "Cordelius"),
                Pair("doug", "Doug"), Pair("pearl", "Pearl"), Pair("chuck", "Chuck"),
                Pair("charlie", "Charlie"), Pair("mico", "Mico"), Pair("kit", "Kit"),
                Pair("larry_lawrie", "Larry & Lawrie"), Pair("melodie", "Melodie"),
                Pair("angelo", "Angelo"), Pair("draco", "Draco"), Pair("lily", "Lily"),
                Pair("berry", "Berry"), Pair("juju", "Juju"), Pair("shade", "Shade"),
                Pair("meg", "Meg"), Pair("sandy", "Sandy"), Pair("amber", "Amber"),
                Pair("lou", "Lou"), Pair("gene", "Gene"), Pair("max", "Max"),
                Pair("mr_p", "Mr. P"), Pair("sprout", "Sprout"), Pair("squeak", "Squeak"),
                Pair("tara", "Tara"), Pair("ruffs", "Ruffs"), Pair("surge", "Surge")
            )

            // Let's add them
            realBrawlers.forEachIndexed { index, b ->
                val role = when (b.first) {
                    "piper", "colt", "brock", "eight_bit", "rico", "bea", "nani", "belle", "mandy", "maisie", "angelo" -> "Keskin Nişancı"
                    "moe", "clancy", "shelly", "griff", "lola", "eve", "janet", "rt", "pearl", "surge" -> "Hasar Verici"
                    "kenji", "mortis", "edgar", "crow", "leon", "stu", "buzz", "fang", "sam", "cordelius", "mico", "shade" -> "Suikastçı"
                    "el_primo", "bull", "rosa", "darryl", "jacky", "frank", "ash", "buster", "hank", "doug", "draco", "meg" -> "Tank"
                    "byron", "poco", "gus", "pam", "gray", "kit", "berry", "gene", "max", "ruffs" -> "Destek"
                    "spike", "nita", "jessie", "bo", "emz", "gale", "otis", "chester", "willow", "charlie", "juju", "sandy", "amber", "lou", "mr_p", "squeak", "tara" -> "Kontrolcü"
                    "dynamike", "barley", "tick", "penny", "carl", "grom", "bonnie", "chuck", "larry_lawrie", "sprout" -> "Topçu / Fırlatıcı"
                    else -> "Savaşçı"
                }

                val tier = when (index % 4) {
                    0 -> "S"
                    1 -> "A"
                    2 -> "B"
                    else -> "C"
                }

                val counterTips = when (role) {
                    "Keskin Nişancı" -> "Suikastçılar (Edgar, Leon, Kenji) ve duvar arkasından yaklaşan fırlatıcılar."
                    "Suikastçı" -> "Yakın dövüşçüler (Shelly, Bull, El Primo) ve sersemletme yeteneğine sahip alan kontrolcüleri."
                    "Tank" -> "Tank katilleri (Colette, Gale, Otis) ve can yüzdesine göre possesses hasar vuran keskin nişancılar."
                    "Destek" -> "Yüksek anlık hasar çıkaran suikastçılar ve tüm takımı tek seferde tehdit eden alan hasarı fırlatıcıları."
                    "Kontrolcü" -> "Uzak menzilliler ve kontrol alanlarının dışından hasar vuran keskin nişancılar."
                    "Topçu / Fırlatıcı" -> "Hızlı hareket edebilen veya üstlerine doğrudan atlayabilen suikastçılar (Mortis, Edgar, Mico)."
                    else -> "Menzil avantaji olan keskin nişancılar ve yoğun baskı kuran suikastçılar."
                }

                val bestModes = when (role) {
                    "Keskin Nişancı" -> "Ödül Avı, Nakavt, Elmas Kapmaca"
                    "Suikastçı" -> "Nakavt, Savaş Topu, Hesaplaşma"
                    "Tank" -> "Savaş Topu, Sıcak Bölge, Soygun"
                    "Destek" -> "Savaş Topu, Elmas Kapmaca, Nakavt"
                    else -> "Savaş Topu, Sıcak Bölge, Elmas Kapmaca"
                }

                list.add(
                    BrawlerMeta(
                        brawlerId = b.first,
                        brawlerName = b.second,
                        brawlerRole = role,
                        tier = tier,
                        bestModes = bestModes,
                        counterTips = counterTips,
                        userNotes = "Bu brawler için strateji detaylarınızı girin."
                    )
                )
            }

            // Fill up to 103 items with custom brawlers
            var placeholderIndex = list.size + 1
            while (list.size < 103) {
                list.add(
                    BrawlerMeta(
                        brawlerId = "ozel_brawler_$placeholderIndex",
                        brawlerName = "Özel Savaşçı $placeholderIndex",
                        brawlerRole = "Gizemli Savaşçı",
                        tier = "B",
                        bestModes = "Herhangi Bir Mod",
                        counterTips = "Farklı taktikler ve hızlı counters deneyerek zayıf yönlerini keşfedin.",
                        userNotes = "Kendi taktik, anti-brawler ve özel brawler kombinasyon notlarınızı buraya yazabilirsiniz!"
                    )
                )
                placeholderIndex++
            }

            brawlerDao.insertBrawlers(list)
        }
    }
}
