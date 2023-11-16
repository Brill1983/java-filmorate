package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventStorage {

    Optional<Event> getEventById(long eventId);

    List<Event> getEventsByUserId(long userId);

    Event add(Event event);

    Event update(Event event);

//    void delete(long eventId);

    // TODO - почистить после проверки
//    static Event createEvent(long userId, EventType eventType, long entityId, Operation operation) {
//        Event event = new Event();
//        event.setUserId(userId);
//        event.setEventType(eventType);
//        event.setEntityId(entityId);
//        event.setOperation(operation);
//        event.setTimestamp(LocalDateTime.now());
//        return event;
//    }
}
