/*-
 * #START_LICENSE#
 * oasis-smp-spi
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
package eu.europa.ec.smp.spi.handler;

import eu.europa.ec.dynamicdiscovery.core.extension.impl.oasis10.OasisSMP10ServiceMetadataReader;
import eu.europa.ec.dynamicdiscovery.core.validator.OasisSmpSchemaValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.exception.XmlInvalidAgainstSchemaException;
import eu.europa.ec.smp.spi.api.SmpDataServiceApi;
import eu.europa.ec.smp.spi.api.SmpIdentifierServiceApi;
import eu.europa.ec.smp.spi.api.SmpXmlSignatureApi;
import eu.europa.ec.smp.spi.api.model.RequestData;
import eu.europa.ec.smp.spi.api.model.ResourceIdentifier;
import eu.europa.ec.smp.spi.api.model.ResponseData;
import eu.europa.ec.smp.spi.converter.DomUtils;
import eu.europa.ec.smp.spi.exceptions.ResourceException;
import eu.europa.ec.smp.spi.exceptions.SignatureException;
import eu.europa.ec.smp.spi.validation.Subresource10Validator;
import gen.eu.europa.ec.ddc.api.smp10.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;
import javax.xml.transform.TransformerException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static eu.europa.ec.smp.spi.exceptions.ResourceException.ErrorCode.*;

@Component
public class OasisSMPSubresource10Handler extends AbstractOasisSMPHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OasisSMPSubresource10Handler.class);

    private static final String NS = "http://docs.oasis-open.org/bdxr/ns/SMP/2016/05";
    private static final String DOC_SIGNED_SERVICE_METADATA_EMPTY = "<SignedServiceMetadata xmlns=\"" + NS + "\"/>";
    private static final String PARSER_DISALLOW_DTD_PARSING_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    final SmpXmlSignatureApi signatureApi;
    final SmpDataServiceApi smpDataApi;
    final SmpIdentifierServiceApi smpIdentifierApi;
    final Subresource10Validator serviceMetadataValidator;
    final OasisSMP10ServiceMetadataReader reader;

    public OasisSMPSubresource10Handler(SmpDataServiceApi smpDataApi,
                                        SmpIdentifierServiceApi smpIdentifierApi,
                                        SmpXmlSignatureApi signatureApi,
                                        Subresource10Validator serviceMetadataValidator) {
        this.signatureApi = signatureApi;
        this.smpDataApi = smpDataApi;
        this.smpIdentifierApi = smpIdentifierApi;
        this.serviceMetadataValidator = serviceMetadataValidator;
        this.reader = new OasisSMP10ServiceMetadataReader();
    }

    public void generateResource(RequestData resourceData, ResponseData responseData, List<String> fields) throws ResourceException {

        ResourceIdentifier identifier = getResourceIdentifier(resourceData);
        ResourceIdentifier subresourceIdentifier = getSubresourceIdentifier(resourceData);

        ServiceMetadata subresource = new ServiceMetadata();
        ServiceInformationType serviceInformationType =   new ServiceInformationType();
        ProcessListType processListType = new ProcessListType();
        ProcessType processType = new ProcessType();
        processType.setProcessIdentifier(new ProcessIdentifier());
        processType.getProcessIdentifier().setScheme("[test-schema]");
        processType.getProcessIdentifier().setValue("[test-value]");
        processType.setServiceEndpointList(new ServiceEndpointList());
        EndpointType endpointType = new EndpointType();
        endpointType.setTransportProfile("bdxr-transport-ebms3-as4-v1p0");
        endpointType.setEndpointURI("https://mypage.eu");
        endpointType.setCertificate("Certificate data ".getBytes());
        endpointType.setServiceDescription("Service description for partners ");
        endpointType.setTechnicalContactUrl("www.best-page.eu");
        processListType.getProcesses().add(processType);
        processType.getServiceEndpointList().getEndpoints().add(endpointType);
        serviceInformationType.setProcessList(processListType);
        subresource.setServiceInformation(serviceInformationType);
        serviceInformationType.setParticipantIdentifier(new ParticipantIdentifierType());
        serviceInformationType.getParticipantIdentifier().setValue(identifier.getValue());
        serviceInformationType.getParticipantIdentifier().setScheme(identifier.getScheme());
        serviceInformationType.setDocumentIdentifier(new DocumentIdentifier());
        serviceInformationType.getDocumentIdentifier().setValue(subresourceIdentifier.getValue());
        serviceInformationType.getDocumentIdentifier().setScheme(subresourceIdentifier.getScheme());

        try {
            reader.serializeNative(subresource, responseData.getOutputStream(), true);
        } catch (TechnicalException e) {
            throw new ResourceException(PARSE_ERROR, "Can not marshal extension for service group: [" + identifier + "]. Error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    @Override
    public void readResource(RequestData resourceData, ResponseData responseData) throws ResourceException {
        ResourceIdentifier resourceIdentifier = getResourceIdentifier(resourceData);
        ResourceIdentifier subresourceIdentifier = getSubresourceIdentifier(resourceData);

        if (resourceData.getResourceInputStream() == null) {
            LOG.warn("Empty document input stream for service-group: [{}] and service metadata [{}]", resourceIdentifier, subresourceIdentifier);
            return;
        }

        Document docEnvelopedMetadata;
        try {
            byte[] bytearray = readFromInputStream(resourceData.getResourceInputStream());
            docEnvelopedMetadata = DomUtils.toSignedSubresource10Document(bytearray);
        } catch (IOException e) {
            throw new ResourceException(PARSE_ERROR, "Can not marshal extension for service group: ["
                    + resourceIdentifier + "]. Error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }

        try {
            signatureApi.createEnvelopedSignature(resourceData, docEnvelopedMetadata.getDocumentElement(), Collections.emptyList());
        } catch (SignatureException e) {
            throw new ResourceException(PROCESS_ERROR, "Error occurred while signing the message!: ["
                    + resourceIdentifier + "]. Error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }

        try {
            DomUtils.serialize(docEnvelopedMetadata, responseData.getOutputStream());
            responseData.setContentType("text/xml");
        } catch (TransformerException e) {
            throw new ResourceException(INTERNAL_ERROR, "Error occurred while writing the message: ["
                    + resourceIdentifier + "]. Error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    @Override
    public void storeResource(RequestData resourceData, ResponseData responseData) throws ResourceException {
        InputStream inputStream = resourceData.getResourceInputStream();
        // reading resource multiple time make sure it can be rest
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        inputStream.mark(Integer.MAX_VALUE - 2);
        validateResource(resourceData);

        try {
            inputStream.reset();
        } catch (IOException e) {
            throw new ResourceException(PARSE_ERROR, "Can not reset input stream", e);
        }

        try {
            StreamUtils.copy(inputStream, responseData.getOutputStream());
        } catch (IOException e) {
            throw new ResourceException(PARSE_ERROR, "Error occurred while copying the ServiceGroup", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateResource(RequestData resourceData) throws ResourceException {
        ResourceIdentifier identifier = getResourceIdentifier(resourceData);
        ResourceIdentifier documentIdentifier = getSubresourceIdentifier(resourceData);
        byte[] bytearray;
        try {
            bytearray = readFromInputStream(resourceData.getResourceInputStream());
            OasisSmpSchemaValidator.validateOasisSMP10Schema(bytearray);
        } catch (IOException | XmlInvalidAgainstSchemaException e) {
            throw new ResourceException(INVALID_RESOURCE, "Error occurred while validation Oasis SMP 1.0 ServiceMetadata: [" + identifier + "] with error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }

        ServiceMetadata subresource;
        try {
            subresource = (ServiceMetadata) reader.parseNative(new ByteArrayInputStream(bytearray));
        } catch (TechnicalException e) {
            throw new ResourceException(INVALID_RESOURCE, "Error occurred while validation Oasis SMP 1.0 ServiceMetadata: [" + identifier + "] with error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
        serviceMetadataValidator.validate(identifier, documentIdentifier, subresource);
    }

}
