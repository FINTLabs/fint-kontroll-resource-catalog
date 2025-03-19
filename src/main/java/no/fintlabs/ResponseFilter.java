package no.fintlabs;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class ResponseFilter extends HttpFilter {
    private static final String TRACE_ID_HEADER_NAME = "X-Correlation-Id";
    private final Tracer tracer;

    public ResponseFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            String correlationId = Objects.requireNonNull(tracer.currentSpan()).context().traceId();
            response.setHeader(TRACE_ID_HEADER_NAME, correlationId);
            chain.doFilter(request, response);
        }
        catch (NullPointerException e) {
            throw  new NullPointerException("No active span found");
        }
    }
}

