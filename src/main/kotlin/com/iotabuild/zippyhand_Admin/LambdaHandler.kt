package com.iotabuild.zippyhand_Admin

import com.amazonaws.serverless.exceptions.ContainerInitializationException
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

class LambdaHandler : RequestHandler<AwsProxyRequest, AwsProxyResponse> {
    companion object {
        private var handler: SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse>? = null

        init {
            try {
                handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(ZippyhandAdminApplication::class.java)
            } catch (e: Throwable) {
                // We log the error and re-throw it to ensure Lambda knows it's a failed start
                println("FATAL: Failed to initialize Spring Boot application")
                e.printStackTrace()
                throw RuntimeException("Could not initialize Spring Boot application: ${e.message}", e)
            }
        }
    }

    override fun handleRequest(input: AwsProxyRequest, context: Context): AwsProxyResponse {
        return handler?.proxy(input, context) 
            ?: throw RuntimeException("Handler not initialized")
    }
}
