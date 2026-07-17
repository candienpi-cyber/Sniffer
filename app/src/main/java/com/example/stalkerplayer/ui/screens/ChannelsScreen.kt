package com.example.stalkerplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.stalkerplayer.data.model.Channel
import com.example.stalkerplayer.data.model.Genre
import com.example.stalkerplayer.ui.theme.Accent
import com.example.stalkerplayer.ui.theme.OnSurfaceMuted
import com.example.stalkerplayer.ui.theme.Primary
import com.example.stalkerplayer.ui.theme.Surface
import com.example.stalkerplayer.viewmodel.ChannelsViewModel
import com.example.stalkerplayer.viewmodel.LoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    viewModel: ChannelsViewModel,
    portalName: String,
    onBack: () -> Unit,
    onPlayChannel: (Channel) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(portalName, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = viewModel::updateSearch,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Rechercher une chaîne...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.genres, key = { it.id }) { genre: Genre ->
                    val selected = genre.id == viewModel.selectedGenreId
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectGenre(genre.id) },
                        label = { Text(genre.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when (val state = viewModel.loadState) {
                is LoadState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent)
                    }
                }
                is LoadState.Error -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    val list = viewModel.filteredChannels
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list, key = { it.id }) { channel: Channel ->
                            ChannelCard(channel = channel, onClick = { onPlayChannel(channel) })
                        }
                        if (viewModel.hasMorePages) {
                            item {
                                TextButton(onClick = { viewModel.loadChannels(reset = false) }) {
                                    Text("Charger plus de chaînes")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262636)),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl != null) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Filled.LiveTv, contentDescription = null, tint = Accent)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    channel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Text(
                    "Chaîne ${channel.number}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
            }
        }
    }
}
