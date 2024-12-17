package no.fintlabs.applicationResourceLocation;

public record ApplicationResourceLocationExtended(
        Long id,
        Long applicationResourceId,
        String resourceId,
        String orgUnitId,
        String orgUnitName,
        Long resourceLimit){
}
