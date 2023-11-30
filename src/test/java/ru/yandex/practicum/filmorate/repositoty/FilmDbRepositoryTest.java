package ru.yandex.practicum.filmorate.repositoty;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaCategory;
import ru.yandex.practicum.filmorate.storage.FilmDbRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class FilmDbRepositoryTest {

    private final FilmDbRepository filmStorage;
    private Film film1;
    private Film film2;

    @BeforeEach
    public void beforeEach() {
        film1 = Film.builder()
                .name("film1")
                .description("some description")
                .releaseDate(LocalDate.of(1990, 1, 5))
                .duration(80)
                .mpa(new MpaCategory(1, "G"))
                .build();
        film1.setGenres(Set.of(new Genre(1, null)));
        film1.setDirectors(Set.of(new Director(1, "Director1")));

        film2 = Film.builder()
                .id(1L)
                .name("FILM2")
                .description("NEW_DESC")
                .releaseDate(LocalDate.of(1991, 2, 6))
                .duration(100)
                .mpa(new MpaCategory(2, "PG"))
                .build();
        film2.setGenres(Set.of(new Genre(1, null), new Genre(2, null)));
        film2.setDirectors(Set.of(new Director(1, null), new Director(2, null)));
    }

    @Test
    public void testAddFilm() {

        Film backedFilm = filmStorage.addFilm(film1);

        assertThat(backedFilm)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "film1")
                .hasFieldOrPropertyWithValue("description", "some description")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1990, 1, 5))
                .hasFieldOrPropertyWithValue("duration", 80);

        Integer genreId = backedFilm.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList()).get(0);

        assertThat(genreId)
                .isEqualTo(1);

        assertThat(backedFilm.getDirectors().size())
                .isEqualTo(1);

        Integer mpaCatId = backedFilm.getMpa().getId();

        assertThat(mpaCatId)
                .isEqualTo(1);
    }

    @Test
    public void testUpdateFilm() {

        filmStorage.addFilm(film1);

        Film backedFilm = filmStorage.updateFilm(film2);

        assertThat(backedFilm)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "FILM2")
                .hasFieldOrPropertyWithValue("description", "NEW_DESC")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1991, 2, 6))
                .hasFieldOrPropertyWithValue("duration", 100);

        Integer mpaCatId = backedFilm.getMpa().getId();

        assertThat(mpaCatId)
                .isEqualTo(2);
        assertEquals(2, backedFilm.getGenres().size(), "Wrong genre list");
        assertThat(backedFilm.getDirectors().size())
                .isEqualTo(2);
    }

    @Test
    public void testGetAllFilms() {

        filmStorage.addFilm(film1);

        List<Film> filmList = filmStorage.getFilmsList();

        assertThat(filmList.size())
                .isEqualTo(1);
    }

    @Test
    public void testGetFilmById() {

        filmStorage.addFilm(film1);

        Optional<Film> userOptional = filmStorage.getFilmById(1);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(filmFromDb ->
                        assertThat(filmFromDb).hasFieldOrPropertyWithValue("id", 1L)
                                .hasFieldOrPropertyWithValue("name", "film1")
                                .hasFieldOrPropertyWithValue("description", "some description")
                                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1990, 1, 5))
                                .hasFieldOrPropertyWithValue("duration", 80)
                );
    }
}
