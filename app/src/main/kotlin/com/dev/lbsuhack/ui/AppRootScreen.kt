package com.dev.lbsuhack.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.RestoreSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.navigation.api.AppNavigation
import com.dev.lbsuhack.core.navigation.api.LocalAppNavigation
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.Navigator
import com.dev.lbsuhack.core.navigation.api.NavigationInstaller
import com.dev.lbsuhack.core.navigation.event.AppEvent
import com.dev.lbsuhack.core.navigation.event.AppEventBus
import com.dev.lbsuhack.core.navigation.layer.NavigationLayer
import com.dev.lbsuhack.feature.auth.navigation.AuthLoginRoute
import com.dev.lbsuhack.feature.feed.navigation.FeedRoute
import com.dev.lbsuhack.feature.profile.navigation.ProfileRoute
import com.dev.lbsuhack.feature.tournaments.navigation.TournamentsRoute
import com.dev.lbsuhack.navigation.AppNavigator
import org.koin.compose.getKoin
import org.koin.compose.koinInject

@Composable
fun AppRootScreen(
    bus: AppEventBus = koinInject(),
    observeSession: ObserveSessionUseCase = koinInject(),
    restoreSession: RestoreSessionUseCase = koinInject(),
) {
    val koin = getKoin()
    val installers = remember { koin.getAll<NavigationInstaller>() }
    val layers = remember { koin.getAll<NavigationLayer>() }
    val sessionState by observeSession.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(restoreSession) {
        restoreSession()
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

    Box(modifier = Modifier.fillMaxSize()) {
        when (sessionState) {
            SessionState.Unknown -> SplashScreen()

            SessionState.Unauthenticated -> key(AUTH_HOST_KEY) {
                AppNavHost(
                    startRoute = AuthLoginRoute,
                    installers = installers,
                    layers = layers,
                )
            }

            is SessionState.Authenticated -> key(GLOBAL_HOST_KEY) {
                AppNavHost(
                    startRoute = FeedRoute,
                    installers = installers,
                    layers = layers,
                    appNavigationFactory = { navigator ->
                        object : AppNavigation {
                            override fun openProfile() {
                                navigator.navigate(ProfileRoute)
                            }
                        }
                    },
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AppNavHost(
    startRoute: NavKey,
    installers: List<NavigationInstaller>,
    layers: List<NavigationLayer>,
    appNavigationFactory: ((Navigator) -> AppNavigation)? = null,
) {
    val backStack = rememberNavBackStack(startRoute)
    val navigator = remember(backStack, startRoute) { AppNavigator(backStack) }

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

    val saveableStateDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val viewModelDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()
    val entryDecorators: List<NavEntryDecorator<NavKey>> =
        remember(saveableStateDecorator, viewModelDecorator) {
            listOf(saveableStateDecorator, viewModelDecorator)
        }

    val appNavigation = appNavigationFactory?.let { factory ->
        remember(navigator) { factory(navigator) }
    } ?: EmptyAppNavigation

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalAppNavigation provides appNavigation,
    ) {
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider,
            entryDecorators = entryDecorators,
            sceneStrategy = combinedStrategy,
            onBack = navigator::back,
        )
    }
}

private object EmptyAppNavigation : AppNavigation {
    override fun openProfile() = Unit
}

private const val AUTH_HOST_KEY = "auth-host"
private const val GLOBAL_HOST_KEY = "global-host"
