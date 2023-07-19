package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private long id = 0L;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

}
