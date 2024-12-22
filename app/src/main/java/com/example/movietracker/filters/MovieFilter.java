package com.example.movietracker.filters;

import com.example.movietracker.db.MovieWithGenres;
import com.example.movietracker.enums.Status;
import com.example.movietracker.models.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovieFilter {

    private String title;

    private Status status;

    private Integer ratingMin;
    private Integer ratingMax;

    private Integer yearMin;
    private Integer yearMax;

    private List<Genre> includedGenres;

    public void setTitle(String title) {
        this.title = title.isEmpty()
                ? null
                : title;
    }
    public String getTitle() {return this.title;}

    public void setStatus(Status status) {
        this.status = status;
    }
    public Status getStatus() {return this.status;}

    public void setRatingMin(Integer ratingMin) {
        this.ratingMin = ratingMin;
    }
    public Integer getRatingMin() { return this.ratingMin; }

    public void setRatingMax(Integer ratingMax) {
        this.ratingMax = ratingMax;
    }
    public Integer getRatingMax() { return this.ratingMax; }

    public void setYearMin(Integer yearMin) {
        this.yearMin = yearMin;
    }
    public Integer getYearMin() { return this.yearMin; }

    public void setYearMax(Integer yearMax) {
        this.yearMax = yearMax;
    }
    public Integer getYearMax() { return this.yearMax; }

    public void setGenres(List<Genre> includedGenres) {
        this.includedGenres = includedGenres;
    }
    public void addGenre(Genre includedGenre) {
        this.includedGenres.add(includedGenre);
    }
    public List<Genre> getGenres() { return this.includedGenres; }



    public void clearTitle() {
        this.title = null;
    }
    public void clearStatus() {
        this.status = null;
    }
    public void clearRatingMin() {
        this.ratingMin = null;
    }
    public void clearRatingMax() {
        this.ratingMax = null;
    }
    public void clearYearMin() {
        this.yearMin = null;
    }
    public void clearYearMax() {
        this.yearMax = null;
    }
    public void clearGenres() {
        this.includedGenres = new ArrayList<>();
    }
    public void clearAll() {
        clearTitle();
        clearStatus();
        clearRatingMin();
        clearRatingMax();
        clearYearMin();
        clearYearMax();
        clearGenres();
    }


    public MovieFilter() {
        // Инициализация значений по умолчанию
        clearAll();
    }

    // Метод для применения фильтра к списку
    public List<MovieWithGenres> applyFilter(List<MovieWithGenres> baseMovies) {
        return baseMovies.stream()
                .filter(this::filterByTitle)
                .filter(this::filterByStatus)
                .filter(this::filterByRating)
                .filter(this::filterByYear)
                .filter(this::filterByGenres)
                .collect(Collectors.toList());
    }

    private boolean filterByTitle(MovieWithGenres movieWithGenres) {
        // Или фильтра по названию нет, или название фильма начинается с  названия из фильтра
        return title == null ||
                title.isEmpty() ||
                movieWithGenres.movie.getTitle().toLowerCase().startsWith(title.toLowerCase());
    }

    private boolean filterByStatus(MovieWithGenres movieWithGenres) {
        // Или фильтра по статусу нет, или статус фильма совпадает с статусом из фильтра
        return status == null ||
                movieWithGenres.movie.getStatus() == status;
    }

    private boolean isWithinRange(Integer value, Integer min, Integer max) {
        return (min == null || value >= min) && (max == null || value <= max);
    }

    private boolean filterByRating(MovieWithGenres movieWithGenres) {
        // Если фильтров по рейтингу нет, возвращаем true
        if (ratingMin == null && ratingMax == null) {
            return true;
        }
        Integer rating = movieWithGenres.movie.getRating();
        // Если у фильма нет рейтинга, и есть фильтр по рейтингу, возвращаем false
        if (rating == null) {
            return false;
        }
        // Проверяем рейтинг на соответствие фильтрам
        return isWithinRange(rating, ratingMin, ratingMax);
    }

    private boolean filterByYear(MovieWithGenres movieWithGenres) {
        // Если фильтров по году нет, возвращаем true
        if (yearMin == null && yearMax == null) {
            return true;
        }
        Integer year = movieWithGenres.movie.getYear();
        // Если в фильме нет года, и есть фильтр по году, возвращаем false
        if (year == null) {
            return false;
        }
        // Проверяем год на соответствие фильтрам
        return isWithinRange(year, yearMin, yearMax);
    }

    private boolean filterByGenres(MovieWithGenres movieWithGenres) {
        // Если в фильтре жанров нет, возвращаем true
        if (includedGenres == null || includedGenres.isEmpty()) {
            return true;
        }
        // Проверяем, что фильм содержит каждый жанр из фильтра
        for (Genre genre : includedGenres) {
            boolean a = movieWithGenres.genres.contains(genre);
            if (!movieWithGenres.genres.contains(genre)) {
                return false;
            }
        }
        return true;
    }
}
