package com.example.stalkerplayer.data.model

/**
 * Configuration d'un portail Stalker/Ministra enregistré par l'utilisateur.
 */
data class PortalConfig(
    val id: String,
    val name: String,
    val portalUrl: String,   // ex: http://monportail.com/c/ ou http://monportail.com/stalker_portal/
    val macAddress: String,  // ex: 00:1A:79:XX:XX:XX
    val login: String = "",
    val password: String = ""
)

data class Genre(
    val id: String,
    val title: String
)

data class Channel(
    val id: String,
    val name: String,
    val number: String,
    val logoUrl: String?,
    val genreId: String?,
    val cmd: String // commande brute renvoyée par le portail, utilisée pour create_link
)

sealed class PortalResult<out T> {
    data class Success<T>(val data: T) : PortalResult<T>()
    data class Error(val message: String) : PortalResult<Nothing>()
}
