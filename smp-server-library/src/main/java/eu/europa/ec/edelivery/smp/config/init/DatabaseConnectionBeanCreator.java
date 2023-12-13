/*-
 * #START_LICENSE#
 * smp-server-library
 * %%
 * Copyright (C) 2017 - 2023 European Commission | eDelivery | DomiSMP
 * %%
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * [PROJECT_HOME]\license\eupl-1.2\license.txt or https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 * #END_LICENSE#
 */
package eu.europa.ec.edelivery.smp.config.init;

import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

import static eu.europa.ec.edelivery.smp.exceptions.ErrorCode.CONFIGURATION_ERROR;
import static eu.europa.ec.edelivery.smp.exceptions.ErrorCode.INTERNAL_ERROR;

/**
 * Class database connection using the environment variables. It does not relay to any spring beans/services to prevent
 * circular dependencies because beans and services are relaying to application properties from the database.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class DatabaseConnectionBeanCreator {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(DatabaseConnectionBeanCreator.class);

    final DatabaseConnectionProperties databaseConnectionConfig;

    public DatabaseConnectionBeanCreator(DatabaseConnectionProperties environmentProperties) {
        this.databaseConnectionConfig = environmentProperties;
    }

    public DataSource getDataSource() {
        String jndiDatasourceName = databaseConnectionConfig.getDatabaseJNDI();
        if (StringUtils.isNotBlank(jndiDatasourceName)) {
            LOG.info("User datasource with JNDI: [{}] ", jndiDatasourceName);
            JndiObjectFactoryBean jndiDataSource = new JndiObjectFactoryBean();
            jndiDataSource.setJndiName(jndiDatasourceName);
            try {
                jndiDataSource.afterPropertiesSet();
            } catch (IllegalArgumentException | NamingException e) {
                // rethrow
                throw new SMPRuntimeException(INTERNAL_ERROR, e, "Invalid JNDI datasource: " + jndiDatasourceName, e.getMessage());
            }
            return (DataSource) jndiDataSource.getObject();
        }
        String jdbcURL = databaseConnectionConfig.getJdbcUrl();
        if (StringUtils.isBlank(jdbcURL)) {
            throw new SMPRuntimeException(CONFIGURATION_ERROR, "Invalid datasource configuration. Both jndi or jdbc url are empty");
        }

        LOG.info("Create datasource with URL: [{}].", jdbcURL);
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(databaseConnectionConfig.getJdbcDriver());
        driverManagerDataSource.setUrl(jdbcURL);
        driverManagerDataSource.setUsername(databaseConnectionConfig.getJdbcUsername());
        driverManagerDataSource.setPassword(databaseConnectionConfig.getJdbcPassword());

        return driverManagerDataSource;
    }

    public LocalContainerEntityManagerFactoryBean smpEntityManagerFactory(DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
        Properties prop = new Properties();
        // set envers to store deleted data
        prop.setProperty("org.hibernate.envers.store_data_at_delete", "true");

        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setPersistenceUnitName("smpEntityManagerFactory");
        lef.setDataSource(dataSource);
        lef.setJpaVendorAdapter(jpaVendorAdapter);
        lef.setPackagesToScan(
                "eu.europa.ec.edelivery.smp.data.model",
                "eu.europa.ec.edelivery.smp.data.model.user",
                "eu.europa.ec.edelivery.smp.data.model.doc",
                "eu.europa.ec.edelivery.smp.data.model.ext"
        );
        lef.setJpaProperties(prop);
        return lef;
    }


    public PlatformTransactionManager getSmpTransactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }


    public JpaVendorAdapter getJpaVendorAdapter() {
        String hibernateDialect = databaseConnectionConfig.getDatabaseDialect();
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        if (!StringUtils.isBlank(hibernateDialect)) {
            hibernateJpaVendorAdapter.setDatabasePlatform(hibernateDialect);
        }
        hibernateJpaVendorAdapter.setGenerateDdl(databaseConnectionConfig.updateDatabaseEnabled());
        hibernateJpaVendorAdapter.setShowSql(databaseConnectionConfig.isShowSqlEnabled());
        return hibernateJpaVendorAdapter;
    }
}
