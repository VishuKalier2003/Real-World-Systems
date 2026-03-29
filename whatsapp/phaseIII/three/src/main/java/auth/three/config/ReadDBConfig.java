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
    basePackages="auth.three.repo.read",
    entityManagerFactoryRef="readEntityManager",
    transactionManagerRef="readTransactionManager"
)
public class ReadDBConfig {
    
    @Bean(name = "readEntityManager")
    public LocalContainerEntityManagerFactoryBean readEntityManager(
        @Qualifier("readDataSource") DataSource dataSource
    ) {
        LocalContainerEntityManagerFactoryBean rm = new LocalContainerEntityManagerFactoryBean();
        rm.setDataSource(dataSource);
        rm.setPackagesToScan("auth.three.model");
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        rm.setJpaVendorAdapter(adapter);
        HashMap<String, Object> props = new HashMap<>();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.show_sql", true);
        // Setting properties for Hibernate session
        rm.setJpaPropertyMap(props);
        return rm;
    }

    @Bean(name = "readTransactionManager")
    public PlatformTransactionManager readTransactionManager(@Qualifier("readEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
