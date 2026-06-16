package com.yuri.yeaicodegenplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Resource;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 缓存管理器配置
 *
 * @author yuri
 * @create 2026-06-16 23:54
 */
@Configuration
public class RedisCacheManagerConfig {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public CacheManager cacheManager() {
        // 配置 ObjectMapper 支持 Java8 时间类型
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))   // 默认缓存时间 30 分钟
                .disableCachingNullValues()         // 禁用缓存空值
                // key 使用 String 序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
                // value 使用 JSON 序列化器（支持复杂对象）
                // 注：如果对 value 进行 JSON 序列化，可能会出现无法反序列化情况，因为 Redis 中没有存储 Java 类信息，不知道反序列化成哪个类就会出现报错
                // .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                // 针对 good_app_page 配置 5 分钟过期
                .withCacheConfiguration("good_app_page", defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}
