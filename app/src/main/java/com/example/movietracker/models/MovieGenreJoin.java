package com.example.movietracker.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        primaryKeys = {"movieId", "genreId"},
        foreignKeys = {
                @ForeignKey(entity = Movie.class, parentColumns = "id_movie", childColumns = "movieId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Genre.class, parentColumns = "id_genre", childColumns = "genreId", onDelete = ForeignKey.CASCADE)
        },
        tableName = "movie_genre_join"
)
public class MovieGenreJoin {
    @ColumnInfo(name = "movieId")
    private int movieId;
    @ColumnInfo(name = "genreId")
    private int genreId;

    public MovieGenreJoin(int movieId, int genreId)
    {
        this.movieId = movieId;
        this.genreId = genreId;
    }

    // Getters and setters
    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }
}
