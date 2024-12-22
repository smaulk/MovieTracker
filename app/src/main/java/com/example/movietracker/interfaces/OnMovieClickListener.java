package com.example.movietracker.interfaces;

import com.example.movietracker.db.MovieWithGenres;

public interface OnMovieClickListener {
    void onMovieClick(MovieWithGenres movie);
}