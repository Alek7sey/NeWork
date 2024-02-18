package ru.netology.nework.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.ActivityAppBinding
import ru.netology.nework.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    @Inject
    lateinit var appAuth: AppAuth
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var navController: NavController

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_graph) as NavHostFragment

        navController = navHostFragment.navController

        val navView = binding.bottomNav

        val toolbar = binding.toolbar

        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.postsFeedFragment || destination.id == R.id.eventsFeedFragment || destination.id == R.id.usersFragment) {
                navView.visibility = View.VISIBLE
            } else {
                navView.visibility = View.GONE
            }

        }

        intent?.let {
            if (it.action == Intent.ACTION_SEND) {
                val text = it.getStringExtra(Intent.EXTRA_TEXT)
                if (text.isNullOrBlank()) {
                    Snackbar.make(binding.root, R.string.content_can_t_be_empty, LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok) {
                            finish()
                        }.show()
                    return@let
                }
//                findNavController(R.id.nav_graph).navigate(
//                    R.id.action_postsFeedFragment_to_postAddFragment,
//                    Bundle().apply { editTextArg = text }
//                )
            }
        }

        var currentMenuProvider: MenuProvider? = null

        authViewModel.data.observe(this) {
            //        currentMenuProvider?.let(::removeMenuProvider)
//            override fun onCreateOptionsMenu(menu: Menu): Boolean {
//                menuInflater.inflate(R.menu.auth_menu, menu)
//           //     menuInflater.inflate(R.menu.auth_menu, menu)
//                menu.setGroupVisible(R.id.registered, authViewModel.authorized)
//                menu.setGroupVisible(R.id.unregistered, !authViewModel.authorized)
//                return true
//            }
            if (authViewModel.authenticated) {
                toolbar.menu.setGroupVisible(R.id.registered, true)
                toolbar.menu.setGroupVisible(R.id.unregistered, false)
            } else {
                toolbar.menu.setGroupVisible(R.id.unregistered, true)
                toolbar.menu.setGroupVisible(R.id.registered, false)
            }

            toolbar.overflowIcon = resources.getDrawable(R.drawable.ic_account_circle)
            toolbar.setOnMenuItemClickListener { menuItem ->
                if (!authViewModel.authenticated) {
                    when (menuItem.itemId) {
                        R.id.signUp -> {
                            findNavController(R.id.nav_graph).navigate(R.id.action_postsFeedFragment_to_registrationFragment)
                            true
                        }

                        R.id.signIn -> {
                            findNavController(R.id.nav_graph).navigate(R.id.action_postsFeedFragment_to_loginFragment)
                            true
                        }

                        else -> false
                    }
                } else {
                    when (menuItem.itemId) {
                        R.id.logout -> {
                            appAuth.clearAuth()
                            true
                        }

                        R.id.profil -> {
                            findNavController(R.id.nav_graph).navigate(R.id.action_eventsFeedFragment_to_profileMyFragment)
                            true
                        }

                        else -> false
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
