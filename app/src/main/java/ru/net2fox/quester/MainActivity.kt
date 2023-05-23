package ru.net2fox.quester

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.elevation.SurfaceColors
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.data.auth.AuthRepository
import ru.net2fox.quester.databinding.ActivityMainBinding
import ru.net2fox.quester.ui.character.CharacterFragmentDirections
import ru.net2fox.quester.ui.leaderboard.LeaderboardFragmentDirections
import ru.net2fox.quester.ui.list.ListFragmentDirections
import ru.net2fox.quester.ui.moderator.log.LogFragmentDirections
import ru.net2fox.quester.ui.placeholder.PlaceholderFragmentDirections
import ru.net2fox.quester.ui.tasks.taskdetailed.TaskDetailedFragmentDirections
import ru.net2fox.quester.ui.userprofile.UserProfileFragmentDirections

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val bottomNavigation = binding.bottomNavigation
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment).navController
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.userProfileFragment,
            R.id.characterFragment,
            R.id.listFragment,
            R.id.leaderboardFragment,
            R.id.signInFragment,
            R.id.logFragment,
            R.id.placeholderFragment
        ))

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigation.setupWithNavController(navController)
        val defaultNavBarColor = window.navigationBarColor
        // Установка цвета системной панели навигации
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)

        val hideBottomNavDestinations = setOf(
            R.id.signInFragment,
            R.id.signUpFragment,
            R.id.taskDetailedFragment,
            R.id.settingsFragment,
            R.id.skillFragment,
            R.id.logFragment,
            R.id.placeholderFragment
        )
        navController.addOnDestinationChangedListener {_, destination, _ ->
            if (destination.id in hideBottomNavDestinations) {
                binding.bottomNavigation.visibility = View.GONE
                window.navigationBarColor = defaultNavBarColor
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
                window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
            }
            this.invalidateOptionsMenu()
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings ->
                    {
                        when (navController.currentDestination?.id) {
                            R.id.characterFragment -> {
                                navController.navigate(CharacterFragmentDirections.actionCharacterFragmentToSettingsFragment())
                            }
                            R.id.listFragment -> {
                                navController.navigate(ListFragmentDirections.actionListFragmentToSettingsFragment())
                            }
                            R.id.taskDetailedFragment -> {
                                navController.navigate(TaskDetailedFragmentDirections.actionTaskDetailedFragmentToSettingsFragment())
                            }
                            R.id.leaderboardFragment -> {
                                navController.navigate(LeaderboardFragmentDirections.actionLeaderboardFragmentToSettingsFragment())
                            }
                            R.id.logFragment -> {
                                navController.navigate(LogFragmentDirections.actionLogFragmentToSettingsFragment())
                            }
                            R.id.userProfileFragment -> {
                                navController.navigate(UserProfileFragmentDirections.actionUserProfileFragmentToSettingsFragment())
                            }
                        }
                        true
                    }
                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                if (navController.currentDestination?.id == R.id.settingsFragment ||
                    navController.currentDestination?.id == R.id.signInFragment ||
                    navController.currentDestination?.id == R.id.signUpFragment ||
                    navController.currentDestination?.id == R.id.placeholderFragment
                ) {
                    menu.findItem(R.id.action_settings).isVisible = false
                }
            }
        })

        checkInternet()
        if (FirebaseAuth.getInstance().currentUser == null) {
            navController.navigate(PlaceholderFragmentDirections.actionPlaceholderFragmentToSignInFragment())
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                if (AuthRepository.get().isModerator()) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        navController.navigate(PlaceholderFragmentDirections.actionPlaceholderFragmentToLogFragment())
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        navController.navigate(PlaceholderFragmentDirections.actionPlaceholderFragmentToUserProfileFragment())
                    }
                }
            }
        }
    }

    private fun checkInternet() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                gotConnection()
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)
                lostConnection()
            }
        }

        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        if (!isConnected) {
            lostConnection()
        }
    }

    private fun lostConnection() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.mainGroup.visibility = View.GONE
            binding.bottomNavigation.visibility = View.GONE
            binding.noInternetGroup.visibility = View.VISIBLE
        }
    }

    private fun gotConnection() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.noInternetGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
            val destination = findNavController(R.id.nav_host_fragment_content_main).currentDestination!!
            if (destination.id == R.id.signInFragment ||
                destination.id == R.id.signUpFragment ||
                destination.id == R.id.settingsFragment ||
                destination.id == R.id.placeholderFragment) {
                binding.bottomNavigation.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}