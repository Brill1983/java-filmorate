package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {

    private long id = 0L;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private int rate;
    private MpaCategory mpa;
    private List<Genre> genres = new ArrayList<>();

    public Film(long id, String name, String description, LocalDate releaseDate, int duration, int rate, MpaCategory mpa) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.rate = rate;
        this.mpa = mpa;
    }
}
