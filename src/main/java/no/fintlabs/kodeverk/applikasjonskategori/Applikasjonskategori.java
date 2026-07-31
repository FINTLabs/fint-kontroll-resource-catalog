package no.fintlabs.kodeverk.applikasjonskategori;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "applikasjonskategori_kodeverk",
        uniqueConstraints = @UniqueConstraint(name = "uk_applikasjonskategori_name", columnNames = "name")
)
public class Applikasjonskategori {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Applikasjonskategori(String name) {
        this.name = name;
    }
}
