package no.fintlabs.applicationResource;


import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Slf4j
public class AppicationResourceSpesificationBuilder {
    private final String search;
    private final List<String> orgUnitIds;
    private final String resourceType;
    private final List<String> userType;
    private final String accessType;
    private final List<String> applicationCategory;


    public AppicationResourceSpesificationBuilder(String search, List<String> orgUnitIds, String resourceType, List<String> userType, String accessType, List<String> applicationCategory) {
        this.search = search;
        this.orgUnitIds = orgUnitIds;
        this.resourceType = resourceType;
        this.userType = userType;
        this.accessType = accessType;
        this.applicationCategory = applicationCategory;
    }

    public Specification<ApplicationResource> build() {
        Specification<ApplicationResource> applicationResourceSpecification;
        
        if (search != null) {
            applicationResourceSpecification = resourceNameLike(search);
        }
        else {
            applicationResourceSpecification = Specification.where(null);
        }

        if (orgUnitIds != null){
            applicationResourceSpecification = applicationResourceSpecification.and(allAuthorizedOrgUnitIds(orgUnitIds));
        }

        if (resourceType != null) {
            applicationResourceSpecification = applicationResourceSpecification.and(resourceTypeLike(resourceType));
        }

        if (userType != null) {
            applicationResourceSpecification = applicationResourceSpecification.and(userTypeLike(userType));
        }

        if (accessType != null){
            applicationResourceSpecification= applicationResourceSpecification.and(accessTypeLike(accessType));
        }

        if (applicationCategory != null){
            applicationResourceSpecification= applicationResourceSpecification.and(applicationCategoryLike(applicationCategory));
        }

        return applicationResourceSpecification;
    }

    public Specification<ApplicationResource> allAuthorizedOrgUnitIds(List<String> orgUnitIds) {
        return (root, query, criteriaBuilder) -> {
            Join<ApplicationResource, ApplicationResourceLocation> orgUnitJoin = root.join("validForOrgUnits");

            return orgUnitJoin.get("orgUnitId").in(orgUnitIds);
        };
    }

    public Specification<ApplicationResource> resourceNameLike(String search) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("resourceName")),"%" + search.toLowerCase() + "%" );
    }

    public Specification<ApplicationResource> resourceTypeLike(String resourceType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("resourceType")),"%" + resourceType.toLowerCase() + "%" );
    }

    public Specification<ApplicationResource> userTypeLike(List<String> userType) {
        return (root, query, criteriaBuilder) -> {
            Join<ApplicationResource, String> userTypeJoin = root.join("validForRoles");

            return userTypeJoin.in(userType);
        };
    }

    public Specification<ApplicationResource> accessTypeLike(String accessType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("accessType"), accessType);
    }

    public Specification<ApplicationResource> applicationCategoryLike(List<String> applicationCategory) {
        return (root, query, criteriaBuilder) -> {
            Join<ApplicationResource,String> applicationCategoryJoin = root.join("applicationCategory");

            return applicationCategoryJoin.in(applicationCategory);
        };
    }



}
