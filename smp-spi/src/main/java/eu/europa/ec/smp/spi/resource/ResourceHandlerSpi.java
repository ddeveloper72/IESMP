package eu.europa.ec.smp.spi.resource;

import eu.europa.ec.smp.spi.api.model.RequestData;
import eu.europa.ec.smp.spi.api.model.ResponseData;
import eu.europa.ec.smp.spi.exceptions.ResourceException;

import java.util.List;


/**
 * The class implementing the ResourceHandlerSpi must support read transformation, store transformation, and
 * validation methods for the particular resource type, such as Oasis SMP 1.0 document, CPP document, etc.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 *
 */
public interface ResourceHandlerSpi {

    /**
     * Method get data from the resource in the input stream, and it writes transformation of the data as they are returned to
     *
     * @param resourceData the resource data
     * @param responseData the date object for setting the response
     */
    void readResource(RequestData resourceData, ResponseData responseData) throws ResourceException;

    void storeResource(RequestData resourceData, ResponseData responseData) throws ResourceException;

    /**
     * Validate resource schema and data. if resource is invalid the error is thrown
     * @param resourceData the resource data
     */
    void validateResource(RequestData resourceData) throws ResourceException;


    /**
     * Validate resource schema and data. if resource is invalid the error is thrown
     * @param resourceData the resource data
     */
    void generateResource(RequestData resourceData, ResponseData responseData, List<String> fields) throws ResourceException;

}
