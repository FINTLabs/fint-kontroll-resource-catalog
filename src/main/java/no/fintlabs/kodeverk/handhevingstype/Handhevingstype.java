package no.fintlabs.kodeverk.handhevingstype;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
