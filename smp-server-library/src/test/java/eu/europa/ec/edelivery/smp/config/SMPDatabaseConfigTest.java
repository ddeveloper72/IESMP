/*-
 * #%L
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
 * #L%
 */
package eu.europa.ec.edelivery.smp.config;

import eu.europa.ec.edelivery.smp.config.init.DatabaseConnectionBeanCreator;
import eu.europa.ec.edelivery.smp.config.init.DatabaseConnectionProperties;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SMPDatabaseConfigTest {

    public static final String DATABASE_DRIVER = "org.h2.Driver";
    public static final String DATABASE_DIALECT = "org.hibernate.dialect.H2Dialect";
    public static final String DATABASE_URL = "jdbc:h2:file:./target/myDb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_SERVER=TRUE";
    public static final String DATABASE_USERNAME = "smp-dev";
    public static final String DATABASE_PASS = "smp-dev";

    DatabaseConnectionProperties environmentProperties = Mockito.mock(DatabaseConnectionProperties.class);

    SMPDatabaseConfig testInstance = new SMPDatabaseConfig();
    @Before
    public void init(){
        ReflectionTestUtils.setField(testInstance, "databaseConnectionBeanCreator", new DatabaseConnectionBeanCreator(environmentProperties));
    }

    @Test
    public void getDataSourceMissingConfiguration() {
        SMPRuntimeException result = assertThrows(SMPRuntimeException.class, () -> testInstance.getDataSource());

        assertEquals("Configuration error: [Invalid datasource configuration. Both jndi or jdbc url are empty]!", result.getMessage());
    }

    @Test
    public void getJNDIForDataSourceMissing() {
        Mockito.doReturn("jdbc/eDeliverySmpDs").when(environmentProperties).getDatabaseJNDI();

        SMPRuntimeException result = assertThrows(SMPRuntimeException.class, () -> testInstance.getDataSource());

        MatcherAssert.assertThat(result.getMessage(), CoreMatchers.containsString("Invalid JNDI datasource: jdbc/eDeliverySmpDs"));
    }

    @Test
    public void getDataSource() {
        setJdbcProperties();

        DataSource result = testInstance.getDataSource();

        Assert.assertNotNull(result);
        Assert.assertEquals(DriverManagerDataSource.class, result.getClass());
    }

    @Test
    public void jpaVendorAdapter() {
        setHibernateDatabaseDialect();
        JpaVendorAdapter result = testInstance.jpaVendorAdapter();

        Assert.assertNotNull(result);
    }

    @Test
    public void smpEntityManagerFactory() {
        setJdbcProperties();
        setHibernateDatabaseDialect();

        LocalContainerEntityManagerFactoryBean result = testInstance.smpEntityManagerFactory(testInstance.getDataSource(), testInstance.jpaVendorAdapter());
        Assert.assertNotNull(result);
    }

    @Test
    public void smpTransactionManager() {
        setJdbcProperties();
        setHibernateDatabaseDialect();

        EntityManagerFactory entityManagerFactory = testInstance.smpEntityManagerFactory(testInstance.getDataSource(), testInstance.jpaVendorAdapter()).getObject();
        PlatformTransactionManager result = testInstance.smpTransactionManager(entityManagerFactory);
        Assert.assertNotNull(result);
    }


    private void setJdbcProperties() {
        Mockito.doReturn(DATABASE_DRIVER).when(environmentProperties).getJdbcDriver();
        Mockito.doReturn(DATABASE_URL).when(environmentProperties).getJdbcUrl();
        Mockito.doReturn(DATABASE_USERNAME).when(environmentProperties).getJdbcUsername();
        Mockito.doReturn(DATABASE_PASS).when(environmentProperties).getJdbcPassword();
    }

    private void setHibernateDatabaseDialect() {
        Mockito.doReturn(DATABASE_DIALECT).when(environmentProperties).getDatabaseDialect();
    }
}
