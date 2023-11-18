package ru.yandex.practicum.filmorate.repositoty;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserDbRepositoryTest {
//
//    @Autowired
//    private UserStorage userStorage;
//
//    @Test
//    public void testSaveUser() {
//        User user2 = User.builder()
//                .email("user@ya.ru")
//                .login("userLogin")
//                .name("userName")
//                .birthday(LocalDate.of(1990, 1, 5))
//                .build();
//        User backedUser = userStorage.saveUser(user2);
//
//        assertThat(backedUser)
//                .hasFieldOrPropertyWithValue("id", 1L)
//                .hasFieldOrPropertyWithValue("email", "user@ya.ru")
//                .hasFieldOrPropertyWithValue("login", "userLogin")
//                .hasFieldOrPropertyWithValue("name", "userName");
//    }
//
//    @Test
//    public void testUpdateUser() {
//
//        User user2 = User.builder()
//                .email("user@ya.ru")
//                .login("userLogin")
//                .name("userName")
//                .birthday(LocalDate.of(1990, 1, 5))
//                .build();
//        userStorage.saveUser(user2);
//
//        User user1 = User.builder()
//                .id(1L)
//                .email("newUser@ya.ru")
//                .login("newUserLogin")
//                .name("newUserName")
//                .birthday(LocalDate.of(1990, 1, 5))
//                .build();
//        User backedUser = userStorage.updateUser(user1);
//
//        assertThat(backedUser)
//                .hasFieldOrPropertyWithValue("id", 1L)
//                .hasFieldOrPropertyWithValue("email", "newUser@ya.ru")
//                .hasFieldOrPropertyWithValue("login", "newUserLogin")
//                .hasFieldOrPropertyWithValue("name", "newUserName");
//    }
//
//    @Test
//    public void testFindUserById() {
//
//        User user1 = User.builder()
//                .email("user@ya.ru")
//                .login("userLogin")
//                .name("userName")
//                .birthday(LocalDate.of(1990, 1, 5))
//                .build();
//        userStorage.saveUser(user1);
//
//        Optional<User> userOptional = userStorage.getUserById(1);
//
//        assertThat(userOptional)
//                .isPresent()
//                .hasValueSatisfying(user ->
//                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
//                                .hasFieldOrPropertyWithValue("email", "user@ya.ru")
//                                .hasFieldOrPropertyWithValue("login", "userLogin")
//                                .hasFieldOrPropertyWithValue("name", "userName")
//                );
//    }
//
//    @Test
//    public void testGetAllUsers() {
//
//        User user2 = User.builder()
//                .email("user@ya.ru")
//                .login("userLogin")
//                .name("userName")
//                .birthday(LocalDate.of(1990, 1, 5))
//                .build();
//        userStorage.saveUser(user2);
//
//        List<User> userList = userStorage.getAllUsers();
//
//        assertThat(userList.size())
//                .isEqualTo(1);
//    }
}
