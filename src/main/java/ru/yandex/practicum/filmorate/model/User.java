package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private long id = 0L;

    @Email(message = "Передан неправильный или пустой email")
    private String email;

    @NotBlank(message = "Передан пустой логин")
    @Pattern(regexp = "\\S+", message = "В логине не должно быть пробелов")
    private String login;

    private String name;

    @PastOrPresent(message = "День рождения не может быть в будущем.")
    private LocalDate birthday;
}
