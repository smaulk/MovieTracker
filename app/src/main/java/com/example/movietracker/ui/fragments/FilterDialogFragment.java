package com.example.movietracker.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.movietracker.R;
import com.example.movietracker.models.Genre;
import com.example.movietracker.ui.viewmodel.MoviesViewModel;

import java.util.ArrayList;
import java.util.List;


public class FilterDialogFragment extends DialogFragment implements GenrePickerDialogFragment.GenrePickerDialogListener {

    private EditText ratingMin, ratingMax, yearMin, yearMax;
    private Button chooseGenresBtn, clearBtn, applyBtn;

    private MoviesViewModel moviesViewModel;
    private List<Genre> genreList;
    private List<Integer> selectedGenreIds;

    // Интерфейс для передачи выбранных фильтров
    public interface OnFilterAppliedListener {
        void onFilterApplied(
                Integer ratingMin, Integer ratingMax,
                Integer yearMin, Integer yearMax,
                List<Genre> genres
        );
    }

    private OnFilterAppliedListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Проверка на использование targetFragment для передачи данных
        if (getTargetFragment() instanceof OnFilterAppliedListener) {
            listener = (OnFilterAppliedListener) getTargetFragment(); // Приводим targetFragment к интерфейсу
        } else {
            throw new ClassCastException(getTargetFragment().toString() + " must implement OnFilterAppliedListener");
        }
    }

    // Убираем лишний фон диалогового окна
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Убирает фон вокруг диалога
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_filter, container, false);
        moviesViewModel = new ViewModelProvider(this).get(MoviesViewModel.class);

        ratingMin = view.findViewById(R.id.ratingMinEditText);
        ratingMax = view.findViewById(R.id.ratingMaxEditText);
        yearMin = view.findViewById(R.id.yearMinEditText);
        yearMax = view.findViewById(R.id.yearMaxEditText);
        chooseGenresBtn = view.findViewById(R.id.chooseGenresButton);
        clearBtn = view.findViewById(R.id.clearButton);
        applyBtn = view.findViewById(R.id.applyButton);

        selectedGenreIds = new ArrayList<>();

        // Устанавливаем ограничение на ввод рейтинга от 1 до 5
        InputFilter ratingRangeFilter = getRatingFilter();
        ratingMin.setFilters(new InputFilter[]{ratingRangeFilter});
        ratingMax.setFilters(new InputFilter[]{ratingRangeFilter});

        Bundle args = getArguments();
        // Получаем данные фильтров из Bundle
        if (args != null) {
            Integer savedRatingMin = (Integer) args.getSerializable("ratingMin");
            Integer savedRatingMax = (Integer) args.getSerializable("ratingMax");
            Integer savedYearMin = (Integer) args.getSerializable("yearMin");
            Integer savedYearMax = (Integer) args.getSerializable("yearMax");

            // Устанавливаем значения, если они есть
            if (savedRatingMin != null) ratingMin.setText(String.valueOf(savedRatingMin));
            if (savedRatingMax != null) ratingMax.setText(String.valueOf(savedRatingMax));
            if (savedYearMin != null) yearMin.setText(String.valueOf(savedYearMin));
            if (savedYearMax != null) yearMax.setText(String.valueOf(savedYearMax));

            List<Integer> selectedGenreIds = getArguments().getIntegerArrayList("selectedGenres");
            if(selectedGenreIds != null){
                onGenresSelected(selectedGenreIds);
            }

        }

        // Загружаем список жанров
        loadGenres();

        chooseGenresBtn.setOnClickListener(v -> openGenrePickerDialog());
        clearBtn.setOnClickListener(v -> clearData());

        applyBtn.setOnClickListener(v -> {
            if(sendData()){
                dismiss();
            }
        });

        return view;
    }

    private Integer parseInt(String str){
        return str.isEmpty()
                ? null
                : Integer.parseInt(str);
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
            chooseGenresBtn.setText("Выберите жанры");
        }
        else{
            chooseGenresBtn.setText("Выбрано жанров: " + selectedGenreIds.size());
        }
    }

    // Метод для загрузки списка жанров из базы данных
    private void loadGenres() {
        // Предполагаем, что у нас есть Dao для жанров
        moviesViewModel.getAllGenresList(result -> {
            genreList = result;
        });
    }

    private List<Genre> getSelectedGenresList(){
        List<Genre> selectedGenres = new ArrayList<>();

        for (Integer id: selectedGenreIds) {
            selectedGenres.add(genreList.get(id - 1));
        }

        return  selectedGenres;
    }

    private void clearData()
    {
        ratingMin.setText("");
        ratingMax.setText("");
        yearMin.setText("");
        yearMax.setText("");
        onGenresSelected(new ArrayList<>());
    }

    private boolean sendData()
    {
        Integer ratingMinInt = parseInt(ratingMin.getText().toString());
        Integer ratingMaxInt = parseInt(ratingMax.getText().toString());
        Integer yearMinInt = parseInt(yearMin.getText().toString());
        Integer yearMaxInt = parseInt(yearMax.getText().toString());

        List<Genre> selectedGenres = getSelectedGenresList();

        // Если Min больше Max
        if(!checkMinMax(ratingMinInt, ratingMaxInt) || !checkMinMax(yearMinInt, yearMaxInt)) {
            return false;
        }

        // Отправляем выбранные фильтры в MoviesFragment
        listener.onFilterApplied(
                ratingMinInt, ratingMaxInt,
                yearMinInt, yearMaxInt,
                selectedGenres
        );

        return true;
    }

    // Проверка, что Min не больше Max
    private boolean checkMinMax(Integer min, Integer max)
    {
        // Если какое то значение равно null
        if(min == null || max == null){
            return true;
        }
        boolean res = min <= max;
        // Если Min больше Max
        if(!res){
            Toast.makeText(getContext(), "Мин. значение не может быть больше Макс.", Toast.LENGTH_SHORT).show();
        }
        return res;
    }

     private InputFilter getRatingFilter() {
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                try {
                    int input = Integer.parseInt(dest.toString() + source.toString());
                    if (input >= 1 && input <= 5) {
                        return null;  // Оставить ввод
                    }
                } catch (NumberFormatException e) {
                    // Игнорируем ошибку, если это не число
                }
                Toast.makeText(getContext(), "Рейтинг должен быть от 1 до 5", Toast.LENGTH_SHORT).show();
                return "";  // Отменить ввод, если не число в пределах от 1 до 5
            }
        };
     }
}
