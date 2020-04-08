package com.glowapps.vidify.mobile

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.glowapps.vidify.R
import com.glowapps.vidify.util.openURL
import com.glowapps.vidify.util.share
import com.google.android.material.navigation.NavigationView

class MainMobileActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainMobileActivity"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mobile_activity_main)

        setupActionBar()
        setupNavbar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Clickable buttons in the top action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "$item clicked")
        when (item.itemId) {
            R.id.share_button -> share(this)
            R.id.twitter_button -> openURL(this, getString(R.string.twitter_link))
            else -> Log.e(TAG, "Unknown options item selected: ${item.itemId}")
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupNavbar() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each menu should be considered as top level
        // destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_devices, R.id.nav_help, R.id.nav_subscribe), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun openWebsite() {

    }
}
