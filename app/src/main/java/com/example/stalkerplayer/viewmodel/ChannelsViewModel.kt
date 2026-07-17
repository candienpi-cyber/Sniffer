package com.example.stalkerplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stalkerplayer.data.PortalConnectResult
import com.example.stalkerplayer.data.StalkerPortalClient
import com.example.stalkerplayer.data.connectAsync
import com.example.stalkerplayer.data.createLinkAsync
import com.example.stalkerplayer.data.getChannelsAsync
import com.example.stalkerplayer.data.getGenresAsync
import com.example.stalkerplayer.data.model.Channel
import com.example.stalkerplayer.data.model.Genre
import com.example.stalkerplayer.data.model.PortalConfig
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

sealed class LoadState {
    object Idle : LoadState()
    object Loading : LoadState()
    data class Error(val message: String) : LoadState()
    object Ready : LoadState()
}

class ChannelsViewModel : ViewModel() {

    private var client: StalkerPortalClient? = null

    var loadState by mutableStateOf<LoadState>(LoadState.Idle)
        private set

    var genres by mutableStateOf<List<Genre>>(emptyList())
        private set

    var channels by mutableStateOf<List<Channel>>(emptyList())
        private set

    var selectedGenreId by mutableStateOf("*")
        private set

    var currentPage by mutableStateOf(1)
        private set

    var hasMorePages by mutableStateOf(false)
        private set

    var searchQuery by mutableStateOf("")
        private set

    val filteredChannels: List<Channel>
        get() = if (searchQuery.isBlank()) channels
        else channels.filter { it.name.contains(searchQuery, ignoreCase = true) }

    fun connect(config: PortalConfig) {
        loadState = LoadState.Loading
        val newClient = StalkerPortalClient(config)
        client = newClient
        viewModelScope.launch {
            when (val result = newClient.connectAsync()) {
                is PortalConnectResult.Success -> {
                    genres = listOf(Genre("*", "Toutes les chaînes")) + newClient.getGenresAsync()
                    loadChannels(reset = true)
                }
                is PortalConnectResult.Error -> {
                    loadState = LoadState.Error(result.message)
                }
            }
        }
    }

    fun selectGenre(genreId: String) {
        selectedGenreId = genreId
        loadChannels(reset = true)
    }

    fun loadChannels(reset: Boolean) {
        val c = client ?: return
        loadState = LoadState.Loading
        val page = if (reset) 1 else currentPage + 1
        viewModelScope.launch {
            val (newChannels, more) = c.getChannelsAsync(selectedGenreId, page)
            channels = if (reset) newChannels else channels + newChannels
            currentPage = page
            hasMorePages = more
            loadState = LoadState.Ready
        }
    }

    fun updateSearch(query: String) {
        searchQuery = query
    }

    suspend fun resolvePlaybackUrl(channel: Channel): String? {
        return client?.createLinkAsync(channel.cmd)
    }
}
