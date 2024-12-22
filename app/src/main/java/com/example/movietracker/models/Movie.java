package com.example.movietracker.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.movietracker.enums.Status;
import com.example.movietracker.enums.StatusConverter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity(tableName = "movies")
public class Movie {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_movie")
    private int id;

    @ColumnInfo(name = "title")
    @NotNull
    private String title;

    @ColumnInfo(name = "year")
    @Nullable
    private Integer year;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "status")
    @TypeConverters(StatusConverter.class)
    private Status status;

    @ColumnInfo(name = "rating")
    @Nullable
    private Integer rating;

    @ColumnInfo(name = "comment")
    private String comment;


    public Movie (@NonNull String title, @Nullable Integer year, String description,
                  Status status, @Nullable Integer rating, String comment) {
        this.title = title;
        this.year = year;
        this.description = description;
        this.status = status;
        this.rating = rating;
        this.comment = comment;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    @Nullable
    public Integer getYear() { return year; }
    public void setYear(@Nullable Integer year) { this.year = year; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Nullable
    public Integer getRating() { return rating; }
    public void setRating(@Nullable Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

}
