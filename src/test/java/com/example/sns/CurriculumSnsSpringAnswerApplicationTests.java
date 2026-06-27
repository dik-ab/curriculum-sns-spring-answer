package com.example.sns;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.sns.auth.EmailVerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CurriculumSnsSpringAnswerApplicationTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private EmailVerificationTokenRepository emailTokens;

    @Test
    void registerVerifyLoginAndPost() throws Exception {
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"username\":\"alice\",\"displayName\":\"Alice\",\"password\":\"password123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("alice")));

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        String verificationToken = emailTokens.findAll().getFirst().getToken();
        mvc.perform(get("/auth/verify-email").param("token", verificationToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("メールアドレスを確認しました")));

        String accessToken = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", not("")))
            .andReturn().getResponse().getContentAsString()
            .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");

        mvc.perform(post("/posts")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"First Spring SNS post\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", is("First Spring SNS post")))
            .andExpect(jsonPath("$.author.username", is("alice")));
    }
}
