package ru.yandex.practicum.filmorate.exceptions;

public class LikeAlreadyExistException extends RuntimeException {
    public LikeAlreadyExistException(String msg) {
        super(msg);
    }
}
