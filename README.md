# RELOOP: Smart Recycling & Rewards Platform
## College Java Project – Evaluation 1 Working Prototype

RELOOP is a smart recycling platform where citizens bring everyday recyclables (plastic bottles, aluminium cans, paper, cardboard, glass) to community collection kiosks, collection operators inspect and record verified quantities, and a Core Java backend engine calculates and credits reward points to customer digital wallets.

---

## 1. Quick Start (Windows PowerShell)

### Prerequisites:
1. **JDK 17 or higher** installed (`javac -version`).
2. **MySQL 8.0** installed and service running (`MySQL80`).
3. **MySQL Connector/J JAR** located in `lib/` (already pre-packaged in `lib/mysql-connector-j-8.3.0.jar`).

### Step 1: Initialize Database & Seed Data
Open PowerShell and import the provided schema and seed data into MySQL:
```powershell
mysql -u root -p < database/schema.sql
```
*(Enter your MySQL root password when prompted)*

### Step 2: Configure Database Credentials
Edit `config/db.properties` and enter your MySQL password:
```properties
db.url=jdbc:mysql://localhost:3306/reloop
db.user=root
db.password=YOUR_MYSQL_PASSWORD_HERE
```

### Step 3: Compile the Complete Project
Run this single command from the project root folder:
```powershell
javac -d out -cp "lib/*" (Get-ChildItem -Recurse src -Filter *.java).FullName
```

### Step 4: Run the Application
Start the application (verifies database and starts built-in Java HTTP Server on port 8080):
```powershell
java -cp "out;lib/*" com.reloop.Main
```

### Step 5: Open in Web Browser
Navigate to:
- **Public Landing Page**: `http://localhost:8080/`
- **Customer Portal**: `http://localhost:8080/customer.html`
- **Operator Verification Desk**: `http://localhost:8080/operator.html`
- **Admin Console**: `http://localhost:8080/admin.html`

### Default Pre-Configured Demo Accounts:
The system automatically seeds these test accounts on startup if not present:
- **Customer**: `customer@reloop.com` / `customer123`
- **Operator**: `operator@reloop.com` / `operator123` (Assigned to Station 1: GreenMart Central)
- **Admin**: `admin@reloop.com` / `admin123`

---

## 2. Testing Component Verification Suite
To verify the core Java logic (PBKDF2 hashing, points calculation, weight validation, JSON parsing, OOP polymorphism) independently:
```powershell
java -cp "out;lib/*" com.reloop.test.TestEvaluation1
```

---

## 3. Technology Stack & Strict Restrictions Adherence

| Component | Technology | Rationale |
| :--- | :--- | :--- |
| **Backend** | **Core Java SE** (JDK 17/21/26) | 100% Core Java. Zero Spring Boot, zero Hibernate, zero Python, zero Node.js. |
| **HTTP Layer** | `com.sun.net.httpserver.HttpServer` | Built directly into standard Java SE runtime. Zero third-party web frameworks. |
| **Database** | **MySQL 8.0** with **JDBC** | Relational normalized schema with foreign keys and ACID transactions. |
| **Security** | `PBKDF2WithHmacSHA256` | 16-byte random salt, stored as `salt:hash` to prevent rainbow table attacks. |
| **JSON Utility** | Pure Standard Java (`JsonUtil.java`) | Zero-dependency JSON serializer and parser using StringBuilder and standard collections. |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript | Modern luxury design system: Forest Green (`#0A2619`), Ivory (`#FBFBF9`), and Champagne Gold (`#C8A951`). |

---

## 4. End-to-End Working Workflow in Evaluation 1

```
[1. Customer Portal]
  └── Registers or logs in (customer@reloop.com)
  └── Selects "GreenMart Central Station" and chooses 10 Plastic Bottles & 2 kg Paper
  └── Clicks "Submit Drop-Off Request" (Status created as PENDING in MySQL)

[2. Operator Desk]
  └── Operator logs in (operator@reloop.com)
  └── Sees customer drop-off appear in live "Pending Submissions Queue"
  └── Clicks "Inspect & Verify"
  └── Enters measured weights/quantities (live points calculator previews reward)
  └── Clicks "Verify & Credit Wallet"

[3. Java Backend Engine]
  └── Validates positive weights via WeightValidator
  └── Computes points via PointsCalculator: (10 * 2 pts) + (2 kg * 10 pts) = 40 pts
  └── Executes JDBC ACID Transaction (setAutoCommit(false)):
        • Updates submission status to VERIFIED
        • Inserts verified line items
        • Credits customer wallet (+40 pts)
        • Logs audit record in points_transactions
        • Commits transaction (commit())

[4. Verification & Audit]
  └── Customer dashboard immediately reflects updated balance (40 points)
  └── Admin console reflects updated platform metrics and station volume
```

---

## 5. Complete Java Class Inventory (Evaluation 1)

All 49 classes are actively implemented with full fields, methods, constructors, and real application logic:

### Model Package (`com.reloop.model`)
1. **`User`** (`abstract class`): Root entity for all platform actors. Defines common fields (`id`, `name`, `email`, `passwordHash`, `role`, `phone`, `stationId`) and abstract method `getDashboardType()`.
2. **`Customer`** (`class` extends `User`): Represents recycling consumers; adds `totalSubmissions`, `currentPointsBalance`, and overrides `getDashboardType()`.
3. **`Operator`** (`class` extends `User`): Represents kiosk staff; encapsulates station assignment (`stationId`, `stationName`), shift check, and overrides `getDashboardType()`.
4. **`Admin`** (`class` extends `User`): Platform administrator; overrides `getDashboardType()`.
5. **`Material`** (`class`): Recyclable material entity (`id`, `name`, `unit`, `pointsPerUnit`, `active`, `calculatePoints()`).
6. **`RecyclingStation`** (`class`): Physical kiosk entity (`id`, `name`, `address`, `area`, `openTime`, `closeTime`, `isOpenNow()`).
7. **`StationMaterial`** (`class`): Models junction association between stations and accepted materials.
8. **`Submission`** (`class`): Recycling drop-off header (`id`, `customerId`, `stationId`, `operatorId`, `status`, `items`, `calculateTotalPoints()`).
9. **`SubmissionItem`** (`class`): Line item in a submission (`id`, `submissionId`, `materialId`, `quantity`, `pointsEarned`).
10. **`Wallet`** (`class`): Customer balance container (`id`, `customerId`, `balance`, `credit()`, `debit()`).
11. **`PointsTransaction`** (`class`): Audit ledger entry (`id`, `walletId`, `submissionId`, `points`, `type`, `createdAt`).
12. **`StationSummary`** (`class`): DTO for aggregated station statistics (`stationName`, `totalSubmissions`, `pointsDistributed`).
13. **`UserSession`** (`class`): In-memory authenticated session with token and expiry tracking.

### Exception Package (`com.reloop.exception`)
14. **`ReloopException`** (`class` extends `Exception`): Base custom checked exception.
15. **`DatabaseException`** (`class` extends `ReloopException`): Wraps low-level `SQLException` / database connectivity errors.
16. **`ValidationException`** (`class` extends `ReloopException`): Thrown on invalid user input or negative weights.
17. **`AuthenticationException`** (`class` extends `ReloopException`): Thrown on bad credentials or unauthorized access.
18. **`ResourceNotFoundException`** (`class` extends `ReloopException`): Thrown when requested IDs do not exist in the database.

### DAO Package (`com.reloop.dao`)
19. **`DBConnection`** (`class`): Central static connection factory reading `config/db.properties` via `FileInputStream` and `Properties`.
20. **`UserDAO`** (`class`): PreparedStatement CRUD for users, password hash retrieval, and polymorphic mapping.
21. **`MaterialDAO`** (`class`): Fetches active materials and station-specific accepted materials.
22. **`StationDAO`** (`class`): Queries station records and computes aggregated station summaries.
23. **`SubmissionDAO`** (`class`): Creates submissions, queries pending drop-offs, and updates status with transaction support.
24. **`SubmissionItemDAO`** (`class`): Inserts and retrieves line items with material join details.
25. **`WalletDAO`** (`class`): Atomically updates customer wallet balance inside a JDBC transaction.
26. **`TransactionDAO`** (`class`): Logs points transactions for audit compliance.
27. **`AuditDAO`** (`class`): Computes platform-wide KPIs (total recyclers, total submissions, total points distributed).

### Service Package (`com.reloop.service`)
28. **`PointsCalculator`** (`class`): Pure business logic for calculating points from quantities and material rates.
29. **`WeightValidator`** (`class`): Validates that submitted quantities/weights are strictly positive and within bounds.
30. **`SessionManager`** (`class`): Thread-safe session registry using `ConcurrentHashMap` and UUID tokens.
31. **`AuthService`** (`class`): Coordinates user registration, PBKDF2 hash verification, and initial wallet setup.
32. **`RecyclingService`** (`class`): Coordinates submission creation and customer history retrieval.
33. **`SubmissionVerifier`** (`class`): Executes the operator verification workflow with ACID transaction commit/rollback.
34. **`WalletService`** (`class`): Encapsulates customer balance checks and transaction history.
35. **`AdminService`** (`class`): Aggregates system metrics, station performance, and user rosters for admin view.

### Utility Package (`com.reloop.util`)
36. **`PasswordUtil`** (`class`): Implements `PBKDF2WithHmacSHA256` hashing and constant-time password verification.
37. **`InputValidator`** (`class`): Regex email validator, password length checker, and string sanitization.
38. **`JsonUtil`** (`class`): Custom zero-dependency JSON parser and serializer in standard Java.
39. **`ResponseBuilder`** (`class`): Helper constructing standardized `{ "success": true, "data": ... }` JSON responses.

### Handler Package (`com.reloop.handler`)
40. **`BaseHttpHandler`** (`abstract class`): Abstract HTTP handler providing CORS, JSON streaming, and Bearer token parsing.
41. **`StaticFileHandler`** (`class`): Serves static web assets (`index.html`, `customer.html`, CSS, JS) with MIME headers.
42. **`AuthHandler`** (`class`): Dispatches `/api/auth/register`, `/api/auth/login`, `/api/auth/me`, `/api/auth/logout`.
43. **`StationHandler`** (`class`): Dispatches `/api/stations` and `/api/stations?stationId=X`.
44. **`MaterialHandler`** (`class`): Dispatches `/api/materials`.
45. **`SubmissionHandler`** (`class`): Dispatches `/api/submissions/create`, `/pending`, `/verify`, `/reject`, `/customer`.
46. **`WalletHandler`** (`class`): Dispatches `/api/wallet/balance` and `/api/wallet/transactions`.
47. **`AdminHandler`** (`class`): Dispatches `/api/admin/overview`.

### Server & Main Package (`com.reloop.server`, `com.reloop`, `com.reloop.test`)
48. **`HttpServerApp`** (`class`): Bootstraps `com.sun.net.httpserver.HttpServer` on port 8080 with a thread pool executor.
49. **`Main`** (`class`): System entry point; checks MySQL connectivity, seeds demo users, and starts the server.
50. **`TestEvaluation1`** (`class`): Component verification suite testing all core algorithms independently.
#   R e l o o p  
 