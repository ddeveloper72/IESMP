/*-
 * #START_LICENSE#
 * smp-webapp
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

package eu.europa.ec.smp.spi.utils;

import eu.europa.ec.smp.spi.exceptions.ResourceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import static eu.europa.ec.smp.spi.exceptions.ResourceException.ErrorCode.INVALID_RESOURCE;

/**
 * @author gutowpa
 * @since 3.0.0
 */
public final class DomUtils {

    public static final String DISALLOW_DOCTYPE_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    /**
     * Class has only static members. Is not meant to create instances  - also SONAR warning.
     */
    private DomUtils() {

    }

    private static final String NS = "http://docs.oasis-open.org/bdxr/ns/SMP/2016/05";
    private static final String DOC_SIGNED_SERVICE_METADATA_EMPTY = "<SignedServiceMetadata xmlns=\"" + NS + "\"/>";
    private static final String PARSER_DISALLOW_DTD_PARSING_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final Logger LOG = LoggerFactory.getLogger(DomUtils.class);

    /**
     * Method parses serviceMetadata XML and envelopes it to SignedServiceMetadata.
     *
     * @param serviceMetadataXml
     * @return w3d dom element
     */
    public static Document toSignedSubresource10Document(byte[] serviceMetadataXml) throws ResourceException {
        LOG.debug("toSignedSubresource10Document");
        try {
            Document docServiceMetadata = parse(serviceMetadataXml);
            Document root = parse(DOC_SIGNED_SERVICE_METADATA_EMPTY.getBytes());
            Node imported = root.importNode(docServiceMetadata.getDocumentElement(), true);
            root.getDocumentElement().appendChild(imported);
            return root;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new ResourceException(INVALID_RESOURCE, "Invalid Signed serviceMetadataXml with error: " + ExceptionUtils.getRootCauseMessage(ex), ex);
        }
    }


    public static Document parse(byte[] subresourceXml) throws SAXException, IOException, ParserConfigurationException, ResourceException {
        if (subresourceXml == null) {
            throw new ResourceException(INVALID_RESOURCE, "Invalid serviceMetadataXml: null");
        }
        LOG.debug("Parse document with size [{}]", subresourceXml.length);
        InputStream inputStream = new ByteArrayInputStream(subresourceXml);
        return getDocumentBuilder().parse(inputStream);
    }

    public static String toString(Document doc) throws TransformerException {
        Transformer transformer = createNewSecureTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    public static byte[] toByteArray(Document doc) throws TransformerException {
        LOG.debug("Convert document to byte array");
        Transformer transformer = createNewSecureTransformer();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(stream));
        return stream.toByteArray();
    }

    public static void serialize(Document doc, OutputStream outputStream) throws TransformerException {
        Transformer transformer = createNewSecureTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
    }

    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setFeature(PARSER_DISALLOW_DTD_PARSING_FEATURE, true);
        return dbf.newDocumentBuilder();
    }

    /**
     * Create new secure transformer with secure processing and disallow doctype declaration,
     * @return new transformer
     * @throws TransformerConfigurationException
     */
    public static Transformer createNewSecureTransformer() throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        // generic secure file processing
        setConfigurationOption(factory, XMLInputFactory.SUPPORT_DTD, false);
        setConfigurationOption(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        setConfigurationOption(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setConfigurationOption(factory, DISALLOW_DOCTYPE_FEATURE, true);
        setAttributeOption(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setAttributeOption(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newTransformer();
    }

    /**
     * Set configuration option to the factor. If the feature is not supported by the factory,
     * the feature is ignored.
     * @param factory transformer factory
     * @param feature feature to set
     * @param value value to set
     */
    protected static void setConfigurationOption(TransformerFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (UnsupportedOperationException | TransformerConfigurationException e) {
            LOG.warn("TransformerFactory initialization error. The feature [{}] is not supported by current factory. The feature is ignored.", feature);
        }
    }

    protected static void setAttributeOption(TransformerFactory factory, String feature, Object value) {
        try {
            factory.setAttribute(feature, value);
        } catch (IllegalArgumentException e) {
            LOG.warn("TransformerFactory initialization error. The attribute [{}] is not supported by current factory. The feature is ignored.", feature);
        }
    }
}
