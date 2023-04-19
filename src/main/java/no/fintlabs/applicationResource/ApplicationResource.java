package no.fintlabs.applicationResource;

import lombok.*;
import no.fintlabs.resource.Resource;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(schema = "public")
public class ApplicationResource extends Resource {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String applicationId;
    private String applicationAccessType;
    private String applicationAccessRole;
    private String platform;
    private String accessType;
}


