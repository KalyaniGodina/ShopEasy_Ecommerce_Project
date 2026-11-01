package com.shopeasy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
@AutoConfigureMockMvc
class AuthTests {

    @Autowired
    MockMvc mvc;

    private static String json(String email, String password) {
        return "{\"name\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void ordersRequireSignIn() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{}"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void productWritesRequireAdmin() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void signupLogsUserInAndMeWorks() throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(json("new.user@example.com", "password123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new.user@example.com"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) r.getRequest().getSession(false);
        mvc.perform(get("/api/auth/me").session(session))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        String body = json("dup@example.com", "password123");
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isConflict());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@shopeasy.com\",\"password\":\"wrong-password\"}"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shortPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(json("short@example.com", "abc")))
           .andExpect(status().isBadRequest());
    }
}
