package services;

import cache.MemoryCacheService;
import cache.RedisCacheService;

import java.util.Optional;

public interface CacheService {
    boolean isEnabled();

    void setEnabled(boolean enabled);

    void put(String key, String value, long ttlSeconds);

    Optional<String> get(String key);

    void remove(String key);

    void clear();

    default void put(String key, String value) {
        put(key, value, 300L);
    }

    default boolean contains(String key) {
        return get(key).isPresent();
    }

    static CacheService defaultCache() {
        RedisCacheService redisCacheService = RedisCacheService.fromEnvironment();

        if (redisCacheService.isRedisAvailable()) {
            return redisCacheService;
        }

        return new MemoryCacheService();
    }
}
