package no.fintlabs.applicationResource;


import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
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
        Specification<ApplicationResource> applicationResourceSpecification;

        if (orgUnitIds.contains(OrgUnitType.ALLORGUNITS.name())) {
            applicationResourceSpecification = Specification.where(null);
        } else {
            applicationResourceSpecification = allAuthorizedOrgUnitIds(orgUnitIds);
        }

        return applicationResourceSpecification;
    }


    public Specification<ApplicationResource> allAuthorizedOrgUnitIds(List<String> orgUnitIds) {
        return  (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.get("validForOrgUnits")).value(orgUnitIds)
            ;
        };
    }
