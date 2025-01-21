package no.fintlabs.kodeverk.brukertype;

import no.fintlabs.DatabaseIntegrationTest;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.fintlabs.applicationResource.ApplicationResourceUserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import({BrukertypeService.class})
class BrukertypeServiceIntegrationTest extends DatabaseIntegrationTest {
    @Autowired
    private BrukertypeRepository brukertypeRepository;
    @Autowired
    private BrukertypeService brukertypeService;

    ApplicationResourceUserType applicationResourceUserTypeStudent =
            new ApplicationResourceUserType(BrukertypeLabels.STUDENT.name(),"Elev");
    ApplicationResourceUserType applicationResourceUserTypeEmployeeStaff =
            new ApplicationResourceUserType(BrukertypeLabels.EMPLOYEESTAFF.name(),"Ansatte utenom skole");

    @BeforeEach
    public void setUp() {
        brukertypeRepository.deleteAll();
        brukertypeService.save(applicationResourceUserTypeStudent);
        brukertypeService.save(applicationResourceUserTypeEmployeeStaff);
    }
    @Test
    void whenSavingTwoApplicationResourceUserTypesRepositoryShouldBeUpdated() {
        assertEquals(2, brukertypeRepository.count());
        assertEquals("Elev", brukertypeRepository.findBrukertypeByLabel(BrukertypeLabels.STUDENT).get().getFkLabel());
        assertEquals("Ansatte utenom skole", brukertypeRepository.findBrukertypeByLabel(BrukertypeLabels.EMPLOYEESTAFF).get().getFkLabel());
    }
    @Test
    void whenSavingBrukertypeWithNewDisplayNameRepositoryShouldBeUpdated() {
        ApplicationResourceUserType applicationResourceUserTypeStudentUpdated =
                new ApplicationResourceUserType(BrukertypeLabels.STUDENT.name(),"Elever");
        brukertypeService.save(applicationResourceUserTypeStudentUpdated);

        assertEquals(2, brukertypeRepository.count());
        assertEquals("Elever", brukertypeRepository.findBrukertypeByLabel(BrukertypeLabels.STUDENT).get().getFkLabel());
    }
}