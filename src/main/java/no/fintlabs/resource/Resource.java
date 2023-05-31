package no.fintlabs.resource;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public abstract class Resource {
    private String id;
    private String resourceId;
    private String resourceName;
    private String resourceType;
}
