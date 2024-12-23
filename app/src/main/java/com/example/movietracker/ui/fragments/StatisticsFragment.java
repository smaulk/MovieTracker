package com.example.movietracker.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietracker.R;
import com.example.movietracker.db.MovieWithGenres;
import com.example.movietracker.enums.Status;
import com.example.movietracker.models.Genre;
import com.example.movietracker.ui.viewmodel.MoviesViewModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsFragment extends Fragment {
    private MoviesViewModel moviesViewModel;
    private List<MovieWithGenres> moviesWithGenres;

    private TextView movieCountTextView, avgRatingTextView, toWatchCountTextView,
            watchingCountTextView, watchedCountTextView,
            moviesByYearTextView, topMoviesTextView, topGenresTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_statistics, container, false);
        moviesViewModel = new ViewModelProvider(this).get(MoviesViewModel.class);
        // Инициализация всех элементов интерфейса
        movieCountTextView = view.findViewById(R.id.movieCountTextView);
        avgRatingTextView = view.findViewById(R.id.avgRatingTextView);
        toWatchCountTextView = view.findViewById(R.id.toWatchCountTextView);
        watchingCountTextView = view.findViewById(R.id.watchingCountTextView);
        watchedCountTextView = view.findViewById(R.id.watchedCountTextView);
        moviesByYearTextView = view.findViewById(R.id.moviesByYearTextView);
        topMoviesTextView = view.findViewById(R.id.topMoviesTextView);
        topGenresTextView = view.findViewById(R.id.topGenresTextView);


        moviesViewModel.getAllMoviesList(result -> {
            moviesWithGenres = result;
            getGeneralInfo();
        });

        return view;
    }

    private void getGeneralInfo(){
        int movieCount = moviesWithGenres.size();
        double avgRating = calculateAvgRating();

        int countToWatch = countByStatus(Status.TO_WATCH);
        int countWatching = countByStatus(Status.WATCHING);
        int countWatched = countByStatus(Status.WATCHED);

        int countBefore80 = countMoviesByYearRange(0, 1980);
        int count8090 = countMoviesByYearRange(1981, 1990);
        int count9000 = countMoviesByYearRange(1991, 2000);
        int count0010 = countMoviesByYearRange(2001, 2010);
        int count1020 = countMoviesByYearRange(2011, 2020);
        int count2030 = countMoviesByYearRange(2021, 2030);

        List<MovieWithGenres> top5Movies = getTopRatedMovies(5);
        List<String> top5Genres = getTopGenres(5);

        // Подсчет фильмов по годам
        String yearsStats = "До 1980: " + countBefore80 + "\n" +
                "1981-1990: " + count8090 + "\n" +
                "1991-2000: " + count9000 + "\n" +
                "2001-2010: " + count0010 + "\n" +
                "2011-2020: " + count1020 + "\n" +
                "2021-2030: " + count2030;

        // Топ 5 фильмов
        StringBuilder topMoviesString = new StringBuilder();
        for (int i = 0; i < top5Movies.size(); i++) {
            MovieWithGenres movie = top5Movies.get(i);
            topMoviesString.append(movie.movie.getTitle())
                    .append(" (Рейтинг: ")
                    .append(movie.movie.getRating() != null
                            ? movie.movie.getRating()
                            : "отсутствует")
                    .append(")");
            // Добавляем \n, если это не последний элемент
            if (i < top5Movies.size() - 1) {
                topMoviesString.append("\n");
            }
        }

        // Топ 5 жанров
        StringBuilder topGenresString = new StringBuilder();
        for (int i = 0; i < top5Genres.size(); i++) {
            String genre = top5Genres.get(i);
            topGenresString.append(genre);
            if (i < top5Genres.size() - 1) {
                topGenresString.append("\n");
            }
        }

        requireActivity().runOnUiThread(() -> {
            // Отображение информации о фильмах и рейтинге
            movieCountTextView.setText("Количество фильмов: " + movieCount);
            avgRatingTextView.setText("Средний рейтинг: " + String.format("%.1f", avgRating));

            toWatchCountTextView.setText("Хочу посмотреть: " + countToWatch);
            watchingCountTextView.setText("Смотрю: " + countWatching);
            watchedCountTextView.setText("Просмотрено: " + countWatched);

            moviesByYearTextView.setText(yearsStats);
            topMoviesTextView.setText(topMoviesString.toString());
            topGenresTextView.setText(topGenresString.toString());
        });
    }

    private int countByStatus(Status status){
        int count = 0;
        for (MovieWithGenres movieWithGenre: moviesWithGenres) {
            if(movieWithGenre.movie.getStatus() == status){
                count++;
            }
        }

        return count;
    }


    private double calculateAvgRating(){
        double totalRating = 0;
        int count = 0;

        for (MovieWithGenres movieWithGenres : moviesWithGenres) {
            if (movieWithGenres.movie.getRating() != null) {
                totalRating += movieWithGenres.movie.getRating();
                count++;
            }
        }

        return count > 0 ? totalRating / count : 0;
    }

    private int countMoviesByYearRange(int startYear, int endYear) {
        int count = 0;

        for (MovieWithGenres movieWithGenres : moviesWithGenres) {
            Integer releaseYear = movieWithGenres.movie.getYear(); // Предполагаем, что этот метод существует
            if (releaseYear!=null && releaseYear >= startYear && releaseYear <= endYear) {
                count++;
            }
        }

        return count;
    }

    private List<MovieWithGenres> getTopRatedMovies(int count) {
        return moviesWithGenres.stream()
                .sorted((m1, m2) -> Double.compare(
                        m2.movie.getRating() != null ? m2.movie.getRating() : 0,
                        m1.movie.getRating() != null ? m1.movie.getRating() : 0
                )) // Если рейтинг null, ставим 0
                .limit(count) // Берем только count фильмов
                .collect(Collectors.toList());
    }

    public List<String> getTopGenres(int count) {
        // Создаем карту для подсчета количества вхождений каждого жанра
        Map<Genre, Long> genreCountMap = moviesWithGenres.stream()
                .flatMap(movieWithGenres -> movieWithGenres.genres.stream()) // Разворачиваем жанры каждого фильма в поток
                .collect(Collectors.groupingBy(genre -> genre, Collectors.counting())); // Подсчитываем, сколько раз каждый жанр встречается

        // Сортируем жанры по количеству вхождений (по убыванию) и берем первые 'count' жанров
        return genreCountMap.entrySet().stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue())) // Сортируем по количеству
                .limit(count) // Ограничиваем топ 'count'
                .map(entry -> entry.getKey().getName() + " (Фильмов: " + entry.getValue() + ")") // Формируем строку вида "Жанр (Количество фильмов)"
                .collect(Collectors.toList());
    }
}
