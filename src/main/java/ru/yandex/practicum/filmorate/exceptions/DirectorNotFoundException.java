package ru.yandex.practicum.filmorate.exceptions;

public class DirectorNotFoundException extends RuntimeException {

    public DirectorNotFoundException(String message) {
        super(message);
    }

    public DirectorNotFoundException(int id) {
        super("Режиссер с ID=" + id + " не найден.");
    }
}
