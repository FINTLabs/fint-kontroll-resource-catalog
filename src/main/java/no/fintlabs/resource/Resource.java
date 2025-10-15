package no.fintlabs.resource;


import lombok.*;

import jakarta.persistence.*;
import lombok.experimental.SuperBuilder;
import no.fintlabs.audit.AuditEntity;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public abstract class Resource extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String resourceId;
    protected String resourceName;
    protected String resourceType;
    protected UUID identityProviderGroupObjectId;
    protected String identityProviderGroupName;

    public Resource() {
    }
}
