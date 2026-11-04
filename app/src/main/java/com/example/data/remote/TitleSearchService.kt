package com.example.data.remote

import com.example.data.model.Category
import com.example.data.model.TitleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class TitleSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun searchTitles(query: String, category: Category?): List<TitleSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val encoded = try {
            URLEncoder.encode(trimmed, "UTF-8")
        } catch (_: Exception) {
            trimmed
        }

        when (category) {
            Category.ANIME -> searchAnime(encoded)
            Category.SERIE -> searchSeries(encoded)
            Category.FILME -> searchMovies(encoded)
            null -> {
                val animeDeferred = async { searchAnime(encoded) }
                val seriesDeferred = async { searchSeries(encoded) }
                val moviesDeferred = async { searchMovies(encoded) }

                val animeResults = try { animeDeferred.await() } catch (_: Exception) { emptyList() }
                val seriesResults = try { seriesDeferred.await() } catch (_: Exception) { emptyList() }
                val movieResults = try { moviesDeferred.await() } catch (_: Exception) { emptyList() }

                val combined = mutableListOf<TitleSearchResult>()
                val maxLen = maxOf(animeResults.size, seriesResults.size, movieResults.size)
                for (i in 0 until maxLen) {
                    if (i < movieResults.size) combined.add(movieResults[i])
                    if (i < seriesResults.size) combined.add(seriesResults[i])
                    if (i < animeResults.size) combined.add(animeResults[i])
                }
                combined
            }
        }
    }

    private fun searchAnime(encodedQuery: String): List<TitleSearchResult> {
        return try {
            val url = "https://api.jikan.moe/v4/anime?q=$encodedQuery&limit=8"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Vortex-App/2.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val dataArray = json.optJSONArray("data") ?: return emptyList()

            val results = mutableListOf<TitleSearchResult>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val malId = item.optInt("mal_id")
                val title = item.optString("title", "Sem título")
                val synopsis = item.optString("synopsis", "").trim()
                var episodes = item.optInt("episodes", 0)
                val year = item.optInt("year", 0).let { if (it > 0) it else 2023 }
                
                // If episodes is 0 or null, check if ongoing and try to fetch latest episode number
                if (episodes <= 0) {
                    episodes = fetchAnimeEpisodesCount(malId, title)
                }
                
                // Get genres
                val genresArray = item.optJSONArray("genres")
                val genresList = mutableListOf<String>()
                if (genresArray != null) {
                    for (g in 0 until genresArray.length()) {
                        genresList.add(genresArray.getJSONObject(g).optString("name", ""))
                    }
                }
                val genreStr = if (genresList.isNotEmpty()) genresList.joinToString(", ") else "Anime, Ação"

                // Image
                val images = item.optJSONObject("images")?.optJSONObject("jpg")
                val coverUrl = images?.optString("large_image_url") 
                    ?: images?.optString("image_url") ?: ""

                val safeEpisodes = if (episodes > 0) episodes else 12

                results.add(
                    TitleSearchResult(
                        title = title,
                        coverUrl = coverUrl,
                        synopsis = synopsis,
                        category = Category.ANIME,
                        releaseYear = year,
                        genre = genreStr,
                        totalSeasons = 1,
                        episodesPerSeason = safeEpisodes,
                        totalEpisodes = safeEpisodes,
                        source = "MyAnimeList (Jikan)",
                        externalId = malId.toString(),
                        seasonBreakdown = "1 temporada • $safeEpisodes episódios"
                    )
                )
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun fetchAnimeEpisodesCount(malId: Int, title: String): Int {
        if (malId <= 0) return 12
        // Specific quick fallback for legendary long-running titles
        if (title.contains("One Piece", ignoreCase = true)) return 1150
        if (title.contains("Detective Conan", ignoreCase = true)) return 1140
        if (title.contains("Naruto Shippuuden", ignoreCase = true)) return 500
        if (title.contains("Bleach", ignoreCase = true)) return 366

        return try {
            val url = "https://api.jikan.moe/v4/anime/$malId/episodes?limit=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Vortex-App/2.0")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val pagination = json.optJSONObject("pagination")
                    val itemsObj = pagination?.optJSONObject("items")
                    val total = itemsObj?.optInt("total", 0) ?: 0
                    if (total > 0) return total

                    val lastPage = pagination?.optInt("last_visible_page", 0) ?: 0
                    if (lastPage > 0) {
                        return lastPage * 100
                    }
                }
            }
            12
        } catch (_: Exception) {
            12
        }
    }

    private fun searchSeries(encodedQuery: String): List<TitleSearchResult> {
        return try {
            val url = "https://api.tvmaze.com/search/shows?q=$encodedQuery"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Vortex-App/2.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val results = mutableListOf<TitleSearchResult>()

            val maxItems = minOf(array.length(), 8)
            for (i in 0 until maxItems) {
                val obj = array.getJSONObject(i)
                val show = obj.optJSONObject("show") ?: continue
                val showId = show.optInt("id", 0)
                val name = show.optString("name", "Sem título")
                val rawSummary = show.optString("summary", "")
                val cleanSummary = rawSummary.replace(Regex("<.*?>"), "").trim()
                
                val premiered = show.optString("premiered", "")
                val year = if (premiered.length >= 4) {
                    premiered.substring(0, 4).toIntOrNull() ?: 2022
                } else 2022

                val genresArray = show.optJSONArray("genres")
                val genresList = mutableListOf<String>()
                if (genresArray != null) {
                    for (g in 0 until genresArray.length()) {
                        genresList.add(genresArray.getString(g))
                    }
                }
                val genreStr = if (genresList.isNotEmpty()) genresList.joinToString(", ") else "Drama, Série"

                val imageObj = show.optJSONObject("image")
                val coverUrl = imageObj?.optString("original") 
                    ?: imageObj?.optString("medium") ?: ""

                // Fetch real seasons and episodes breakdown from TVMaze
                val (seasonsCount, totalEpisodes, epPerSeason, breakdown) = fetchSeriesSeasonsDetails(showId, name)

                results.add(
                    TitleSearchResult(
                        title = name,
                        coverUrl = coverUrl,
                        synopsis = cleanSummary,
                        category = Category.SERIE,
                        releaseYear = year,
                        genre = genreStr,
                        totalSeasons = seasonsCount,
                        episodesPerSeason = epPerSeason,
                        totalEpisodes = totalEpisodes,
                        source = "TVMaze",
                        externalId = showId.toString(),
                        seasonBreakdown = breakdown
                    )
                )
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    private data class SeriesDetails(
        val seasonsCount: Int,
        val totalEpisodes: Int,
        val episodesPerSeason: Int,
        val breakdown: String
    )

    private fun fetchSeriesSeasonsDetails(showId: Int, showName: String): SeriesDetails {
        if (showId <= 0) {
            return SeriesDetails(1, 10, 10, "1 temporada • 10 episódios")
        }

        try {
            // Fetch seasons info: https://api.tvmaze.com/shows/{id}/seasons
            val seasonsUrl = "https://api.tvmaze.com/shows/$showId/seasons"
            val seasonsReq = Request.Builder()
                .url(seasonsUrl)
                .header("User-Agent", "Vortex-App/2.0")
                .build()
            val seasonsResp = client.newCall(seasonsReq).execute()
            if (seasonsResp.isSuccessful) {
                val seasonsBody = seasonsResp.body?.string()
                if (seasonsBody != null) {
                    val seasonsArr = JSONArray(seasonsBody)
                    val seasonsCount = seasonsArr.length().coerceAtLeast(1)
                    var totalEps = 0
                    var firstSeasonEps = 0
                    val breakdownList = mutableListOf<String>()

                    for (s in 0 until seasonsArr.length()) {
                        val seasonObj = seasonsArr.getJSONObject(s)
                        val seasonNum = seasonObj.optInt("number", s + 1)
                        val epOrder = seasonObj.optInt("episodeOrder", 0)
                        if (epOrder > 0) {
                            totalEps += epOrder
                            if (s == 0) firstSeasonEps = epOrder
                            breakdownList.add("T$seasonNum: $epOrder eps")
                        }
                    }

                    if (totalEps > 0) {
                        val breakdownStr = "$seasonsCount temp${if (seasonsCount > 1) "s" else ""} • " +
                                (if (breakdownList.isNotEmpty()) breakdownList.take(3).joinToString(", ") else "$totalEps episódios")
                        return SeriesDetails(
                            seasonsCount = seasonsCount,
                            totalEpisodes = totalEps,
                            episodesPerSeason = if (firstSeasonEps > 0) firstSeasonEps else (totalEps / seasonsCount).coerceAtLeast(1),
                            breakdown = breakdownStr
                        )
                    }
                }
            }

            // Fallback: fetch episodes array: https://api.tvmaze.com/shows/{id}/episodes
            val episodesUrl = "https://api.tvmaze.com/shows/$showId/episodes"
            val episodesReq = Request.Builder()
                .url(episodesUrl)
                .header("User-Agent", "Vortex-App/2.0")
                .build()
            val episodesResp = client.newCall(episodesReq).execute()
            if (episodesResp.isSuccessful) {
                val episodesBody = episodesResp.body?.string()
                if (episodesBody != null) {
                    val epArr = JSONArray(episodesBody)
                    val totalEps = epArr.length()
                    var maxSeason = 1
                    var s1Count = 0
                    for (i in 0 until epArr.length()) {
                        val epObj = epArr.getJSONObject(i)
                        val season = epObj.optInt("season", 1)
                        if (season > maxSeason) maxSeason = season
                        if (season == 1) s1Count++
                    }

                    if (totalEps > 0) {
                        return SeriesDetails(
                            seasonsCount = maxSeason,
                            totalEpisodes = totalEps,
                            episodesPerSeason = if (s1Count > 0) s1Count else (totalEps / maxSeason).coerceAtLeast(1),
                            breakdown = "$maxSeason temporada(s) • $totalEps episódios no total"
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Handled safely
        }

        return SeriesDetails(1, 10, 10, "1 temporada • 10 episódios")
    }

    private fun searchMovies(encodedQuery: String): List<TitleSearchResult> {
        return try {
            val url = "https://itunes.apple.com/search?term=$encodedQuery&entity=movie&limit=8"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Vortex-App/2.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val resultsArray = json.optJSONArray("results") ?: return emptyList()

            val results = mutableListOf<TitleSearchResult>()
            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(i)
                val title = item.optString("trackName", "Sem título")
                val synopsis = item.optString("longDescription", item.optString("shortDescription", ""))
                val genre = item.optString("primaryGenreName", "Filme")
                
                val releaseDate = item.optString("releaseDate", "")
                val year = if (releaseDate.length >= 4) {
                    releaseDate.substring(0, 4).toIntOrNull() ?: 2023
                } else 2023

                var artwork = item.optString("artworkUrl100", "")
                if (artwork.isNotEmpty()) {
                    artwork = artwork.replace("100x100bb.jpg", "600x600bb.jpg")
                }

                // Parse duration from trackTimeMillis
                val trackTimeMillis = item.optLong("trackTimeMillis", 0L)
                val durationFormatted = if (trackTimeMillis > 0L) {
                    val totalMinutes = (trackTimeMillis / 60000L).toInt()
                    val hours = totalMinutes / 60
                    val minutes = totalMinutes % 60
                    when {
                        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                        hours > 0 -> "${hours}h"
                        minutes > 0 -> "${minutes}m"
                        else -> ""
                    }
                } else {
                    ""
                }

                results.add(
                    TitleSearchResult(
                        title = title,
                        coverUrl = artwork,
                        synopsis = synopsis,
                        category = Category.FILME,
                        releaseYear = year,
                        genre = genre,
                        totalSeasons = 0,
                        episodesPerSeason = 0,
                        totalEpisodes = 0,
                        duration = durationFormatted,
                        source = "iTunes Movie DB",
                        externalId = item.optLong("trackId", 0L).toString(),
                        seasonBreakdown = if (durationFormatted.isNotBlank()) "Duração: $durationFormatted" else "Filme"
                    )
                )
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun checkTitleForUpdates(title: TitleEntity): TitleUpdateInfo = withContext(Dispatchers.IO) {
        val trimmed = title.name.trim()
        val encoded = try {
            URLEncoder.encode(trimmed, "UTF-8")
        } catch (_: Exception) {
            trimmed
        }

        when (title.categoryEnum) {
            Category.SERIE -> {
                try {
                    val url = "https://api.tvmaze.com/singlesearch/shows?q=$encoded&embed=episodes"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "Vortex-App/2.0")
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            val embedded = json.optJSONObject("_embedded")
                            val episodesArray = embedded?.optJSONArray("episodes")
                            if (episodesArray != null && episodesArray.length() > 0) {
                                val totalRemoteEpisodes = episodesArray.length()
                                var maxSeason = 1
                                for (i in 0 until episodesArray.length()) {
                                    val epObj = episodesArray.getJSONObject(i)
                                    val s = epObj.optInt("season", 1)
                                    if (s > maxSeason) maxSeason = s
                                }

                                if (totalRemoteEpisodes > title.totalEpisodes || maxSeason > title.totalSeasons) {
                                    return@withContext TitleUpdateInfo(
                                        newTotalEpisodes = maxOf(totalRemoteEpisodes, title.totalEpisodes),
                                        newTotalSeasons = maxOf(maxSeason, title.totalSeasons),
                                        hasUpdates = true,
                                        description = "Novos episódios/temporadas encontrados ($totalRemoteEpisodes episódios, $maxSeason temporadas)."
                                    )
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Handled safely
                }
            }

            Category.ANIME -> {
                try {
                    val url = "https://api.jikan.moe/v4/anime?q=$encoded&limit=3"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "Vortex-App/2.0")
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            val data = json.optJSONArray("data")
                            if (data != null && data.length() > 0) {
                                val item = data.getJSONObject(0)
                                val episodes = item.optInt("episodes", 0)
                                if (episodes > title.totalEpisodes) {
                                    return@withContext TitleUpdateInfo(
                                        newTotalEpisodes = episodes,
                                        newTotalSeasons = title.totalSeasons,
                                        hasUpdates = true,
                                        description = "Novos episódios oficiais adicionados ($episodes episódios disponíveis)."
                                    )
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Handled safely
                }
            }

            Category.FILME -> {
                // Movies
            }
        }

        TitleUpdateInfo(
            newTotalEpisodes = title.totalEpisodes,
            newTotalSeasons = title.totalSeasons,
            hasUpdates = false,
            description = "Nenhuma nova temporada ou episódio disponível no momento."
        )
    }
}

data class TitleUpdateInfo(
    val newTotalEpisodes: Int,
    val newTotalSeasons: Int,
    val hasUpdates: Boolean,
    val description: String
)
