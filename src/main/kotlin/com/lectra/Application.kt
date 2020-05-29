package com.lectra

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobContainerClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayInputStream

@SpringBootApplication
class Application

fun main() {
    runApplication<Application>()
}

@RestController
class Controller(private val azureBlobStorage: AzureBlobStorage) {

    @GetMapping("ping", produces = ["text/plain"])
    fun ping() = "pong"

    @PutMapping("containers/{containerName}/{blobName}", consumes = ["text/plain"])
    fun postContent(@PathVariable containerName: String, @PathVariable blobName: String, @RequestBody content: String): ResponseEntity<String> {
        azureBlobStorage.createOrGetContainer(containerName).uploadText(blobName, content)
        return ResponseEntity.ok("")
    }

    @GetMapping("containers/{containerName}/{blobName}", produces = ["text/plain"])
    fun getContent(@PathVariable containerName: String, @PathVariable blobName: String) =
            azureBlobStorage.createOrGetContainer(containerName).getText(blobName)
}

@Component
class AzureBlobStorage(
        @Value("\${azure.storage.default-endpoints-protocol}")
        private val defaultEndpointsProtocol: String,
        @Value("\${azure.storage.account-name}")
        private val accountName: String,
        @Value("\${azure.storage.account-key}")
        private val accountKey: String,
        @Value("\${azure.storage.blob-endpoint}")
        private val blobEndpoint: String) {

    private val connectionString = "DefaultEndpointsProtocol=$defaultEndpointsProtocol;AccountName=$accountName;AccountKey=$accountKey;BlobEndpoint=$blobEndpoint;"

    fun createOrGetContainer(containerName: String): BlobContainerClient {
        val containerClient = BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient()
        if (!containerClient.exists()) {
            containerClient.create()
        }
        return containerClient
    }
}

fun BlobContainerClient.uploadText(blobName: String, text: String) {
    val blobClient = getBlobClient(blobName)
    val content = text.toByteArray()
    blobClient.upload(ByteArrayInputStream(content), content.size.toLong())
}

fun BlobContainerClient.getText(blobName: String) =
        getBlobClient(blobName).openInputStream().use { String(it.readAllBytes()) }