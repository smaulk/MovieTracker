package com.example.movietracker.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.movietracker.models.Genre;

import java.util.ArrayList;
import java.util.List;

public class GenrePickerDialogFragment extends DialogFragment {

    // Список жанров, которые будут отображаться в списке
    private List<Genre> genreList;
    private List<Integer> selectedGenreIds = new ArrayList<>();
    private GenrePickerDialogListener listener;

    // Интерфейс для передачи выбранных жанров в активность/фрагмент
    public interface GenrePickerDialogListener {
        void onGenresSelected(List<Integer> selectedGenreIds);
    }

    // Устанавливаем список жанров
    public void setGenreList(List<Genre> genreList) {
        this.genreList = genreList;
    }

    // Устанавливаем список выбранных id жанров
    public void setSelectedGenreIds(List<Integer> selectedGenreIds)
    {
        // Создаем новый список,чтобы он не влиял на основной список
        this.selectedGenreIds  = new ArrayList<>();
        this.selectedGenreIds.addAll(selectedGenreIds);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Проверяем, что жанры были переданы в DialogFragment
        if (genreList == null || genreList.isEmpty()) {
            throw new IllegalStateException("Genre list cannot be null or empty");
        }

        // Массив для хранения состояния выбора жанров (выбран или нет)
        boolean[] checkedItems = new boolean[genreList.size()];
        // Если уже был выбран, помечаем
        for (int i = 0; i < genreList.size(); i++) {
            checkedItems[i] = selectedGenreIds.contains(genreList.get(i).getId());
        }

        // Создаем список жанров и передаем его в AlertDialog
        CharSequence[] genreNames = new CharSequence[genreList.size()];
        for (int i = 0; i < genreList.size(); i++) {
            genreNames[i] = genreList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Выберите жанры")
                .setMultiChoiceItems(genreNames, checkedItems, (dialog, which, isChecked) -> {
                    // Когда пользователь выбирает или снимает выбор
                    int genreId = genreList.get(which).getId();
                    if (isChecked) {
                        if (!selectedGenreIds.contains(genreId)) {
                            selectedGenreIds.add(genreId);  // Добавляем в список выбранных жанров
                        }
                    } else {
                        selectedGenreIds.remove(Integer.valueOf(genreId));  // Убираем из списка
                    }
                })
                .setPositiveButton("Ок", (dialog, id) -> {
                    // Передаем выбранные жанры обратно в активность/фрагмент
                    if (listener != null) {
                        listener.onGenresSelected(selectedGenreIds);
                    }
                })
                .setNegativeButton("Отмена", (dialog, id) -> dismiss())
                .create();

        return builder.create();
    }

    // Устанавливаем слушателя для передачи выбранных жанров
    public void setListener(GenrePickerDialogListener listener) {
        this.listener = listener;
    }
}
