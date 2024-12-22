package com.example.movietracker.db;

import androidx.room.Embedded;

import com.example.movietracker.models.Genre;
import com.example.movietracker.models.Movie;

import org.jetbrains.annotations.Nullable;

public class MovieWithGenreData {
    @Embedded
    public Movie movie;

    @Embedded
    public Genre genre;
}