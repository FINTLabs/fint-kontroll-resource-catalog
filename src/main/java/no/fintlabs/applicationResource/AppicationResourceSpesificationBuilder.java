package no.fintlabs.applicationResource;


import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.opa.model.OrgUnitType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Slf4j
public class AppicationResourceSpesificationBuilder {
    private final String search;
    private final List<String> orgUnitIds;
    private final String type;
    private final List<String> userType;
    private final String accessType;
    private final List<String> applicationCategory;


    public AppicationResourceSpesificationBuilder(String search, List<String> orgUnitIds, String type, List<String> userType, String accessType, List<String> applicationCategory) {
        this.search = search;
        this.orgUnitIds = orgUnitIds;
        this.type = type;
        this.userType = userType;
        this.accessType = accessType;
        this.applicationCategory = applicationCategory;
    }

    public Specification<ApplicationResource> build() {
        Specification<ApplicationResource> applicationResourceSpecification = null;
        
        if (orgUnitIds != null){
            applicationResourceSpecification = allAuthorizedOrgUnitIds(orgUnitIds);
        }
       

        return applicationResourceSpecification;
    }

    public Specification<ApplicationResource> allAuthorizedOrgUnitIds(List<String> orgUnitIds) {
        return (root, query, criteriaBuilder) -> {
            // Create a join from ApplicationResource to ApplicationResourceLocation
            Join<ApplicationResource, ApplicationResourceLocation> orgUnitJoin = root.join("validForOrgUnits");

            // Check if the orgUnitId of the joined ApplicationResourceLocation is in the provided list of orgUnitIds
            return orgUnitJoin.get("orgUnitId").in(orgUnitIds);
        };
    }



//    public Specification<ApplicationResource> allAuthorizedOrgUnitIds(List<String> orgUnitIds) {
//
//        Join<ApplicationResource, ApplicationResourceLocation> orgUnitJoin = root.join("validForOrgUnits");
//
//        return  (root, query, criteriaBuilder) -> criteriaBuilder
//                .in(root.get("validForOrgUnits")).value(orgUnitIds)
//            ;
//        };
}
