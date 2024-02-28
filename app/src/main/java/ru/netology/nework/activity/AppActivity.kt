package ru.netology.nework.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.ActivityAppBinding
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    @Inject
    lateinit var appAuth: AppAuth

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
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
