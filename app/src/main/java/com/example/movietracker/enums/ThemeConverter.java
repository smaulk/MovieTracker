package com.example.movietracker.enums;

import java.util.Arrays;
import java.util.List;

public class ThemeConverter {

    public static String toString(Theme theme)
    {
        return getStrings().get(theme.ordinal());
    }

    public static Theme fromString(String theme)
    {
        int index = getStrings().indexOf(theme);
        return Theme.values()[index];
    }

    private static List<String> getStrings() {
        return Arrays.asList(
                "system",
                "light",
                "dark"
        );
    }
}
