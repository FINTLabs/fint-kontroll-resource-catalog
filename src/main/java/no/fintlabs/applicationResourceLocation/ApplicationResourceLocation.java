package no.fintlabs.applicationResourceLocation;

import lombok.*;
import no.fintlabs.applicationResource.ApplicationResource;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "application_resource_location")
public class ApplicationResourceLocation {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String resourceId;
    private String orgunitId;
    private String orgUnitName;
    private Long resourceLimit;

}
