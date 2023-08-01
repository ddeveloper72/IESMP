package eu.europa.ec.edelivery.smp.services.resource;

import eu.europa.ec.edelivery.smp.data.dao.AbstractJunit5BaseDao;
import eu.europa.ec.edelivery.smp.data.dao.ConfigurationDao;
import eu.europa.ec.edelivery.smp.servlet.ResourceRequest;
import eu.europa.ec.edelivery.smp.servlet.ResourceResponse;
import eu.europa.ec.smp.spi.def.OasisSMPServiceGroup10;
import eu.europa.ec.smp.spi.def.OasisSMPServiceMetadata10;
import eu.europa.ec.smp.spi.handler.OasisSMPServiceGroup10Handler;
import eu.europa.ec.smp.spi.handler.OasisSMPServiceMetadata10Handler;
import eu.europa.ec.smp.spi.validation.ServiceMetadata10Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

// add SPI examples to the context
@ContextConfiguration(classes = {OasisSMPServiceGroup10.class,
        OasisSMPServiceMetadata10.class,
        OasisSMPServiceGroup10Handler.class,
        OasisSMPServiceMetadata10Handler.class,
        ServiceMetadata10Validator.class})
class ResourceHandlerServiceTest extends AbstractJunit5BaseDao  {

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    ResourceHandlerService testInstance;

    protected ResourceRequest requestData = Mockito.mock(ResourceRequest.class);
    protected ResolvedData resolvedData = Mockito.mock(ResolvedData.class);
    protected ResourceResponse responseData = Mockito.mock(ResourceResponse.class);


    @BeforeEach
    public void prepareDatabase() throws IOException {


        testUtilsDao.clearData();
        testUtilsDao.createSubresources();
        testUtilsDao.createResourceMemberships();
        resetKeystore();
        configurationDao.reloadPropertiesFromDatabase();

        // for reading the resource Oasis SMP 1.0
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn("/").when(request).getContextPath();
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

    }

    @Test
    void createResource() {
    }

    @Test
    void testReadResource() {
        Mockito.doReturn(resolvedData).when(requestData).getResolvedData();
        Mockito.doReturn(testUtilsDao.getResourceDefSmp()).when(resolvedData).getResourceDef();
        Mockito.doReturn(testUtilsDao.getD1()).when(resolvedData).getDomain();
        Mockito.doReturn(testUtilsDao.getResourceD1G1RD1()).when(resolvedData).getResource();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Mockito.doReturn(baos).when(responseData).getOutputStream();

        testInstance.readResource(requestData, responseData);
        assertTrue(baos.size() > 0);
    }

    @Test
    void testReadSubresource() {

        Mockito.doReturn(resolvedData).when(requestData).getResolvedData();
        Mockito.doReturn(testUtilsDao.getResourceDefSmp()).when(resolvedData).getResourceDef();
        Mockito.doReturn(testUtilsDao.getSubresourceDefSmpMetadata()).when(resolvedData).getSubResourceDef();
        Mockito.doReturn(testUtilsDao.getD1()).when(resolvedData).getDomain();
        Mockito.doReturn(testUtilsDao.getResourceD1G1RD1()).when(resolvedData).getResource();
        Mockito.doReturn(testUtilsDao.getSubresourceD2G1RD1_S1()).when(resolvedData).getSubresource();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Mockito.doReturn(baos).when(responseData).getOutputStream();

        testInstance.readSubresource(requestData, responseData);
        assertTrue(baos.size() > 0);
    }

    @Test
    void testCreateResource() {
        Mockito.doReturn(resolvedData).when(requestData).getResolvedData();
        Mockito.doReturn(ResourceHandlerService.class.getResourceAsStream("/examples/oasis-smp-1.0/ServiceGroupOK.xml"))
                .when(requestData).getInputStream();

        Mockito.doReturn(testUtilsDao.getResourceDefSmp()).when(resolvedData).getResourceDef();
        Mockito.doReturn(testUtilsDao.getD1()).when(resolvedData).getDomain();
        Mockito.doReturn(testUtilsDao.getResourceD1G1RD1()).when(resolvedData).getResource();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Mockito.doReturn(baos).when(responseData).getOutputStream();

        testInstance.createResource(testUtilsDao.getUser1(), requestData, responseData);
    }

    @Test
    void testCreateSubResource() {
        Mockito.doReturn(resolvedData).when(requestData).getResolvedData();
        Mockito.doReturn(ResourceHandlerService.class.getResourceAsStream("/examples/oasis-smp-1.0/ServiceMetadataOK.xml"))
                .when(requestData).getInputStream();

        Mockito.doReturn(testUtilsDao.getResourceDefSmp()).when(resolvedData).getResourceDef();
        Mockito.doReturn(testUtilsDao.getSubresourceDefSmpMetadata()).when(resolvedData).getSubResourceDef();
        Mockito.doReturn(testUtilsDao.getD1()).when(resolvedData).getDomain();
        Mockito.doReturn(testUtilsDao.getResourceD1G1RD1()).when(resolvedData).getResource();
        Mockito.doReturn(testUtilsDao.getSubresourceD2G1RD1_S1()).when(resolvedData).getSubresource();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Mockito.doReturn(baos).when(responseData).getOutputStream();

        testInstance.createSubresource(requestData, responseData);
    }

}
