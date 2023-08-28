package ru.yandex.practicum.filmorate.repositoty;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class FriendsDbRepositoryTest {
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private FriendsStorage friendsStorage;

    @Test
    public void testAddFriendGetFriendListDeleteFriend() {

        User user1 = User.builder()
                .email("user1@ya.ru")
                .login("user1Login")
                .name("user1Name")
                .birthday(LocalDate.of(1990, 1, 5))
                .build();
        userStorage.saveUser(user1);

        User user2 = User.builder()
                .email("user2@ya.ru")
                .login("user2Login")
                .name("user2Name")
                .birthday(LocalDate.of(1990, 1, 5))
                .build();
        userStorage.saveUser(user2);

        friendsStorage.addAsFriend(1, 2);

        List<User> friendList = friendsStorage.getFriendsList(1);

        assertThat(friendList.size())
                .isEqualTo(1);

        User friend = friendList.get(0);

        assertThat(friend)
                .hasFieldOrPropertyWithValue("id", 2L)
                .hasFieldOrPropertyWithValue("email", "user2@ya.ru")
                .hasFieldOrPropertyWithValue("login", "user2Login")
                .hasFieldOrPropertyWithValue("name", "user2Name")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1990, 1, 5));

        boolean deleteResult = friendsStorage.deleteFriend(1, 2);

        assertThat(deleteResult)
                .isEqualTo(true);
    }

    @Test
    public void testGetCommonFriends() {

        User user1 = User.builder()
                .email("user1@ya.ru")
                .login("user1Login")
                .name("user1Name")
                .birthday(LocalDate.of(1990, 1, 5))
                .build();
        userStorage.saveUser(user1);

        User user2 = User.builder()
                .email("user2@ya.ru")
                .login("user2Login")
                .name("user2Name")
                .birthday(LocalDate.of(1990, 1, 5))
                .build();
        userStorage.saveUser(user2);

        User user3 = User.builder()
                .email("U3@ya.ru")
                .login("U3")
                .name("USER3")
                .birthday(LocalDate.of(1985, 2, 1))
                .build();
        userStorage.saveUser(user3);

        friendsStorage.addAsFriend(2, 1);
        friendsStorage.addAsFriend(3, 1);

        List<User> commonFriendsList = friendsStorage.getCommonFriends(2, 3);

        assertThat(commonFriendsList.size())
                .isEqualTo(1);

        assertThat(commonFriendsList.get(0).getId())
                .isEqualTo(1);
    }
}
