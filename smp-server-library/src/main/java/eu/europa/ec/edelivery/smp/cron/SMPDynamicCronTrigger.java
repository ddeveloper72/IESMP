/*-
 * #START_LICENSE#
 * smp-server-library
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
package eu.europa.ec.edelivery.smp.cron;

import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Calendar;
import java.util.Date;

/**
 * Cron trigger with option to reset cron expression to new value
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
public class SMPDynamicCronTrigger implements Trigger {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(SMPDynamicCronTrigger.class);
    final SMPPropertyEnum cronExpressionProperty;
    Date nextExecutionDate;
    CronTrigger cronTrigger;


    public SMPDynamicCronTrigger(String expression, SMPPropertyEnum cronExpressionProperty) {
        cronTrigger = new CronTrigger(expression);
        this.cronExpressionProperty = cronExpressionProperty;
    }

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        if (cronTrigger == null) {
            LOG.debug("Cron is disabled.");
            return null;
        }
        nextExecutionDate = cronTrigger.nextExecutionTime(triggerContext);
        return nextExecutionDate;
    }

    public String getExpression() {
        return cronTrigger.getExpression();
    }

    public void updateCronExpression(CronExpression expression) {
        if (expression == null) {
            LOG.debug("Disable cron trigger for property: [{}]. ", cronExpressionProperty.getProperty());
            cronTrigger = null;
            nextExecutionDate = null;
            return;
        }
        cronTrigger = new CronTrigger(expression.toString());
        LOG.debug("Set new cron expression: [{}] for property: [{}]. ", expression,
                cronExpressionProperty.getProperty());

        nextExecutionDate = Calendar.getInstance().getTime();
    }

    /**
     * Return next scheduled execution date
     *
     * @return next scheduled execution date;
     */
    public Date getNextExecutionDate() {
        return nextExecutionDate;
    }

    /**
     * Method returns the property which sets the cron expression
     *
     * @return property name
     */
    public SMPPropertyEnum getCronExpressionProperty() {
        return cronExpressionProperty;
    }
}
