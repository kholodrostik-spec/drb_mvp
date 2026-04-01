# DrbMVP

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue)
![AWS S3](https://img.shields.io/badge/AWS-S3-yellow)
![License](https://img.shields.io/badge/license-MIT-green)

## About the project

**DrbMVP** is a REST API backend for an Irish location discovery platform. Users can explore locations across Ireland, leave reviews with photos, and interact with map data. The project includes JWT authentication, AWS S3 photo uploads, API request logging, and email notifications.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Requirements](#requirements)
3. [How to run](#how-to-run)
4. [AWS S3 Setup](#aws-s3-setup)
5. [Database Setup](#database-setup)
6. [Email Setup](#email-setup)
7. [Swagger UI](#swagger-ui)
8. [API Overview](#api-overview)

---

## Tech Stack

- **Java 21**
- **Spring Boot 3.4.3** — REST API, Security, Mail
- **PostgreSQL** — primary database
- **Liquibase** — database migrations
- **AWS SDK v2** — S3 file storage
- **JWT** — stateless authentication
- **SpringDoc OpenAPI** — Swagger UI
- **Lombok** — boilerplate reduction

---

## Requirements

- Java 21+
- PostgreSQL 15+
- Maven 3.8+
- AWS account with S3 bucket
- Gmail account with App Password (for email notifications)

---

## How to run

**1. Clone the repository**
```bash
git clone https://github.com/kholodrostik-spec/drb_mvp.git
cd drb_mvp/DrbMVP
```

**2. Create PostgreSQL database**
```sql
CREATE DATABASE drbdb;
```

**3. Configure `application.properties`**

Copy the example and fill in your values:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Minimum required configuration:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/drbdb
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

jwt.secret=your-very-secret-key-minimum-32-characters-long
jwt.expiration=86400000
```

**4. Run the application**
```bash
mvn spring-boot:run
```

**5. Open Swagger UI**
```
http://localhost:8080/swagger-ui.html
```

---

## AWS S3 Setup

The project uses AWS S3 to store review photos. Follow these steps to configure it:

**1. Create an S3 bucket**
- Go to [AWS Console → S3](https://s3.console.aws.amazon.com/)
- Click **Create bucket**
- Choose a unique bucket name and select region (recommended: `eu-west-1` for Ireland)
- Keep default settings and create the bucket

**2. Create IAM user with S3 access**
- Go to [AWS Console → IAM → Users](https://console.aws.amazon.com/iam/home#/users)
- Click **Add users** → enter a name (e.g. `drb-s3-user`)
- Select **Attach policies directly** → search and attach `AmazonS3FullAccess`
- Go to the user → **Security credentials** → **Create access key**
- Choose **Application running outside AWS** → copy **Access Key ID** and **Secret Access Key**

**3. Add credentials to `application.properties`**
```properties
# ── AWS S3 ──────────────────────────────────────
aws.s3.bucket-name=        # your bucket name, e.g. drb-photos
aws.s3.region=             # AWS region, e.g. eu-west-1
aws.s3.access-key=         # Access Key ID from IAM
aws.s3.secret-key=         # Secret Access Key from IAM

spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

> ⚠️ **Never commit real AWS credentials to Git.** Add `application.properties` to `.gitignore` or use environment variables.

**Supported photo formats:** JPEG, PNG, WEBP, HEIC — max **10 MB**

Photos are stored under the path: `reviews/{locationId}/{uuid}.{ext}`

---

## Database Setup

Migrations are managed by **Liquibase** and run automatically on startup.

Key tables:
| Table | Description |
|-------|-------------|
| `users` | Registered users with roles |
| `locations` | Map locations with coordinates |
| `reviews` | User reviews with rating, comment, photo |
| `user_photos` | S3 photo metadata |
| `api_logs` | API request history |

To add the `photo_s3_key` column manually if needed:
```sql
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS photo_s3_key VARCHAR(1024);
```

---

## Email Setup

The project sends registration confirmation emails via Gmail SMTP.

**1. Enable 2-Factor Authentication** on your Gmail account

**2. Generate App Password**
- Go to [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
- Select **Mail** → **Windows Computer** → Generate
- Copy the 16-character password

**3. Add to `application.properties`**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Disable email during development** (optional):
```properties
app.email.enabled=false
```

> ℹ️ Email is sent asynchronously — it will not block the registration response even if SMTP is unavailable.

---

## Swagger UI

After starting the application, open:
```
http://localhost:8080/swagger-ui.html
```

**How to authenticate in Swagger:**

1. Use `POST /api/auth/register` or `POST /api/auth/login` to get a JWT token
2. Click the **Authorize** button (🔒) at the top right
3. Enter the token in the format: `Bearer <your_token>`
4. Click **Authorize** — now all protected endpoints are accessible

---

## API Overview

### Auth — `/api/auth`
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and get JWT token |

### Locations — `/api/locations`
| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `POST` | `/api/locations` | Add a new location | USER |
| `POST` | `/api/locations/reviews` | Add or update a review with optional photo | USER |
| `DELETE` | `/api/locations/reviews/photo` | Delete review photo from S3 | USER |
| `DELETE` | `/api/locations/reviews/comment` | Clear review comment | USER |
| `DELETE` | `/api/locations/reviews/rating` | Clear review rating | USER |

### Users — `/api/users`
| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `DELETE` | `/api/users/{id}` | Delete a user (own account or any as admin) | USER / ADMIN |

### API Logs — `/api/logs`
| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `DELETE` | `/api/logs/{id}` | Delete a specific log entry | ADMIN |
| `DELETE` | `/api/logs` | Delete all log entries | ADMIN |

---

## Security notes

- All `/api/locations/**`, `/api/map/**`, `/api/transport/**` endpoints require `ROLE_USER` or `ROLE_ADMIN`
- `/api/email/**` requires `ROLE_ADMIN`
- `/api/logs/**` DELETE requires `ROLE_ADMIN`
- A user can only delete their own account; admins can delete any user
- JWT tokens expire after 24 hours (`86400000` ms) by default

---

## License

MIT License — see [LICENSE](LICENSE) for details.
