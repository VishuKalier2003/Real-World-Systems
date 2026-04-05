# MySQL Access Denied Error — Debugging & Resolution (Spring Boot + Replication Setup)

## 1. What was the error?

During application startup, the Spring Boot service failed to initialize the JPA `EntityManagerFactory` due to a database authentication failure. The error observed was:

```
Access denied for user 'app_user'@'192.168.65.1' (using password: YES)
```

This prevented Hibernate from establishing a JDBC connection, causing the application context initialization to fail and the server to shut down.

---

## 2. Why did this error occur?

The issue occurred due to **MySQL user host-based authentication rules**.

In MySQL, authentication is not just based on username and password, but also on the **host from which the connection originates**.

In this setup:

* Spring Boot application was running on the host machine.
* MySQL databases (primary and replica) were running inside Docker containers.
* Therefore, connections were coming from a host like:

  ```
  192.168.65.1
  ```

However, the database user was likely defined as:

```
'app_user'@'localhost'
```

This caused a mismatch:

```
app_user@localhost ≠ app_user@192.168.65.1
```

As a result, MySQL rejected the connection attempt.

---

## 3. How was the error identified?

The issue was identified through application logs during startup.

### Key steps:

1. Application failed during context initialization.
2. Logs were inspected for root cause.
3. The following critical message was found:

```
Access denied for user 'app_user'@'192.168.65.1'
```

This indicated:

* Credentials were correct (password provided)
* But access was denied due to **host mismatch**

Additional clues:

* Error occurred during Hibernate initialization
* Failure in `readEntityManager` bean creation
* JDBC connection could not be established

---

## 4. How was the issue resolved?

The fix involved updating MySQL user permissions to allow connections from any host.

### Steps taken:

#### Step 1 — Connect to MySQL (Primary and Replica)

```
docker exec -it mysql-primary mysql -uroot -proot
docker exec -it mysql-replica mysql -uroot -proot
```

#### Step 2 — Recreate user with correct host access

```sql
DROP USER IF EXISTS 'app_user'@'%';

CREATE USER 'app_user'@'%' IDENTIFIED BY 'app_pass';

GRANT ALL PRIVILEGES ON whatsapp_write.* TO 'app_user'@'%';

FLUSH PRIVILEGES;
```

#### Step 3 — Restart application

After updating user permissions, the Spring Boot application successfully connected to both primary and replica databases.

---

## 5. When else can this error occur?

This type of error can occur in several scenarios:

### a) Docker-based setups

When applications run outside containers but databases run inside containers, leading to different host identities.

---

### b) Microservices architecture

When services communicate across machines or containers and database users are restricted to specific hosts.

---

### c) Cloud deployments (AWS, GCP, etc.)

When application servers connect to managed databases from different IP addresses.

---

### d) Load-balanced environments

When requests originate from multiple IPs but database users are restricted to limited hosts.

---

### e) Incorrect MySQL user configuration

When users are created with:

```
'user'@'localhost'
```

instead of:

```
'user'@'%'
```

---

## Final Takeaway

This issue highlights an important system design principle:

* **Database authentication is host-aware, not just credential-based.**
* Moving from local setups to containerized or distributed systems introduces **network-level identity changes**.
* Proper database user configuration is essential for connectivity in distributed environments.

For local development:

* Use `'user'@'%'` for flexibility.

For production:

* Restrict hosts explicitly for better security while ensuring correct connectivity paths.
