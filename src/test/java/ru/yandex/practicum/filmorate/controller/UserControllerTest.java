package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;
    private User user;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        user = User.builder()
                .name("Dima")
                .email("brill@yandex.ru")
                .birthday(LocalDate.of(1983, 12, 9))
                .login("Brill")
                .build();
    }

    @Test
    void addUserAndGetUserList() {
        userController.saveUser(user);
        List<User> list = userController.getAllUsers();
        assertEquals(1, userController.getAllUsers().size());
        assertEquals(1, list.get(0).getId());
        assertEquals(user.getName(), list.get(0).getName());
        assertEquals(user.getEmail(), list.get(0).getEmail());
        assertEquals(user.getBirthday(), list.get(0).getBirthday());
        assertEquals(user.getLogin(), list.get(0).getLogin());
    }

    @Test
    void addUserWithBlankEmail() {
        user.setEmail("");
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    userController.saveUser(user);
                });
        assertEquals(ValidationException.class, exception.getClass());
    }

    @Test
    void addUserWithWrongEmail() {
        user.setEmail("brillyandex.ru");
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    userController.saveUser(user);
                });
        assertEquals(ValidationException.class, exception.getClass());
    }

    @Test
    void addUserWithBlankLogin() {
        user.setLogin("");
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    userController.saveUser(user);
                });
        assertEquals(ValidationException.class, exception.getClass());
    }

    @Test
    void addUserWithWrongLogin() {
        user.setLogin("Bril l");
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    userController.saveUser(user);
                });
        assertEquals(ValidationException.class, exception.getClass());
    }

    @Test
    void addUserWithFutureBirthday() {
        user.setBirthday(LocalDate.of(2030, 12, 9));
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    userController.saveUser(user);
                });
        assertEquals(ValidationException.class, exception.getClass());
    }

    @Test
    void addUserWithNullName() {
        user.setName(null);
        userController.saveUser(user);
        List<User> list = userController.getAllUsers();
        assertEquals("Brill", list.get(0).getName());
    }

    @Test
    void addUserWithBlankName() {
        user.setName("");
        userController.saveUser(user);
        List<User> list = userController.getAllUsers();
        assertEquals("Brill", list.get(0).getName());
    }

    @Test
    void updateUser() {
        userController.saveUser(user);
        User user2 = User.builder()
                .id(1)
                .name("Dmitry")
                .email("brill1@bk.ru")
                .birthday(LocalDate.of(1983, 12, 9))
                .login("Sycophant")
                .build();
        userController.updateUser(user2);
        List<User> list = userController.getAllUsers();
        assertEquals("Dmitry", list.get(0).getName());
        assertEquals("brill1@bk.ru", list.get(0).getEmail());
        assertEquals("Sycophant", list.get(0).getLogin());
    }

    @Test
    void updateUserWithWrongId() {
        userController.saveUser(user);
        User user2 = User.builder()
                .id(2)
                .name("Dmitry")
                .email("brill1@bk.ru")
                .birthday(LocalDate.of(1983, 12, 9))
                .login("Sycophant")
                .build();
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    userController.updateUser(user2);
                });
        assertEquals("Пользователя с ID 2 нет в базе", exception.getMessage());
    }
}