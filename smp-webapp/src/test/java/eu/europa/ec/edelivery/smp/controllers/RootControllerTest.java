package eu.europa.ec.edelivery.smp.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RootControllerTest {

    RootController testInstance = new RootController();

    @Test
    void testRedirectOldIndexPath() {
        ModelMap mockModel = Mockito.mock(ModelMap.class);
        ModelAndView result = testInstance.redirectOldIndexPath(mockModel);

        assertNotNull(result);
        assertEquals("redirect:/index.html", result.getViewName());
    }

    @ParameterizedTest
    @CsvSource({
            ", text/html",
            "/index.html, text/html",
            "/favicon.png, image/png",
            "/favicon.ico, image/x-ico"
    })
    void testGetStaticResources(String pathInfo, String contentType) throws IOException {
        //given
        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);
        Mockito.when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);
        //when
        byte[] result = testInstance.getStaticResources(mockHttpServletRequest, mockHttpServletResponse);
        //then
        assertNotNull(result);
        Mockito.verify(mockHttpServletResponse).setContentType(contentType);

    }

    @Test
    void testRedirectWithUsingRedirectPrefix() {
        ModelMap mockModel = Mockito.mock(ModelMap.class);
        ModelAndView result = testInstance.redirectWithUsingRedirectPrefix(mockModel);

        assertNotNull(result);
        assertEquals("redirect:/ui/index.html", result.getViewName());
    }

}
