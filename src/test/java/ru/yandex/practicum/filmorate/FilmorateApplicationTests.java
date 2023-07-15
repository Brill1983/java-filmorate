package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaCategory;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmoRateApplicationTests {
	private final UserStorage userStorage;
	private final GenreStorage genreStorage;
	private final MpaCategoryStorage mpaCategoryStorage;
	private final FilmStorage filmStorage;
	private final LikesStorage likesStorage;
	private final FriendsStorage friendsStorage;

	@Test
	public void testSaveUser() {
		User user = User.builder()
				.email("user@ya.ru")
				.login("userLogin")
				.name("userName")
				.birthday(LocalDate.of(1990, 1, 5))
				.build();
		User backedUser = userStorage.saveUser(user);

		assertThat(backedUser)
				.hasFieldOrPropertyWithValue("id", 4L)
				.hasFieldOrPropertyWithValue("email", "user@ya.ru")
				.hasFieldOrPropertyWithValue("login", "userLogin")
				.hasFieldOrPropertyWithValue("name", "userName");
	}

	@Test
	public void testUpdateUser() {

		User user = User.builder()
				.id(1L)
				.email("newUser@ya.ru")
				.login("newUserLogin")
				.name("newUserName")
				.birthday(LocalDate.of(1990, 1, 5))
				.build();
		User backedUser = userStorage.updateUser(user);

		assertThat(backedUser)
				.hasFieldOrPropertyWithValue("id", 1L)
				.hasFieldOrPropertyWithValue("email", "newUser@ya.ru")
				.hasFieldOrPropertyWithValue("login", "newUserLogin")
				.hasFieldOrPropertyWithValue("name", "newUserName");
	}

	@Test
	public void testFindUserById() {

		Optional<User> userOptional = userStorage.getUserById(1);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
								.hasFieldOrPropertyWithValue("email", "newUser@ya.ru")
								.hasFieldOrPropertyWithValue("login", "newUserLogin")
								.hasFieldOrPropertyWithValue("name", "newUserName")
				);
	}

	@Test
	public void testGetAllUsers() {

		List<User> userList= userStorage.getAllUsers();

		assertThat(userList.size())
				.isEqualTo(4);
	}

	@Test
	public void testAddFilm() {
		MpaCategory category = MpaCategory.builder().name("G").build();

		MpaCategory backedCategory = mpaCategoryStorage.createNewMpaCategory(category);
		assertThat(backedCategory)
				.hasFieldOrPropertyWithValue("id", 1)
				.hasFieldOrPropertyWithValue("name", "G");

		Genre genre = Genre.builder().name("Комедия").build();

		Genre backedGenre = genreStorage.createNewGenre(genre);
		assertThat(backedGenre)
				.hasFieldOrPropertyWithValue("id", 1)
				.hasFieldOrPropertyWithValue("name", "Комедия");

		Film film = Film.builder()
				.name("film1")
				.description("some description")
				.releaseDate(LocalDate.of(1990, 1, 5))
				.duration(80)
				.categoryMpa(new MpaCategory(1,"G"))
				.build();
		film.setGenres(List.of(new Genre(1, "Комедия")));

		Film backedFilm = filmStorage.addFilm(film);

		assertThat(backedFilm)
				.hasFieldOrPropertyWithValue("id", 1L)
				.hasFieldOrPropertyWithValue("name", "film1")
				.hasFieldOrPropertyWithValue("description", "some description")
				.hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1990, 1, 5))
				.hasFieldOrPropertyWithValue("duration", 80);

		Integer genreId = backedFilm.getGenres().get(0).getId();

		assertThat(genreId)
				.isEqualTo(1);

		Integer mpaCatId = backedFilm.getCategoryMpa().getId();

		assertThat(mpaCatId)
				.isEqualTo(1);
	}

	@Test
	public void testUpdateFilm() {
		MpaCategory category = MpaCategory.builder().name("GG").build();
		mpaCategoryStorage.createNewMpaCategory(category);
		Genre genre = Genre.builder().name("Трагедия").build();
		genreStorage.createNewGenre(genre);
		Film film = Film.builder()
				.id(1L)
				.name("FILM2")
				.description("NEW_DESC")
				.releaseDate(LocalDate.of(1991, 2, 6))
				.duration(100)
				.categoryMpa(new MpaCategory(2,"PG"))
				.build();
		film.setGenres(List.of(new Genre(1, "Комедия"), new Genre(2, "Драмма")));

		Film backedFilm = filmStorage.updateFilm(film);

		assertThat(backedFilm)
				.hasFieldOrPropertyWithValue("id", 1L)
				.hasFieldOrPropertyWithValue("name", "FILM2")
				.hasFieldOrPropertyWithValue("description", "NEW_DESC")
				.hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1991, 2, 6))
				.hasFieldOrPropertyWithValue("duration", 100);

		Integer genreId = backedFilm.getGenres().get(0).getId();

		assertThat(genreId).isEqualTo(1);

		Integer mpaCatId = backedFilm.getCategoryMpa().getId();

		assertThat(mpaCatId)
				.isEqualTo(2);
	}

	@Test
	public void testGetAllFilms() {

		List<Film> filmList= filmStorage.getAllFilms();

		assertThat(filmList.size())
				.isEqualTo(1);
	}

	@Test
	public void testGetFilmById() {

		Optional<Film> userOptional = filmStorage.getFilmById(1);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(film ->
						assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
								.hasFieldOrPropertyWithValue("name", "FILM2")
								.hasFieldOrPropertyWithValue("description", "NEW_DESC")
								.hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1991, 2, 6))
								.hasFieldOrPropertyWithValue("duration", 100)
				);
	}

	@Test
	public void testUserLikeFilmDeleteLikeCheckLikeGetPopularFilmList() {
		likesStorage.userLikedFilm(1,1);
		boolean checkLikeTrue = likesStorage.checkUserLikedFilm(1,1);

		assertThat(checkLikeTrue)
				.isEqualTo(true);

		List<Film> popularFilmList = filmStorage.getPopularFilmList(10);

		assertThat(popularFilmList.size())
				.isEqualTo(1);

		likesStorage.deleteLike(1,1);

		boolean checkLikeFalse = likesStorage.checkUserLikedFilm(1,1);

		assertThat(checkLikeFalse)
				.isEqualTo(false);
	}

//	void addAsFriend(long userId, long friendId);
	@Test
	public void testAddFriendGetFriendListDeleteFriend() {

		User user = User.builder()
				.email("U2@ya.ru")
				.login("U2")
				.name("USER2")
				.birthday(LocalDate.of(1989, 12, 15))
				.build();
		userStorage.saveUser(user);

		friendsStorage.addAsFriend(1, 2);

		List<User> friendList = friendsStorage.getFriendsList(1);

		assertThat(friendList.size())
				.isEqualTo(1);

		User friend = friendList.get(0);

		assertThat(friend)
				.hasFieldOrPropertyWithValue("id", 2L)
				.hasFieldOrPropertyWithValue("email", "U2@ya.ru")
				.hasFieldOrPropertyWithValue("login", "U2")
				.hasFieldOrPropertyWithValue("name", "USER2")
				.hasFieldOrPropertyWithValue("birthday", LocalDate.of(1989,12,15));

		boolean deleteResult = friendsStorage.deleteFriend(1,2);

		assertThat(deleteResult)
				.isEqualTo(true);
	}

//	List<User> getCommonFriends(long id, long otherId);
	@Test
	public void testGetCommonFriends() {
		User user1 = User.builder()
				.email("user@ya.ru")
				.login("userLogin")
				.name("userName")
				.birthday(LocalDate.of(1990, 1, 5))
				.build();
		userStorage.saveUser(user1);

		User user2 = User.builder()
				.email("U2@ya.ru")
				.login("U2")
				.name("USER2")
				.birthday(LocalDate.of(1989, 12, 15))
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

		List<User> commonFriendsList = friendsStorage.getCommonFriends(2,3);

		assertThat(commonFriendsList.size())
				.isEqualTo(1);

		assertThat(commonFriendsList.get(0).getId())
				.isEqualTo(1);
	}
}
