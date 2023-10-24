package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.opa.model.Scope;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import no.fintlabs.opa.AuthorizationClient;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ApplicationResourceService {
    private final ApplicationResourceRepository applicationResourceRepository;
    private final ApplicationResourceEntityProducerService applicationResourceEntityProducerService;
    private final AuthorizationClient authorizationClient;

    public ApplicationResourceService(ApplicationResourceRepository applicationResourceRepository, ApplicationResourceEntityProducerService applicationResourceEntityProducerService,AuthorizationClient authorizationClient) {
        this.applicationResourceRepository = applicationResourceRepository;
        this.applicationResourceEntityProducerService = applicationResourceEntityProducerService;
        this.authorizationClient = authorizationClient;
    }

    public void save(ApplicationResource applicationResource){
        applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(applicationResource.getResourceId())
                .ifPresentOrElse(onSaveExistingApplicationResource(applicationResource),
                        onSaveNewApplicationResource(applicationResource));
    }

    private Runnable onSaveNewApplicationResource(ApplicationResource applicationResource) {
        return () -> {
            ApplicationResource newApplicationResource = applicationResourceRepository.save(applicationResource);
        };
    }

    private Consumer<ApplicationResource> onSaveExistingApplicationResource(ApplicationResource applicationResource) {
        return existingApplicationResource -> {
            applicationResource.setId(existingApplicationResource.getId());
            applicationResourceRepository.save(applicationResource);
        };
    }

    @Transactional
    public List<ApplicationResourceDTOFrontendDetail> getAllApplicationResources(FintJwtEndUserPrincipal principal) {
        ModelMapper modelMapper = new ModelMapper();
        List<ApplicationResource> applicationResources = applicationResourceRepository.findAllApplicationResources();
        return applicationResources.stream()
                .map(applicationResource -> modelMapper.map(applicationResource, ApplicationResourceDTOFrontendDetail.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional <ApplicationResourceDTOFrontendDetail> getApplicationResourceById(FintJwtEndUserPrincipal principal, Long id) {
        ModelMapper modelMapper = new ModelMapper();
        Optional<ApplicationResource> applicationResourceOptional = applicationResourceRepository.findById(id);
        return applicationResourceOptional
                .map(applicationResource -> modelMapper.map(applicationResource, ApplicationResourceDTOFrontendDetail.class));

    }



    public List<ApplicationResourceDTOFrontendList> getApplicationResourceDTOFrontendList(FintJwtEndUserPrincipal principal,
                                                                                          String search) {
        List<String> validOrgUnits = getAllAuthorizedOrgUnitIDs();
        List<ApplicationResource> applicationResources;

        applicationResources = applicationResourceRepository.findApplicationResourceByOrgUnitIds(search,validOrgUnits);
        log.info("Fetching applicationResources. Count: " + applicationResources.size());

        return applicationResources
                .stream()
                .map(ApplicationResource::toApplicationResourceDTOFrontendList)
                .toList();
    }

    public List<String> getAllAuthorizedOrgUnitIDs(){
        List<Scope> scopes = authorizationClient.getUserScopes();
        List<String> authorizedOrgUnitIDs = scopes.stream()
                .filter(s -> s.getObjectType().equals("resource"))
                .map(Scope::getOrgUnits)
                .flatMap(Collection::stream)
                .toList();
        log.info("Authorized orgUnitIDs : " +authorizedOrgUnitIDs);
        return authorizedOrgUnitIDs;
    }



    @PostConstruct
    public void init(){
        log.info("Starting applicationResourceService....");


        List<String> validForRolesAppRes1 = new ArrayList<>();
        validForRolesAppRes1.add("student");
        List<String> validForRolesAppRes2 = new ArrayList<>();
        validForRolesAppRes2.add("employee");
        List<String> validForRolesAppRes3 = new ArrayList<>();
        validForRolesAppRes3.add("student");
        validForRolesAppRes3.add("employee");

        List<String> plattformAppres1 = new ArrayList<>();
        plattformAppres1.add("WIN");
        plattformAppres1.add("Linux");
        List<String> plattformAppres2 = new ArrayList<>();
        plattformAppres2.add("Mac");
        plattformAppres2.add("WIN");
        List<String> plattformAppres3 = new ArrayList<>();
        plattformAppres3.add("ios");
        plattformAppres3.add("android");
        plattformAppres3.add("WIN");

        //ApplicationResource1
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("adobek12");
        appRes1.setResourceName("Adobe K12 Utdanning");
        appRes1.setResourceType("ApplicationResource");
        appRes1.setIdentityProviderGroupObjectId(UUID.fromString("735e619a-8905-4f68-9dab-b908076c097b"));

        appRes1.setResourceLimit(1000L);
        appRes1.setResourceOwnerOrgUnitId("6");
        appRes1.setResourceOwnerOrgUnitName("KOMP Utdanning og kompetanse");
        appRes1.setValidForRoles(validForRolesAppRes1);
        ApplicationResourceLocation applicationResourceLocation1 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgunitId("194")
                .orgUnitName("VGMIDT Midtbyen videregående skole")
                .resourceLimit(100L)
                .build();
        ApplicationResourceLocation applicationResourceLocation2 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgunitId("198")
                .orgUnitName("VGSTOR Storskog videregående skole")
                .resourceLimit(200L)
                .build();
        List<ApplicationResourceLocation> locationsAppRes1 = new ArrayList<>();
        locationsAppRes1.add(applicationResourceLocation1);
        locationsAppRes1.add(applicationResourceLocation2);
        appRes1.setValidForOrgUnits(locationsAppRes1);
        appRes1.setApplicationAccessType("ApplikasjonTilgang");
        appRes1.setApplicationAccessRole("Full access");
        appRes1.setPlatform(plattformAppres1);
        appRes1.setAccessType("device");
        this.save(appRes1);
        applicationResourceEntityProducerService.publish(appRes1);


        //ApplicationResource2
        ApplicationResource appRes2 = new ApplicationResource();
        appRes2.setResourceId("msproject");
        appRes2.setResourceName("Microsoft Project Enterprise");
        appRes2.setResourceType("ApplicationResource");
        appRes2.setIdentityProviderGroupObjectId(UUID.fromString("f1f7e61f-73cb-49c0-bb72-5b17b3083ced"));
        appRes2.setResourceLimit(100L);

        appRes2.setResourceOwnerOrgUnitId("5");
        appRes2.setResourceOwnerOrgUnitName("FAK Finans og administrasjon");
        appRes2.setValidForRoles(validForRolesAppRes2);
        ApplicationResourceLocation applicationResourceLocation3 = ApplicationResourceLocation
                .builder()
                .resourceId("msproject")
                .orgunitId("26")
                .orgUnitName("OKO Økonomiavdeling")
                .resourceLimit(20L)
                .build();
        ApplicationResourceLocation applicationResourceLocation4 = ApplicationResourceLocation
                .builder()
                .resourceId("msproject")
                .orgunitId("30")
                .orgUnitName("OKO Regnskapsseksjon")
                .resourceLimit(30L)
                .build();
        List<ApplicationResourceLocation> locationsAppRes2 = new ArrayList<>();
        locationsAppRes2.add(applicationResourceLocation3);
        locationsAppRes2.add(applicationResourceLocation4);
        appRes2.setValidForOrgUnits(locationsAppRes2);
        appRes2.setApplicationAccessType("ApplikasjonTilgang");
        appRes2.setApplicationAccessRole("Full access");
        appRes2.setPlatform(plattformAppres2);
        appRes2.setAccessType("device");
        this.save(appRes2);
        applicationResourceEntityProducerService.publish(appRes2);

        //ApplicationResource3
        ApplicationResource appRes3 = new ApplicationResource();
        appRes3.setResourceId("mskabal");
        appRes3.setResourceName("Microsoft Kabal");
        appRes3.setResourceType("ApplicationResource");
        appRes3.setIdentityProviderGroupObjectId(UUID.fromString("f08a85cf-f2da-4456-8568-bc144926cb9b"));
        appRes3.setResourceLimit(300L);

        appRes3.setResourceOwnerOrgUnitId("36");
        appRes3.setResourceOwnerOrgUnitName("DIGIT Digitaliseringsavdeling");
        appRes3.setValidForRoles(validForRolesAppRes3);
        ApplicationResourceLocation applicationResourceLocation5 = ApplicationResourceLocation
                .builder()
                .resourceId("mskabal")
                .orgunitId("47")
                .orgUnitName("DIGIT Fagtjenester")
                .resourceLimit(70L)
                .build();
        ApplicationResourceLocation applicationResourceLocation6 = ApplicationResourceLocation
                .builder()
                .resourceId("mskabal")
                .orgunitId("38")
                .orgUnitName("DIGIT Teknologiseksjon")
                .resourceLimit(30L)
                .build();
        List<ApplicationResourceLocation> locationsAppRes3 = new ArrayList<>();
        locationsAppRes3.add(applicationResourceLocation5);
        locationsAppRes3.add(applicationResourceLocation6);
        appRes3.setValidForOrgUnits(locationsAppRes3);
        appRes3.setApplicationAccessType("ApplikasjonTilgang");
        appRes3.setApplicationAccessRole("Full access");
        appRes3.setPlatform(plattformAppres3);
        appRes3.setAccessType("device");
        this.save(appRes3);
        applicationResourceEntityProducerService.publish(appRes3);
    }


}
