package com.example.androidfinaltask

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.example.androidfinaltask.data.repository.FirebaseRepository
import com.example.androidfinaltask.databinding.ActivityMainBinding
import com.example.androidfinaltask.ui.fragments.BookmarkFragment
import com.example.androidfinaltask.ui.fragments.ExploreFragment
import com.example.androidfinaltask.ui.fragments.HomeFragment
import com.example.androidfinaltask.ui.fragments.LoginFragment
import com.example.androidfinaltask.ui.fragments.ProfileFragment
import com.example.androidfinaltask.ui.fragments.SplashFragment
import com.example.androidfinaltask.ui.viewmodel.AuthViewModel
import com.example.androidfinaltask.ui.viewmodel.NewsViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val newsViewModel: NewsViewModel by viewModels()
    val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNavigation()
        observeAuthState()
        
        authViewModel.initPreferences(this)

        val isLoggedIn = FirebaseRepository.isUserLoggedIn()
        
        if (savedInstanceState == null) {
            val rememberMe = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getBoolean("remember_me", false)
            
            if (isLoggedIn && rememberMe) {
                val splashFragment = SplashFragment().apply {
                    onNavigate = {
                        supportFragmentManager.commit {
                            replace(R.id.fragment_container, HomeFragment())
                        }
                    }
                }
                supportFragmentManager.commit {
                    replace(R.id.fragment_container, splashFragment)
                }
            } else {
                if (isLoggedIn && !rememberMe) {
                    authViewModel.signOut()
                }
                supportFragmentManager.commit {
                    replace(R.id.fragment_container, LoginFragment())
                }
            }
        }
        
        binding.root.post {
            val loggedIn = FirebaseRepository.isUserLoggedIn()
            if (loggedIn) {
                showBottomNavigation()
            } else {
                hideBottomNavigation()
            }
        }
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is com.example.androidfinaltask.ui.viewmodel.AuthState.Success -> {
                    showBottomNavigation()
                }
                is com.example.androidfinaltask.ui.viewmodel.AuthState.NotAuthenticated -> {
                    hideBottomNavigation()
                }
                else -> {}
            }
        }
    }

    private fun showBottomNavigation() {
        if (binding.bottomNavigation.visibility != View.VISIBLE) {
            binding.bottomNavigation.visibility = View.VISIBLE
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.root as ConstraintLayout)
            constraintSet.connect(
                R.id.fragment_container,
                ConstraintSet.BOTTOM,
                R.id.bottomNavigation,
                ConstraintSet.TOP
            )
            constraintSet.applyTo(binding.root as ConstraintLayout)
        }
    }

    private fun hideBottomNavigation() {
        if (binding.bottomNavigation.visibility != View.GONE) {
            binding.bottomNavigation.visibility = View.GONE
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.root as ConstraintLayout)
            constraintSet.connect(
                R.id.fragment_container,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            constraintSet.applyTo(binding.root as ConstraintLayout)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, HomeFragment())
                    }
                    true
                }
                R.id.nav_explore -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, ExploreFragment())
                    }
                    true
                }
                R.id.nav_bookmark -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, BookmarkFragment())
                    }
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, ProfileFragment())
                    }
                    true
                }
                else -> false
            }
        }
    }
}
