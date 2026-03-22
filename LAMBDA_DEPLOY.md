# AWS Lambda Deployment Guide

## Prerequisites

Ensure you have the following installed:
- Docker Desktop
- AWS CLI (configured with `aws configure`)

## Build the Application

1. Build the Shadow JAR:
   ```bash
   ```bash
   .\gradlew.bat clean shadowJar -x test
   ```

2. Build the Docker Image:
   ```bash
   docker build --platform linux/amd64 --provenance=false -t zippyhand-admin -f Dockerfile.lambda .
   ```

## Deploy to AWS Lambda

### 1. Push to ECR
(Assuming you have an ECR repository created)

```bash
aws ecr get-login-password --region <your-region> | docker login --username AWS --password-stdin <your-account-id>.dkr.ecr.<your-region>.amazonaws.com
docker tag zippyhand-admin:latest <your-account-id>.dkr.ecr.<your-region>.amazonaws.com/zippyhand-admin:latest
docker push <your-account-id>.dkr.ecr.<your-region>.amazonaws.com/zippyhand-admin:latest
```

### 2. Update/Create Lambda Function

Ensure your Lambda function is configured with the following **Environment Variables**:

| Variable Name | Description | Example Value |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL for the database | `jdbc:postgresql://host:5432/db?user=u&password=p&sslmode=require` |
| `CASHFREE_APP_ID` | Cashfree App ID | `your_app_id` |
| `CASHFREE_SECRET_KEY` | Cashfree Secret Key | `your_secret_key` |
| `CASHFREE_ENVIRONMENT` | Environment (TEST/PRODUCTION) | `PRODUCTION` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL Strategy | `validate` (recommended), `none`, or `update` |
| `SPRING_JPA_SHOW_SQL` | Show SQL logs | `false` |

### 3. Memory & Timeout Settings
- **Memory**: Recommended at least **1024 MB** (or 2048 MB for faster SnapStart initialization).
- **Timeout**: Set to at least **30 seconds** (Spring Boot startup can take time).

## Troubleshooting

- **Handler Not Found**: Ensure the `CMD` in Dockerfile matches `com.iotabuild.zippyhand_Admin.StreamLambdaHandler::handleRequest`.
- **Architecture Mismatch**: If you see `Runtime.BadFunctionCode`, check if the Lambda Architecture (x86_64 vs arm64) matches your Docker build platform (`linux/amd64`).
- **Database Connection**: Ensure the Lambda function has network access (VPC/NAT Gateway) to reach the database.
- **Memory**: Spring Boot 3 requires at least 1024MB, ideally 2048MB for faster startup.
