package ru.netology.nework.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navController = this.findNavController(R.id.nav_graph)
        val navView: BottomNavigationView = findViewById(R.id.bottom_nav)
        navView.setupWithNavController(navController)


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
//                    R.id.action_feedFragment_to_newPostFragment,
//                    Bundle().apply {
//                        textArg = text
//                    }
//                )
            }
        }

        var currentMenuProvider: MenuProvider? = null

        authViewModel.state.observe(this) {
            currentMenuProvider?.let(::removeMenuProvider)

            addMenuProvider(
                object : MenuProvider {
                    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                        menuInflater.inflate(R.menu.auth_menu, menu)
                        menu.setGroupVisible(R.id.registered, authViewModel.authorized)
                        menu.setGroupVisible(R.id.unregistered, !authViewModel.authorized)
                    }

                    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                        when (menuItem.itemId) {
                            R.id.signUp -> {
                                findNavController(R.id.nav_graph).navigate(R.id.action_postsFeedFragment_to_registrationFragment)
                                true

                            }

                            R.id.signIn -> {
                                findNavController(R.id.nav_graph).navigate(R.id.action_postsFeedFragment_to_loginFragment)
                                true
                            }

                            R.id.logout -> {
                                appAuth.clear()
                                true
                            }

                            else -> false
                        }
                }.also {
                    currentMenuProvider = it
                }
            )
        }
    }
}
