package com.example.movietracker.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;

import com.example.movietracker.R;
import com.example.movietracker.adapters.MoviesAdapter;
import com.example.movietracker.db.MovieWithGenres;
import com.example.movietracker.enums.StatusConverter;
import com.example.movietracker.filters.MovieFilter;
import com.example.movietracker.models.Genre;
import com.example.movietracker.ui.viewmodel.MoviesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MoviesFragment extends Fragment implements FilterDialogFragment.OnFilterAppliedListener {
    private RecyclerView recyclerView;
    private FloatingActionButton addMovieBtn;
    private ImageButton filterBtn, searchBtn;
    private AppCompatEditText searchEditText;
    private NavController navController;
    private MoviesViewModel moviesViewModel;

    private MovieFilter filter;
    private List<MovieWithGenres> movieList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);
        filter = new MovieFilter();
        movieList = new ArrayList<>();
        moviesViewModel = new ViewModelProvider(this).get(MoviesViewModel.class);
        navController = NavHostFragment.findNavController(this);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        addMovieBtn = view.findViewById(R.id.addMovieButton);
        filterBtn = view.findViewById(R.id.filterButton);
        searchBtn = view.findViewById(R.id.searchButton);
        searchEditText = view.findViewById(R.id.searchEditText);

        addMovieBtn.setOnClickListener(v -> {
            // Переход к фрагменту для создания фильма
            navController.navigate(R.id.action_moviesFragment_to_saveMovieFragment);
        });

        Spinner statusSpinner= view.findViewById(R.id.statusSpinner);
        List<String> statuses = new ArrayList<>(StatusConverter.getRuNames());
        statuses.add(0, "Все");  // Добавляем "Все" в начало списка
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.primary_spinner_item,
                R.id.spinner_item_text,
                statuses
        );
        statusSpinner.setAdapter(statusAdapter);
        statusSpinner.setSelection(0);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Если в качестве статуса выбрано "Все"
                if(position == 0) {
                    filter.clearStatus();
                } else {
                    // Выбираем по статусу ( -1 т.к. в начало мы добавили "Все")
                    filter.setStatus(StatusConverter.fromInt(position - 1));
                }
                updateMovieList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });


        searchBtn.setOnClickListener(v -> {
            // Показываем или скрываем поле ввода при нажатии на кнопку поиска
            if (searchEditText.getVisibility() == View.GONE) {
                searchEditText.setVisibility(View.VISIBLE);
                searchEditText.requestFocus();
                // Открываем клавиатуру
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            } else {
                searchEditText.setVisibility(View.GONE);
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Обновляем фильтр по введенному тексту
                filter.setTitle(charSequence.toString());
                updateMovieList();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        filterBtn.setOnClickListener(v -> showFilterDialog());

        // Заполняем список всех фильмов
        fillMovieList();

        return view;
    }


    // Получаем список фильмов с жанрами из бд, и отрисовываем
    public void fillMovieList()
    {
        moviesViewModel.getAllMoviesList(result -> {
            movieList = result;
            updateMovieList();
        });
    }

    // Фильтруем фильмы и отрисовываем
    private void updateMovieList(){
        List<MovieWithGenres> filteredMovies = filter.applyFilter(movieList);
        requireActivity().runOnUiThread(() -> {
            // Проверяем, если адаптер уже был создан, обновляем данные
            MoviesAdapter adapter = (MoviesAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.updateMovieList(filteredMovies);  // Обновляем данные адаптера
            } else {
                // Если адаптер еще не был создан, создаем новый
                recyclerView.setAdapter(new MoviesAdapter(filteredMovies, this::openEditMovie));
                setupSwipeToDelete();
            }
        });
    }

    private void openEditMovie(MovieWithGenres movieWithGenres)
    {
        // Передаем данные в фрагмент редактирования фильма
        Bundle bundle = new Bundle();
        bundle.putSerializable("movieWithGenres", movieWithGenres);  // Передаем фильм в Bundle
        navController.navigate(R.id.action_moviesFragment_to_saveMovieFragment, bundle);
    }

    // Метод для показа диалога фильтрации
    private void showFilterDialog() {

        // Создаём Bundle и передаем текущие фильтры в диалог
        Bundle filterData = new Bundle();
        filterData.putSerializable("ratingMin", filter.getRatingMin());
        filterData.putSerializable("ratingMax", filter.getRatingMax());
        filterData.putSerializable("yearMin", filter.getYearMin());
        filterData.putSerializable("yearMax", filter.getYearMax());

        // Передаем список жанров, если они есть
        ArrayList<Integer> selectedGenreIds = new ArrayList<>();
        for (Genre genre : filter.getGenres()) {
            selectedGenreIds.add(genre.getId());  // Преобразуем список жанров в список ID
        }
        filterData.putIntegerArrayList("selectedGenres", selectedGenreIds);


        FilterDialogFragment filterDialog = new FilterDialogFragment();
        filterDialog.setArguments(filterData);  // Передаем данные в диалог
        filterDialog.setTargetFragment(this, 0); // Устанавливаем фрагмент как слушателя
        filterDialog.show(getParentFragmentManager(), "filterDialog");
    }

    // Реализация интерфейса OnFilterAppliedListener
    @Override
    public void onFilterApplied(
            Integer ratingMin, Integer ratingMax,
            Integer yearMin, Integer yearMax,
            List<Genre> genres
    ) {
        filter.setRatingMin(ratingMin);
        filter.setRatingMax(ratingMax);
        filter.setYearMin(yearMin);
        filter.setYearMax(yearMax);
        filter.setGenres(genres);
        // Применяем фильтрацию
        updateMovieList();
    }


    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Не поддерживаем перемещение элементов
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                MoviesAdapter adapter = (MoviesAdapter) recyclerView.getAdapter();
                int position = viewHolder.getAdapterPosition();
                MovieWithGenres movieWithGenres = adapter.getMovieFromPosition(position);
                // Показываем окно подтверждения
                new AlertDialog.Builder(requireContext())
                        .setTitle("Подтверждение удаления")
                        .setMessage("Вы уверены, что хотите удалить этот фильм?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            // Удаляем фильм из адаптера
                            adapter.removeMovieElement(position);
                            // Удаляем фильм из базы данных
                            moviesViewModel.deleteMovie(movieWithGenres.movie);
                            // Удаляем из списка
                            movieList.remove(position);
                        })
                        .setNegativeButton("Отмена", (dialog, which) -> {
                            // Возвращаем элемент обратно в адаптер
                            adapter.notifyItemChanged(position);
                        })
                        .setOnCancelListener(dialog -> {
                            // Возвращаем элемент обратно в адаптер, если окно закрыто без выбора
                            adapter.notifyItemChanged(position);
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                // Получаем view (элемент, на котором мы меняем background)
                View itemView = viewHolder.itemView;

                // Проверяем, что свайп активен
                if (isCurrentlyActive) {
                    // Вычисляем альфа-значение в зависимости от dX (чем больше свайп, тем выше альфа)
                    float swipeProgress = Math.abs(dX) / itemView.getWidth(); // От 0 до 1
                    int alpha = (int) (255 * swipeProgress); // 0 до 255

                    // Получаем основной цвет (например, красный)
                    int color = ContextCompat.getColor(recyclerView.getContext(), R.color.error);

                    // Применяем цвет с изменённой альфой
                    int colorWithAlpha = (alpha << 24) | (color & 0x00FFFFFF);

                    float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, itemView.getResources().getDisplayMetrics());

                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setShape(GradientDrawable.RECTANGLE);
                    drawable.setCornerRadius(radius); // Радиус скругления
                    drawable.setColor(colorWithAlpha); // Цвет фона (красный, можно динамически менять)
                    itemView.setBackground(drawable);

                } else {
                    // Восстанавливаем исходный фон, если свайп завершён
                    itemView.setBackgroundResource(R.drawable.rounded_background);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
