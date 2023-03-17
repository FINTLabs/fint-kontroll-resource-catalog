package no.fintlabs.applicationResource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Application {
    private Long id;
    private String applicationId;
    private String applicationName;
    private List<String> applicationResourceIds;
}
