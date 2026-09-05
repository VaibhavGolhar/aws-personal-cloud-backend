# Pay-as-you-Go Personal Cloud

A personal cloud storage platform offering a Google Drive-like interface with an AWS-style pay-as-you-go billing model. Users only pay for the storage and requests they actually consume.

## Prerequisites

To run this project, you will need:
1. **Java 17** (if running locally without Docker)
2. **Docker and Docker Compose** (recommended for easiest setup)
3. **AWS Account** (for S3 storage)
4. **PostgreSQL** (if running locally without Docker)

---

## Step 1: AWS S3 & IAM Setup

This application relies on AWS S3 to store user files. You must create an S3 bucket and an IAM user with programmatic access.

### 1. Create an S3 Bucket
1. Log in to your AWS Management Console and navigate to **S3**.
2. Click **Create bucket**.
3. **Bucket name**: Choose a globally unique name (e.g., `my-personal-cloud-bucket-123`). *You will need this name later.*
4. **Region**: Choose a region close to you (e.g., `us-east-1` or `ap-south-1`). *Note this region name.*
5. **Object Ownership**: Leave as "ACLs disabled".
6. **Block Public Access**: Keep **"Block all public access" ENABLED**. The backend securely streams files to users, so the bucket itself should remain completely private.
7. Click **Create bucket**.

### 2. Create an IAM User
1. Navigate to **IAM** (Identity and Access Management) in the AWS Console.
2. Click **Users** -> **Add users**.
3. **User name**: `personal-cloud-app-user` (or anything you prefer).
4. Click **Next**.
5. Under Permissions, select **Attach policies directly**.
6. Search for and check **`AmazonS3FullAccess`**. (For production, you should create a stricter custom policy that only allows actions on your specific bucket).
7. Click **Next** and **Create user**.

### 3. Generate AWS Credentials
1. Click on your newly created IAM user (`personal-cloud-app-user`).
2. Go to the **Security credentials** tab.
3. Scroll down to **Access keys** and click **Create access key**.
4. Choose **Application running outside AWS** and click Next.
5. Copy your **Access key ID** and **Secret access key**. Keep these safe, you will need them to run the app.

---

## Step 2: Environment Configuration

Create a file named `.env` in the root of the project directory. Docker Compose will automatically read variables from this file.

Populate it with your database credentials, AWS credentials, and JWT secret:

```env
# ================================
# Database Configuration
# ================================
DB_USER=pgc_user
DB_PASSWORD=pgc_secure_password
DB_NAME=pgc_db

# ================================
# AWS Configuration
# ================================
# Use the credentials generated in Step 1
AWS_ACCESS_KEY_ID=your_aws_access_key_here
AWS_SECRET_ACCESS_KEY=your_aws_secret_key_here
# e.g., us-east-1, ap-south-1
AWS_REGION=your_aws_region_here
# The exact name of your S3 bucket
S3_BUCKET_NAME=your_s3_bucket_name_here

# ================================
# Security Configuration
# ================================
# A random secret key for signing JWTs (Must be at least 32 characters long)
JWT_SECRET=super_secret_jwt_signature_key_change_me_in_production
# Token expiration time in milliseconds (86400000 = 24 hours)
JWT_EXPIRATION_MS=86400000
```

---

## Step 3: Running the Application

### Option A: Using Docker Compose (Recommended)

This is the easiest way to run the application as it automatically provisions a PostgreSQL database and wires it to the Spring Boot backend.

1. Ensure your Docker daemon is running.
2. Ensure your `.env` file is created and populated in the root directory.
3. Open a terminal in the root directory and run:
   ```bash
   docker-compose up -d --build
   ```
4. To view the logs and verify it started successfully:
   ```bash
   docker-compose logs -f backend
   ```

### Option B: Running Locally with Maven (Without Docker)

If you prefer to run the application directly on your host machine:

1. Ensure you have a PostgreSQL server running locally.
2. Create a database named `personal_cloud`.
3. Export the environment variables in your terminal before running. In Linux/macOS:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_db_password
   export DB_NAME=personal_cloud
   export AWS_ACCESS_KEY_ID=your_aws_access_key
   export AWS_SECRET_ACCESS_KEY=your_aws_secret_key
   export AWS_REGION=your_aws_region
   export S3_BUCKET_NAME=your_bucket_name
   export JWT_SECRET=your_jwt_secret_key
   ```
4. Build and run the application using the Maven wrapper:
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/Personal-Cloud-0.0.1-SNAPSHOT.jar
   ```

---

## Step 4: Exploring the API (Swagger)

Once the application is running (either via Docker or Maven), you can explore and test the API endpoints using the interactive Swagger UI.

1. Open your browser and navigate to:
   **http://localhost:8080/swagger-ui.html**
2. **Authentication flow in Swagger**:
   - Use the `/api/auth/register` endpoint to create a new user.
   - Use the `/api/auth/login` endpoint to log in. You will receive a JWT token in the response.
   - Click the green **Authorize** button at the top right of the Swagger UI.
   - Enter your token in the format: `Bearer <your_jwt_token>` and click Authorize.
   - You can now test the secured endpoints (like `/api/files` and `/api/billing/current`).

---

## Architecture Highlights
The application is structured around a domain-based package layout:
- `auth`: Authentication and registration logic (controllers, services, security extensions).
- `billing`: Calculates cost based on accumulated usage using exact S3 pricing formulas.
- `storage`: S3 integration and file metadata management.
- `usage`: Tracks per-user storage bytes and API requests.
- `user`: User entity and repository layer.
- `security`: JWT filters, entry points, and Spring Security configurations.
- `common`: Global exception handlers and core configurations.
