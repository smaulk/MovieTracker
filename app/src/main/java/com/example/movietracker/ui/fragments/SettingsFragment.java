package com.example.movietracker.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.movietracker.R;

public class SettingsFragment extends Fragment {
    private Button changeThemeButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        changeThemeButton = view.findViewById(R.id.changeThemeButton);

        // Устанавливаем слушатель на кнопку для смены темы
        changeThemeButton.setOnClickListener(v -> {
            toggleTheme();
        });

        return view;
    }


    private void toggleTheme() {
        // Получаем текущую тему из SharedPreferences
        SharedPreferences preferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        String currentTheme = preferences.getString("theme", "dark"); // "dark" - тема по умолчанию

        // Меняем тему
        SharedPreferences.Editor editor = preferences.edit();
        if ("dark".equals(currentTheme)) {
            editor.putString("theme", "light");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Light тема
        } else {
            editor.putString("theme", "dark");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // Dark тема
        }
        editor.apply();

        // Перезапускаем активность, чтобы применить новую тему
        requireActivity().recreate();
    }
}
