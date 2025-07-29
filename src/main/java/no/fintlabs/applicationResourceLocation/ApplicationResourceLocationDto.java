package no.fintlabs.applicationResourceLocation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import no.fintlabs.applicationResource.ApplicationResource;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationResourceLocationDto {

    private String resourceId;
    private String resourceName;
    private String orgUnitId;
    private String orgUnitName;
    private Long resourceLimit;

}
