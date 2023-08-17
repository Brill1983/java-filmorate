package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorRepository;

import javax.validation.ValidationException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;

    public List<Director> getAllDirectors() {
        return directorRepository.getAllDirectors();
    }

    public Director getDirectorById(int id) {
        return directorRepository.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        if (StringUtils.isBlank(director.getName())) {
            throw new ValidationException("Имя режиссера обзятельно для заполнения");
        }
        return directorRepository.addDirector(director);
    }

    public Director updateDirector(Director director) {
        if (StringUtils.isBlank(director.getName())) {
            throw new ValidationException("Имя режиссера обзятельно для заполнения");
        }
        return directorRepository.updateDirector(director);
    }

    public void deleteDirector(int id) {
        directorRepository.deleteDirector(id);
    }

}
