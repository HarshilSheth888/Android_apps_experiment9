package com.example.spandsqlitedemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spandsqlitedemo.data.PreferenceManager
import com.example.spandsqlitedemo.databinding.ActivityMainBinding
import com.example.spandsqlitedemo.ui.MainPagerAdapter
import com.example.spandsqlitedemo.ui.SettingsFragment
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(), SettingsFragment.OnThemeAccentChangedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupViewPagerAndTabs()
        applyThemeAccent(prefManager.themeAccent)
    }

    private fun setupViewPagerAndTabs() {
        val pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.tab_sqlite)
                    tab.setIcon(R.drawable.ic_task)
                }
                else -> {
                    tab.text = getString(R.string.tab_preferences)
                    tab.setIcon(R.drawable.ic_settings)
                }
            }
        }.attach()
    }

    override fun onThemeAccentChanged(accent: String) {
        applyThemeAccent(accent)
    }

    private fun applyThemeAccent(accent: String) {
        val bgDrawable = when (accent) {
            "coral" -> R.drawable.bg_header_coral
            "emerald" -> R.drawable.bg_header_emerald
            "amber" -> R.drawable.bg_header_amber
            else -> R.drawable.bg_header_violet
        }

        val indicatorColor = when (accent) {
            "coral" -> ContextCompat.getColor(this, R.color.theme_coral)
            "emerald" -> ContextCompat.getColor(this, R.color.theme_emerald)
            "amber" -> ContextCompat.getColor(this, R.color.theme_amber)
            else -> ContextCompat.getColor(this, R.color.theme_violet)
        }

        binding.layoutMainHeader.setBackgroundResource(bgDrawable)
        binding.tabLayout.setSelectedTabIndicatorColor(indicatorColor)
        binding.tabLayout.setTabTextColors(
            ContextCompat.getColor(this, R.color.text_secondary),
            indicatorColor
        )
    }
}
