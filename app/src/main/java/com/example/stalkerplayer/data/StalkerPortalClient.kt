package com.example.stalkerplayer.data

import com.example.stalkerplayer.data.model.Channel
import com.example.stalkerplayer.data.model.Genre
import com.example.stalkerplayer.data.model.PortalConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Client pour le protocole "Stalker Portal" / Ministra utilisé par de nombreux
 * fournisseurs IPTV (MAG boxes émulées). Implémente le cycle:
 * handshake -> get_profile -> get_genres -> get_ordered_list -> create_link
 */
class StalkerPortalClient(private val config: PortalConfig) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private var token: String? = null

    // Base normalisée: on retire un éventuel "portal.php" ou trailing slash superflu
    private val baseUrl: String = config.portalUrl.trimEnd('/')

    private val userAgent =
        "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG250 stbapp ver: 2 rev: 250 Safari/533.3"

    private fun cookieHeader(): String =
        "mac=${config.macAddress}; stb_lang=fr; timezone=Europe/Paris"

    private fun buildUrl(query: String): String {
        // La plupart des portails exposent l'API sur /portal.php ou /server/load.php
        return "$baseUrl/portal.php?$query"
    }

    private fun request(query: String, needsAuth: Boolean = true): JSONObject? {
        return try {
            val urlStr = buildUrl(query)
            val builder = Request.Builder()
                .url(urlStr)
                .header("User-Agent", userAgent)
                .header("Cookie", cookieHeader())
                .header("X-User-Agent", "Model: MAG250; Link: WiFi")
                .header("Referer", "$baseUrl/c/")

            if (needsAuth && token != null) {
                builder.header("Authorization", "Bearer $token")
            }

            client.newCall(builder.build()).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                if (body.isBlank()) return null
                try {
                    JSONObject(body)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            // Toute erreur réseau (URL invalide, portail injoignable, pas de
            // connexion internet, timeout...) ne doit jamais faire planter
            // l'application : on renvoie simplement "pas de réponse".
            null
        }
    }

    /**
     * Étape 1: handshake. Récupère un token de session à utiliser dans les
     * requêtes suivantes.
     */
    fun handshake(): Boolean {
        val json = request(
            "type=stb&action=handshake&token=&JsHttpRequest=1-xml",
            needsAuth = false
        ) ?: return false
        val js = json.optJSONObject("js") ?: return false
        val newToken = js.optString("token", "")
        return if (newToken.isNotBlank()) {
            token = newToken
            true
        } else false
    }

    /**
     * Étape 2: validation du profil. Nécessaire sur la plupart des portails
     * pour activer complètement la session (obligations d'authentification MAC).
     */
    fun getProfile(): Boolean {
        val query = "type=stb&action=get_profile&hd=1&ver=ImageDescription%3A%200.2.18-r23-250%3B" +
            "&num_banks=2&sn=&stb_type=MAG250&client_type=STB&image_version=218" +
            "&video_out=hdmi&device_id=&device_id2=&signature=&auth_second_step=1" +
            "&hw_version=1.7-BD-00&not_valid_token=0&metrics=%7B%22mac%22%3A%22${config.macAddress}%22%7D" +
            "&hw_version_2=&api_signature=262&JsHttpRequest=1-xml"
        val json = request(query) ?: return false
        return json.optJSONObject("js") != null
    }

    /** Étape 3: liste des catégories/genres de chaînes. */
    fun getGenres(): List<Genre> {
        val json = request("type=itv&action=get_genres&JsHttpRequest=1-xml") ?: return emptyList()
        val arr = json.optJSONArray("js") ?: return emptyList()
        val result = mutableListOf<Genre>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            result.add(
                Genre(
                    id = obj.optString("id"),
                    title = obj.optString("title")
                )
            )
        }
        return result
    }

    /**
     * Étape 4: liste des chaînes, paginée. genreId = "*" pour tout récupérer.
     */
    fun getChannels(genreId: String = "*", page: Int = 1): Pair<List<Channel>, Boolean> {
        val query = "type=itv&action=get_ordered_list&genre=$genreId&fav=0&sortby=number" +
            "&hd=0&p=$page&JsHttpRequest=1-xml"
        val json = request(query) ?: return emptyList<Channel>() to false
        val js = json.optJSONObject("js") ?: return emptyList<Channel>() to false
        val data = js.optJSONArray("data") ?: return emptyList<Channel>() to false

        val totalItems = js.optInt("total_items", 0)
        val maxPageItems = js.optInt("max_page_items", data.length())
        val hasMore = if (maxPageItems > 0) page * maxPageItems < totalItems else false

        val channels = mutableListOf<Channel>()
        for (i in 0 until data.length()) {
            val obj = data.optJSONObject(i) ?: continue
            channels.add(
                Channel(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    number = obj.optString("number"),
                    logoUrl = obj.optString("logo").takeIf { it.isNotBlank() }
                        ?.let { if (it.startsWith("http")) it else "$baseUrl/$it" },
                    genreId = obj.optString("tv_genre_id"),
                    cmd = obj.optString("cmd")
                )
            )
        }
        return channels to hasMore
    }

    /**
     * Étape 5: résolution du lien de lecture réel à partir de la commande
     * brute d'une chaîne. C'est ce lien qui doit être passé au lecteur vidéo.
     */
    fun createLink(cmd: String): String? {
        val encodedCmd = java.net.URLEncoder.encode(cmd, "UTF-8")
        val query = "type=itv&action=create_link&cmd=$encodedCmd&series=&forced_storage=undefined" +
            "&disable_ad=0&download=0&force_ch_link_check=0&JsHttpRequest=1-xml"
        val json = request(query) ?: return null
        val js = json.optJSONObject("js") ?: return null
        val rawCmd = js.optString("cmd", "")
        if (rawCmd.isBlank()) return null
        // Le format renvoyé ressemble souvent à "ffmpeg http://..." ou juste l'URL
        return if (rawCmd.contains(" ")) rawCmd.substringAfter(" ") else rawCmd
    }

    /**
     * Enchaîne handshake + validation de profil. À appeler avant toute
     * autre opération, et à réessayer une fois en cas d'expiration du token.
     */
    fun connect(): PortalConnectResult {
        if (!handshake()) return PortalConnectResult.Error("Handshake impossible : vérifiez l'URL du portail")
        if (!getProfile()) return PortalConnectResult.Error("Profil refusé : vérifiez l'adresse MAC")
        return PortalConnectResult.Success
    }
}

sealed class PortalConnectResult {
    object Success : PortalConnectResult()
    data class Error(val message: String) : PortalConnectResult()
}

/** Petites fonctions utilitaires exécutées sur un thread IO. */
suspend fun StalkerPortalClient.connectAsync() = withContext(Dispatchers.IO) { connect() }
suspend fun StalkerPortalClient.getGenresAsync() = withContext(Dispatchers.IO) { getGenres() }
suspend fun StalkerPortalClient.getChannelsAsync(genreId: String, page: Int) =
    withContext(Dispatchers.IO) { getChannels(genreId, page) }
suspend fun StalkerPortalClient.createLinkAsync(cmd: String) = withContext(Dispatchers.IO) { createLink(cmd) }
