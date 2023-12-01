package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.validator.Create;
import ru.yandex.practicum.filmorate.validator.Update;

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
@EqualsAndHashCode(exclude = {"email", "login", "name", "birthday"})
public class User {

    private long id = 0L;

    @NotBlank(groups = Create.class, message = "Передан пустой email")
    @Email(groups = {Create.class, Update.class}, message = "Передан неправильный формат email")
    private String email;

    @NotBlank(groups = Create.class, message = "Передан пустой логин")
    @Pattern(groups = {Create.class, Update.class}, regexp = "\\S+", message = "В логине не должно быть пробелов")
    private String login;

    private String name;

    @PastOrPresent(groups = {Create.class, Update.class}, message = "День рождения не может быть в будущем.")
    private LocalDate birthday;
}
