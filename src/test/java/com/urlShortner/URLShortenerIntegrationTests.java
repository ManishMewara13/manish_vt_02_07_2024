package com.urlShortner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class URLShortenerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testShortenURLAndRedirect() throws Exception {
        String longUrl = "https://example.com/very/long/url";

        // Test shortening URL
        String shortUrl = mockMvc.perform(MockMvcRequestBuilders.post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(longUrl))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.short_url").exists())
                .andReturn().getResponse().getContentAsString();

        // Test redirection
        mockMvc.perform(MockMvcRequestBuilders.get("/" + shortUrl))
                .andExpect(MockMvcResultMatchers.status().isFound()); // expects a redirect (302)
    }
}
