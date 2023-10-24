package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
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
                applicationResourceService.getApplicationResourceDTOFrontendList(principal, search,orgUnits);

        ResponseEntity<Map<String, Object>> entity = toResponseEntity(
                toPage(applicationResourceDTOFrontendLists,
                        PageRequest.of(page, size))
        );
        return entity;
    }

    public ResponseEntity<Object> toResponsEntity(FintJwtEndUserPrincipal principal,
                                                  String id,
                                                  String type){
        //TODO: Hente ett applicationResource object. Pakke inn i ResponseEntity og returnere dette.
        return null;
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


}
