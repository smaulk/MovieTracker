package com.example.movietracker.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.movietracker.enums.Status;
import com.example.movietracker.models.Movie;

import java.util.List;

@Dao
public interface MovieDao {
    @Insert
    long insert(Movie movie);

    // Вставка списка объектов Genre
    @Insert
    void insertAll(List<Movie> movies);

    @Query("SELECT * FROM movies")
    List<Movie> getAllMovies();

    @Query("SELECT * FROM movies " +
            "WHERE status=:status")
    List<Movie> getAllMoviesByStatus(final Status status);

    @Update
    void update(Movie movie);

    @Delete
    void delete(Movie movie);
}