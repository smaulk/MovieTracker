package com.example.movietracker.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.movietracker.R;
import com.example.movietracker.db.DatabaseExporter;
import com.example.movietracker.db.DatabaseImporter;
import com.example.movietracker.enums.Theme;
import com.example.movietracker.enums.ThemeConverter;
import com.example.movietracker.ui.MainActivity;

import java.io.IOException;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Устанавливаем слушатели на кнопки для смены темы
        view.findViewById(R.id.systemThemeButton)
                .setOnClickListener(v -> setTheme(Theme.SYSTEM));
        view.findViewById(R.id.lightThemeButton)
                .setOnClickListener(v -> setTheme(Theme.LIGHT));
        view.findViewById(R.id.darkThemeButton)
                .setOnClickListener(v -> setTheme(Theme.DARK));

        view.findViewById(R.id.importButton)
                .setOnClickListener(v -> DatabaseImporter.startFilePicker(requireActivity()));
        view.findViewById(R.id.exportButton)
                .setOnClickListener(v -> makeExport());

        return view;
    }


    private void setTheme(Theme theme) {
        // Записываем выбранную тему в SharedPreferences
        SharedPreferences preferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("theme", ThemeConverter.toString(theme));
        editor.apply();
        // Пересоздаем активность
        requireActivity().recreate();
    }

    private void makeExport()
    {
        MainActivity activity = (MainActivity) requireActivity();
        try {
            DatabaseExporter.exportDatabase(activity);
            Toast.makeText(activity,
                    "Данные успешно сохранены! Путь:\n\"Загрузки\"-> \"MovieTrackerExport\"",
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity,
                    "Произошла ошибка при сохранении данных!",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
