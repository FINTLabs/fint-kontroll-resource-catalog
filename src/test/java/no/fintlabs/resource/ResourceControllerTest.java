package no.fintlabs.resource;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.ServletException;
import no.fintlabs.OrgUnitType;
import no.fintlabs.ProblemDetailFactory;
import no.fintlabs.RequestBodyLogging;
import no.fintlabs.ServiceConfiguration;
import no.fintlabs.applicationResource.AccessTypeService;
import no.fintlabs.applicationResource.ApplicationCategoryService;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceFrontendRequest;
import no.fintlabs.applicationResource.ApplicationResourceMapper;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.fintlabs.kodeverk.brukertype.BrukertypeService;
import no.fintlabs.kodeverk.handhevingstype.HandhevingstypeLabels;
import no.fintlabs.opa.OpaService;
import no.fintlabs.resourceGroup.ResourceGroupPublishComponent;
import no.fintlabs.resourceAvailability.ResourceAvailabilityService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(ResourceController.class)
@Testcontainers
@ActiveProfiles("test")
@Import({
        ApplicationResourceMapper.class,
        RequestBodyLogging.class
})
public class ResourceControllerTest  {
    private MockMvc mockMvc;
    @MockBean
    private ApplicationResourceService applicationResourceService;
    @MockBean
    private ResourceAvailabilityService resourceAvailabilityService;
    @MockBean
    private OpaService opaService;
    @MockBean
    private ApplicationCategoryService applicationCategoryService;
    @MockBean
    private AccessTypeService accessTypeService;
    @MockBean
    BrukertypeService brukertypeService;
    @MockBean
    ServiceConfiguration serviceConfiguration;
    @MockBean
    private Tracer tracer;
    @MockBean
    private ProblemDetailFactory problemDetailFactory;
    @MockBean
    private ResourceGroupPublishComponent resourceGroupPublishComponent;

    private ApplicationResource resource1;
    private ApplicationResource resource2;
    private FintJwtEndUserPrincipal principal;

    @Autowired
    private WebApplicationContext context;

    private final static String ID_TOKEN = "dummyToken";

    @BeforeEach
    public void setUp() throws ServletException {
        Jwt jwt = createMockJwtToken();
        createSecurityContext(jwt);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
        principal = FintJwtEndUserPrincipal.from(jwt);

        resource1 = ApplicationResource.builder()
                .resourceId("1")
                .resourceName("Resource B")
                .licenseEnforcement(HandhevingstypeLabels.FREEALL.name())
                .status("ACTIVE")
                .build();

        resource2 = ApplicationResource.builder()
                .resourceId("2")
                .resourceName("Resource A")
                .licenseEnforcement(HandhevingstypeLabels.FREEALL.name())
                .status("ACTIVE")
                .build();
    }

    @Test
    public void getAllActiveResources_ShouldReturnTwoResources() throws Exception {

        Sort sort = Sort.by(Sort.Order.asc("resourceName"));
        Pageable pageable = PageRequest.of(0, 100, sort);

        given((opaService.getOrgUnitsInScope(Mockito.any(String.class)))).willReturn(List.of(OrgUnitType.ALLORGUNITS.name()));
        given(applicationResourceService.searchApplicationResources(
                principal,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of("ACTIVE", "PENDING_ACTIVE"),
                pageable,
                false))
                .willReturn(new PageImpl<>(List.of(resource2, resource1)));
        given(resourceAvailabilityService.getUsage(Mockito.anyString())).willReturn(0L);

        mockMvc.perform(get("/api/resources/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resources", hasSize(2)))
                .andExpect(jsonPath("$.resources[0].resourceUsage").value(0))
                .andReturn();
    }

    @Test
    public void createApplicationResource_ShouldPassRequestToService() throws Exception {
        mockMvc.perform(post("/api/resources/v1")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceName": "Resource A",
                                  "resourceType": "APPLICATION",
                                  "applicationCategory": [1],
                                  "platform": ["WEB"],
                                  "validForRoles": ["STUDENT"],
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(applicationResourceService).createApplicationResource(argThat((ApplicationResourceFrontendRequest request) ->
                List.of(1L).equals(request.getApplicationCategory())
                        && "Resource A".equals(request.getResourceName())
        ));
    }

    @Test
    public void createApplicationResource_ShouldAcceptUnknownApplicationCategoryIds() throws Exception {
        mockMvc.perform(post("/api/resources/v1")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceName": "Resource A",
                                  "applicationCategory": [999]
                                }
                                """))
                .andExpect(status().isCreated());

        verify(applicationResourceService).createApplicationResource(argThat((ApplicationResourceFrontendRequest request) ->
                List.of(999L).equals(request.getApplicationCategory())
        ));
    }

    @Test
    public void publishAll_ShouldUseCacheModeByDefault() throws Exception {
        mockMvc.perform(post("/api/resources/admin/publishall"))
                .andExpect(status().isOk());

        verify(resourceGroupPublishComponent).publishResourceGroups(false, false);
    }

    @Test
    public void publishAll_ShouldForcePublishWhenPublishAllParameterIsTrue() throws Exception {
        mockMvc.perform(post("/api/resources/admin/publishall")
                        .param("publishAll", "true"))
                .andExpect(status().isOk());

        verify(resourceGroupPublishComponent).publishResourceGroups(true, false);
    }

    @Test
    public void publishAll_ShouldPublishToMsGraphWhenPublishToMsGraphParameterIsTrue() throws Exception {
        mockMvc.perform(post("/api/resources/admin/publishall")
                        .param("publishToMsGraph", "true"))
                .andExpect(status().isOk());

        verify(resourceGroupPublishComponent).publishResourceGroups(false, true);
    }

    @Test
    public void publishAllMsGraphAsCreate_ShouldPublishAllMsGraphAsCreate() throws Exception {
        mockMvc.perform(post("/api/resources/admin/publishall-ms-graph-create"))
                .andExpect(status().isOk());

        verify(resourceGroupPublishComponent).publishAllResourceGroupsMsGraphAsCreate();
    }

    @Test
    public void updateApplicationResource_ShouldPassRequestToService() throws Exception {
        mockMvc.perform(put("/api/resources/v1")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "resourceId": "app-1",
                                  "resourceName": "Resource A",
                                  "resourceType": "APPLICATION",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isAccepted());

        ArgumentCaptor<ApplicationResourceFrontendRequest> requestCaptor =
                ArgumentCaptor.forClass(ApplicationResourceFrontendRequest.class);
        verify(applicationResourceService).updateApplicationResource(requestCaptor.capture());

        ApplicationResourceFrontendRequest request = requestCaptor.getValue();
        assertEquals(1L, request.getId());
        assertEquals("app-1", request.getResourceId());
        assertNull(request.getPlatform());
        assertNull(request.getApplicationCategory());
    }

    @Test
    public void updateApplicationResource_ShouldAcceptUnknownApplicationCategoryIds() throws Exception {
        mockMvc.perform(put("/api/resources/v1")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "applicationCategory": [999]
                                }
                                """))
                .andExpect(status().isAccepted());

        verify(applicationResourceService).updateApplicationResource(argThat((ApplicationResourceFrontendRequest request) ->
                List.of(999L).equals(request.getApplicationCategory())
        ));
    }

    private void createSecurityContext(Jwt jwt) throws ServletException {
        SecurityContextHolder.getContext().setAuthentication(createJwtAuthentication(jwt));
        SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
        authInjector.afterPropertiesSet();
    }

    private UsernamePasswordAuthenticationToken createJwtAuthentication(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(jwt, null, authorities);
        return authentication;
    }

    private Jwt createMockJwtToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("authenticated", "ROLE_USER"));
        Jwt jwt = new Jwt(ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), claims, claims);
        return jwt;
    }
}
