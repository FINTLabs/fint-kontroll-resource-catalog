package no.fintlabs.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application,Long> {

    Optional<Application> findApplicationByApplicationIdEndingWithIgnoreCase(String applicationId);
}
