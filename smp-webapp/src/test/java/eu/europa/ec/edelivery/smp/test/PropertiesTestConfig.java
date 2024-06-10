/*-
 * #START_LICENSE#
 * smp-webapp
 * %%
 * Copyright (C) 2017 - 2024 European Commission | eDelivery | DomiSMP
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

package eu.europa.ec.edelivery.smp.test;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.nio.file.Path;
import java.nio.file.Paths;

import static eu.europa.ec.edelivery.smp.config.enums.SMPEnvPropertyEnum.*;
import static eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum.*;

/**
 * Created by gutowpa on 11/01/2018.
 */
@Configuration
@PropertySources({
        @PropertySource(value = "classpath:config.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
})
@ComponentScan(basePackages = "eu.europa.ec.edelivery.smp")
public class PropertiesTestConfig {
    public static final String DATABASE_URL = "jdbc:h2:file:./target/DomiSmpWebDb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_SERVER=TRUE;Mode=MySQL";
    public static final String DATABASE_USERNAME = "smp";
    public static final String DATABASE_PASS = "smp";
    public static final String DATABASE_DRIVER = "org.h2.Driver";
    public static final String DATABASE_DIALECT = "org.hibernate.dialect.H2Dialect";

    public static final String BUILD_FOLDER = "target";
    public static final Path SECURITY_PATH = Paths.get(BUILD_FOLDER, "keystores");

    static {
        System.setProperty(JDBC_DRIVER.getProperty(), DATABASE_DRIVER);
        System.setProperty(HIBERNATE_DIALECT.getProperty(), DATABASE_DIALECT);
        System.setProperty(JDBC_URL.getProperty(), DATABASE_URL);
        System.setProperty(JDBC_USER.getProperty(), DATABASE_USERNAME);
        System.setProperty(JDBC_PASSWORD.getProperty(), DATABASE_PASS);
        // show generates sql statements
        //System.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        System.setProperty(DATABASE_SHOW_SQL.getProperty(), "true");
        //System.setProperty("spring.jpa.properties.hibernate.format_sql", "true");
        System.setProperty("logging.level.org.hibernate.type", "trace");

        System.setProperty(KEYSTORE_PASSWORD.getProperty(), "{DEC}{test123}");
        System.setProperty(TRUSTSTORE_PASSWORD.getProperty(), "{DEC}{test123}");
        System.setProperty(RESOURCE_SCH_MANDATORY.getProperty(), "false");

        System.setProperty(SMP_MODE_DEVELOPMENT.getProperty(), "true");
        System.setProperty(DATABASE_CREATE_DDL.getProperty(), "true");
        System.setProperty(SECURITY_FOLDER.getProperty(), SECURITY_PATH.toFile().getPath());
    }
}
