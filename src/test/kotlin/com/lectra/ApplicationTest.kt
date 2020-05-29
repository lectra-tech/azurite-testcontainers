package com.lectra

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*


@Testcontainers
@SpringBootTest
class ApplicationTest(@Autowired private val wac: WebApplicationContext) {

    private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

    companion object {
        @Container
        private val azuriteContainer = AzuriteContainer().withExposedPorts(10000)

        @DynamicPropertySource
        @JvmStatic
        fun configureApplication(registry: DynamicPropertyRegistry) {
            registry.add("azure.storage.blob-endpoint") { "http://127.0.0.1:${azuriteContainer.getMappedPort(10000)}/devstoreaccount1" }
        }
    }

    @Test
    fun `should put and get blob content`() {
        val containerName = "container-" + UUID.randomUUID()
        val blobName = "blob-" + UUID.randomUUID()

        mockMvc.put("/containers/${containerName}/${blobName}") {
            content = "Hello Azure"
            contentType = MediaType.TEXT_PLAIN
        }.andExpect {
            status { isOk }
        }

        mockMvc.get("/containers/${containerName}/${blobName}").andExpect {
            status { isOk }
            content {
                string("Hello Azure")
            }
        }

    }
}