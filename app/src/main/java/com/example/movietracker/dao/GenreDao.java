package com.example.movietracker.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.movietracker.models.Genre;
import com.example.movietracker.models.Movie;

import java.util.List;

@Dao
public interface GenreDao {
    @Insert
    long insert(Genre genre);

    // Вставка списка объектов Genre
    @Insert
    void insertAll(List<Genre> genres);

    @Query("SELECT * FROM genres")
    List<Genre> getAllGenres();

    @Delete
    void delete(Genre genre);
}
