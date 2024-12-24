package com.example.movietracker.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DatabaseExporter {

    public static String exportDirectory = "/MovieTrackerExport";
    private static String fileName = "MovieTrackerDB.db";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void exportDatabase(Context context) throws IOException {
        // Получаем путь к базе данных
        File currentDB = context.getDatabasePath(AppDatabase.DATABASE_NAME);

        // Получаем контент-резолвер для работы с MediaStore
        ContentResolver resolver = context.getContentResolver();

        // Путь для сохранения базы данных с именем MovieTrackerDB.db в папке "Загрузки"
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // Имя файла
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + exportDirectory);

        // Получаем URI для сохранения в MediaStore
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        try (OutputStream outputStream = resolver.openOutputStream(uri);
             FileInputStream inputStream = new FileInputStream(currentDB)) {

            // Копируем базу данных в новый файл в MediaStore
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            Log.d("DatabaseExporter", "Database exported to: " + uri);
        }

    }

}
