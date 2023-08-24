package ru.yandex.practicum.filmorate.exceptions;

public class IncorrectRequestBodyException extends RuntimeException {

    public IncorrectRequestBodyException(String message) {
        super(message);
    }
}
