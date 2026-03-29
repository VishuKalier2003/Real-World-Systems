package auth.three.config;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(
        basePackages = "auth.three.repo.write",     // Only the files under the folder are mapped to write DB
        entityManagerFactoryRef = "writeEntityManager",     // defines the entityManager that works around persistent context and talks to DB via ORM
        transactionManagerRef = "writeTransactionManager"   // defines transactionManager to define transaction boundaries
)
public class WriteDBConfig {

    @Bean(name = "writeEntityManager")
    public LocalContainerEntityManagerFactoryBean writeEntityManager(
            @Qualifier("writeDataSource") DataSource dataSource) {
        // Creates EntityManager instances
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);       // binds write database datasource
        em.setPackagesToScan("auth.three.model");      // scans the package for Entity classes
        // Using Hibernate as the JPA provider (JPA only defines the contract as interface and does not implement hence needs an adapter)
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> props = new HashMap<>();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", true);
        // Setting properties for Hibernate session
        em.setJpaPropertyMap(props);
        return em;
    }

    @Bean(name = "writeTransactionManager")
    // PlatformTransactionManager is an interface for transactionManager utilities that can be used by other concrete classes
    public PlatformTransactionManager writeTransactionManager(
            @Qualifier("writeEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}