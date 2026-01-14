package ru.kurs.inventory.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SystemSettingService {

    private final SystemSettingRepository repository;

    public SystemSettingService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SystemSetting> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public String get(String key, String defaultValue) {
        return repository.findByKey(key).map(SystemSetting::getValue).orElse(defaultValue);
    }

    public void upsert(String key, String value) {
        SystemSetting setting = repository.findByKey(key)
                .orElseGet(() -> repository.save(new SystemSetting(key, value)));
        setting.setValue(value);
        repository.save(setting);
    }
}
