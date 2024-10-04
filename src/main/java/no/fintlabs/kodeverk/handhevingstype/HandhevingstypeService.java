package no.fintlabs.kodeverk.handhevingstype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HandhevingstypeService {
    private final HandhevingstypeRepository handhevingstypeRepository;

    public HandhevingstypeService(HandhevingstypeRepository handhevingstypeRepository) {
        this.handhevingstypeRepository = handhevingstypeRepository;
    }

    public Handhevingstype updateHandhevingstype(Long id, HandhevingstypePatchDTO handhevingstypePatchDTO) {
        Handhevingstype currentHandhevingstype = handhevingstypeRepository.findById(id).orElse(null);
        if (currentHandhevingstype != null) {
            currentHandhevingstype.setFkLabel(handhevingstypePatchDTO.getFkLabel());
            log.info("Handhevingstype updated: {} - {} - {}",currentHandhevingstype.getId(),currentHandhevingstype.getFkLabel(),currentHandhevingstype.getLabel());
            return handhevingstypeRepository.saveAndFlush(currentHandhevingstype);
        }
        return null;
    }

    public Handhevingstype getHandhevingstypeById(Long id) {
        return handhevingstypeRepository.findById(id).orElse(null);
    }
}
