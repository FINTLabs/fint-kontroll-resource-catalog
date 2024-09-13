package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceDTOFrontendList;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ResponseFactory {
    private final ApplicationResourceService applicationResourceService;

    public ResponseFactory(ApplicationResourceService applicationResourceService) {
        this.applicationResourceService = applicationResourceService;
    }

    public ResponseEntity<Map<String, Object>> toResponsEntity(FintJwtEndUserPrincipal principal,
                                                               String search,
                                                               String type,
                                                               int page,
                                                               int size) {
        List<ApplicationResourceDTOFrontendList> applicationResourceDTOFrontendLists =
                applicationResourceService.getApplicationResourceDTOFrontendList(principal, search);

        ResponseEntity<Map<String, Object>> entity = toResponseEntity(
                toPage(applicationResourceDTOFrontendLists,
                        PageRequest.of(page, size))
        );
        return entity;
    }

    public ResponseEntity<Map<String, Object>> toResponsEntity(FintJwtEndUserPrincipal principal,
                                                               String search,
                                                               List<String> orgUnits,
                                                               String type,
                                                               int page,
                                                               int size) {
        List<ApplicationResourceDTOFrontendList> applicationResourceDTOFrontendLists =
                applicationResourceService.getApplicationResourceDTOFrontendList(principal, search, orgUnits);

        ResponseEntity<Map<String, Object>> entity = toResponseEntity(
                toPage(applicationResourceDTOFrontendLists,
                        PageRequest.of(page, size))
        );
        return entity;
    }

    // new for V1
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

        ResponseEntity<Map<String, Object>> entity = toResponseEntity(
                toPage(applicationResourceDTOFrontendLists, PageRequest.of(page, size))
        );

        return entity;
    }


    public ResponseEntity<Map<String, Object>> toResponseEntity(Page<ApplicationResourceDTOFrontendList> page) {
        return new ResponseEntity<>(
                Map.of("totalItems", page.getTotalElements(),
                        "resources", page.getContent(),
                        "currentPage", page.getNumber(),
                        "totalPages", page.getTotalPages()
                ),
                HttpStatus.OK
        );
    }

    private Page<ApplicationResourceDTOFrontendList> toPage(List<ApplicationResourceDTOFrontendList> dtoFrontendList, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), dtoFrontendList.size());

        return start > dtoFrontendList.size()
                ? new PageImpl<>(new ArrayList<>(), paging, dtoFrontendList.size())
                : new PageImpl<>(dtoFrontendList.subList(start, end), paging, dtoFrontendList.size());
    }

    private Page<Object> toPageGeneric(List<Object> dtoList, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), dtoList.size());

        return start > dtoList.size()
                ? new PageImpl<>(new ArrayList<>(), paging, dtoList.size())
                : new PageImpl<>(dtoList.subList(start, end), paging, dtoList.size());
    }


    public ResponseEntity<Map<String, Object>> toResponseEntity(List<ApplicationResource> allApplicationResourcesForAdmins, int page, int size) {
        return null;
    }
}
