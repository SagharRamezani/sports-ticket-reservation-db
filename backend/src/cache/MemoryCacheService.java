package cache;

import services.CacheService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryCacheService implements CacheService {
    private final Map<String, CacheEntry> storage = new ConcurrentHashMap<>();
    private volatile boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!enabled) {
            clear();
        }
    }

    @Override
    public void put(String key, String value, long ttlSeconds) {
        if (!enabled || key == null || key.isBlank() || value == null) {
            return;
        }

        long safeTtlSeconds = ttlSeconds <= 0 ? 300L : ttlSeconds;
        long expiresAt = Instant.now().getEpochSecond() + safeTtlSeconds;

        storage.put(key, new CacheEntry(value, expiresAt));
    }

    @Override
    public Optional<String> get(String key) {
        if (!enabled || key == null || key.isBlank()) {
            return Optional.empty();
        }

        CacheEntry entry = storage.get(key);

        if (entry == null) {
            return Optional.empty();
        }

        if (entry.isExpired()) {
            storage.remove(key);
            return Optional.empty();
        }

        return Optional.of(entry.value());
    }

    @Override
    public void remove(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        storage.remove(key);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    public int size() {
        removeExpiredEntries();
        return storage.size();
    }

    public void removeExpiredEntries() {
        for (Map.Entry<String, CacheEntry> item : storage.entrySet()) {
            if (item.getValue().isExpired()) {
                storage.remove(item.getKey());
            }
        }
    }

    private record CacheEntry(String value, long expiresAt) {
        private boolean isExpired() {
            return Instant.now().getEpochSecond() >= expiresAt;
        }
    }
}
