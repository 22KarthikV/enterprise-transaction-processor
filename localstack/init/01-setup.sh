#!/bin/bash
# Provisions AWS resources in LocalStack on container start.
# Mounted to /etc/localstack/init/ready.d/01-setup.sh - LocalStack runs every
# script in that directory once the gateway is ready. Safe to re-run.
set -euo pipefail

REGION="${AWS_DEFAULT_REGION:-us-east-1}"

echo "Provisioning LocalStack resources..."

# --- SQS: DLQ first, then main queue with redrive policy pointing at it ---
DLQ_URL=$(awslocal sqs create-queue --queue-name transaction-dlq --region "$REGION" --query QueueUrl --output text)
DLQ_ARN=$(awslocal sqs get-queue-attributes --queue-url "$DLQ_URL" --attribute-names QueueArn --region "$REGION" --query 'Attributes.QueueArn' --output text)

awslocal sqs create-queue \
  --queue-name transaction-queue \
  --region "$REGION" \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$DLQ_ARN\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"}"

# --- SNS ---
awslocal sns create-topic --name transaction-events --region "$REGION"

# --- DynamoDB ---
awslocal dynamodb create-table \
  --table-name transaction-audit \
  --attribute-definitions AttributeName=transactionId,AttributeType=S AttributeName=timestamp,AttributeType=S \
  --key-schema AttributeName=transactionId,KeyType=HASH AttributeName=timestamp,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region "$REGION" \
  || echo "transaction-audit table already exists, skipping"

# --- S3 ---
awslocal s3 mb "s3://transaction-archive" --region "$REGION" \
  || echo "transaction-archive bucket already exists, skipping"

# --- Secrets Manager ---
create_secret() {
  local name="$1"
  local value="$2"
  awslocal secretsmanager create-secret --name "$name" --secret-string "$value" --region "$REGION" >/dev/null 2>&1 \
    || awslocal secretsmanager put-secret-value --secret-id "$name" --secret-string "$value" --region "$REGION" >/dev/null
}

create_secret "/transaction-processor/db-password" "changeme"
create_secret "/transaction-processor/jwt-secret" "local-dev-jwt-secret-change-in-prod"
create_secret "/transaction-processor/redis-url" "redis://redis:6379"
create_secret "/transaction-processor/exchange-rates" '{"USD":0.79,"EUR":0.86,"INR":0.0095,"GBP":1.0}'

echo "LocalStack resource provisioning complete."
