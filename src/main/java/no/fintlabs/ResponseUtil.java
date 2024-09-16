package no.fintlabs;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceDTOFrontendList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ResponseUtil {

    public  <T> Page<T> toPage(List<T> list, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), list.size());

        return start > list.size()
                ? new PageImpl<>(new ArrayList<>(), paging, list.size())
                : new PageImpl<>(list.subList(start, end), paging, list.size());
    }


    public <T> ResponseEntity<Map<String, Object>> toResponseEntity(Page<T> page) {
        return new ResponseEntity<>(
                Map.of(
                        "totalItems", page.getTotalElements(),
                        "resources", page.getContent(),
                        "currentPage", page.getNumber(),
                        "totalPages", page.getTotalPages()
                ),
                HttpStatus.OK
        );
    }






}
