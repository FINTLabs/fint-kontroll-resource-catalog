package no.fintlabs;

import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationExtended;
import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.kodeverk.brukertype.Brukertype;
import no.fintlabs.resourceGroup.AzureGroup;
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
    FintCache<Long, ApplicationResource> publishedApplicationCache() {
        return createCache(ApplicationResource.class);
    }

    @Bean
    FintCache<Long, AzureGroup> azureGroupCache() {

        return createCache(AzureGroup.class);
    }
    @Bean
    FintCache<Long, Brukertype> brukertypeCache() {
        return createCache(Brukertype.class);
    }

    @Bean
    FintCache<Long, ApplicationResourceLocationExtended> publishedExtendedApplicationResourceLocation() {
        return createCache(ApplicationResourceLocationExtended.class);
    }


    private <V> FintCache<Long, V> createCache(Class<V> resourceClass) {
        return fintCacheManager.createCache(
                resourceClass.getName().toLowerCase(Locale.ROOT),
                Long.class,
                resourceClass
        );
    }
}
