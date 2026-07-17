package com.example.stalkerplayer.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.stalkerplayer.data.model.PortalConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "portals_store")

/**
 * Stocke la liste des portails Stalker configurés par l'utilisateur, sous
 * forme de JSON, dans les préférences DataStore.
 */
class PortalRepository(private val context: Context) {

    private val portalsKey = stringPreferencesKey("portals_json")

    val portals: Flow<List<PortalConfig>> = context.dataStore.data.map { prefs ->
        val raw = prefs[portalsKey] ?: "[]"
        parsePortals(raw)
    }

    suspend fun addPortal(name: String, portalUrl: String, macAddress: String, login: String, password: String) {
        context.dataStore.edit { prefs ->
            val current = parsePortals(prefs[portalsKey] ?: "[]").toMutableList()
            current.add(
                PortalConfig(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    portalUrl = portalUrl,
                    macAddress = macAddress,
                    login = login,
                    password = password
                )
            )
            prefs[portalsKey] = serializePortals(current)
        }
    }

    suspend fun removePortal(id: String) {
        context.dataStore.edit { prefs ->
            val current = parsePortals(prefs[portalsKey] ?: "[]").filterNot { it.id == id }
            prefs[portalsKey] = serializePortals(current)
        }
    }

    private fun parsePortals(raw: String): List<PortalConfig> {
        val list = mutableListOf<PortalConfig>()
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            list.add(
                PortalConfig(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    portalUrl = obj.optString("portalUrl"),
                    macAddress = obj.optString("macAddress"),
                    login = obj.optString("login"),
                    password = obj.optString("password")
                )
            )
        }
        return list
    }

    private fun serializePortals(list: List<PortalConfig>): String {
        val arr = JSONArray()
        list.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("portalUrl", it.portalUrl)
            obj.put("macAddress", it.macAddress)
            obj.put("login", it.login)
            obj.put("password", it.password)
            arr.put(obj)
        }
        return arr.toString()
    }
}
