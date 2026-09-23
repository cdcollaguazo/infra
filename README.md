# Infra

Shared AWS infrastructure for the `cdcollaguazo` platform.

Built with **Java** and **AWS CDK**, this project provides the common infrastructure used by the applications in the platform.

The deployment is configurable so the same infrastructure can be reused with a different domain, AWS account, or platform configuration.

---

## 1. Responsibilities

This project:

- Provides the shared AWS foundation for the platform.
- Exposes common entry points for static and dynamic traffic.
- Keeps shared infrastructure separate from application-specific resources.
- Publishes shared resource references for application repositories.

Each application manages its own services, Target Groups, and routing rules.

---

## 2. Architecture

CloudFront is the public entry point.

```text
Internet
   |
CloudFront
   |--------------------> S3
   |                      Static frontends
   |
   +--------------------> Internal ALB
                              |
                        Shared Listener
                              |
                  Application-owned rules
```

Static frontends share the same S3 bucket. Dynamic traffic is routed through the shared ALB, while each application owns its own ALB rule and Target Group.

---

## 3. Project Structure

```text
infra/
├── .github/                # CI/CD workflows
└── src/                    # Infrastructure as Code
```

The Java source contains the CDK application, stacks, constructs, and configuration.

---

## 4. Usage

### Requirements

- Java 21
- Maven
- AWS CLI
- AWS credentials for AWS operations

### Environment variables

Set values for the following:
```text
PLATFORM_NAME: example
PLATFORM_HOST: example.com
HOSTED_ZONE_ID: Z1234567890ABC
CERTIFICATE_ARN: arn:aws:acm:us-east-1:1234567890:certificate/xyz-123
ROOT_DB_USER: dummy
```

### Build CloudFormation templates

Package the application:

```
./mvnw clean package
```

Generate the CloudFormation templates:

```
java -jar target/infra-1.0-SNAPSHOT.jar
```

### Deploy to AWS

Configure your credentials:

```
aws configure
```

Deploy Infra stack using CloudFormation:

```
aws cloudformation deploy \
    --stack-name Infra \
    --template-file cdk.out/Infra.template.json
```
