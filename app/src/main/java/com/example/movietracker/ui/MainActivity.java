package com.example.movietracker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.movietracker.R;

import com.example.movietracker.db.AppDatabase;
import com.example.movietracker.db.DatabaseImporter;
import com.example.movietracker.enums.Theme;
import com.example.movietracker.enums.ThemeConverter;
import com.example.movietracker.exceptions.InvalidFileFormatException;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получаем NavHostFragment и NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        navController = navHostFragment.getNavController();

        // Настройка BottomNavigationView для навигации
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        if (navController != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                navigateWithAnimation(item);
                return true;
            });
        }

        // Добавим слушатель для перехода между фрагментами
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
           // Убираем/Показываем нижнюю панель навигации
            bottomNavigationView.setVisibility(
                    destination.getId() == R.id.saveMovieFragment
                            ? View.GONE
                            :View.VISIBLE
            );
            // Изменяем цвет статус бара, в зависимости от фрагмента
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                setStatusBarColorFromAttr(
                        destination.getId() == R.id.moviesFragment
                                ? com.google.android.material.R.attr.colorPrimary
                                : com.google.android.material.R.attr.colorOnBackground
                );
            }, 100);
        });
    }

    private void navigateWithAnimation(@NonNull MenuItem item) {
        if (navController == null) return;
        // Настраиваем анимации переходов
        NavOptions navOptions = new NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.fade_in)
                .setPopExitAnim(R.anim.fade_out)
                .build();

        // Выполняем навигацию
        try {
            navController.navigate(item.getItemId(), null, navOptions);
        } catch (IllegalArgumentException e) {
        }
    }


    private void applyTheme() {
        // Получаем текущую тему из SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        Theme currentTheme = ThemeConverter.fromString(
                preferences.getString("theme", ThemeConverter.toString(Theme.SYSTEM))
        );

        if(currentTheme == Theme.SYSTEM) {
            currentTheme = isSystemDarkThemeActive(this) ? Theme.DARK : Theme.LIGHT;
        }
        // Устанавливаем тему в зависимости от выбранной
        setTheme(currentTheme == Theme.LIGHT
                ? R.style.Theme_MovieTracker_Light
                : R.style.Theme_MovieTracker
        );
    }

    public static boolean isSystemDarkThemeActive(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    // Установить цвет статус бара
    public void setStatusBarColorFromAttr(@AttrRes int attr) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = getColorFromAttr(attr);
            // Устанавливаем цвет статус-бара
            Window window = getWindow();
            if (window != null) {
                window.setStatusBarColor(color);
                setStatusBarAppearance(isColorLight(color));
            }
        }
    }

    // Получить цвет из атрибута
    public int getColorFromAttr(@AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        if (theme.resolveAttribute(attr, typedValue, true)) {
            return typedValue.data; // Получаем значение цвета
        }
        // Возвращаем цвет по умолчанию, если атрибут не найден
        return ContextCompat.getColor(this, android.R.color.black);
    }

    // Проверить, цвет светлый или темный
    public boolean isColorLight(@ColorInt int color) {
        // Извлекаем компоненты RGB
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        // Рассчитываем яркость по формуле: (0.299 * R + 0.587 * G + 0.114 * B)
        double brightness = (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
        // Если яркость выше 0.5, цвет считается светлым
        return brightness > 0.5;
    }

    // Установить цвет текста на статус баре
    public void setStatusBarAppearance(boolean isLightStatusBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Window window = getWindow();
            if (window != null) {
                WindowInsetsController insetsController = window.getInsetsController();
                if (insetsController != null) {
                    // Устанавливаем элементы в зависимости от яркости
                    int appearance = isLightStatusBar
                            ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                            : 0;
                    insetsController.setSystemBarsAppearance(
                            appearance,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                }
            }
        }
    }

    // Обработка загрузки файла
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Проверка результата выбора файла
        if (requestCode == DatabaseImporter.PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                // Импортируем выбранный файл в базу данных
                DatabaseImporter.handleFilePickerResult(this, requestCode, resultCode, data);
                Toast.makeText(this,
                        "Данные успешно загружены!",
                        Toast.LENGTH_SHORT).show();

                AppDatabase.recreateDatabase(this);
                // Получаем ссылку на фрагмент MovieFragment
                navController.navigate(R.id.moviesFragment);

            } catch (InvalidFileFormatException e) {
                // Обрабатываем ошибку, если файл имеет неправильный формат
                Toast.makeText(this,
                        "Выбран файл с неверным форматом: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,
                        "Произошла ошибка при загрузке!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}