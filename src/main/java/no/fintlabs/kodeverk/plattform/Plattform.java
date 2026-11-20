package no.fintlabs.kodeverk.plattform;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "plattform_kodeverk",
        uniqueConstraints = @UniqueConstraint(name = "uk_plattform_name", columnNames = "name")
)
public class Plattform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,unique = true)
    private String name;



}
