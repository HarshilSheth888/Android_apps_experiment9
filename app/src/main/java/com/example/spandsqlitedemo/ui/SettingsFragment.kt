package com.example.spandsqlitedemo.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.spandsqlitedemo.R
import com.example.spandsqlitedemo.data.PreferenceManager
import com.example.spandsqlitedemo.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefManager: PreferenceManager

    interface OnThemeAccentChangedListener {
        fun onThemeAccentChanged(accent: String)
    }

    private var themeListener: OnThemeAccentChangedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnThemeAccentChangedListener) {
            themeListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())

        loadPreferencesToUi()
        setupListeners()
        updateRawXmlInspector()
    }

    private fun loadPreferencesToUi() {
        binding.tvGreetingTitle.text = "Hello, ${prefManager.userName} 👋"
        binding.etUserName.setText(prefManager.userName)

        binding.switchCompactView.isChecked = prefManager.isCompactView
        binding.switchAutoSort.isChecked = prefManager.isAutoSortCompleted
        binding.switchNotifications.isChecked = prefManager.isNotificationsEnabled

        updateAccentButtonsSelection(prefManager.themeAccent)
        updateBannerGradient(prefManager.themeAccent)
    }

    private fun setupListeners() {
        // User Name Live Update to SharedPreferences
        binding.etUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = s?.toString()?.trim() ?: "Alex"
                prefManager.userName = if (name.isEmpty()) "Alex" else name
                binding.tvGreetingTitle.text = "Hello, ${prefManager.userName} 👋"
                updateRawXmlInspector()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Accent Theme Buttons
        binding.btnAccentViolet.setOnClickListener {
            setThemeAccent("violet")
        }
        binding.btnAccentCoral.setOnClickListener {
            setThemeAccent("coral")
        }
        binding.btnAccentEmerald.setOnClickListener {
            setThemeAccent("emerald")
        }
        binding.btnAccentAmber.setOnClickListener {
            setThemeAccent("amber")
        }

        // Switches
        binding.switchCompactView.setOnClickListener {
            prefManager.isCompactView = binding.switchCompactView.isChecked
            updateRawXmlInspector()
        }

        binding.switchAutoSort.setOnClickListener {
            prefManager.isAutoSortCompleted = binding.switchAutoSort.isChecked
            updateRawXmlInspector()
        }

        binding.switchNotifications.setOnClickListener {
            prefManager.isNotificationsEnabled = binding.switchNotifications.isChecked
            updateRawXmlInspector()
        }

        // Inspector Buttons
        binding.btnRefreshInspector.setOnClickListener {
            updateRawXmlInspector()
            Toast.makeText(requireContext(), "SharedPreferences XML refreshed", Toast.LENGTH_SHORT).show()
        }

        binding.btnResetPrefs.setOnClickListener {
            prefManager.resetToDefaults()
            loadPreferencesToUi()
            updateRawXmlInspector()
            themeListener?.onThemeAccentChanged(prefManager.themeAccent)
            Toast.makeText(requireContext(), "Preferences reset to default values", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setThemeAccent(accent: String) {
        prefManager.themeAccent = accent
        updateAccentButtonsSelection(accent)
        updateBannerGradient(accent)
        updateRawXmlInspector()
        themeListener?.onThemeAccentChanged(accent)
    }

    private fun updateAccentButtonsSelection(accent: String) {
        binding.btnAccentViolet.setIconResource(if (accent == "violet") R.drawable.ic_check else 0)
        binding.btnAccentCoral.setIconResource(if (accent == "coral") R.drawable.ic_check else 0)
        binding.btnAccentEmerald.setIconResource(if (accent == "emerald") R.drawable.ic_check else 0)
        binding.btnAccentAmber.setIconResource(if (accent == "amber") R.drawable.ic_check else 0)
    }

    private fun updateBannerGradient(accent: String) {
        val bgDrawable = when (accent) {
            "coral" -> R.drawable.bg_header_coral
            "emerald" -> R.drawable.bg_header_emerald
            "amber" -> R.drawable.bg_header_amber
            else -> R.drawable.bg_header_violet
        }
        binding.bannerSettings.setBackgroundResource(bgDrawable)
    }

    private fun updateRawXmlInspector() {
        val allPrefs = prefManager.getAllPreferences()
        val xmlBuilder = StringBuilder()

        xmlBuilder.append("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n")
        xmlBuilder.append("<map>\n")

        allPrefs.entries.sortedBy { it.key }.forEach { (key, value) ->
            when (value) {
                is String -> xmlBuilder.append("  <string name=\"$key\">$value</string>\n")
                is Boolean -> xmlBuilder.append("  <boolean name=\"$key\" value=\"$value\" />\n")
                is Int -> xmlBuilder.append("  <int name=\"$key\" value=\"$value\" />\n")
                is Long -> xmlBuilder.append("  <long name=\"$key\" value=\"$value\" />\n")
                is Float -> xmlBuilder.append("  <float name=\"$key\" value=\"$value\" />\n")
                else -> xmlBuilder.append("  <entry name=\"$key\">$value</entry>\n")
            }
        }

        xmlBuilder.append("</map>")
        binding.tvRawPrefsContent.text = xmlBuilder.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
