package com.example.stalkerplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stalkerplayer.data.PortalRepository
import com.example.stalkerplayer.data.model.PortalConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PortalsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = PortalRepository(app)

    private val _portals = MutableStateFlow<List<PortalConfig>>(emptyList())
    val portals: StateFlow<List<PortalConfig>> = _portals

    init {
        viewModelScope.launch {
            repository.portals.collect { _portals.value = it }
        }
    }

    fun addPortal(name: String, url: String, mac: String, login: String, password: String) {
        viewModelScope.launch {
            repository.addPortal(name, url, mac, login, password)
        }
    }

    fun removePortal(id: String) {
        viewModelScope.launch {
            repository.removePortal(id)
        }
    }
}
