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
package eu.europa.ec.edelivery.smp.services.ui;

import eu.europa.ec.edelivery.smp.config.SMPEnvironmentProperties;
import eu.europa.ec.edelivery.smp.config.enums.SMPEnvPropertyEnum;
import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.cron.SMPDynamicCronTrigger;
import eu.europa.ec.edelivery.smp.data.dao.ConfigurationDao;
import eu.europa.ec.edelivery.smp.data.model.DBConfiguration;
import eu.europa.ec.edelivery.smp.data.ui.PropertyRO;
import eu.europa.ec.edelivery.smp.data.ui.PropertyValidationRO;
import eu.europa.ec.edelivery.smp.data.ui.ServiceResultProperties;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.utils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum.SMP_CLUSTER_ENABLED;
import static eu.europa.ec.edelivery.smp.cron.CronTriggerConfig.TRIGGER_BEAN_PROPERTY_REFRESH;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT;

/**
 * UI property services
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
@Service
public class UIPropertyService {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(UIPropertyService.class);
    ConfigurationDao configurationDao;
    final SMPDynamicCronTrigger refreshPropertiesTrigger;

    public UIPropertyService(ConfigurationDao configurationDao,
                             @Qualifier(TRIGGER_BEAN_PROPERTY_REFRESH) SMPDynamicCronTrigger refreshPropertiesTrigger) {
        this.configurationDao = configurationDao;
        this.refreshPropertiesTrigger = refreshPropertiesTrigger;
    }

    /**
     * Method returns Domain resource object list for page.
     *
     * @param page
     * @param pageSize
     * @param sortField
     * @param sortOrder
     * @param filterByProperty
     * @return
     */
    public ServiceResultProperties getTableList(int page, int pageSize,
                                                String sortField,
                                                String sortOrder, String filterByProperty) {

        LOG.debug("Get properties for page [{}], pageSize [{}] and filter [{}]", page, pageSize, filterByProperty);
        List<SMPPropertyEnum> filteredProperties = Arrays.asList(SMPPropertyEnum.values()).stream()
                .filter(prop -> StringUtils.isBlank(filterByProperty)
                        || StringUtils.containsIgnoreCase(prop.getProperty(), filterByProperty))
                .collect(Collectors.toList());
        LOG.debug("Got filtered properties count [{}]", filteredProperties.size());

        Map<String, DBConfiguration> changedProps = configurationDao.getPendingUpdateProperties().stream()
                .collect(Collectors.toMap(DBConfiguration::getProperty, Function.identity()));

        List<PropertyRO> properties = filteredProperties.stream()
                .skip(page < 0 ? 0 : page * (long) pageSize)
                .limit(pageSize < 0 ? SMPPropertyEnum.values().length : pageSize)
                .map(prop -> createProperty(prop, changedProps))
                .collect(Collectors.toList());

        ServiceResultProperties result = new ServiceResultProperties();
        result.getServiceEntities().addAll(properties);
        result.setCount((long) filteredProperties.size());
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setFilter(filterByProperty);
        result.setServerRestartNeeded(configurationDao.isServerRestartNeeded());
        return result;
    }

    public PropertyRO createProperty(SMPPropertyEnum propertyType, Map<String, DBConfiguration> changedProps) {

        PropertyRO property = new PropertyRO(propertyType.getProperty(),
                configurationDao.getCachedProperty(propertyType),
                propertyType.getPropertyType().name(),
                propertyType.getDesc());

        property.setEncrypted(propertyType.isEncrypted());
        property.setRestartNeeded(propertyType.isRestartNeeded());
        property.setMandatory(propertyType.isMandatory());
        property.setValuePattern(property.getValuePattern());

        if (changedProps.containsKey(property.getProperty())) {
            String newVal = changedProps.get(propertyType.getProperty()).getValue();
            if (!StringUtils.equals(newVal, property.getValue())) {
                property.setNewValue(changedProps.get(propertyType.getProperty()).getValue());
                property.setUpdateDate(refreshPropertiesTrigger.getNextExecutionDate());
            } else {
                LOG.debug("Property [{}] has newer update time, but it has the same value as the current value!", propertyType);
            }
        }
        return property;
    }

    @Transactional
    public void updatePropertyList(List<PropertyRO> properties) {
        for (PropertyRO property : properties) {
            configurationDao.setPropertyToDatabase(property.getProperty(), property.getValue());
        }
        Boolean isClusterEnabled = configurationDao.getCachedPropertyValue(SMP_CLUSTER_ENABLED);
        if (isClusterEnabled) {
            LOG.info("Properties were updated in database. Changed properties will be activated to all cluster nodes at: [{}]!",
                    ISO_8601_EXTENDED_DATETIME_FORMAT.format(refreshPropertiesTrigger.getNextExecutionDate()));
            return;
        }
        configurationDao.reloadPropertiesFromDatabase();
    }

    public PropertyValidationRO validateProperty(PropertyRO propertyRO) {
        LOG.info("Validate property: [{}]", propertyRO.getProperty());
        PropertyValidationRO propertyValidationRO = new PropertyValidationRO();
        propertyValidationRO.setProperty(propertyRO.getProperty());
        propertyValidationRO.setValue(propertyRO.getValue());

        Optional<SMPPropertyEnum> optPropertyEnum = SMPPropertyEnum.getByProperty(propertyRO.getProperty());
        if (!optPropertyEnum.isPresent()) {
            LOG.debug("Property: [{}] is not SMP property!", propertyRO.getProperty());
            propertyValidationRO.setErrorMessage("Property [" + propertyRO.getProperty() + "] is not SMP property!");
            propertyValidationRO.setPropertyValid(false);
            return propertyValidationRO;
        }
        SMPPropertyEnum propertyEnum = optPropertyEnum.get();
        if (StringUtils.isBlank(propertyRO.getValue()) && propertyEnum.isMandatory()) {
            propertyValidationRO.setErrorMessage("Property [" + propertyRO.getProperty() + "] must not be NULL OR empty!");
            propertyValidationRO.setPropertyValid(false);
            return propertyValidationRO;
        }

        // try to parse value
        try {
            File confDir = Paths.get(SMPEnvironmentProperties.getInstance().getEnvPropertyValue(SMPEnvPropertyEnum.SECURITY_FOLDER)).toFile();
            PropertyUtils.parseProperty(propertyEnum, propertyRO.getValue(), confDir);
        } catch (SMPRuntimeException ex) {
            propertyValidationRO.setErrorMessage(ex.getMessage());
            propertyValidationRO.setPropertyValid(false);
            return propertyValidationRO;
        }

        propertyValidationRO.setPropertyValid(true);
        return propertyValidationRO;
    }
}
