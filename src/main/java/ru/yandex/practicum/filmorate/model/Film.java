package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.validator.ReleaseDateConstrain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {

    private long id = 0L;

    @NotBlank(message = "Название фильма - обязательно к заполнению")
    private String name;

    @Size(max = 200, message = "Длина описания не должна превышать 200 символов")
    private String description;

    @ReleaseDateConstrain
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность не может быть 0 или отрицательной")
    private int duration;

//    private int rate;

    private MpaCategory mpa;

    private Set<Director> directors = new HashSet<>();

    private Set<Genre> genres = new HashSet<>();

    private Set<User> likes = new HashSet<>();

}
