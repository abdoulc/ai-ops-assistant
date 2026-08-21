package org.abdel.aiops.api.filter;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorrelationIdFilterTest {

    @Test
    void shouldPreserveValidIncomingCorrelationId()
            throws ServletException, IOException {

        MockHttpServletResponse response =
                executeFilterWithHeader("incident-123");

        assertThat(response.getHeader(
                CorrelationIdFilter.HEADER_NAME
        )).isEqualTo("incident-123");
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing()
            throws ServletException, IOException {

        MockHttpServletResponse response =
                executeFilterWithHeader(null);

        assertThat(response.getHeader(
                CorrelationIdFilter.HEADER_NAME
        ))
                .isNotBlank()
                .matches("[0-9a-f-]{36}");
    }

    @Test
    void shouldReplaceInvalidCorrelationId()
            throws ServletException, IOException {

        MockHttpServletResponse response =
                executeFilterWithHeader("incident 123\r\ninjected");

        assertThat(response.getHeader(
                CorrelationIdFilter.HEADER_NAME
        ))
                .isNotEqualTo("incident 123\r\ninjected")
                .matches("[0-9a-f-]{36}");
    }

    @Test
    void shouldExposeCorrelationIdInMdcAndClearItAfterRequest()
            throws ServletException, IOException {

        CorrelationIdFilter filter =
                new CorrelationIdFilter();

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                "incident-123"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) -> {
                    assertThat(MDC.get(
                            CorrelationIdFilter.MDC_KEY
                    )).isEqualTo("incident-123");
                }
        );

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY))
                .isNull();
    }

    @Test
    void shouldClearMdcWhenRequestProcessingFails() {
        CorrelationIdFilter filter =
                new CorrelationIdFilter();

        MockHttpServletRequest request =
                new MockHttpServletRequest();
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                "incident-123"
        );

        assertThatThrownBy(() -> filter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) -> {
                    throw new ServletException("Processing failed");
                }
        )).isInstanceOf(ServletException.class);

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY))
                .isNull();
    }


    private MockHttpServletResponse executeFilterWithHeader(
            String correlationId
    ) throws ServletException, IOException {

        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        if (correlationId != null) {
            request.addHeader(
                    CorrelationIdFilter.HEADER_NAME,
                    correlationId
            );
        }

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                new MockFilterChain()
        );

        return response;
    }
}
