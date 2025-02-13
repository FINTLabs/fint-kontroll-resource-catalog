package no.fintlabs.resource;

import jakarta.servlet.ServletException;
import no.fintlabs.Application;
import no.fintlabs.ResponseFactory;
import no.fintlabs.ServiceConfiguration;
import no.fintlabs.applicationResource.*;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kodeverk.brukertype.BrukertypeService;
import no.fintlabs.kodeverk.handhevingstype.HandhevingstypeLabels;
import no.fintlabs.opa.OpaService;
import no.fintlabs.opa.model.OrgUnitType;
import no.fintlabs.resourceGroup.AzureGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResourceController.class)
//@Testcontainers
//@ActiveProfiles("test")
//@Import({ApplicationResourceService.class})
public class ResourceControllerTest  {
    private MockMvc mockMvc;
    @MockBean
    private ApplicationResourceService applicationResourceService;
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

    private ApplicationResource resource1;
    private ApplicationResource resource2;

    @Autowired
    private WebApplicationContext context;

    private final static String ID_TOKEN = "dummyToken";

    @BeforeEach
    public void setUp() throws ServletException {
        Jwt jwt = createMockJwtToken();
        createSecurityContext(jwt);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

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

        PageRequest pageRequest = PageRequest.of(0, 100, Sort.by("resourceName"));

        given((opaService.getOrgUnitsInScope(Mockito.any(String.class)))).willReturn(List.of(OrgUnitType.ALLORGUNITS.name()));
        given(applicationResourceService.findBySearchCriteria(
                null, null,null, null, null, null,
                pageRequest))
                .willReturn(new PageImpl<>(List.of(resource2, resource1)));

        MvcResult result = mockMvc.perform(get("/api/resources/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationResources", hasSize(2)))
                .andReturn();
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
