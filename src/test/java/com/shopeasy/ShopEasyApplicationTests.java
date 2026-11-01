package com.shopeasy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
@AutoConfigureMockMvc
class ShopEasyApplicationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void productsAreSeededWithImages() throws Exception {
        mvc.perform(get("/api/products"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.length()").value(12))
           .andExpect(jsonPath("$[0].imageUrl").exists());
    }

    @Test
    void searchByBrand() throws Exception {
        mvc.perform(get("/api/products?brand=Nike"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.length()").value(2));
    }
}
