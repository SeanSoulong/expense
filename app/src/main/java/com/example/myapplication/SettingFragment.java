package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.myapplication.utill.LocaleHelper;

public class SettingFragment extends PreferenceFragmentCompat {
    private static final String PREF_LANGUAGE = "app_language";

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        ListPreference languagePreference = findPreference(PREF_LANGUAGE);
        if (languagePreference != null) {
            // Listen for language changes
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String language = newValue.toString();

                // Update the language setting in LocaleHelper
                LocaleHelper.setLocale(requireActivity(), language);

                // Recreate activity to apply the new language
                requireActivity().recreate();

                return true;
            });
        }
    }
}
