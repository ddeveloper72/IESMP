package eu.europa.ec.edelivery.smp.services;

import eu.europa.ec.edelivery.smp.data.dao.ConfigurationDao;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum.SSO_CAS_SMP_USER_DATA_URL_PATH;
import static eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum.SSO_CAS_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ConfigurationServiceTest {

    ConfigurationDao configurationDaoMock = mock(ConfigurationDao.class);
    ConfigurationService testInstance = new ConfigurationService(configurationDaoMock);

    @Test
    public void testGetCasUserDataURL() throws MalformedURLException {
        String casUrl = "http://test:123/path";
        String casUserDataPath = "userdata/data.hsp";
        doReturn(new URL(casUrl)).when(configurationDaoMock).getCachedPropertyValue(SSO_CAS_URL);
        doReturn(casUserDataPath).when(configurationDaoMock).getCachedPropertyValue(SSO_CAS_SMP_USER_DATA_URL_PATH);

        URL result = testInstance.getCasUserDataURL();
        assertNotNull(result);
        // expected - the same server but different context path
        assertEquals("http://test:123/" + casUserDataPath, result.toString());
    }
}
