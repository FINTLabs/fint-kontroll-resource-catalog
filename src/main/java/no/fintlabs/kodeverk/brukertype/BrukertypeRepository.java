package no.fintlabs.kodeverk.brukertype;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrukertypeRepository extends JpaRepository<Brukertype, Long> {

    Optional<Brukertype> findBrukertypeByLabel(BrukertypeLabels label);
}
