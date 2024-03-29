package no.fintlabs.applicationResourceLocation;

import lombok.*;


import jakarta.persistence.*;

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
    @Column(name = "orgunit_id")
    private String orgUnitId;
    private String orgUnitName;
    private Long resourceLimit;

}
