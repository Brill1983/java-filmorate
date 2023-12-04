package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.validator.Create;
import ru.yandex.practicum.filmorate.validator.ReleaseDateConstrain;
import ru.yandex.practicum.filmorate.validator.Update;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmTest {

    private static final Validator validator;

    static {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }


    @Test
    void shouldCreateCorrectFilm() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2030, 1, 10));
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertTrue(violations.isEmpty(), "Присутствуют нарушения.");
        assertEquals(film.getDescription(), "description", "Описание было присвоено некорректно.");
        assertEquals(film.getName(), "name", "Имя было присвоено некорректно.");
        assertEquals(film.getReleaseDate(), LocalDate.of(2030, 1, 10),
                "Дата релиза была присвоена некорректно.");
    }

    @Test
    void shouldValidateEmptyName() {
        Film film = new Film();
        film.setName(" ");
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertFalse(violations.isEmpty(), "Нарушения отсутствуют.");
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals(NotBlank.class, violation.getConstraintDescriptor().getAnnotation().annotationType(),
                "Нарушение \"NotBlank\" не найдено.");
        assertEquals("name", violation.getPropertyPath().toString(),
                "Не найдено нарушение для поля \"name\".");
        assertEquals("Название фильма - обязательно к заполнению", violation.getMessage());
    }

    @Test
    void shouldValidateLongDescription() {
        Film film = new Film();
        film.setName("name");
        film.setDuration(100);
        film.setDescription(".SKBFzkjshbfbzbfzhbfkbfkbjhfbhjzbhbHBKVkgGVhgVKHGVHGJvKVkhGCVcGH" +
                ".SKBFzkjshbfbzbfzhbfkbfkbjhfbhjzbhbHBKVkgGVhgVKHGVHGJvKVkhGCVcGH" +
                ".SKBFzkjshbfbzbfzhbfkbfkbjhfbhjzbhbHBKVkgGVhgVKHGVHGJvKVkhGCVcGH" +
                ".SKBFzkjshbfbzbfzhbfkbfkbjhfbhjzbhbHBKVkgGVhgVKHGVHGJvKVkhGCVcGH"); //256 символов
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class, Update.class);
        assertFalse(violations.isEmpty(), "Нарушения отсутствуют.");
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals(Size.class, violation.getConstraintDescriptor().getAnnotation().annotationType(),
                "Нарушение \"Size\" не найдено.");
        assertEquals("description", violation.getPropertyPath().toString(),
                "Не найдено нарушение для поля \"description\".");
        assertEquals("Длина описания не должна превышать 200 символов", violation.getMessage());
    }

    @Test
    void shouldCreate200SymbolsSizeDescription() {
        Film film = new Film();
        film.setName("name");
        film.setDuration(100);
        film.setDescription(".SKBFzkjshbfbzbfzhbfkbfkbjhfbhjzbhbHBKVkgGVhgVKHGVHGJvKVkhGCVcGH" +
                ".SKBFzkjshbfbzbfzhbfkbfkbjhfbhjzbhbHBKVkgGVhgVKHGVHGJvKVkhGCVcGH" +
                ".SKBFzkjshbfbzbfzhbfkbfkbjhfbhjzbhbHBKVkgGVhgVKHGVHGJvKVkhGCVcGH" +
                "khGCVcGH"); //200 символов
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class, Update.class);
        assertTrue(violations.isEmpty(), "Нарушения присутствуют.");
    }

    @Test
    void shouldValidateIncorrectReleaseDate() {
        Film film = new Film();
        film.setName("name");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class, Update.class);
        assertFalse(violations.isEmpty(), "Нарушения отсутствуют.");
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals(ReleaseDateConstrain.class, violation.getConstraintDescriptor().getAnnotation().annotationType(),
                "Нарушение \"ReleaseDateConstraint\" не найдено.");
        assertEquals("releaseDate", violation.getPropertyPath().toString(),
                "Не найдено нарушение для поля \"releaseDate\".");
        assertEquals("Film release date must be after 28.12.1895", violation.getMessage());
    }

    @Test
    void shouldCreateFilmWith28December1895ReleaseDate() {
        Film film = new Film();
        film.setName("name");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class, Update.class);
        assertTrue(violations.isEmpty(), "Нарушения присутствуют.");
    }

    @Test
    void shouldValidateZeroDuration() {
        Film film = new Film();
        film.setName("name");
        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class, Update.class);
        assertFalse(violations.isEmpty(), "Нарушения отсутствуют.");
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals(Positive.class, violation.getConstraintDescriptor().getAnnotation().annotationType(),
                "Нарушение \"Positive\" не найдено.");
        assertEquals("duration", violation.getPropertyPath().toString(),
                "Не найдено нарушение для поля \"duration\".");
        assertEquals("Продолжительность не может быть 0 или отрицательной", violation.getMessage());
    }

    @Test
    void shouldValidateNegativeDuration() {
        Film film = new Film();
        film.setName("name");
        film.setDuration(-10);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class, Update.class);
        assertFalse(violations.isEmpty(), "Нарушения отсутствуют.");
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals(Positive.class, violation.getConstraintDescriptor().getAnnotation().annotationType(),
                "Нарушение \"Positive\" не найдено.");
        assertEquals("duration", violation.getPropertyPath().toString(),
                "Не найдено нарушение для поля \"duration\".");
        assertEquals("Продолжительность не может быть 0 или отрицательной", violation.getMessage());

    }
}
