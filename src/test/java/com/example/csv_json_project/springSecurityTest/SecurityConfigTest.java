package com.example.csv_json_project.springSecurityTest;

import com.example.csv_json_project.springSecurity.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPublicEndpoint() throws Exception {
        mockMvc.perform(get("/api/csv/upload"))
                .andExpect(status().isOk());
    }

    @Test
    void testH2ConsoleEndpoint() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testAuthenticatedEndpoint() throws Exception {
        mockMvc.perform(get("/api/some-authenticated-endpoint"))
                .andExpect(status().isOk());
    }

    @Test
    void testUnauthenticatedEndpoint() throws Exception {
        mockMvc.perform(get("/api/some-authenticated-endpoint"))
                .andExpect(status().isUnauthorized());
    }
}