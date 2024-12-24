package com.example.movietracker.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.movietracker.dao.GenreDao;
import com.example.movietracker.dao.MovieDao;
import com.example.movietracker.dao.MovieGenreJoinDao;
import com.example.movietracker.enums.StatusConverter;
import com.example.movietracker.models.Genre;
import com.example.movietracker.models.Movie;
import com.example.movietracker.models.MovieGenreJoin;
import com.example.movietracker.providers.GenreProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {Movie.class, Genre.class, MovieGenreJoin.class}, version = 1)
@TypeConverters({StatusConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "movies-db";

    public abstract MovieDao movieDao();
    public abstract GenreDao genreDao();
    public abstract MovieGenreJoinDao movieGenreJoinDao();

    private static volatile AppDatabase INSTANCE;

    // Фабричный метод для создания базы данных
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .addCallback(populateCallback) // Добавляем Callback
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Пересоздание объекта базы данных
    public static void recreateDatabase(Context context) {
        // Убираем INSTANCE, чтобы создался новый
        INSTANCE = null;
        // Пересоздаем базу данных
        getInstance(context);
    }


    // Callback для заполнения данных
    private static final RoomDatabase.Callback populateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase database = INSTANCE;
                if (database != null) {
                    populateDatabase(database);
                }
            });
        }
    };

    // Метод для предварительного заполнения базы
    private static void populateDatabase(AppDatabase db) {
        GenreDao dao = db.genreDao();
        List<String> genresNames = GenreProvider.getGenres();
        List<Genre> genres = new ArrayList<Genre>();
        for (String genreName : genresNames) {
            genres.add(new Genre(genreName));
        }

        dao.insertAll(genres);
    }
}
