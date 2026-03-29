# MySQL Replication Error — Debugging & Resolution (Phase III Setup)

## 1. What was the error?

While setting up MySQL primary–replica replication, the replica failed to establish a connection with the primary database. The replication status showed:

* `Slave_IO_Running: Connecting` (instead of **Yes**)
* `Slave_SQL_Running: Yes`
* `Seconds_Behind_Master: NULL`

The most critical error observed:

```
Last_IO_Error: Authentication plugin 'caching_sha2_password' reported error: Authentication requires secure connection.
```

This indicates that the replica was unable to authenticate with the primary database due to a mismatch in authentication requirements.

---

## 2. Why did this error occur?

The issue stems from the default authentication mechanism in MySQL 8:

* MySQL 8 uses **`caching_sha2_password`** as the default authentication plugin.
* This plugin **requires a secure (SSL/TLS) connection** for authentication.

However, in the current setup:

* The replication connection between primary and replica was **not configured with SSL**.
* As a result, MySQL rejected the authentication attempt from the replica.

In essence:

```
Replica → tries insecure connection
Primary → expects secure authentication (SSL)
→ Connection rejected
```

---

## 3. How was the error identified?

The issue was diagnosed using MySQL replication status logs:

### Step 1 — Check replication status

```
SHOW SLAVE STATUS\G;
```

### Step 2 — Key indicators observed

* `Slave_IO_Running: Connecting` → Replica cannot connect to primary
* `Seconds_Behind_Master: NULL` → Replication not active
* `Last_IO_Errno: 2061` → Authentication-related failure

### Step 3 — Critical log message

```
Last_IO_Error:
Authentication plugin 'caching_sha2_password' reported error:
Authentication requires secure connection.
```

This message directly points to:

* Authentication plugin mismatch
* Missing SSL configuration

---

## 4. How was the issue resolved?

The fix involved switching the authentication plugin for the replication user to one that does not require SSL.

### Step-by-step resolution

#### Step 1 — Recreate replication user on primary

```sql
DROP USER IF EXISTS 'replica'@'%';

CREATE USER 'replica'@'%' 
IDENTIFIED WITH mysql_native_password 
BY 'replica123';

GRANT REPLICATION SLAVE ON *.* TO 'replica'@'%';

FLUSH PRIVILEGES;
```

#### Step 2 — Reconfigure replication on replica

```sql
STOP SLAVE;

RESET SLAVE ALL;

CHANGE MASTER TO
  MASTER_HOST='mysql-primary',
  MASTER_USER='replica',
  MASTER_PASSWORD='replica123',
  MASTER_LOG_FILE='mysql-bin.000003',
  MASTER_LOG_POS=863;

START SLAVE;
```

#### Step 3 — Verify replication

```sql
SHOW SLAVE STATUS\G;
```

Expected result:

* `Slave_IO_Running: Yes`
* `Slave_SQL_Running: Yes`
* `Seconds_Behind_Master: 0` (or small number)

---

## 5. When else can this error occur?

This specific error can appear in multiple scenarios involving MySQL 8 authentication and replication:

### a) Replication without SSL (most common)

When using `caching_sha2_password` without configuring SSL between primary and replica.

---

### b) Connecting external clients (e.g., apps, tools)

If an application or tool connects to MySQL using:

* insecure connection
* while MySQL expects secure authentication

---

### c) Docker / local development environments

Common in setups where:

* SSL is not configured
* default MySQL 8 authentication is used

---

### d) Cross-network replication

When primary and replica are on different machines and:

* SSL is enforced
* but certificates are not configured

---

### e) Misconfigured JDBC clients

Some older drivers or configurations do not support `caching_sha2_password`, leading to similar authentication failures.

---

## Final Takeaway

This issue highlights a critical system design insight:

* **Application-level scaling is tightly coupled with infrastructure configuration.**
* Authentication mechanisms, network security, and database settings must align.
* Default configurations (like MySQL 8 auth plugins) can introduce hidden constraints.

For local development:

* Use `mysql_native_password` for simplicity.

For production:

* Prefer **SSL-enabled replication with secure authentication**.
