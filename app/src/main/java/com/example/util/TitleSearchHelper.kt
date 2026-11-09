package com.example.util

import java.text.Normalizer
import java.util.regex.Pattern

object TitleSearchHelper {

    private val DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

    fun normalize(text: String): String {
        val decomposed = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
        return DIACRITICS_PATTERN.matcher(decomposed)
            .replaceAll("")
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // Dicionário de equivalências e sinônimos conhecidos em português/Brasil
    // Chave normalizada para conjunto de nomes alternativos / termos populares
    private val EQUIVALENCE_MAP: Map<String, List<String>> = mapOf(
        "chaves" to listOf("el chavo del ocho", "el chavo", "chaves", "chaves em desenho animado"),
        "el chavo" to listOf("chaves", "el chavo del ocho"),
        "chapolin" to listOf("el chapulin colorado", "chapolin colorado", "chapolin"),
        "el chapulin colorado" to listOf("chapolin", "chapolin colorado"),
        "one piece" to listOf("one piece", "luffy", "rei dos piratas"),
        "attack on titan" to listOf("shingeki no kyojin", "ataque dos titas", "ataque dos gigantes", "aot"),
        "shingeki no kyojin" to listOf("attack on titan", "ataque dos titas", "ataque dos gigantes"),
        "demon slayer" to listOf("kimetsu no yaiba", "cacador de demonios"),
        "kimetsu no yaiba" to listOf("demon slayer", "cacador de demonios"),
        "fullmetal alchemist" to listOf("fullmetal", "o alquimista de aco"),
        "death note" to listOf("death note", "caderno da morte"),
        "interstellar" to listOf("interestelar"),
        "interestelar" to listOf("interstellar"),
        "dune" to listOf("duna"),
        "duna" to listOf("dune"),
        "spider man" to listOf("homem aranha"),
        "homem aranha" to listOf("spider man", "miranha"),
        "batman" to listOf("cavaleiro das trevas", "o homem morcego"),
        "breaking bad" to listOf("a quimica do mal", "breaking bad"),
        "game of thrones" to listOf("guerra dos tronos", "got"),
        "money heist" to listOf("la casa de papel"),
        "la casa de papel" to listOf("money heist"),
        "squid game" to listOf("round 6"),
        "round 6" to listOf("squid game"),
        "stranger things" to listOf("stranger things", "bagulhos sinistros"),
        "the boys" to listOf("os rapazes", "the boys"),
        "spirited away" to listOf("a viagem de chihiro", "sen to chihiro no kamikakushi"),
        "a viagem de chihiro" to listOf("spirited away", "sen to chihiro"),
        "howls moving castle" to listOf("o castelo animado"),
        "o castelo animado" to listOf("howls moving castle"),
        "my hero academia" to listOf("boku no hero academia", "boku no hero"),
        "boku no hero" to listOf("my hero academia"),
        "jujutsu kaisen" to listOf("batalha de feiticeiros", "jujutsu"),
        "hunter x hunter" to listOf("hunter hunter", "hxh"),
        "naruto" to listOf("naruto shippuden", "narutinho"),
        "dragon ball" to listOf("dragon ball z", "dbz", "dragon ball super"),
        "bleach" to listOf("bleach thousand year blood war"),
        "chainsaw man" to listOf("o homem motosserra"),
        "the lord of the rings" to listOf("o senhor dos aneis"),
        "o senhor dos aneis" to listOf("the lord of the rings"),
        "harry potter" to listOf("harry potter", "hp"),
        "star wars" to listOf("guerra nas estrelas")
    )

    fun matchesQuery(query: String, targetTitle: String, extraSearchFields: List<String> = emptyList()): Boolean {
        val normQuery = normalize(query)
        if (normQuery.isBlank()) return true

        val normTitle = normalize(targetTitle)
        if (normTitle.contains(normQuery)) return true

        // Checar extra search fields (sinopse, gênero, etc.)
        for (field in extraSearchFields) {
            val normField = normalize(field)
            if (normField.contains(normQuery)) return true
        }

        // Checar equivalências/sinônimos mapeados
        val queryAliases = getAliasesFor(normQuery)
        for (alias in queryAliases) {
            if (normTitle.contains(alias) || alias.contains(normTitle)) {
                return true
            }
        }

        val titleAliases = getAliasesFor(normTitle)
        for (alias in titleAliases) {
            if (alias.contains(normQuery) || normQuery.contains(alias)) {
                return true
            }
        }

        // Palavras individuais da busca
        val queryWords = normQuery.split(" ").filter { it.length > 2 }
        if (queryWords.isNotEmpty()) {
            val titleWords = normTitle.split(" ")
            var allWordsMatch = true
            for (qw in queryWords) {
                val wordMatches = titleWords.any { tw -> tw.startsWith(qw) || tw.contains(qw) } ||
                        getAliasesFor(qw).any { alias -> normTitle.contains(alias) }
                if (!wordMatches) {
                    allWordsMatch = false
                    break
                }
            }
            if (allWordsMatch) return true
        }

        return false
    }

    private fun getAliasesFor(term: String): List<String> {
        val list = mutableListOf<String>()
        EQUIVALENCE_MAP.forEach { (key, aliases) ->
            if (term == key || term.contains(key) || key.contains(term)) {
                list.addAll(aliases.map { normalize(it) })
            }
            if (aliases.any { normalize(it) == term || normalize(it).contains(term) }) {
                list.add(normalize(key))
                list.addAll(aliases.map { normalize(it) })
            }
        }
        return list.distinct()
    }
}
