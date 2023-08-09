package eu.europa.ec.edelivery.smp.error;

import eu.europa.ec.dynamicdiscovery.exception.MalformedIdentifierException;
import eu.europa.ec.edelivery.smp.error.xml.ErrorResponse;
import eu.europa.ec.edelivery.smp.exceptions.BadRequestException;
import eu.europa.ec.edelivery.smp.exceptions.ErrorBusinessCode;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.*;

public class ServiceErrorControllerAdviceTest {

    ServiceErrorControllerAdvice testIntance = new ServiceErrorControllerAdvice();

    @Test
    public void handleRuntimeException() {
        ResponseEntity re = testIntance.handleRuntimeException(new RuntimeException("RuntimeExceptionMessage"));

        assertEquals(INTERNAL_SERVER_ERROR, re.getStatusCode());
        assertEquals(ErrorBusinessCode.TECHNICAL.toString(), ((ErrorResponse)re.getBody()).getBusinessCode());


    }

    @Test
    public void handleBadRequestException() {

        ResponseEntity re = testIntance.handleBadRequestException(new BadRequestException(ErrorBusinessCode.WRONG_FIELD, "BadRequestExceptionMessage"));

        assertEquals(BAD_REQUEST, re.getStatusCode());
        assertEquals(ErrorBusinessCode.WRONG_FIELD.toString(), ((ErrorResponse)re.getBody()).getBusinessCode());
    }


    @Test
    public void handleMalformedIdentifierException() {
        ResponseEntity re = testIntance.handleMalformedIdentifierException(new MalformedIdentifierException("MalformedIdentifierExceptionMessage", null));

        assertEquals(BAD_REQUEST, re.getStatusCode());
        assertEquals(ErrorBusinessCode.FORMAT_ERROR.toString(), ((ErrorResponse)re.getBody()).getBusinessCode());
    }

    @Test
    public void handleAuthenticationException() {

        ResponseEntity re = testIntance.handleRuntimeException(new AuthenticationException("AuthenticationException") {
            @Override
            public String getMessage() {
                return super.getMessage();
            }
        });

        assertEquals(UNAUTHORIZED, re.getStatusCode());
        assertEquals(ErrorBusinessCode.UNAUTHORIZED.toString(), ((ErrorResponse)re.getBody()).getBusinessCode());
    }

    @Test
    public void handleAccessDeniedException() {
        ResponseEntity re = testIntance.handleAccessDeniedException(new AccessDeniedException("AccessDeniedExceptionMessage"));

        assertEquals(UNAUTHORIZED, re.getStatusCode());
        assertEquals(ErrorBusinessCode.UNAUTHORIZED.toString(), ((ErrorResponse)re.getBody()).getBusinessCode());
    }

/*
    @Test
    public void handleXmlInvalidAgainstSchemaException() {
        ResponseEntity re = testIntance.handleXmlInvalidAgainstSchemaException(
                new XmlInvalidAgainstSchemaException("XmlInvalidAgainstSchemaExceptionMessage", null));

        assertEquals(BAD_REQUEST, re.getStatusCode());
        assertEquals(ErrorBusinessCode.XSD_INVALID.toString(), ((ErrorResponse)re.getBody()).getBusinessCode());
    }

 */
}
