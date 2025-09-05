# Fintra Stock Trading Platform - Aspendos

<div align="center">
  <h3>Modern Finance and Stock Exchange Management Platform</h3>
  <p>A comprehensive financial trading platform with JWT authentication, real-time trading, and portfolio management</p>
</div>

This project is a **Spring Boot + Next.js based full-stack application** designed to handle **stock trading and financial management** operations. The platform allows users to trade stocks, manage portfolios, and administrators to oversee the entire trading ecosystem. Key features include:

- **User Management**: Registration, authentication, and role-based access control (Admin, Trader, Analyst).
- **Stock Trading**: Real-time stock trading with order matching and settlement system.
- **Portfolio Management**: Comprehensive portfolio tracking with P&L calculations.
- **Customer Management**: Customer onboarding, account management, and trading permissions.
- **Operational Transactions**: Stock transfers, cash transactions, and end-of-day processing.
- **Security**: JWT-based authentication and authorization with Turkish character support.
- **Real-time Updates**: Kafka-based event streaming for trade notifications.

## Technology Stack

### Backend
- **Framework**: Java 21, Spring Boot 3
- **Database**: Microsoft SQL Server 2022 Express
- **Security**: Spring Security with JWT authentication
- **Message Queue**: Apache Kafka with Kafka UI
- **Caching**: Redis
- **Documentation**: Swagger/OpenAPI
- **Build Tool**: Maven
- **Email**: MailDev for development
- **Logging**: SLF4J

### Frontend
- **Framework**: Next.js 15 with React 19
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: Radix UI primitives
- **Charts**: Recharts for financial data visualization
- **Internationalization**: i18next (Turkish support)
- **Build Tool**: Turbopack for fast development

### Infrastructure
- **Containerization**: Docker, Docker Compose
- **Development Tools**: Hot reload, live debugging
- **Production**: Multi-stage Docker builds for optimization

## How to Use

### Clone the Repository

To start working with this project, you'll need to clone the repository to your local machine:

```bash
git clone https://gitea.infina.com.tr/akademi-25/Aspendos.git
cd Aspendos
```

### Configure Environment Variables

Create a `.env` file in the project root with the following configuration:

```env
# Database Configuration
DB_HOST=localhost
DB_PORT=1435
DB_NAME=fintra_stock_trading
DB_USERNAME=sa
DB_PASSWORD=StrongPassword123!

# JWT Configuration
JWT_SECRET=T4f@7z!9qP8mD6sB1eK0uH5rJ2xZ8cVw
JWT_EXPIRATION=86400000

# Mail Configuration
MAIL_HOST=localhost
MAIL_PORT=1025

# Server Configuration
SERVER_PORT=8080

# Email Configuration
APP_EMAIL_FROM=noreply@fintra.com.tr
APP_EMAIL_PASSWORD_RESET_SUBJECT="Fintra Stock Trading - Password Reset"
APP_EMAIL_PASSWORD_RESET_TEMPLATE=password-reset

# Password Reset Configuration
APP_PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES=15

# OpenAPI / Swagger Metadata
APP_VERSION=1.0.0
APP_DESCRIPTION="REST API for Fintra Stock Trading Platform - A comprehensive financial trading platform with JWT authentication, password reset functionality, and email notifications"
APP_CONTACT_NAME="Fintra Development Team"
APP_CONTACT_EMAIL=dev@fintra.com.tr
APP_CONTACT_URL=https://fintra.com.tr
APP_LICENSE_NAME="MIT License"
APP_LICENSE_URL=https://opensource.org/licenses/MIT

# Infina API Configuration
INFINA_API_INFO_URL=https://apitest.infina.com.tr/infina-services/rest/srv/v1.1/HisseTanim
INFINA_API_PRICE_URL=https://apitest.infina.com.tr/infina-services/rest/srv/v1.1/HisseFiyat
INFINA_API_INFO_KEY=XvusCI7S4hyr008JUcPyiAdMSzSHKmWa99VYFXeU
```

## Running the Application

### Development Mode (Docker Compose)

The easiest way to run the entire application stack is using Docker Compose:

#### Step 1: Start All Services

```bash
docker-compose up --build
```

This command will start all required services:
- **SQL Server**: Database server on port 1435
- **Redis**: Caching server on port 6379
- **Kafka**: Message queue on port 29092
- **Kafka UI**: Management interface on port 8090
- **MailDev**: Email testing on port 1080
- **Backend**: Spring Boot API on port 8080
- **Frontend**: Next.js application on port 3000

#### Step 2: Create Database

After SQL Server container is running, create the database:

**For Linux/Mac:**
```bash
docker exec -it fintra-mssql \
  /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U SA -P 'StrongPassword123!' \
  -C \
  -Q "CREATE DATABASE fintra_stock_trading;"
```

**For Windows CMD:**
```cmd
docker exec -it fintra-mssql ^
  /opt/mssql-tools18/bin/sqlcmd ^
  -S localhost -U SA -P "StrongPassword123!" ^
  -C ^
  -Q "CREATE DATABASE fintra_stock_trading;"
```

**For Windows PowerShell:**
```powershell
docker exec -it fintra-mssql `
  /opt/mssql-tools18/bin/sqlcmd `
  -S localhost -U SA -P 'StrongPassword123!' `
  -C `
  -Q "CREATE DATABASE fintra_stock_trading;"
```

#### Step 3: Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8090
- **MailDev**: http://localhost:1080

### Development Mode (Local)

For local development without Docker:

#### Backend Setup

```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

### Production Mode

For production deployment, the Docker Compose setup automatically builds optimized images:

```bash
# Build and start in production mode
docker-compose up --build -d

# Stop the application
docker-compose down
```

## Core Features

### User Roles and Permissions

- **ADMIN**: Full system access, user management, customer management
- **TRADER**: Customer portfolio management, trading operations
- **ANALYST**: Read-only access to reports and analytics

### Trading System

- **Order Management**: Buy/Sell orders with price and quantity limits
- **Order Matching**: Automatic matching engine for trade execution
- **T+2 Settlement**: Two-day settlement cycle with automatic processing
- **Portfolio Tracking**: Real-time portfolio valuation and P&L calculations

### Customer Management

- **Account Types**: Individual and Corporate accounts
- **Trading Permissions**: Full trading or participation-only restrictions
- **Identity Validation**: Turkish ID number and Tax number validation
- **Multi-account Support**: Customers can have multiple trading accounts

### Financial Operations

- **Cash Transactions**: Deposits and withdrawals with balance tracking
- **Stock Transfers**: Inter-portfolio and external institution transfers
- **End-of-Day Processing**: Automated EOD operations and date advancement
- **Commission Tracking**: Transaction fees and cost calculations

## Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Granular permissions per user role
- **Password Security**: Strong password requirements with Turkish character support
- **Email Validation**: Turkish character support in email addresses
- **Data Encryption**: Secure storage of sensitive information

## API Documentation

The application includes comprehensive API documentation via Swagger/OpenAPI:

- **Development**: http://localhost:8080/swagger-ui.html
- **Interactive Testing**: Built-in API testing interface
- **Role-based Examples**: Different examples for each user role
- **Turkish Language Support**: Localized error messages and examples

## Database Schema

The application uses the following core entities:

- **User**: System users with roles and authentication
- **Customer**: Trading customers with accounts and permissions
- **Account**: Individual trading accounts with cash balances
- **Equity**: Stock definitions with pricing history
- **EquityOrder**: Buy/sell orders with status tracking
- **Trade**: Executed trades with settlement information
- **CashTransaction**: Cash deposits and withdrawals
- **EquityTransfer**: Stock transfer operations

## Additional Information

- **Automated Data Initialization**: System creates sample data on startup
- **Business Day Calculations**: Proper handling of weekends and holidays
- **Multi-language Support**: Turkish interface with proper character encoding
- **Responsive Design**: Optimized for desktop and mobile devices

## Development Tools

- **Hot Reload**: Automatic code reloading during development
- **Database Migrations**: Automatic schema updates via Hibernate
- **Email Testing**: MailDev for testing email notifications
- **Message Queue UI**: Kafka UI for monitoring event streams
- **API Testing**: Swagger UI for interactive API testing

## License

This project is released under the MIT License.