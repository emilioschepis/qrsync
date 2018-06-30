package com.emilioschepis.qrsync.ui.preferences

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.emilioschepis.qrsync.R

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.preferences_root_frl, PreferencesFragment(), "settings")
                .commitAllowingStateLoss()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.activity_title_preferences)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
