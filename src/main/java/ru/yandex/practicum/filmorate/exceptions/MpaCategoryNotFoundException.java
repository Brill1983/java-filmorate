package ru.yandex.practicum.filmorate.exceptions;

public class MpaCategoryNotFoundException extends RuntimeException {
    public MpaCategoryNotFoundException(String message) {
        super(message);
    }
}
