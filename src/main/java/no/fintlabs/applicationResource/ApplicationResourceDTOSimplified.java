package no.fintlabs.applicationResource;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;

import java.util.List;
@Getter
@Setter
@Builder
public class ApplicationResourceDTOSimplified {
    private Long id;
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private Long resourceLimit;
}
