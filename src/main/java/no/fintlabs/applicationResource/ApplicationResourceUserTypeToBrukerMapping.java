package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kodeverk.brukertype.Brukertype;
import no.fintlabs.kodeverk.brukertype.BrukertypeLabels;

@Slf4j
public class ApplicationResourceUserTypeToBrukerMapping {
    public static Brukertype mapResourceUserTypeToBrukerType(ApplicationResourceUserType applicationResourceUserType) {
        Brukertype brukertype = new Brukertype();
        if (applicationResourceUserType.internalUserType() == null || applicationResourceUserType.sourceUserType() == null) {
            log.warn("InternalUserType or SourceUserType is null. Cannot map to Brukertype");
            return null;
        }
        brukertype.setFkLabel(applicationResourceUserType.sourceUserType());
        brukertype.setLabel(BrukertypeLabels.valueOf(applicationResourceUserType.internalUserType()));
        return brukertype;
    }
}
