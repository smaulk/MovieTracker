package com.example.movietracker.enums;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.List;

public class StatusConverter {
    @TypeConverter
    public static Status fromInt(int value) {
        return Status.values()[value];
    }

    @TypeConverter
    public static int toInt(Status status) {
        return status.ordinal();
    }

    public static List<String> getRuNames() {
        return Arrays.asList(
                "Хочу посмотреть",
                "Смотрю",
                "Просмотрено"
        );
    }
}
