package com.evs.electricvehiclestore;

import com.evs.electricvehiclestore.controller.HomeController;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomeControllerTest {
    @Test
    void publicEntryPointsForwardToTheFrontendWithoutRedirectingToSignIn() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new HomeController()).build();
        for (String path : new String[]{"/", "/signin", "/signup", "/register"}) {
            mvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(forwardedUrl("/index.html"));
        }
    }
}
