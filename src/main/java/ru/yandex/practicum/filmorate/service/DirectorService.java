package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectRequestBodyException;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorRepository;
    private final ValidationService validationService;

    public List<Director> getAllDirectors() {
        return directorRepository.getAllDirectors();
    }

    public Director getDirectorById(int id) {
        return directorRepository.getDirectorById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Режиссер с ID " + id + " нет в базе"));
    }

    public Director addDirector(Director director) {
        if (StringUtils.isBlank(director.getName())) {
            throw new IncorrectRequestBodyException("Имя режиссера обзятельно для заполнения");
        }
        return directorRepository.addDirector(director);
    }

    public Director updateDirector(Director director) {
        validationService.validDirectorId(director.getId());
        if (StringUtils.isBlank(director.getName())) {
            throw new IncorrectRequestBodyException("Имя режиссера обзятельно для заполнения");
        }
        return directorRepository.updateDirector(director);
    }

    public void deleteDirector(int id) {
        getDirectorById(id);
        directorRepository.deleteDirector(id);
    }

}
