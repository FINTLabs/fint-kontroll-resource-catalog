package no.fintlabs.applicationResource;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.kodeverk.handhevingstype.Handhevingstype;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;

public class ApplicationResourceSpecification {

    public static Specification<ApplicationResource> hasNameLike(String search) {
        return (root, query, criteriaBuilder) ->
                search == null ? criteriaBuilder.conjunction() : criteriaBuilder.like(criteriaBuilder.lower(root.get("resourceName")),"%" + search.toLowerCase() + "%" );
    }

    public static Specification<ApplicationResource> isAccessable(boolean hasAccessAllToAppResources, Set<Long> accessableRestrictedResourceIds) {

        if (hasAccessAllToAppResources) {
            return (root, query, criteriaBuilder) ->criteriaBuilder.conjunction();
        }
        return Specification.where(resourceAccessIsUnlimited().or(restrictedResourceIsInAccessableRestrictedResources(accessableRestrictedResourceIds)));
    }

    public static Specification<ApplicationResource> resourceAccessIsUnlimited() {
        Set<String > unlimitedLicenceEnforcementTypes = Handhevingstype.getUnRestrictedLicenceEnforcementTypes();
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("licenseEnforcement")).value(unlimitedLicenceEnforcementTypes);
    }
    public static Specification<ApplicationResource> restrictedResourceIsInAccessableRestrictedResources(Set<Long> restrictedApplicationResourceIds) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("Id")).value(restrictedApplicationResourceIds)
        );
    }

    public static Specification<ApplicationResource> isInFilteredOrgUnits(List<String> orgUnitIds) {

        if (orgUnitIds == null || orgUnitIds.isEmpty()) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) -> {
                    Join<ApplicationResource, ApplicationResourceLocation> orgUnitJoin = root.join("validForOrgUnits");

            return orgUnitJoin.get("orgUnitId").in(orgUnitIds);
        };
    }

    public static Specification<ApplicationResource> userTypeLike(List<String> userTypes) {
        if (userTypes == null || userTypes.isEmpty()) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) -> {
            Join<ApplicationResource, String> userTypeJoin = root.join("validForRoles");

            return userTypeJoin.in(userTypes);
        };
    }

    public static Specification<ApplicationResource> applicationCategoryLike(List<String> applicationCategories) {
        if (applicationCategories == null || applicationCategories.isEmpty()) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) -> {
            Join<ApplicationResource,String> applicationCategoryJoin = root.join("applicationCategory");

            return applicationCategoryJoin.in(applicationCategories);
        };
    }

    public static Specification<ApplicationResource> accessTypeLike(String accessType) {
        return (root, query, criteriaBuilder) ->
                accessType == null ?criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("accessType"), accessType);
    }

    public static Specification<ApplicationResource> statuslike(List<String> status) {
        if (status == null || status.isEmpty()) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.conjunction();
        }
        return ((root, query, criteriaBuilder) -> {
            CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("status"));
            for (String statu : status) {
                inClause.value(statu);
            }
            return inClause;
        });
    }
}
