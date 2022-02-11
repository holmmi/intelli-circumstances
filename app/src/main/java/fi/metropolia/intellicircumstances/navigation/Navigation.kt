package fi.metropolia.intellicircumstances.navigation

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fi.metropolia.intellicircumstances.bluetooth.BluetoothModel
import fi.metropolia.intellicircumstances.view.faq.FaqView
import fi.metropolia.intellicircumstances.view.home.HomeView
import fi.metropolia.intellicircumstances.view.measure.MeasureView
import fi.metropolia.intellicircumstances.view.settings.SettingsView
import fi.metropolia.intellicircumstances.view.spaces.PropertiesView
import fi.metropolia.intellicircumstances.view.spaces.SpacesView

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val bluetoothModel = BluetoothModel(LocalContext.current.applicationContext as Application)
    Scaffold(
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                NavHost(navController = navController, startDestination = NavigationRoutes.HOME) {
                    composable(NavigationRoutes.FAQ) { FaqView(navController) }
                    composable(NavigationRoutes.HOME) { HomeView(navController) }
                    composable(NavigationRoutes.MEASURE) { MeasureView(navController) }
                    composable(NavigationRoutes.PROPERTIES) { PropertiesView(navController) }
                    composable(NavigationRoutes.SETTINGS) { SettingsView(navController) }
                    composable(
                        NavigationRoutes.SPACES,
                        arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
                    ) {
                        SpacesView(navController, it.arguments?.getString("propertyId")?.toLong(), bluetoothModel = bluetoothModel)
                    }
                }
            }
        },
        bottomBar = { BottomNavigationBar(navController) }
    )
}

object NavigationRoutes {
    const val FAQ = "home/faq"
    const val HOME = "home"
    const val MEASURE = "measure"
    const val PROPERTIES = "properties"
    const val SETTINGS = "settings"
    const val SPACES = "properties/{propertyId}"
}