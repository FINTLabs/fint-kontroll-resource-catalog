package no.fintlabs.kodeverk.applikasjonskategori;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ApplikasjonskategoriRepository extends JpaRepository<Applikasjonskategori, Long> {

    List<Applikasjonskategori> findByNameIn(Collection<String> names);
}
