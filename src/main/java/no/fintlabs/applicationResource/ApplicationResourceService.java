package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceGroup.AzureGroup;
import no.fintlabs.resourceGroup.ResourceGroupProducerService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.*;
import java.util.function.Consumer;

import static no.fintlabs.opa.model.OrgUnitType.ALLORGUNITS;


@Slf4j
@Service
public class ApplicationResourceService {

    private final ApplicationResourceRepository applicationResourceRepository;
    private final ResourceGroupProducerService resourceGroupProducerService;
    private final FintCache<Long, AzureGroup> azureGroupCache;
    private final AuthorizationUtil authorizationUtil;

    public ApplicationResourceService(ApplicationResourceRepository applicationResourceRepository,
                                      ResourceGroupProducerService resourceGroupProducerService,
                                      FintCache<Long, AzureGroup> azureGroupCache, AuthorizationUtil authorizationUtil) {
        this.applicationResourceRepository = applicationResourceRepository;
        this.resourceGroupProducerService = resourceGroupProducerService;
        this.azureGroupCache = azureGroupCache;
        this.authorizationUtil = authorizationUtil;
    }

    public void saveApplicationResource(ApplicationResource applicationResource) {
        System.out.println("resourceId" + applicationResource.getResourceId());
        System.out.println("resourceName: " + applicationResource.getResourceName());
    }

    public void saveApplicationResources(List<ApplicationResource> applicationResources) {
        applicationResources
                .stream()
                .peek(this::save);
    }

    public void save(ApplicationResource applicationResource) {
        applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(applicationResource.getResourceId())
                .ifPresentOrElse(onSaveExistingApplicationResource(applicationResource),
                                 onSaveNewApplicationResource(applicationResource));
    }

    private Runnable onSaveNewApplicationResource(ApplicationResource applicationResource) {
        return () -> {

            resourceGroupProducerService.publish(applicationResourceRepository.save(applicationResource));

        };
    }

    private Consumer<ApplicationResource> onSaveExistingApplicationResource(ApplicationResource applicationResource) {
        return existingApplicationResource -> {
            Long applicationResourceId = existingApplicationResource.getId();
            applicationResource.setId(applicationResourceId);

            Optional<AzureGroup> azureGroup = azureGroupCache.getOptional(applicationResourceId);

            if (!azureGroup.isEmpty()) {
                applicationResource.setIdentityProviderGroupObjectId(azureGroup.get().getId());
                applicationResource.setIdentityProviderGroupName(azureGroup.get().getDisplayName());
            }
            resourceGroupProducerService.publish(applicationResource);
            applicationResourceRepository.save(applicationResource);

        };
    }

    @Transactional
    public ApplicationResourceDTOFrontendDetail getApplicationResourceDTOFrontendDetailById(FintJwtEndUserPrincipal principal, Long id) {
        List<String> validOrgUnits = authorizationUtil.getAllAuthorizedOrgUnitIDs();
        ModelMapper modelMapper = new ModelMapper();

        Optional<ApplicationResource> applicationResourceOptional = applicationResourceRepository.findById(id);

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceOptional
                .map(applicationResource -> modelMapper.map(applicationResource, ApplicationResourceDTOFrontendDetail.class))
                .orElse(new ApplicationResourceDTOFrontendDetail());

        List<ApplicationResourceLocation> applicationResourceLocations = applicationResourceDTOFrontendDetail.getValidForOrgUnits();
        List<String> orgunitsInApplicationResourceLocations = new ArrayList<>();
        applicationResourceLocations.forEach(applicationResourceLocation -> {
            orgunitsInApplicationResourceLocations.add(applicationResourceLocation.getOrgUnitId());
        });

        List<String> validatedOrgUnits = orgunitsInApplicationResourceLocations.stream()
                .filter(orgUnit -> validOrgUnits.contains(ALLORGUNITS.name()) || validOrgUnits.contains(orgUnit))
                .toList();

        if (validatedOrgUnits.isEmpty()) {
            return new ApplicationResourceDTOFrontendDetail();
        } else {
            return applicationResourceDTOFrontendDetail;
        }
    }


    public List<ApplicationResourceDTOFrontendList> getApplicationResourceDTOFrontendList(FintJwtEndUserPrincipal principal,
                                                                                          String search,
                                                                                          List<String> orgUnits) {
        List<String> validOrgUnits = authorizationUtil.getAllAuthorizedOrgUnitIDs();

        List<String> orgUnitsToQuery = orgUnits.stream()
                .filter(orgUnit -> orgUnit.contains(ALLORGUNITS.name()) || validOrgUnits.contains(orgUnit))
                .toList();

        List<ApplicationResource> applicationResources =
                applicationResourceRepository.findApplicationResourceByOrgUnitIds(search, orgUnitsToQuery);

        return applicationResources
                .stream()
                .map(ApplicationResource::toApplicationResourceDTOFrontendList)
                .toList();
    }

    public List<ApplicationResourceDTOFrontendList> getApplicationResourceDTOFrontendList(FintJwtEndUserPrincipal principal,
                                                                                          String search) {
        List<String> validOrgUnits = authorizationUtil.getAllAuthorizedOrgUnitIDs();

        List<ApplicationResource> applicationResources;

        if (validOrgUnits.contains(ALLORGUNITS.name())) {
            applicationResources = applicationResourceRepository.findApplicationResourceByResourceName(search);
        } else {
            applicationResources = applicationResourceRepository.findApplicationResourceByOrgUnitIds(search, validOrgUnits);
        }

        log.info("Fetching applicationResources. Count: " + applicationResources.size());

        return applicationResources
                .stream()
                .map(ApplicationResource::toApplicationResourceDTOFrontendList)
                .toList();
    }

    public Optional<ApplicationResource> getApplicationResourceFromId(Long applicationResourceId) {
        return applicationResourceRepository.findById(applicationResourceId);
    }

    public List<ApplicationResource> getAllApplicationResources() {
        return applicationResourceRepository.findAll();
    }

    //
    //    @PostConstruct
    //    public void init() {
    //        log.info("Starting applicationResourceService....");
    //
    //
    //        List<String> validForRolesAppRes1 = new ArrayList<>();
    //        validForRolesAppRes1.add("student");
    //        List<String> validForRolesAppRes2 = new ArrayList<>();
    //        validForRolesAppRes2.add("employee");
    //        List<String> validForRolesAppRes3 = new ArrayList<>();
    //        validForRolesAppRes3.add("student");
    //        validForRolesAppRes3.add("employee");
    //
    //        List<String> plattformAppres1 = new ArrayList<>();
    //        plattformAppres1.add("WIN");
    //        plattformAppres1.add("Linux");
    //        List<String> plattformAppres2 = new ArrayList<>();
    //        plattformAppres2.add("Mac");
    //        plattformAppres2.add("WIN");
    //        List<String> plattformAppres3 = new ArrayList<>();
    //        plattformAppres3.add("ios");
    //        plattformAppres3.add("android");
    //        plattformAppres3.add("WIN");
    //
    //        //ApplicationResource1
    //        ApplicationResource appRes1 = new ApplicationResource();
    //        appRes1.setResourceId("adobek12");
    //        appRes1.setResourceName("Adobe K12 Utdanning");
    //        appRes1.setResourceType("ApplicationResource");
    //        appRes1.setIdentityProviderGroupObjectId(UUID.fromString("735e619a-8905-4f68-9dab-b908076c097b"));
    //
    //        appRes1.setResourceLimit(1000L);
    //        appRes1.setResourceOwnerOrgUnitId("6");
    //        appRes1.setResourceOwnerOrgUnitName("KOMP Utdanning og kompetanse");
    //        appRes1.setValidForRoles(validForRolesAppRes1);
    //        ApplicationResourceLocation applicationResourceLocation1 = ApplicationResourceLocation
    //                .builder()
    //                .resourceId("adobek12")
    //                .orgunitId("194")
    //                .orgUnitName("VGMIDT Midtbyen videregående skole")
    //                .resourceLimit(100L)
    //                .build();
    //        ApplicationResourceLocation applicationResourceLocation2 = ApplicationResourceLocation
    //                .builder()
    //                .resourceId("adobek12")
    //                .orgunitId("198")
    //                .orgUnitName("VGSTOR Storskog videregående skole")
    //                .resourceLimit(200L)
    //                .build();
    //        List<ApplicationResourceLocation> locationsAppRes1 = new ArrayList<>();
    //        locationsAppRes1.add(applicationResourceLocation1);
    //        locationsAppRes1.add(applicationResourceLocation2);
    //        appRes1.setValidForOrgUnits(locationsAppRes1);
    //        appRes1.setApplicationAccessType("ApplikasjonTilgang");
    //        appRes1.setApplicationAccessRole("Full access");
    //        appRes1.setPlatform(plattformAppres1);
    //        appRes1.setAccessType("device");
    //        this.save(appRes1);
    //        resourceGroupProducerService.publish(appRes1);
    //
    //
    //        //ApplicationResource2
    //        ApplicationResource appRes2 = new ApplicationResource();
    //        appRes2.setResourceId("msproject");
    //        appRes2.setResourceName("Microsoft Project Enterprise");
    //        appRes2.setResourceType("ApplicationResource");
    //        appRes2.setIdentityProviderGroupObjectId(UUID.fromString("f1f7e61f-73cb-49c0-bb72-5b17b3083ced"));
    //        appRes2.setResourceLimit(100L);
    //
    //        appRes2.setResourceOwnerOrgUnitId("5");
    //        appRes2.setResourceOwnerOrgUnitName("FAK Finans og administrasjon");
    //        appRes2.setValidForRoles(validForRolesAppRes2);
    //        ApplicationResourceLocation applicationResourceLocation3 = ApplicationResourceLocation
    //                .builder()
    //                .resourceId("msproject")
    //                .orgunitId("26")
    //                .orgUnitName("OKO Økonomiavdeling")
    //                .resourceLimit(20L)
    //                .build();
    //        ApplicationResourceLocation applicationResourceLocation4 = ApplicationResourceLocation
    //                .builder()
    //                .resourceId("msproject")
    //                .orgunitId("30")
    //                .orgUnitName("OKO Regnskapsseksjon")
    //                .resourceLimit(30L)
    //                .build();
    //        List<ApplicationResourceLocation> locationsAppRes2 = new ArrayList<>();
    //        locationsAppRes2.add(applicationResourceLocation3);
    //        locationsAppRes2.add(applicationResourceLocation4);
    //        appRes2.setValidForOrgUnits(locationsAppRes2);
    //        appRes2.setApplicationAccessType("ApplikasjonTilgang");
    //        appRes2.setApplicationAccessRole("Full access");
    //        appRes2.setPlatform(plattformAppres2);
    //        appRes2.setAccessType("device");
    //        this.save(appRes2);
    //        resourceGroupProducerService.publish(appRes2);
    //
    //        //ApplicationResource3
    //        ApplicationResource appRes3 = new ApplicationResource();
    //        appRes3.setResourceId("mskabal");
    //        appRes3.setResourceName("Microsoft Kabal");
    //        appRes3.setResourceType("ApplicationResource");
    //        appRes3.setIdentityProviderGroupObjectId(UUID.fromString("f08a85cf-f2da-4456-8568-bc144926cb9b"));
    //        appRes3.setResourceLimit(300L);
    //
    //        appRes3.setResourceOwnerOrgUnitId("36");
    //        appRes3.setResourceOwnerOrgUnitName("DIGIT Digitaliseringsavdeling");
    //        appRes3.setValidForRoles(validForRolesAppRes3);
    //        ApplicationResourceLocation applicationResourceLocation5 = ApplicationResourceLocation
    //                .builder()
    //                .resourceId("mskabal")
    //                .orgunitId("47")
    //                .orgUnitName("DIGIT Fagtjenester")
    //                .resourceLimit(70L)
    //                .build();
    //        ApplicationResourceLocation applicationResourceLocation6 = ApplicationResourceLocation
    //                .builder()
    //                .resourceId("mskabal")
    //                .orgunitId("38")
    //                .orgUnitName("DIGIT Teknologiseksjon")
    //                .resourceLimit(30L)
    //                .build();
    //        List<ApplicationResourceLocation> locationsAppRes3 = new ArrayList<>();
    //        locationsAppRes3.add(applicationResourceLocation5);
    //        locationsAppRes3.add(applicationResourceLocation6);
    //        appRes3.setValidForOrgUnits(locationsAppRes3);
    //        appRes3.setApplicationAccessType("ApplikasjonTilgang");
    //        appRes3.setApplicationAccessRole("Full access");
    //        appRes3.setPlatform(plattformAppres3);
    //        appRes3.setAccessType("device");
    //        this.save(appRes3);
    //        resourceGroupProducerService.publish(appRes3);
    //    }


}
