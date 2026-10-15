package br.app.coeur.core.config;

import br.app.coeur.modules.shopping.dto.ShoppingListResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String SHOPPING_LISTS = "shopping-lists";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration shoppingListsConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(ShoppingListResponse.class)));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(SHOPPING_LISTS, shoppingListsConfig)
                // put/evict só são aplicados após o commit da transação,
                // evitando repopular o cache com o dado antigo
                .transactionAware()
                .build();
    }
}
