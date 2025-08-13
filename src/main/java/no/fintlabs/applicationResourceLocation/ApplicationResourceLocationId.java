package no.fintlabs.applicationResourceLocation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationResourceLocationId implements Serializable {
    @Column(name = "resource_ref", nullable = false)
    private Long applicationResourceId;
    @Column(name = "orgunit_id", nullable = false)
    private String orgUnitId;

}