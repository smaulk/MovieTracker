package com.example.movietracker.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.movietracker.R;

import com.example.movietracker.ui.fragments.FilterDialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Получаем текущую тему из SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        String currentTheme = preferences.getString("theme", "dark");

        // Устанавливаем тему в зависимости от выбранной
        if ("dark".equals(currentTheme)) {
            setTheme(R.style.Theme_MovieTracker);
        } else {
            setTheme(R.style.Theme_MovieTracker_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получаем NavHostFragment и NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        NavController navController = navHostFragment.getNavController();

        // Настройка BottomNavigationView для навигации
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Добавим слушатель для перехода между фрагментами
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.saveMovieFragment) {
                // Скрыть BottomNavigationView, когда находимся в SaveMovieFragment
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                // Показать BottomNavigationView, когда вернулись на MoviesFragment
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
    }
}