package no.fintlabs.applicationResource;

import java.util.Arrays;
import java.util.Optional;

public enum ApplicationResourceStatus {
    PENDING_ACTIVE("PENDING_ACTIVE"),
    ACTIVE("ACTIVE"),
    DISABLED("DISABLED"),
    INACTIVE("INACTIVE"),
    DELETED("DELETED");

    private final String value;

    ApplicationResourceStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Optional<ApplicationResourceStatus> from(String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(applicationResourceStatus -> applicationResourceStatus.value.equals(status))
                .findFirst();
    }

    public static String pendingActiveUntilIdentityProviderGroupExists(String status, boolean hasIdentityProviderGroupObjectId) {
        if (ACTIVE.value.equals(status) && !hasIdentityProviderGroupObjectId) {
            return PENDING_ACTIVE.value;
        }

        return status;
    }

}
