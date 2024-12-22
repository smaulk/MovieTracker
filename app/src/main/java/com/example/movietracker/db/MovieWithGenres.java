package com.example.movietracker.db;

import com.example.movietracker.models.Genre;
import com.example.movietracker.models.Movie;

import java.io.Serializable;
import java.util.List;

public class MovieWithGenres implements Serializable {
    public Movie movie;
    public List<Genre> genres;

    public MovieWithGenres(Movie movie, List<Genre> genres)
    {
        this.movie = movie;
        this.genres = genres;
    }
}
