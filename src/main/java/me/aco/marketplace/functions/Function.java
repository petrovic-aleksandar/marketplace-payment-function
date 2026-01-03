package me.aco.marketplace.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import me.aco.marketplace.payment.AddPaymentCommand;
import me.aco.marketplace.payment.PaymentProcessor;
import me.aco.marketplace.payment.PaymentResult;

import java.math.BigDecimal;
import java.util.Optional;

import com.google.gson.Gson;

/**
 * Azure Functions for Payment Processing.
 * Implements the payment logic from marketplace-spring-ca application.
 */
public class Function {
    
    private final PaymentProcessor paymentProcessor = new PaymentProcessor();
    private final Gson gson = new Gson();
    
    /**
     * Process a payment request.
     * POST /api/Payment with JSON body: {"userId": 123, "amount": 50.00}
     */
    @FunctionName("Payment")
    public HttpResponseMessage processPayment(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Processing payment request");

        try {
            // Parse request body
            String requestBody = request.getBody().orElse(null);
            if (requestBody == null || requestBody.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Request body is required\"}")
                    .build();
            }
            
            // Parse payment command from JSON
            PaymentRequest paymentRequest = gson.fromJson(requestBody, PaymentRequest.class);
            AddPaymentCommand command = new AddPaymentCommand(
                paymentRequest.userId,
                paymentRequest.amount
            );
            
            // Process the payment
            PaymentResult result = paymentProcessor.processPayment(command);
            
            // Return response
            if (result.status().equals("SUCCESS")) {
                context.getLogger().info("Payment processed successfully for user: " + result.userId());
                return request.createResponseBuilder(HttpStatus.OK)
                    .body(gson.toJson(result))
                    .build();
            } else {
                context.getLogger().warning("Payment failed: " + result.status());
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(result))
                    .build();
            }
            
        } catch (Exception e) {
            context.getLogger().severe("Error processing payment: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }
    
    /**
     * Internal class for parsing payment requests
     */
    private static class PaymentRequest {
        Long userId;
        BigDecimal amount;
    }
}
