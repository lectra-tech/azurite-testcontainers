package com.lectra

import com.azure.storage.blob.BlobContainerClientBuilder
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.ByteArrayInputStream
import java.util.*

@Testcontainers
class AzuriteTest : WithAssertions {

    companion object {
        // will be shared between test methods
        @Container
        private val azuriteContainer = AzuriteContainer().withExposedPorts(10000)
    }

    // Azurite default configuration
    private val defaultEndpointsProtocol = "http"
    private val accountName = "devstoreaccount1"
    private val accountKey = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw=="
    private val blobEndpoint = "http://127.0.0.1:${azuriteContainer.getMappedPort(10000)}/devstoreaccount1"
    private val connectionString = "DefaultEndpointsProtocol=$defaultEndpointsProtocol;AccountName=$accountName;AccountKey=$accountKey;BlobEndpoint=$blobEndpoint;"

    @Test
    fun `check container is running`() {
        assertThat(azuriteContainer.isRunning).isTrue()
    }

    @Test
    fun `should upload and download blob`() {
        // create a container for blob
        val containerName = "container-" + UUID.randomUUID()
        val containerClient = BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient()
        containerClient.create()

        // create blob and upload text
        val blobName = "blob-" + UUID.randomUUID()
        val blobClient = containerClient.getBlobClient(blobName)
        val content = "hello world".toByteArray()
        blobClient.upload(ByteArrayInputStream(content), content.size.toLong())

        // read text of blob
        val text = blobClient.openInputStream().use { String(it.readAllBytes()) }
        assertThat(text).isEqualTo("hello world")
    }

}