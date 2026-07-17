package com.example.stalkerplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stalkerplayer.data.model.PortalConfig
import com.example.stalkerplayer.ui.theme.Accent
import com.example.stalkerplayer.ui.theme.OnSurfaceMuted
import com.example.stalkerplayer.ui.theme.Primary
import com.example.stalkerplayer.ui.theme.PrimaryVariant
import com.example.stalkerplayer.ui.theme.Surface
import com.example.stalkerplayer.viewmodel.PortalsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalsScreen(
    viewModel: PortalsViewModel,
    onOpenPortal: (PortalConfig) -> Unit
) {
    val portals by viewModel.portals.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mes portails", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = Primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter un portail")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(PrimaryVariant.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        ) {
            if (portals.isEmpty()) {
                EmptyState(onAdd = { showAddSheet = true })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(portals, key = { it.id }) { portal ->
                        PortalCard(
                            portal = portal,
                            onOpen = { onOpenPortal(portal) },
                            onDelete = { viewModel.removePortal(portal.id) }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showAddSheet) {
        AddPortalSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, url, mac, login, password ->
                viewModel.addPortal(name, url, mac, login, password)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Dns,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Aucun portail configuré",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Ajoutez un portail Stalker/Ministra en indiquant son URL et l'adresse MAC autorisée.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ajouter un portail")
        }
    }
}

@Composable
private fun PortalCard(
    portal: PortalConfig,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Primary, Accent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Dns, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(portal.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    portal.portalUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceMuted,
                    maxLines = 1
                )
                Text(
                    "MAC : ${portal.macAddress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
            }
            IconButton(onClick = onOpen) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Ouvrir", tint = Accent)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = OnSurfaceMuted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPortalSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, url: String, mac: String, login: String, password: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var mac by remember { mutableStateOf("00:1A:79:") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Nouveau portail", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nom (ex: Mon fournisseur)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = url, onValueChange = { url = it },
                label = { Text("URL du portail (ex: http://portail.tv/c/)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = mac, onValueChange = { mac = it },
                label = { Text("Adresse MAC") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = login, onValueChange = { login = it },
                label = { Text("Identifiant (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Mot de passe (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onSave(name.ifBlank { "Portail" }, url.trim(), mac.trim(), login, password) },
                enabled = url.isNotBlank() && mac.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enregistrer")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// Alias pratique pour éviter l'ambiguïté Color de Compose
private typealias Color = androidx.compose.ui.graphics.Color
