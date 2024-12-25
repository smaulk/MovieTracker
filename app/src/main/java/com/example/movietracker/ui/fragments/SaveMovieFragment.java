package com.example.movietracker.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.movietracker.R;
import com.example.movietracker.db.MovieWithGenres;
import com.example.movietracker.enums.Status;
import com.example.movietracker.enums.StatusConverter;
import com.example.movietracker.models.Genre;
import com.example.movietracker.models.Movie;
import com.example.movietracker.ui.viewmodel.MoviesViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SaveMovieFragment extends Fragment implements GenrePickerDialogFragment.GenrePickerDialogListener {

    private EditText titleEditText, yearEditText, descriptionEditText, commentEditText;
    private ScrollView scrollView;
    private RatingBar ratingBar;
    private Spinner statusSpinner;
    private Button chooseGenresButton, saveButton;
    private ImageButton backButton;
    private MoviesViewModel moviesViewModel;
    private NavController navController;
    private List<Genre> genreList;
    private List<Integer> selectedGenreIds;
    private MovieWithGenres oldMovieWithGenres;
    private List<Integer> oldMovieGenreIds;


    public SaveMovieFragment() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save_movie, container, false);
        moviesViewModel = new ViewModelProvider(this).get(MoviesViewModel.class);
        navController = NavHostFragment.findNavController(this);

        titleEditText = view.findViewById(R.id.titleEditText);
        yearEditText = view.findViewById(R.id.yearEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        ratingBar = view.findViewById(R.id.ratingBar);
        commentEditText = view.findViewById(R.id.commentEditText);
        statusSpinner = view.findViewById(R.id.statusSpinner);
        backButton = view.findViewById(R.id.backButton);
        saveButton = view.findViewById(R.id.saveButton);
        chooseGenresButton = view.findViewById(R.id.chooseGenresButton);
        scrollView = view.findViewById(R.id.scrollView);

        descriptionEditText.setOnTouchListener((view1, motionEvent)
                -> removeScroll(descriptionEditText.getLineCount()));
        commentEditText.setOnTouchListener((view1, motionEvent)
                -> removeScroll(commentEditText.getLineCount()));

        selectedGenreIds = new ArrayList<>();
        // Загружаем список жанров
        loadGenres();

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item,
                R.id.spinner_item_text,
                StatusConverter.getRuNames()
        );
        // Устанавливаем адаптер в Spinner
        statusSpinner.setAdapter(statusAdapter);

        backButton.setOnClickListener(v -> backToMoviesFragment());
        saveButton.setOnClickListener(v -> {
            if(saveMovie()){
                navigateToMoviesFragment();
            }
        });
        chooseGenresButton.setOnClickListener(v -> openGenrePickerDialog());

        setMovieData();

        return view;
    }

    private boolean removeScroll(int lineCount) {
        // Убираем скролл ScrollView, если количество линий EditText больше 6
        scrollView.requestDisallowInterceptTouchEvent(lineCount > 6);
        return false;
    }

    private void setMovieData() {
        // Получаем данные о фильме, если они переданы
        if (getArguments() != null) {
            oldMovieWithGenres = (MovieWithGenres) getArguments().getSerializable("movieWithGenres");
            if (oldMovieWithGenres != null) {
                // Предзаполняем данные для редактирования
                titleEditText.setText(oldMovieWithGenres.movie.getTitle());
                Integer year = oldMovieWithGenres.movie.getYear();
                yearEditText.setText(year != null ? String.valueOf(year) : "");
                descriptionEditText.setText(oldMovieWithGenres.movie.getDescription());
                Integer rating = oldMovieWithGenres.movie.getRating();
                ratingBar.setRating(rating != null ? rating : 0);
                commentEditText.setText(oldMovieWithGenres.movie.getComment());
                int status = StatusConverter.toInt(oldMovieWithGenres.movie.getStatus());
                statusSpinner.setSelection(status);
                //Заполняем жанры
                oldMovieGenreIds = getIdListFromGenreList(oldMovieWithGenres.genres);
                onGenresSelected(oldMovieGenreIds);
            }
        }
    }

    private boolean saveMovie() {
        String title = titleEditText.getText().toString();
        String yearString = yearEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        float ratingFloat = ratingBar.getRating();
        String comment = commentEditText.getText().toString();
        int statusNum = statusSpinner.getSelectedItemPosition();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "Название обязательно к заполнению!", Toast.LENGTH_SHORT).show();
            return false;
        }
        Integer year = TextUtils.isEmpty(yearString) ? null : Integer.parseInt(yearString);
        Integer rating = ratingFloat < 1 ? null : (int) ratingFloat;
        Status status = StatusConverter.fromInt(statusNum);

        // Если это создание
        if (oldMovieWithGenres == null) {
            //Создаем новый экземпляр фильма
            Movie movie = new Movie(title, year, description, status, rating, comment);

            //Если жанры не выбраны
            if (selectedGenreIds.isEmpty()) {
                moviesViewModel.addMovie(movie);
                return true;
            }
            moviesViewModel.addMovieWithGenres(movie, selectedGenreIds);
            return true;
        }

        // Если это обновление
        oldMovieWithGenres.movie.setTitle(title);
        oldMovieWithGenres.movie.setYear(year);
        oldMovieWithGenres.movie.setDescription(description);
        oldMovieWithGenres.movie.setStatus(status);
        oldMovieWithGenres.movie.setRating(rating);
        oldMovieWithGenres.movie.setComment(comment);

        // Если жанры не выбраны и их не было, или выбранные жанры не изменились
        if ((selectedGenreIds.isEmpty() && oldMovieGenreIds.isEmpty())
                || checkListsEquals(selectedGenreIds, oldMovieGenreIds)
        ) {
            moviesViewModel.updateMovie(oldMovieWithGenres.movie);
            return true;
        }
        // Жанры, которые выбраны, но ранее их не было
        List<Integer> genresNotInMovie = new ArrayList<>(selectedGenreIds);
        genresNotInMovie.removeAll(oldMovieGenreIds);
        // Жанры, которые были ранее, но уже не выбраны
        List<Integer> genresMissingInSelected = new ArrayList<>(oldMovieGenreIds);
        genresMissingInSelected.removeAll(selectedGenreIds);

        moviesViewModel.updateMovieWithGenres(oldMovieWithGenres.movie, genresMissingInSelected, genresNotInMovie);
        return true;
    }

    private boolean checkListsEquals(List<Integer> list1, List<Integer> list2) {
        return list1.size() == list2.size()
                && new HashSet<>(list1).equals(new HashSet<>(list2));
    }

    private void backToMoviesFragment() {
        // Возврат назад в стеке фрагментов
        requireActivity().runOnUiThread(() -> {
            navController.popBackStack(R.id.moviesFragment, false);
        });
    }

    private void navigateToMoviesFragment() {
        // Переходим к фрагменту MoviesFragment после сохранения
        requireActivity().runOnUiThread(() -> {
            navController.navigate(R.id.action_saveMovieFragment_to_moviesFragment);
        });
    }


    // Метод для загрузки списка жанров из базы данных
    private void loadGenres() {
        // Предполагаем, что у нас есть Dao для жанров
        moviesViewModel.getAllGenresList(result -> {
            genreList = result;
        });
    }

    // Открыть диалог для выбора жанров
    private void openGenrePickerDialog() {
        GenrePickerDialogFragment dialog = new GenrePickerDialogFragment();
        // Устанавливаем список жанров
        dialog.setGenreList(genreList);
        // Устанавливаем список id выбранных элементов
        dialog.setSelectedGenreIds(selectedGenreIds);
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "genrePickerDialog");
    }

    @Override
    public void onGenresSelected(List<Integer> selectedGenreIds) {
        this.selectedGenreIds = selectedGenreIds;
        // Показываем количество выбранных жанров на кнопке
        if(selectedGenreIds.isEmpty()){
            chooseGenresButton.setText("Выберите жанры");
        }
        else{
            chooseGenresButton.setText("Выбрано жанров: " + selectedGenreIds.size());
        }
    }

    private List<Integer> getIdListFromGenreList(List<Genre> genres) {
        List<Integer> genresIds = new ArrayList<>();
        for (Genre genre : genres) {
            genresIds.add(genre.getId());
        }

        return genresIds;
    }

}
