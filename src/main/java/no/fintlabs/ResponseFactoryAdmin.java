package no.fintlabs;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceDTOFrontendListForAdmin;
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
public class ResponseFactoryAdmin {

    private final ResponseUtil responseUtil;

    public ResponseFactoryAdmin(ResponseUtil responseUtil) {
        this.responseUtil = responseUtil;
    }


//    public ResponseEntity<Map<String, Object>> toResponseEntityAdmin(List<ApplicationResourceDTOFrontendListForAdmin> allApplicationResourceDTOsForAdmins, int page, int size) {
//
//        ResponseEntity<Map<String, Object>> responseEntityForAdmin = responseUtil.toResponseEntity(
//                responseUtil.toPage (allApplicationResourceDTOsForAdmins, PageRequest.of(page, size))
//        );
//
//        return responseEntityForAdmin;
//    }
}
