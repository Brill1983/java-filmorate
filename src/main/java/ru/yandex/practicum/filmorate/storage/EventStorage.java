package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventStorage {

    Optional<Event> getEventById(long eventId);

    List<Event> getEventsByUserId(long userId);

    Event add(Event event);

    Event update(Event event);
}
