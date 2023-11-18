package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.validator.Create;
import ru.yandex.practicum.filmorate.validator.ReleaseDateConstrain;
import ru.yandex.practicum.filmorate.validator.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {

    private long id = 0L;

    @NotBlank(groups = Create.class, message = "Название фильма - обязательно к заполнению")
    private String name;

    @Size(groups = {Create.class, Update.class}, max = 200, message = "Длина описания не должна превышать 200 символов")
    private String description;

    @ReleaseDateConstrain(groups = {Create.class, Update.class})
    private LocalDate releaseDate;

    @Positive(groups = {Create.class, Update.class}, message = "Продолжительность не может быть 0 или отрицательной")
    private int duration;

    private MpaCategory mpa;

    private Set<Director> directors = new HashSet<>();

    private Set<Genre> genres = new HashSet<>();

    private Set<User> likes = new HashSet<>();

}
