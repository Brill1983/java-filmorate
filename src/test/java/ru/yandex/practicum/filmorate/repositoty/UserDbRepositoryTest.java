package ru.yandex.practicum.filmorate.repositoty;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbRepository;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbRepositoryTest {

    private final NamedParameterJdbcOperations jdbcTemplate;
    private User user1;
    private User user2;
    private UserStorage userStorage;

    @BeforeEach
    public void beforeEach() {
        userStorage = new UserDbRepository(jdbcTemplate);
        user1 = User.builder()
                .email("newUser@ya.ru")
                .login("newUserLogin")
                .name("newUserName")
                .birthday(LocalDate.of(1990, 1, 5))
                .build();
        user2 = User.builder()
                .email("user@ya.ru")
                .login("userLogin")
                .name("userName")
                .birthday(LocalDate.of(1990, 1, 5))
                .build();
    }

    @Test
    public void testSaveUser() {

        User backedUser = userStorage.saveUser(user2);

        assertThat(backedUser)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("email", "user@ya.ru")
                .hasFieldOrPropertyWithValue("login", "userLogin")
                .hasFieldOrPropertyWithValue("name", "userName");
    }

    @Test
    public void testUpdateUser() {

        User backedUser = userStorage.saveUser(user2);

        long id = backedUser.getId();
        user1.setId(id);

        backedUser = userStorage.updateUser(user1);

        assertThat(backedUser)
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("email", "newUser@ya.ru")
                .hasFieldOrPropertyWithValue("login", "newUserLogin")
                .hasFieldOrPropertyWithValue("name", "newUserName");
    }

    @Test
    public void testFindUserById() {
        User backedUser = userStorage.saveUser(user2);
        long id = backedUser.getId();
        Optional<User> userOptional = userStorage.getUserById(id);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", id)
                                .hasFieldOrPropertyWithValue("email", "user@ya.ru")
                                .hasFieldOrPropertyWithValue("login", "userLogin")
                                .hasFieldOrPropertyWithValue("name", "userName")
                );

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user)
                                .usingRecursiveComparison()
                                .isEqualTo(backedUser));
    }

    @Test
    public void testGetAllUsers() {

        userStorage.saveUser(user2);

        List<User> userList = userStorage.getAllUsers();

        assertThat(userList.size())
                .isGreaterThan(0);
    }
}
