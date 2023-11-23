package no.fintlabs.resourceGroup;

import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class CacheConfiguration {

    private final FintCacheManager fintCacheManager;

    public CacheConfiguration(FintCacheManager fintCacheManager) {
        this.fintCacheManager = fintCacheManager;
    }

    @Bean
    FintCache<Long, AzureGroup> azureGroupCache() {
        return createCache(AzureGroup.class);
    }
    private <V> FintCache<Long, V> createCache(Class<V> resourceClass) {
        return fintCacheManager.createCache(
                resourceClass.getName().toLowerCase(Locale.ROOT),
                Long.class,
                resourceClass
        );
    }
}
