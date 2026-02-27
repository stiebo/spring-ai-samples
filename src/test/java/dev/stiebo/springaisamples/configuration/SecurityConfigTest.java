package dev.stiebo.springaisamples.configuration;

import dev.stiebo.springaisamples.controller.TopAttractionsController;
import dev.stiebo.springaisamples.dto.TopAttractionsOutDto;
import dev.stiebo.springaisamples.service.TopAttractionsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TopAttractionsController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopAttractionsService topAttractionsService;

    // Required so SecurityConfig can instantiate JdbcUserDetailsManager without a real DB
    @MockBean
    private DataSource dataSource;

    @Test
    void apiEndpoint_withoutCredentials_returns401() throws Exception {
        mockMvc.perform(post("/api/top-attractions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"Paris\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void apiEndpoint_withValidUser_returns200() throws Exception {
        when(topAttractionsService.getTopAttractions(anyString()))
                .thenReturn(new TopAttractionsOutDto("Paris", new String[]{"Eiffel Tower"}));

        mockMvc.perform(post("/api/top-attractions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"Paris\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerUiPaths_withoutCredentials_areNotBlocked() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus(),
                        "Swagger UI should not be blocked by security"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus(),
                        "/v3/api-docs should not be blocked by security"));
    }
}
