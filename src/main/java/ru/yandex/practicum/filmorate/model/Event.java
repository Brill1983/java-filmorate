package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "eventId")
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    private Long eventId;

    @NotNull
    private Long userId;

    @NotNull
    private EventType eventType;

    @NotNull
    private Long entityId;

    @NotNull
    private Operation operation;

    private Long timestamp = Instant.now().toEpochMilli();

    public Event(Long userId, EventType eventType, Long entityId, Operation operation) {
        this.userId = userId;
        this.eventType = eventType;
        this.entityId = entityId;
        this.operation = operation;
    }
}
