package com.example.movietracker.adapters;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietracker.interfaces.OnMovieClickListener;
import com.example.movietracker.models.Genre;
import com.example.movietracker.R;
import com.example.movietracker.db.MovieWithGenres;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {

    private List<MovieWithGenres> moviesWithGenres;
    private OnMovieClickListener onMovieClickListener;

    public MoviesAdapter(List<MovieWithGenres> moviesWithGenres,
                         OnMovieClickListener onMovieClickListener) {
        this.moviesWithGenres = moviesWithGenres;
        this.onMovieClickListener = onMovieClickListener;
    }

    public MovieWithGenres getMovieFromPosition(int position){
        return moviesWithGenres.get(position);
    }


    // Метод для фильтрации и обновления списка фильмов
    @SuppressLint("NotifyDataSetChanged")
    public void updateMovieList(List<MovieWithGenres> newMovies) {
        if (newMovies != null) {
            this.moviesWithGenres = newMovies;
            notifyDataSetChanged();  // Уведомляем адаптер о смене данных
        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieWithGenres movieWithGenres  = moviesWithGenres.get(position);
        holder.titleTextView.setText(movieWithGenres.movie.getTitle());
        Integer year = movieWithGenres.movie.getYear();
        Integer rating = movieWithGenres.movie.getRating();
        holder.yearTextView.setText("Год: " + (year != null ? year : "пусто"));
        String genresStr = movieWithGenres.genres.isEmpty()
                ? "пусто"
                : movieWithGenres.genres.stream()
                .map(Genre::getName)
                .collect(Collectors.joining(", "));
        holder.genreTextView.setText("Жанры: " + genresStr);
        holder.ratingTextView.setText("Оценка: " + (rating != null ? rating : "пусто"));

        holder.itemView.setOnClickListener(v -> {
            if (onMovieClickListener != null) {
                onMovieClickListener.onMovieClick(movieWithGenres);  // Передаем выбранный фильм
            }
        });
    }

    // Метод для удаления фильма по позиции
    public void removeMovieElement(int position) {
        moviesWithGenres.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return moviesWithGenres.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView yearTextView;
        TextView genreTextView;
        TextView ratingTextView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.movieTitle);
            yearTextView = itemView.findViewById(R.id.movieYear);
            genreTextView = itemView.findViewById(R.id.movieGenres);
            ratingTextView = itemView.findViewById(R.id.movieRating);
        }
    }

}