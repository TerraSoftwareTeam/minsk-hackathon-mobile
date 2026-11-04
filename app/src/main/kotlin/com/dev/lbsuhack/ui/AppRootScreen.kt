package com.dev.lbsuhack.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.dev.lbsuhack.core.navigation.event.AppEvent
import com.dev.lbsuhack.core.navigation.event.AppEventBus
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.MainTabInstaller
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.navigation.layer.NavigationLayer
import com.dev.lbsuhack.core.navigation.routes.HomeRoute
import com.dev.lbsuhack.navigation.AppNavigator
import org.koin.compose.getKoin
import org.koin.compose.koinInject


@Composable
fun AppRootScreen(
    bus: AppEventBus = koinInject(),
) {
    val koin = getKoin()
    val installers = remember { koin.getAll<NavigationInstaller>() }
    val layers = remember { koin.getAll<NavigationLayer>() }
    val tabs = remember { koin.getAll<MainTabInstaller>().sortedBy { it.order } }

    val startDestination: NavKey = tabs.firstOrNull()?.rootRoute ?: HomeRoute

    val backStack = rememberNavBackStack(startDestination)
    val navigator = remember(backStack) { AppNavigator(backStack, startDestination) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val entryProvider = remember(installers) {
        entryProvider {
            installers.forEach { it.install(this) }
        }
    }

    val combinedStrategy = remember(layers) {
        val defaultStrategy: SceneStrategy<NavKey> = SinglePaneSceneStrategy()
        layers.map { it.sceneStrategy }.foldRight(defaultStrategy) { strategy, acc ->
            strategy.then(acc)
        }
    }

    LaunchedEffect(bus, context) {
        bus.events.collect { event ->
            val message = when (event) {
                is AppEvent.Error -> event.text.resolve(context)
                is AppEvent.Toast -> event.text.resolve(context)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        navigator.back()
    }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                entryProvider = entryProvider,
                sceneStrategy = combinedStrategy,
                onBack = { navigator.back() },
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
