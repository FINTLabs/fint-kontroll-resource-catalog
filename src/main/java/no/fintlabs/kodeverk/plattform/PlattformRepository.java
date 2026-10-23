package no.fintlabs.kodeverk.plattform;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface PlattformRepository extends JpaRepository<Plattform,Long> {

    Set<Plattform> findAllBy();


}
