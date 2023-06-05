package ru.yandex.practicum.filmorate.model;


import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Max;
import java.time.LocalDate;

@Data
@Builder
public class Film {

    @NonNull private int id = 0;
    @NonNull private String name;
    @Max(200)
    @NonNull private String description;
    @NonNull private LocalDate releaseDate;
    @NonNull private int duration;
}
