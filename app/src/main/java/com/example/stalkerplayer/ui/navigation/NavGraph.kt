package com.example.stalkerplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stalkerplayer.data.model.Channel
import com.example.stalkerplayer.data.model.PortalConfig
import com.example.stalkerplayer.ui.screens.ChannelsScreen
import com.example.stalkerplayer.ui.screens.PlayerScreen
import com.example.stalkerplayer.ui.screens.PortalsScreen
import com.example.stalkerplayer.viewmodel.ChannelsViewModel
import com.example.stalkerplayer.viewmodel.PortalsViewModel

private object Routes {
    const val PORTALS = "portals"
    const val CHANNELS = "channels"
    const val PLAYER = "player"
}

/**
 * Petit conteneur en mémoire pour transmettre le portail / la chaîne
 * sélectionnés entre les écrans sans les re-sérialiser dans les routes.
 */
private object NavState {
    var currentPortal: PortalConfig? = null
    var currentChannel: Channel? = null
}

@Composable
fun StalkerNavGraph() {
    val navController: NavHostController = rememberNavController()
    val portalsViewModel: PortalsViewModel = viewModel()
    val channelsViewModel: ChannelsViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.PORTALS) {

        composable(Routes.PORTALS) {
            PortalsScreen(
                viewModel = portalsViewModel,
                onOpenPortal = { portal ->
                    NavState.currentPortal = portal
                    channelsViewModel.connect(portal)
                    navController.navigate(Routes.CHANNELS)
                }
            )
        }

        composable(Routes.CHANNELS) {
            val portal = NavState.currentPortal
            ChannelsScreen(
                viewModel = channelsViewModel,
                portalName = portal?.name ?: "Chaînes",
                onBack = { navController.popBackStack() },
                onPlayChannel = { channel ->
                    NavState.currentChannel = channel
                    navController.navigate(Routes.PLAYER)
                }
            )
        }

        composable(Routes.PLAYER) {
            val channel = NavState.currentChannel
            if (channel != null) {
                PlayerScreen(
                    viewModel = channelsViewModel,
                    channel = channel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
