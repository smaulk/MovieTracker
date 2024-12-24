package com.example.movietracker.db;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.movietracker.exceptions.InvalidFileFormatException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseImporter {

    public static final int PICK_FILE_REQUEST_CODE = 1;

    // Метод для запуска выбора файла
    public static void startFilePicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/octet-stream"); // Фильтруем только бинарные файлы
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        String downloadUri = "content://com.android.externalstorage.documents/document/primary%3ADownloads%2F";
        // Устанавливаем начальную папку (папка Загрузок)
        Uri uri = Uri.parse(downloadUri);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);

        // Запускаем выбор файла
        activity.startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    // Этот метод вызывается, когда пользователь выбрал файл
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void handleFilePickerResult(Context context, int requestCode, int resultCode, Intent data) throws IOException, InvalidFileFormatException {
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                // Получаем имя файла и проверяем его расширение
                String fileName = getFileName(context, uri);
                if (fileName != null && !fileName.endsWith(".db")) {
                    throw new InvalidFileFormatException("необходим формат .db");
                }

                // Получаем InputStream из URI
                try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                    if (inputStream != null) {
                        // Путь к базе данных, куда нужно импортировать
                        File currentDB = context.getDatabasePath(AppDatabase.DATABASE_NAME);

                        // Если база данных существует, удалим ее перед копированием
                        if (currentDB.exists()) {
                            // Закрываем текущую базу данных, если она открыта
                            context.deleteDatabase(AppDatabase.DATABASE_NAME); // Закрывает и удаляет старую БД
                        }

                        // Создаем новую базу данных
                        File newDB = new File(currentDB.getParent(), AppDatabase.DATABASE_NAME);

                        // Создаем OutputStream для записи в базу данных
                        try (OutputStream outputStream = new FileOutputStream(newDB)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            // Копируем данные из inputStream в новую базу данных
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            Log.d("DatabaseImporter", "Database imported successfully from: " + uri.getPath());
                        }
                    }
                }
            }
        }
    }

    // Метод для получения имени файла из Uri
    private static String getFileName(Context context, Uri uri) {
        String fileName = null;
        String[] projection = { MediaStore.MediaColumns.DISPLAY_NAME };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            cursor.close();
        }
        return fileName;
    }

}
