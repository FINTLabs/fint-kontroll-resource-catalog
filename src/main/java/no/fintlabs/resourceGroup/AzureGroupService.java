package no.fintlabs.resourceGroup;

import no.fintlabs.cache.FintCache;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AzureGroupService {
    private final FintCache<Long, AzureGroup> azureGroupFintCache;

    public AzureGroupService(FintCache<Long, AzureGroup> azureGroupFintCache) {
        this.azureGroupFintCache = azureGroupFintCache;
    }
    public List<AzureGroup> getAllAzureGroups() {
        return azureGroupFintCache.getAllDistinct();
    }
}
