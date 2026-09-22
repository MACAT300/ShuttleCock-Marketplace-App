package com.example.shuttlecock_frontend.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.data.UserSession
import com.example.shuttlecock_frontend.fragment.CartFragment
import com.example.shuttlecock_frontend.fragment.ExploreFragment
import com.example.shuttlecock_frontend.fragment.FavouriteFragment
import com.example.shuttlecock_frontend.fragment.HomeFragment
import com.example.shuttlecock_frontend.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainHostActivity : AppCompatActivity() {

    private val homeFragment by lazy { HomeFragment() }
    private val exploreFragment by lazy { ExploreFragment() }
    private val cartFragment by lazy { CartFragment() }
    private val favouriteFragment by lazy { FavouriteFragment() }
    private val profileFragment by lazy { ProfileFragment() }

    private var activeFragment: Fragment? = null

    companion object {
        const val EXTRA_START_TAB = "start_tab"
        const val TAB_HOME = "home"
        const val TAB_EXPLORE = "explore"
        const val TAB_CART = "cart"
        const val TAB_WISHLIST = "wishlist"
        const val TAB_PROFILE = "profile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_host)

        if (!UserSession.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragmentContainer, favouriteFragment, "wishlist").hide(favouriteFragment)
                .add(R.id.fragmentContainer, cartFragment, "cart").hide(cartFragment)
                .add(R.id.fragmentContainer, exploreFragment, "explore").hide(exploreFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit()
            activeFragment = homeFragment
        }

        bottomNav.setOnItemSelectedListener { item ->
            val target = when (item.itemId) {
                R.id.nav_home -> homeFragment
                R.id.nav_explore -> exploreFragment
                R.id.nav_cart -> cartFragment
                R.id.nav_wishlist -> favouriteFragment
                R.id.nav_profile -> profileFragment
                else -> return@setOnItemSelectedListener false
            }
            showFragment(target)
            true
        }

        when (intent.getStringExtra(EXTRA_START_TAB)) {
            TAB_EXPLORE -> bottomNav.selectedItemId = R.id.nav_explore
            TAB_CART -> bottomNav.selectedItemId = R.id.nav_cart
            TAB_WISHLIST -> bottomNav.selectedItemId = R.id.nav_wishlist
            TAB_PROFILE -> bottomNav.selectedItemId = R.id.nav_profile
            else -> { /* default Home tab, already showing */ }
        }
    }

    private fun showFragment(target: Fragment) {
        if (target === activeFragment) return
        val transaction = supportFragmentManager.beginTransaction()
        activeFragment?.let { transaction.hide(it) }
        transaction.show(target)
        transaction.commit()
        activeFragment = target
    }

    /** Lets a fragment (e.g. Home's cart icon) jump to another tab programmatically. */
    fun switchTab(tabId: Int) {
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = tabId
    }
}