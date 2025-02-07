package no.fintlabs;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.test.simple.SimpleSpan;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.fintlabs.applicationResource.*;
import no.fintlabs.resource.ResourceController;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;

import static com.github.tomakehurst.wiremock.http.Response.response;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureObservability
@WebMvcTest(ResourceController.class)
@Import({ResponseFilter.class})
@ExtendWith({MockitoExtension.class})
public class ResponseFilterTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ResponseFilter responseFilter;
    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private Tracer tracer;
    @Mock
    private TraceContext.Builder traceContextBuilderMock;
    @MockBean
    private ApplicationResourceService applicationResourceServiceMock;
    @MockBean
    private ApplicationCategoryService applicationCategoryService;
    @MockBean
    private AccessTypeService accessTypeService;
    @MockBean
    private ServiceConfiguration serviceConfiguration;

    @Autowired
    private WebApplicationContext context;

    private final static String ID_TOKEN = "dummyToken";
    private final static String TRACE_ID_HEADER_NAME = "X-Correlation-Id";

    @BeforeEach
    public void setup() throws ServletException {
        Jwt jwt = createMockJwtToken();
        createSecurityContext(jwt);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ResourceController(applicationResourceServiceMock, applicationCategoryService, accessTypeService, serviceConfiguration)).build();
    }

    @Test
    public void testCorrelationIdInResponseHeader() throws Exception {

        HttpServletResponse mockResp = Mockito.mock(HttpServletResponse.class);

        mockResp.getHeader(TRACE_ID_HEADER_NAME);

        ApplicationResourceDTOFrontendDetail applicationResource = new ApplicationResourceDTOFrontendDetail();
        applicationResource.setId(1L);
        FintJwtEndUserPrincipal principal = new FintJwtEndUserPrincipal();

        when(applicationResourceServiceMock.getApplicationResourceDTOFrontendDetailById(principal, 1L))
                .thenReturn(applicationResource);

        ResponseFilter responseFilter = new ResponseFilter(tracer);

        String traceId = "123e4567-e89b-12d3-a456-426614174000";

        tracer.traceContextBuilder().traceId(traceId).spanId("mockSpanId").build();
        when(tracer.currentSpan().context().traceId()).thenReturn(traceId);

        mockMvc.perform(get("/api/resources/1"))
                .andExpect(status().isOk())
                .andExpect(header().string(TRACE_ID_HEADER_NAME, traceId));
            }
    @Test
    public void testDoFilter() throws IOException, ServletException {
        String traceId = "123e4567-e89b-12d3-a456-426614174000";

        tracer.traceContextBuilder().traceId(traceId).spanId("mockSpanId").build();
        when(tracer.currentSpan().context().traceId()).thenReturn(traceId);

        ResponseFilter responseFilter = new ResponseFilter(tracer);

        HttpServletResponse mockResp = Mockito.mock(HttpServletResponse.class);
        HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
        FilterChain mockChain = Mockito.mock(FilterChain.class);

        responseFilter.doFilter(mockReq, mockResp, mockChain);
        assertEquals(traceId, mockResp.getHeader(TRACE_ID_HEADER_NAME));
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
