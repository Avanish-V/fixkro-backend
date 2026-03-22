package com.iotabuild.zippyhand_Admin

import com.amazonaws.serverless.exceptions.ContainerInitializationException
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class StreamLambdaHandler : RequestStreamHandler {
    companion object {
        private var handlerV2: SpringBootLambdaContainerHandler<com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest, com.amazonaws.serverless.proxy.model.AwsProxyResponse>? = null

        init {
            try {
                // Initialize handler with async initialization to speed up cold starts
                handlerV2 = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(ZippyhandAdminApplication::class.java)
                // This informs the container that we are ready even if Spring is still finishing beans
                // drastically reducing the chance of a 502/Gateway Timeout
            } catch (e: Throwable) {
                println("FATAL: Failed to initialize Spring Boot application")
                e.printStackTrace()
                throw RuntimeException("Could not initialize Spring Boot application: ${e.message}", e)
            }
        }
    }

    @Throws(IOException::class)
    override fun handleRequest(inputStream: InputStream, outputStream: OutputStream, context: Context) {
        handlerV2?.proxyStream(inputStream, outputStream, context) 
            ?: throw RuntimeException("StreamLambdaHandler not initialized")
    }
}
