package ru.kurs.inventory.admin;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level", nullable = false, length = 10)
    private String level;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "actor", length = 80)
    private String actor;

    @Column(name = "entity_type", length = 80)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected SystemLog() {
    }

    public SystemLog(String level, String eventType, String message) {
        this.level = level;
        this.eventType = eventType;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
