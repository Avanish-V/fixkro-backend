# Cashfree Payment Integration - Quick Start Guide

## Overview

This implementation provides complete Cashfree payment gateway integration for the ZippyHand service booking platform.

## Setup Instructions

### 1. Environment Variables

Set the following environment variables with your Cashfree credentials:

```bash
# For Windows PowerShell
$env:CASHFREE_APP_ID="your_cashfree_app_id"
$env:CASHFREE_SECRET_KEY="your_cashfree_secret_key"

# For Windows Command Prompt
set CASHFREE_APP_ID=your_cashfree_app_id
set CASHFREE_SECRET_KEY=your_cashfree_secret_key

# For Linux/Mac
export CASHFREE_APP_ID=your_cashfree_app_id
export CASHFREE_SECRET_KEY=your_cashfree_secret_key
```

### 2. Configuration

Update `application.yaml` if needed:

```yaml
cashfree:
  environment: TEST  # Change to PRODUCTION for live environment
  base-url: https://sandbox.cashfree.com/pg  # Use https://api.cashfree.com/pg for production
```

### 3. Build the Application

```bash
./gradlew build
```

### 4. Run the Application

```bash
./gradlew bootRun
```

## API Endpoints

### 1. Create Payment Order

**Endpoint:** `POST /api/v1/payments/create-order`

**Request:**
```json
{
  "orderId": 1,
  "customerName": "John Doe",
  "customerPhone": "9999999999",
  "customerEmail": "john@example.com",
  "amount": 1000,
  "returnUrl": "https://yourapp.com/payment-success",
  "orderNote": "AC Service Payment"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "success": true,
    "orderId": 1,
    "cfOrderId": "order_1_1234567890",
    "paymentSessionId": "session_abc123",
    "paymentLink": "https://sandbox.cashfree.com/pg/view/order/session_abc123",
    "orderAmount": 1000,
    "currency": "INR",
    "status": "ACTIVE"
  }
}
```

### 2. Verify Payment

**Endpoint:** `POST /api/v1/payments/verify`

**Request:**
```json
{
  "orderId": 1
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "success": true,
    "orderId": 1,
    "paymentStatus": "SUCCESS",
    "message": "Payment verified successfully",
    "paymentDetails": {
      "orderId": 1,
      "cfOrderId": "order_1_1234567890",
      "cfPaymentId": "payment_xyz789",
      "paymentStatus": "SUCCESS",
      "amount": 1000,
      "currency": "INR",
      "paymentMode": "UPI",
      "paymentMessage": "Payment successful",
      "bankReference": "REF123456",
      "paymentTime": "2026-01-30T19:00:00",
      "createdAt": "2026-01-30T18:50:00",
      "updatedAt": "2026-01-30T19:00:00"
    }
  }
}
```

### 3. Get Payment Status

**Endpoint:** `GET /api/v1/payments/status/{orderId}`

**Response:**
```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "cfOrderId": "order_1_1234567890",
    "cfPaymentId": "payment_xyz789",
    "paymentStatus": "SUCCESS",
    "amount": 1000,
    "currency": "INR",
    "paymentMode": "UPI",
    "paymentMessage": "Payment successful",
    "bankReference": "REF123456",
    "paymentTime": "2026-01-30T19:00:00",
    "createdAt": "2026-01-30T18:50:00",
    "updatedAt": "2026-01-30T19:00:00"
  }
}
```

### 4. Webhook Endpoint (Public)

**Endpoint:** `POST /api/v1/payments/webhook`

**Headers:**
- `x-webhook-signature`: Cashfree webhook signature

**Note:** This endpoint is automatically called by Cashfree when payment status changes.

### 5. Health Check

**Endpoint:** `GET /api/v1/payments/health`

**Response:**
```json
{
  "status": "UP",
  "service": "Payment Service",
  "timestamp": "2026-01-30T19:00:00"
}
```

## Payment Flow

1. **Create Order**: Customer places an order via your order creation API
2. **Initiate Payment**: Call `/api/v1/payments/create-order` with order details
3. **Redirect Customer**: Use the `paymentLink` from response to redirect customer to Cashfree
4. **Customer Pays**: Customer completes payment on Cashfree page
5. **Webhook Update**: Cashfree sends webhook to update payment status (automatic)
6. **Verify Payment**: Optionally call `/api/v1/payments/verify` to confirm payment
7. **Order Update**: Order payment status is automatically updated

## Payment Status Mapping

| Cashfree Status | Internal Status | Description |
|----------------|----------------|-------------|
| ACTIVE | ACTIVE | Payment order created, awaiting payment |
| PAID/SUCCESS | SUCCESS | Payment completed successfully |
| FAILED/USER_DROPPED | FAILED | Payment failed or user cancelled |
| EXPIRED | EXPIRED | Payment link expired |
| CANCELLED | CANCELLED | Payment cancelled |
| PARTIAL | PARTIAL | Partial payment received |

## Database Schema

A new `payments` table is automatically created with the following structure:

- `id`: Primary key
- `order_id`: Reference to order
- `cf_order_id`: Cashfree order ID
- `cf_payment_id`: Cashfree payment ID
- `customer_name`, `customer_phone`, `customer_email`: Customer details
- `amount`, `currency`: Payment amount
- `payment_status`: Current payment status
- `payment_mode`: Payment method (UPI, Card, etc.)
- `payment_message`: Status message
- `bank_reference`: Bank transaction reference
- `payment_time`: When payment was completed
- `return_url`: Return URL after payment
- `webhook_data`: Raw webhook data for debugging
- `created_at`, `updated_at`: Timestamps

## Security

- Webhook signature verification is implemented using HMAC SHA-256
- Webhook endpoint is public but validates signatures
- All other endpoints can be protected based on your security requirements

## Testing with Cashfree Sandbox

1. Use TEST environment in configuration
2. Get sandbox credentials from Cashfree dashboard
3. Use Cashfree test cards/UPI for testing:
   - Card: 4111 1111 1111 1111
   - CVV: Any 3 digits
   - Expiry: Any future date
   - OTP: 123456

## Webhook Configuration

Configure webhook URL in Cashfree dashboard:
```
https://your-domain.com/api/v1/payments/webhook
```

For local testing, use ngrok:
```bash
ngrok http 8080
# Use the ngrok URL in Cashfree dashboard
https://your-ngrok-url.ngrok.io/api/v1/payments/webhook
```

## Error Handling

All payment-related errors are handled with appropriate HTTP status codes:

- `400 Bad Request`: Invalid payment request
- `403 Forbidden`: Invalid webhook signature
- `404 Not Found`: Payment/Order not found
- `409 Conflict`: Payment already exists for order
- `500 Internal Server Error`: Cashfree API errors or internal errors

## Production Checklist

- [ ] Set `CASHFREE_APP_ID` and `CASHFREE_SECRET_KEY` environment variables
- [ ] Update `cashfree.environment` to `PRODUCTION` in application.yaml
- [ ] Update `cashfree.base-url` to production URL
- [ ] Configure webhook URL in Cashfree production dashboard
- [ ] Test payment flow with real credentials in staging
- [ ] Monitor webhook delivery and payment status updates
- [ ] Set up proper authentication for payment endpoints (if needed)
- [ ] Configure rate limiting for payment APIs
- [ ] Set up monitoring and alerts for payment failures

## Support

For Cashfree API documentation: https://docs.cashfree.com/docs
