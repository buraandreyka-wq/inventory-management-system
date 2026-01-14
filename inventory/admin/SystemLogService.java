package ru.kurs.inventory.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    public void info(String eventType, String message, String actor, String entityType, Long entityId) {
        save("INFO", eventType, message, actor, entityType, entityId);
    }

    public void warn(String eventType, String message, String actor, String entityType, Long entityId) {
        save("WARN", eventType, message, actor, entityType, entityId);
    }

    public void error(String eventType, String message, String actor, String entityType, Long entityId) {
        save("ERROR", eventType, message, actor, entityType, entityId);
    }

    public void save(String level, String eventType, String message, String actor, String entityType, Long entityId) {
        SystemLog log = new SystemLog(level, eventType, message);
        log.setActor(actor);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        systemLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<SystemLog> last(int limit) {
        // simple approach without paging: keep it small and sort in db by id desc
        return systemLogRepository.findAll(org.springframework.data.domain.PageRequest.of(0, limit,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))).getContent();
    }
}
