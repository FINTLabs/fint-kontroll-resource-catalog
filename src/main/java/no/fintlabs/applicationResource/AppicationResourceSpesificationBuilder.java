package no.fintlabs.applicationResource;


import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.opa.model.OrgUnitType;
import org.springframework.data.jpa.domain.Specification;
import no.fintlabs.kodeverk.handhevingstype.HandhevingstypeLabels;

import java.util.List;
import java.util.Set;

@Slf4j
public class AppicationResourceSpesificationBuilder {
    private final String search;
    private final List<String> scopedOrgUnitIds;
    private final List<String> filteredOrgUnitIds;
    private final String resourceType;
    private final List<String> userType;
    private final String accessType;
    private final List<String> applicationCategory;
    private final List<String> status;


    public AppicationResourceSpesificationBuilder(
            String search,
            List<String> scopedOrgUnitIds,
            List<String> filteredOrgUnitIds,
            String resourceType,
            List<String> userType,
            String accessType,
            List<String> applicationCategory,
            List<String> status) {
        this.search = search;
        this.scopedOrgUnitIds = scopedOrgUnitIds;
        this.filteredOrgUnitIds = filteredOrgUnitIds;
        this.resourceType = resourceType;
        this.userType = userType;
        this.accessType = accessType;
        this.applicationCategory = applicationCategory;
        this.status = status;
    }

    public Specification<ApplicationResource> build() {
        Specification<ApplicationResource> applicationResourceSpecification;
        
        if (search != null) {
            applicationResourceSpecification = resourceNameLike(search);
        }
        else {
            applicationResourceSpecification = Specification.where(null);
        }

        if (scopedOrgUnitIds != null && !scopedOrgUnitIds.contains(OrgUnitType.ALLORGUNITS.name())){
            applicationResourceSpecification =
                    applicationResourceSpecification.and(allAuthorizedOrgUnitIds(scopedOrgUnitIds).or(resourceAccessIsUnlimited()));
        }
        if (filteredOrgUnitIds != null){
            applicationResourceSpecification =
                    applicationResourceSpecification.and(allAuthorizedOrgUnitIds(filteredOrgUnitIds));
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

        if (status != null) {
            applicationResourceSpecification = applicationResourceSpecification.and(statuslike(status));
        }

        //applicationResourceSpecification = applicationResourceSpecification.and(isActive());

        return applicationResourceSpecification;
    }

    public static Specification<ApplicationResource> allAuthorizedOrgUnitIds(List<String> orgUnitIds) {

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

    public Specification<ApplicationResource> statuslike(List<String> status) {
        return ((root, query, criteriaBuilder) -> {
            CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("status"));
            for (String statu : status) {
                inClause.value(statu);
            }
            return inClause;
        });

    }


    public Specification<ApplicationResource> isActive() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("status"),"ACTIVE");
    }

    public Specification<ApplicationResource> resourceAccessIsUnlimited() {
        Set<String > unlimitedLicenceEnforcementTypes = Set.of(
                HandhevingstypeLabels.NOTSET.name(),
                HandhevingstypeLabels.FREEALL.name(),
                HandhevingstypeLabels.FREEEDU.name(),
                HandhevingstypeLabels.FREESTUDENT.name());
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("licenseEnforcement")).value(unlimitedLicenceEnforcementTypes);
    }

}
