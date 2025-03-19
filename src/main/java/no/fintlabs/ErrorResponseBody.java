package no.fintlabs;

import java.util.Date;

public record ErrorResponseBody(int status, String message, String correlationId, Date timestamp) {}