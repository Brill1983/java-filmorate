package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Email;
import java.time.LocalDate;

@Data
@Builder
public class User {

    @NonNull private int id = 0;
    @Email
    @NonNull private String email;
    @NonNull private String login;
    private String name;
    @NonNull private LocalDate birthday;

}
