package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceDTOFrontendList;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ResponseFactory {
    private final ApplicationResourceService applicationResourceService;
    private final ResponseUtil responseUtil;

    public ResponseFactory(ApplicationResourceService applicationResourceService, ResponseUtil responseUtil) {
        this.applicationResourceService = applicationResourceService;
        this.responseUtil = responseUtil;
    }

    public ResponseEntity<Map<String, Object>> toResponsEntity(
            FintJwtEndUserPrincipal from,
            String search,
            List<String> orgUnits,
            String type,
            List<String> userType,
            String accessType,
            List<String> applicationCategory,
            List<String> status,
            int page,
            int size) {
        List<ApplicationResourceDTOFrontendList> applicationResourceDTOFrontendLists =
                applicationResourceService.getApplicationResourceDTOFrontendList(
                        from,
                        search,
                        orgUnits,
                        type,
                        userType,
                        accessType,
                        applicationCategory,
                        status
                );

        List<ApplicationResourceDTOFrontendList> applicationResourceDTOFrontendListFiltered = applicationResourceDTOFrontendLists
                .stream().filter(ent -> ent.getIdentityProviderGroupObjectId()!=null)
                .toList();

        return responseUtil.toResponseEntity(
                responseUtil.toPage(applicationResourceDTOFrontendListFiltered, PageRequest.of(page, size))
        );
    }
}
