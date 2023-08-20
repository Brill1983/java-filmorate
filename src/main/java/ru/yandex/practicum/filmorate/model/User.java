package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private long id = 0L;

    @NotBlank(message = "Передан пустой email")
    @Email(message = "Передан неправильный формат email")
    private String email;

    @NotBlank(message = "Передан пустой логин")
    @Pattern(regexp = "\\S+", message = "В логине не должно быть пробелов")
    private String login;

    private String name;

    @PastOrPresent(message = "День рождения не может быть в будущем.")
    private LocalDate birthday;
}
