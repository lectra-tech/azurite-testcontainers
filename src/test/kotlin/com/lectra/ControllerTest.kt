package com.lectra

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class ControllerTest(@Autowired private val wac: WebApplicationContext) {

    private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

    @Test
    fun `ping should respond pong`() {
        mockMvc.get("/ping").andExpect {
            status { isOk }
            content {
                string("pong")
            }
        }
    }

}