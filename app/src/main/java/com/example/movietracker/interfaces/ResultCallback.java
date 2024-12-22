package com.example.movietracker.interfaces;

import com.example.movietracker.db.MovieWithGenres;

import java.util.List;

// Интерфейс для обратного вызова с результатом
public interface ResultCallback<T> {
    void onResult(T result);
}