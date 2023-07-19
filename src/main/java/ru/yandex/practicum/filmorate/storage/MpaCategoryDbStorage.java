package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaCategory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class MpaCategoryDbStorage implements MpaCategoryStorage {

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public List<MpaCategory> findAllMpaCategories() {
        String sql = "select * from MPA_CATEGORIES";
        List<MpaCategory> mpaCategoryList = jdbcTemplate.query(sql, (rs, rowNum) -> makeMpaCategory(rs));
        log.info("Найдено {} категорий", mpaCategoryList.size());
        return mpaCategoryList;
    }

    @Override
    public Optional<MpaCategory> findMpaCategoryById(int id) {
        String sql = "select * from MPA_CATEGORIES where CATEGORY_MPA_ID = :id";
        List<MpaCategory> mpaCategoryList = jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> makeMpaCategory(rs));
        if (!mpaCategoryList.isEmpty()) {
            log.info("Найдена категория MPA с ID: {} и названием {} ", mpaCategoryList.get(0).getId(), mpaCategoryList.get(0).getName());
            return Optional.of(mpaCategoryList.get(0));
        } else {
            log.info("Категория c идентификатором {} не найдена", id);
            return Optional.empty();
        }
    }

    @Override
    public MpaCategory createNewMpaCategory(MpaCategory mpaCategory) {
        String sql = "insert into MPA_CATEGORIES (NAME) VALUES (:name)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("name", mpaCategory.getName());

        jdbcTemplate.update(sql, map, keyHolder);
        mpaCategory.setId(keyHolder.getKey().intValue());
        log.info("Внесена новая категория {} c ID {}", mpaCategory.getName(), mpaCategory.getId());
        return mpaCategory;
    }

    private MpaCategory makeMpaCategory(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("CATEGORY_MPA_ID");
        String name = rs.getString("NAME");
        return new MpaCategory(id, name);
    }
}
