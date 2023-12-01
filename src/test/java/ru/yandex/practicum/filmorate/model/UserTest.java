package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.validator.Create;
import ru.yandex.practicum.filmorate.validator.Update;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private static final Validator validator;

    static {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    @Test
    void shouldCreateCorrectUser() {
        User user = new User();
        user.setLogin("login");
        user.setName("name");
        user.setEmail("email@mail.ru");
        user.setBirthday(LocalDate.of(1990, 1, 12));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Присутствуют нарушения валидации при создании объекта.");
        assertEquals(user.getLogin(), "login", "Логин был присвоен некорректно.");
        assertEquals(user.getName(), "name", "Имя было присвоено некорректно.");
        assertEquals(user.getBirthday(), LocalDate.of(1990, 1, 12),
                "Дата рождения была присвоена некорректно.");
    }


    @Test
    void validateEmailIsNullAndLoginHasWhitespace() {
        User user = new User();
        user.setLogin("Log in");

        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class, Update.class);

        ConstraintViolation<User> violation = violations.iterator().next();

        List<String> viol = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        assertTrue(viol.contains("Передан пустой email") &&
                viol.contains("В логине не должно быть пробелов"));
    }

    @Test
    void validateEmailHasSpellingErrorAndLoginIsNullAndBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("useremail.com");
        user.setBirthday(LocalDate.now().plusMonths(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class, Update.class);

        List<String> viol = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        assertTrue(viol.contains("Передан пустой логин") &&
                viol.contains("День рождения не может быть в будущем.") &&
                viol.contains("Передан неправильный формат email"));

    }
}
