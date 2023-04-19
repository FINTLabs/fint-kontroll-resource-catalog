package no.fintlabs.application;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(schema = "public")
public class Application {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String applicationId;
    private String applicationName;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> applicationResourceIds = new ArrayList<>();
}
