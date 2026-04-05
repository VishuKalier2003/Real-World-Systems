# 📘 Debugging Report — Spring Boot Multi-DataSource (Phase III)

---

## 1. What Error Occurred?

While initializing the Spring Boot application with multiple datasources (read + write), the application failed during startup with the following critical error:

```
jdbcUrl is required with driverClassName.
```

This error occurred during the creation of the `readEntityManager` bean and prevented Hibernate from building the SessionFactory.

Additionally, supporting logs indicated:

```
Database JDBC URL [undefined/unknown]
```

and earlier:

```
Unable to determine Dialect without JDBC metadata
```

This clearly indicates that the application was unable to establish a proper JDBC connection configuration.

---

## 2. Why Did This Error Occur?

The root cause lies in **manual configuration overriding Spring Boot’s auto-configuration behavior**.

The application explicitly disabled:

* `DataSourceAutoConfiguration`
* `HibernateJpaAutoConfiguration`

This means Spring Boot no longer performs:

* automatic datasource property binding
* automatic conversion of `url → jdbcUrl`
* automatic dialect detection

Now, the application uses:

```java
DataSourceBuilder.create().build();
```

By default, this creates a **HikariCP DataSource**, which requires:

```
jdbcUrl (mandatory)
```

However, the configuration provided:

```
spring.datasource.read.url=...
spring.datasource.write.url=...
```

HikariCP does **not recognize `url`**, only `jdbcUrl`.

Because of this mismatch:

* JDBC URL was never set
* Hibernate could not connect to the database
* Dialect detection failed
* EntityManager initialization failed

---

## 3. How Was the Error Identified?

The issue was diagnosed using specific log signals:

### Key Error Signal

```
jdbcUrl is required with driverClassName.
```

This directly indicates that HikariCP did not receive a valid JDBC URL.

---

### Supporting Clues

```
Database JDBC URL [undefined/unknown]
```

This confirms that the datasource was created but **without a valid connection URL**.

---

### Earlier Related Error

```
Unable to determine Dialect without JDBC metadata
```

This occurs when:

* Hibernate cannot connect to the database
* OR JDBC metadata is unavailable

---

### Diagnosis Strategy

1. Observe **top-level exception**
2. Trace **root cause (Caused by)**
3. Identify missing configuration (jdbcUrl)
4. Correlate with manual datasource setup

This systematic log tracing led to the exact misconfiguration.

---

## 4. How Was the Error Resolved?

The issue was resolved by aligning configuration with HikariCP requirements.

---

### Step 1 — Replace `url` with `jdbc-url`

#### ❌ Incorrect

```
spring.datasource.read.url=...
spring.datasource.write.url=...
```

#### ✅ Correct

```
spring.datasource.read.jdbc-url=jdbc:mysql://localhost:3306/whatsapp_read
spring.datasource.write.jdbc-url=jdbc:mysql://localhost:3306/whatsapp_write
```

---

### Step 2 — Remove driver-class-name

#### ❌ Removed

```
spring.datasource.read.driver-class-name=...
spring.datasource.write.driver-class-name=...
```

Reason:

* HikariCP auto-detects driver
* Explicit driver config conflicts with missing jdbcUrl

---

### Step 3 — Ensure Hibernate Dialect (already done)

```
hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

### Step 4 — Fix Entity Class

Error observed:

```
No default constructor for class Users
```

Fix:

```java
@NoArgsConstructor
@Entity
public class Users {
```

---

### Step 5 — Validate Package Scanning

Ensure both EntityManagers scan the same entity package:

```java
em.setPackagesToScan("auth.three.entity");
```

---

### Final Result

After applying these fixes:

* Datasource initialized correctly
* Hikari connection pool started successfully
* Hibernate SessionFactory built without errors
* Application booted normally

---

## 5. When Else Can This Error Occur?

This error is common in **advanced Spring Boot configurations**, especially when auto-configuration is disabled.

---

### Scenario 1 — Multiple DataSources (Manual Setup)

When using:

* multiple databases
* custom `DataSourceBuilder`
* manual EntityManager configuration

---

### Scenario 2 — HikariCP Explicit Configuration

If:

* `driverClassName` is set
* but `jdbcUrl` is missing

Hikari will throw this exact error.

---

### Scenario 3 — Custom Configuration Properties

If incorrect keys are used:

```
url instead of jdbc-url
```

---

### Scenario 4 — Migration to Newer Spring Boot / Hibernate Versions

Newer versions:

* enforce stricter validation
* do not tolerate incomplete datasource configs

---

### Scenario 5 — Disabling Auto-Configuration

Whenever:

```java
exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
}
```

You must manually configure:

* datasource properties
* dialect
* connection settings

---

### Scenario 6 — YAML vs Properties Misalignment

Incorrect mapping like:

```
spring.datasource.url vs spring.datasource.jdbc-url
```

---

## Final Takeaway

This issue highlights a critical transition:

> From **Spring Boot convenience (auto-config)**
> To **explicit infrastructure control (manual config)**

Key principle:

```
Auto-config OFF → You must configure EVERYTHING explicitly
```

Understanding this boundary is essential for building **production-grade distributed systems**.

---
