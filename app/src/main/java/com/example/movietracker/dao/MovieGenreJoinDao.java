package com.example.movietracker.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.movietracker.db.MovieWithGenreData;
import com.example.movietracker.enums.Status;
import com.example.movietracker.models.Genre;
import com.example.movietracker.models.Movie;
import com.example.movietracker.models.MovieGenreJoin;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface MovieGenreJoinDao {

    @Insert
    void insert(MovieGenreJoin movieGenreJoin);

    @Delete
    void delete(MovieGenreJoin movieGenreJoin);

    @Query("SELECT * FROM movies INNER JOIN movie_genre_join " +
            "ON movies.id_movie=movie_genre_join.movieId " +
            "WHERE movie_genre_join.genreId=:genreId")
    List<Movie> getMoviesForGenre(final int genreId);

    @Query("SELECT * FROM genres INNER JOIN movie_genre_join " +
            "ON genres.id_genre=movie_genre_join.genreId " +
            "WHERE movie_genre_join.movieId=:movieId")
    List<Genre> getGenresForMovie(final int movieId);

    @Query("SELECT * FROM movies " +
            "LEFT JOIN movie_genre_join ON movies.id_movie = movie_genre_join.movieId " +
            "LEFT JOIN genres ON genres.id_genre = movie_genre_join.genreId")
    List<MovieWithGenreData> getAllMoviesWithGenre();

    @Query("SELECT * FROM movies " +
            "LEFT JOIN movie_genre_join ON movies.id_movie = movie_genre_join.movieId " +
            "LEFT JOIN genres ON genres.id_genre = movie_genre_join.genreId " +
            "WHERE movies.status=:status")
    List<MovieWithGenreData> getAllMoviesWithGenreByStatus(final Status status);

    // Вставка переданных жанров для одного фильма
    default void insertGenresToMovie(int movieId, List<Integer> genreIds) {
        if(genreIds.isEmpty()){
            return;
        }
        List<MovieGenreJoin> movieGenreJoins = getMovieGenreJoinList(movieId, genreIds);
        // Вставляем все связи за один раз
        if(!movieGenreJoins.isEmpty()){
            insertAll(movieGenreJoins);
        }
    }

    // Вставка списка связей
    @Insert
    void insertAll(List<MovieGenreJoin> movieGenreJoins);


    // Удаление переданных жанров для одного фильма
    default void deleteGenresFromMovie(int movieId, List<Integer> genreIds) {
        if(genreIds.isEmpty()){
            return;
        }
        List<MovieGenreJoin> movieGenreJoins = getMovieGenreJoinList(movieId, genreIds);
        // Удаляем все связи за один раз
        if(!movieGenreJoins.isEmpty()){
            deleteAll(movieGenreJoins);
        }
    }

    @Delete
    void deleteAll(List<MovieGenreJoin> movieGenreJoins);

    @Transaction
    default void updateGenresForMovie(int movieId, List<Integer> deleteGenreIds, List<Integer> insertGenreIds){
        deleteGenresFromMovie(movieId, deleteGenreIds);
        insertGenresToMovie(movieId, insertGenreIds);
    }

    default List<MovieGenreJoin> getMovieGenreJoinList(int movieId, List<Integer> genreIds)
    {
        // Создаем связи для всех жанров
        List<MovieGenreJoin> movieGenreJoins = new ArrayList<>();
        for (Integer genreId : genreIds) {
            MovieGenreJoin movieGenreJoin = new MovieGenreJoin(movieId, genreId);
            movieGenreJoins.add(movieGenreJoin);
        }

        return movieGenreJoins;
    }
}
