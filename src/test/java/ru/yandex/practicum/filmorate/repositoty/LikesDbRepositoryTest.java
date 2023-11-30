package ru.yandex.practicum.filmorate.repositoty;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class LikesDbRepositoryTest {

    @Autowired
    private UserStorage userStorage;
    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private LikesStorage likesStorage;

    @Test
    public void testUserLikeFilmDeleteLikeCheckLikeGetMostPopularFilmList() {
        User user = User.builder()
                .email("user@ya.ru")
                .login("userLogin")
                .name("userName")
                .birthday(LocalDate.of(1990, 1, 5))
                .build();
        userStorage.saveUser(user);

        Film film = Film.builder()
                .name("film1")
                .description("some description")
                .releaseDate(LocalDate.of(1990, 1, 5))
                .duration(80)
                .mpa(new MpaCategory(1, "G"))
                .build();
        film.setGenres(Set.of(new Genre(1, "Комедия")));
        film.setDirectors(Set.of(new Director(1, "Director1")));
        filmStorage.addFilm(film);

        likesStorage.userLikedFilm(1, 1);
        boolean checkLikeTrue = likesStorage.checkUserLikedFilm(1, 1);

        assertThat(checkLikeTrue)
                .isEqualTo(true);

        List<Film> popularFilmList = filmStorage.getMostPopularFilms(10);
        assertThat(popularFilmList.size())
                .isEqualTo(1);

        popularFilmList = filmStorage.getMostPopularFilmsByGenre(1, 10);
        assertThat(popularFilmList.size())
                .isEqualTo(1);

        popularFilmList = filmStorage.getMostPopularFilmsByYear(1990, 10);
        assertThat(popularFilmList.size())
                .isEqualTo(1);

        likesStorage.deleteLike(1, 1);

        boolean checkLikeFalse = likesStorage.checkUserLikedFilm(1, 1);

        assertThat(checkLikeFalse)
                .isEqualTo(false);
    }
}
