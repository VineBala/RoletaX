package com.example.data.remote

import com.example.data.model.Category
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
                .header("User-Agent", "SeraAkisos-App/1.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val dataArray = json.optJSONArray("data") ?: return emptyList()

            val results = mutableListOf<TitleSearchResult>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val title = item.optString("title", "Sem título")
                val synopsis = item.optString("synopsis", "").trim()
                val episodes = item.optInt("episodes", 12).coerceAtLeast(1)
                val year = item.optInt("year", 0).let { if (it > 0) it else 2023 }
                
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

                results.add(
                    TitleSearchResult(
                        title = title,
                        coverUrl = coverUrl,
                        synopsis = synopsis,
                        category = Category.ANIME,
                        releaseYear = year,
                        genre = genreStr,
                        totalSeasons = 1,
                        episodesPerSeason = episodes,
                        totalEpisodes = episodes,
                        source = "MyAnimeList (Jikan)"
                    )
                )
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun searchSeries(encodedQuery: String): List<TitleSearchResult> {
        return try {
            val url = "https://api.tvmaze.com/search/shows?q=$encodedQuery"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "SeraAkisos-App/1.0")
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

                results.add(
                    TitleSearchResult(
                        title = name,
                        coverUrl = coverUrl,
                        synopsis = cleanSummary,
                        category = Category.SERIE,
                        releaseYear = year,
                        genre = genreStr,
                        totalSeasons = 1,
                        episodesPerSeason = 10,
                        totalEpisodes = 10,
                        source = "TVMaze"
                    )
                )
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun searchMovies(encodedQuery: String): List<TitleSearchResult> {
        return try {
            val url = "https://itunes.apple.com/search?term=$encodedQuery&entity=movie&limit=8"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "SeraAkisos-App/1.0")
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

                results.add(
                    TitleSearchResult(
                        title = title,
                        coverUrl = artwork,
                        synopsis = synopsis,
                        category = Category.FILME,
                        releaseYear = year,
                        genre = genre,
                        totalSeasons = 1,
                        episodesPerSeason = 1,
                        totalEpisodes = 1,
                        source = "iTunes Movie DB"
                    )
                )
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun checkTitleForUpdates(title: com.example.data.model.TitleEntity): TitleUpdateInfo = withContext(Dispatchers.IO) {
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
                        .header("User-Agent", "SeraAkisos-App/1.0")
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
                                        description = "Nova temporada/episódio encontrado no catálogo oficial ($totalRemoteEpisodes episódios, $maxSeason temporadas)."
                                    )
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Fallback to standard check
                }
            }

            Category.ANIME -> {
                try {
                    val url = "https://api.jikan.moe/v4/anime?q=$encoded&limit=3"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "SeraAkisos-App/1.0")
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
                // Movies don't have new episodes, but we can verify if sequel or extended data exists
            }
        }

        // Default: no new updates found
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

