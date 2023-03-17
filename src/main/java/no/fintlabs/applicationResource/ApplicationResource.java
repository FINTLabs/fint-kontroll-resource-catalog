package no.fintlabs.applicationResource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.fintlabs.resource.Resource;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResource extends Resource {
    private Long id;
    private String applicationId;
    private String applicationAccessType;
    private String applicationAccessRole;
    private String platform;
    private String accessType;
}


