/*-
 * #START_LICENSE#
 * oasis-smp-spi
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
package eu.europa.ec.smp.spi.handler;

import eu.europa.ec.dynamicdiscovery.core.extension.impl.oasis20.OasisSMP20ServiceMetadataReader;
import eu.europa.ec.dynamicdiscovery.core.validator.OasisSmpSchemaValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.exception.XmlInvalidAgainstSchemaException;
import eu.europa.ec.smp.spi.api.SmpDataServiceApi;
import eu.europa.ec.smp.spi.api.SmpIdentifierServiceApi;
import eu.europa.ec.smp.spi.api.SmpXmlSignatureApi;
import eu.europa.ec.smp.spi.api.model.RequestData;
import eu.europa.ec.smp.spi.api.model.ResourceIdentifier;
import eu.europa.ec.smp.spi.api.model.ResponseData;
import eu.europa.ec.smp.spi.utils.DomUtils;
import eu.europa.ec.smp.spi.exceptions.ResourceException;
import eu.europa.ec.smp.spi.exceptions.SignatureException;
import eu.europa.ec.smp.spi.validation.Subresource20Validator;
import gen.eu.europa.ec.ddc.api.smp20.ServiceMetadata;
import gen.eu.europa.ec.ddc.api.smp20.aggregate.Certificate;
import gen.eu.europa.ec.ddc.api.smp20.aggregate.Endpoint;
import gen.eu.europa.ec.ddc.api.smp20.aggregate.Process;
import gen.eu.europa.ec.ddc.api.smp20.aggregate.ProcessMetadata;
import gen.eu.europa.ec.ddc.api.smp20.basic.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static eu.europa.ec.smp.spi.exceptions.ResourceException.ErrorCode.*;

@Component
public class OasisSMPSubresource20Handler extends AbstractOasisSMPHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OasisSMPSubresource20Handler.class);

    final SmpXmlSignatureApi signatureApi;
    final SmpDataServiceApi smpDataApi;
    final SmpIdentifierServiceApi smpIdentifierApi;
    final Subresource20Validator serviceMetadataValidator;

    final OasisSMP20ServiceMetadataReader reader;

    public OasisSMPSubresource20Handler(SmpDataServiceApi smpDataApi,
                                        SmpIdentifierServiceApi smpIdentifierApi,
                                        SmpXmlSignatureApi signatureApi,
                                        Subresource20Validator serviceMetadataValidator) {
        this.signatureApi = signatureApi;
        this.smpDataApi = smpDataApi;
        this.smpIdentifierApi = smpIdentifierApi;
        this.serviceMetadataValidator = serviceMetadataValidator;
        this.reader = new OasisSMP20ServiceMetadataReader();
    }


    public void generateResource(RequestData resourceData, ResponseData responseData, List<String> fields) throws ResourceException {

        ResourceIdentifier identifier = getResourceIdentifier(resourceData);
        ResourceIdentifier subresourceIdentifier = getSubresourceIdentifier(resourceData);

        ServiceMetadata subresource = new ServiceMetadata();
        subresource.setSMPVersionID(new SMPVersionID());
        subresource.getSMPVersionID().setValue("2.0");
        subresource.setParticipantID(new ParticipantID());
        subresource.getParticipantID().setValue(identifier.getValue());
        subresource.getParticipantID().setSchemeID(identifier.getScheme());
        subresource.setServiceID(new ServiceID());
        subresource.getServiceID().setValue(subresourceIdentifier.getValue());
        subresource.getServiceID().setSchemeID(subresourceIdentifier.getScheme());
        ProcessMetadata processMetadata = new ProcessMetadata();
        subresource.getProcessMetadatas().add(processMetadata);
        Process process = new Process();
        process.setID(new ID());
        process.getID().setValue("Service");
        process.getID().setSchemeID("service-namespace");
        processMetadata.getProcesses().add(process);
        Endpoint endpoint = new Endpoint();
        endpoint.setExpirationDate(new ExpirationDate());
        endpoint.setActivationDate(new ActivationDate());
        endpoint.getExpirationDate().setValue(OffsetDateTime.now().plusYears(1));
        endpoint.getActivationDate().setValue(OffsetDateTime.now().minusDays(1));
        endpoint.setAddressURI(new AddressURI());
        endpoint.getAddressURI().setValue("http://test.ap.local/msh");
        endpoint.setTransportProfileID(new TransportProfileID());
        endpoint.getTransportProfileID().setValue("bdxr-transport-ebms3-as4-v2p0");
        Certificate certEnc = new Certificate();
        certEnc.setExpirationDate(new ExpirationDate());
        certEnc.setActivationDate(new ActivationDate());
        certEnc.getExpirationDate().setValue(OffsetDateTime.now().plusYears(1));
        certEnc.getActivationDate().setValue(OffsetDateTime.now().minusDays(1));
        certEnc.setSubject(new Subject());
        certEnc.setIssuer(new Issuer());
        certEnc.setTypeCode(new TypeCode());
        certEnc.setContentBinaryObject(new ContentBinaryObject());
        certEnc.getSubject().setValue("CN=test-ap-enc,OU=edelivery,O=digit,C=EU");
        certEnc.getIssuer().setValue("CN=test-ap-enc,OU=edelivery,O=digit,C=EU");
        certEnc.getTypeCode().setValue("http://www.w3.org/2002/03/xkms#Exchange");
        certEnc.getContentBinaryObject().setValue("Put the real certificate data here".getBytes());
        certEnc.getContentBinaryObject().setMimeCode("application/base64");

        Certificate certSig = new Certificate();
        certSig.setExpirationDate(new ExpirationDate());
        certSig.setActivationDate(new ActivationDate());
        certSig.getExpirationDate().setValue(OffsetDateTime.now().plusYears(1));
        certSig.getActivationDate().setValue(OffsetDateTime.now().minusDays(1));
        certSig.setTypeCode(new TypeCode());
        certSig.setContentBinaryObject(new ContentBinaryObject());
        certSig.setSubject(new Subject());
        certSig.setIssuer(new Issuer());
        certSig.getSubject().setValue("CN=test-ap-signature,OU=edelivery,O=digit,C=EU");
        certSig.getIssuer().setValue("CN=test-ap-signature,OU=edelivery,O=digit,C=EU");
        certSig.getTypeCode().setValue("http://www.w3.org/2002/03/xkms#Signature");
        certSig.getContentBinaryObject().setValue("Put the real certificate data here".getBytes());
        certSig.getContentBinaryObject().setMimeCode("application/base64");
        endpoint.getCertificates().add(certEnc);
        endpoint.getCertificates().add(certSig);
        processMetadata.getEndpoints().add(endpoint);


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
            docEnvelopedMetadata = DomUtils.parse(bytearray);
        } catch (IOException | SAXException | ParserConfigurationException e) {
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
            OasisSmpSchemaValidator.validateOasisSMP20ServiceMetadataSchema(bytearray);
        } catch (IOException | XmlInvalidAgainstSchemaException e) {
            throw new ResourceException(INVALID_RESOURCE, "Error occurred while validation Oasis SMP 2.0 ServiceMetadata: [" + identifier + "] with error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }


        ServiceMetadata subresource;
        try {
            subresource = reader.parseNative(new ByteArrayInputStream(bytearray));
        } catch (TechnicalException e) {
            throw new ResourceException(INVALID_RESOURCE, "Error occurred while validation Oasis SMP 2.0 ServiceMetadata: [" + identifier + "] with error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
        serviceMetadataValidator.validate(identifier, documentIdentifier, subresource);

    }

}
