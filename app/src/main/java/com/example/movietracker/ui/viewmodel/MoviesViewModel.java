package com.example.movietracker.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.room.Transaction;

import com.example.movietracker.db.AppDatabase;
import com.example.movietracker.db.MovieWithGenreData;
import com.example.movietracker.db.MovieWithGenres;
import com.example.movietracker.enums.Status;
import com.example.movietracker.interfaces.ResultCallback;
import com.example.movietracker.models.Genre;
import com.example.movietracker.models.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MoviesViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final ExecutorService executorService;

    public MoviesViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    // Метод для получения данных
    public void getAllMoviesList(ResultCallback<List<MovieWithGenres>> callback) {
        executorService.execute(() -> {
            List<MovieWithGenreData> movieWithGenreDataList;
            movieWithGenreDataList = db.movieGenreJoinDao().getAllMoviesWithGenre();

            List<MovieWithGenres> result = processMovieWithGenreData(movieWithGenreDataList);
            callback.onResult(result);
        });
    }

    private List<MovieWithGenres> processMovieWithGenreData(List<MovieWithGenreData> movieWithGenreDataList) {
        Map<Integer, MovieWithGenres> movieMap = new HashMap<>();

        for (MovieWithGenreData mwg : movieWithGenreDataList) {
            Movie movie = mwg.movie;
            Genre genre = mwg.genre;

            if (movieMap.containsKey(movie.getId())) {
                if (genre != null) {
                    Objects.requireNonNull(movieMap.get(movie.getId())).genres.add(genre);
                }
            } else {
                List<Genre> genres = new ArrayList<>();
                if (genre != null) {
                    genres.add(genre);
                }
                movieMap.put(movie.getId(), new MovieWithGenres(movie, genres));
            }
        }

        return new ArrayList<>(movieMap.values());
    }


    public void addMovie(Movie movie) {
        executorService.execute(() -> {
            db.movieDao().insert(movie);
        });
    }

    public void addMovieWithGenres(Movie movie, List<Integer> genreIds) {
        executorService.execute(() -> {
            long id = db.movieDao().insert(movie);
            db.movieGenreJoinDao().insertGenresToMovie((int) id, genreIds);
        });
    }

    public void getAllGenresList(ResultCallback<List<Genre>> callback) {
        executorService.execute(() -> {
            List<Genre> genres = db.genreDao().getAllGenres();
            callback.onResult(genres);
        });
    }

    public void updateMovie(Movie movie) {
        executorService.execute(() -> {
            db.movieDao().update(movie);
        });
    }

    public void updateMovieWithGenres(Movie movie, List<Integer> removeGenreIds, List<Integer> addGenreIds) {
        executorService.execute(() -> {
            db.movieDao().update(movie);
            db.movieGenreJoinDao().updateGenresForMovie(movie.getId(), removeGenreIds, addGenreIds);
        });
    }

    public void addGenresToMovie(int movieId, List<Integer> genreIds) {
        executorService.execute(() -> {
            db.movieGenreJoinDao().insertGenresToMovie(movieId, genreIds);
        });
    }

    public void removeGenresFromMovie(int movieId, List<Integer> genreIds) {
        executorService.execute(() -> {
            db.movieGenreJoinDao().deleteGenresFromMovie(movieId, genreIds);
        });
    }

    public void updateGenresForMovie(int movieId, List<Integer> removeGenreIds, List<Integer> addGenreIds) {
        executorService.execute(() -> {
            db.movieGenreJoinDao().updateGenresForMovie(movieId, removeGenreIds, addGenreIds);
        });
    }

    public void deleteMovie(Movie movie) {
        executorService.execute(() -> {
            db.movieDao().delete(movie);
        });
    }
}