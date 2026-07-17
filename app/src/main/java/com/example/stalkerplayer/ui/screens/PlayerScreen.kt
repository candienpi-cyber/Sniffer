package com.example.stalkerplayer.ui.screens

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.example.stalkerplayer.data.model.Channel
import com.example.stalkerplayer.ui.theme.Accent
import com.example.stalkerplayer.viewmodel.ChannelsViewModel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: ChannelsViewModel,
    channel: Channel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var playbackUrl by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(channel.id) {
        scope.launch {
            val url = viewModel.resolvePlaybackUrl(channel)
            if (url == null) {
                errorMessage = "Impossible de récupérer le flux de cette chaîne."
            } else {
                playbackUrl = url
            }
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(playbackUrl) {
        playbackUrl?.let { url ->
            val httpDataSourceFactory = OkHttpDataSource.Factory(OkHttpClient())
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            val mediaSource = if (url.contains(".m3u8")) {
                HlsMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            } else {
                ProgressiveMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            }
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (playbackUrl != null) {
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (errorMessage == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        }

        errorMessage?.let {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(it, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Text(channel.name, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
