package no.fintlabs.kodeverk.handhevingstype;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "handhevingstype_kodeverk")
public class Handhevingstype {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fkLabel;
    @Enumerated(EnumType.STRING)
    private HandhevingstypeLabels label;

    public static Set<String> getUnRestrictedLicenceEnforcementTypes() {
        return Set.of(
                HandhevingstypeLabels.NOTSET.name(),
                HandhevingstypeLabels.FREEALL.name(),
                HandhevingstypeLabels.FREEEDU.name(),
                HandhevingstypeLabels.FREESTUDENT.name());
    }
}
