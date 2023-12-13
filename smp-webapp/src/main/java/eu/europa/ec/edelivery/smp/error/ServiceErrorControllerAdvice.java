/*-
 * #%L
 * smp-webapp
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

package eu.europa.ec.edelivery.smp.error;

import eu.europa.ec.edelivery.smp.error.exceptions.SMPResponseStatusException;
import eu.europa.ec.edelivery.smp.error.xml.ErrorResponse;
import eu.europa.ec.edelivery.smp.exceptions.BadRequestException;
import eu.europa.ec.edelivery.smp.exceptions.ErrorBusinessCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static eu.europa.ec.edelivery.smp.exceptions.ErrorBusinessCode.FORMAT_ERROR;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


/**
 * Created by gutowpa on 14/09/2017.
 */
@RestControllerAdvice({"eu.europa.ec.edelivery.smp.controllers"})
public class ServiceErrorControllerAdvice extends AbstractErrorControllerAdvice {

    @ExceptionHandler({RuntimeException.class, SMPRuntimeException.class, SMPResponseStatusException.class, AuthenticationException.class,})
    public ResponseEntity handleRuntimeException(RuntimeException ex) {
        return super.handleRuntimeException(ex);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequestException(BadRequestException ex) {
        return buildAndLog(BAD_REQUEST, ex.getErrorBusinessCode(), ex.getMessage(), ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity handleMalformedIdentifierException(IllegalArgumentException ex) {
        return buildAndLog(BAD_REQUEST, FORMAT_ERROR, ex.getMessage(), ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleAccessDeniedException(AccessDeniedException ex) {
        return buildAndLog(UNAUTHORIZED, ErrorBusinessCode.UNAUTHORIZED, ex.getMessage() + " - Only SMP Admin or owner of given ServiceGroup is allowed to perform this action", ex);
    }

    ResponseEntity buildAndLog(HttpStatus status, ErrorBusinessCode businessCode, String msg, Exception exception) {

        ResponseEntity response = ErrorResponseBuilder.status(status)
                .businessCode(businessCode)
                .errorDescription(msg)
                .build();

        String errorUniqueId = ((ErrorResponse) response.getBody()).getErrorUniqueId();
        String logMsg = format("Error unique ID: %s", errorUniqueId);

        LOG.warn(logMsg, exception);
        return response;
    }

}
