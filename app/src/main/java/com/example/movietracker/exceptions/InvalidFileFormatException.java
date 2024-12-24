package com.example.movietracker.exceptions;

// Ошибка, при неверном формате файла
public class InvalidFileFormatException extends Exception {
    public InvalidFileFormatException(String message) {
        super(message);
    }
}
