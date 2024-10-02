package no.fintlabs.kodeverk.brukertype;

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
@Table(name = "brukertype_kodeverk")
public class Brukertype {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fkLabel;
    @Enumerated(EnumType.STRING)
    private BrukertypeLabels label;
}

