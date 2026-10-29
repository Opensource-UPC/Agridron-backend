package pe.com.agridron.field.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.field.domain.exceptions.InvalidGeometryException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that the Field advice wins over the global handler for invalid geometries while
 * the global handler keeps covering every other exception.
 */
@WebMvcTest
@Import(FieldExceptionHandlerWebMvcTest.ThrowingController.class)
class FieldExceptionHandlerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void mapsInvalidGeometryToBadRequest() throws Exception {
        mockMvc.perform(get("/test/geometry/invalid").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_GEOMETRY"))
                .andExpect(jsonPath("$.detail").value("Invalid geometry: exterior ring must be closed"))
                .andExpect(jsonPath("$.details").value("exterior ring must be closed"))
                .andExpect(jsonPath("$.type").value("/api/v1/errors/invalid-geometry"));
    }

    @Test
    void localizesInvalidGeometryMessage() throws Exception {
        mockMvc.perform(get("/test/geometry/invalid")
                        .header("Accept-Language", "es")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_GEOMETRY"))
                .andExpect(jsonPath("$.detail").value("Geometría inválida: exterior ring must be closed"));
    }

    @Test
    void keepsGlobalHandlerForOtherExceptions() throws Exception {
        mockMvc.perform(get("/test/geometry/unexpected").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("UNEXPECTED_ERROR"));
    }

    @RestController
    @RequestMapping("/test/geometry")
    static class ThrowingController {

        @GetMapping("/invalid")
        void invalid() {
            throw new InvalidGeometryException("exterior ring must be closed");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("boom");
        }
    }
}
