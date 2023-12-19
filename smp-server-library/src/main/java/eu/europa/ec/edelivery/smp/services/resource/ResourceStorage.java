/*-
 * #START_LICENSE#
 * smp-server-library
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
package eu.europa.ec.edelivery.smp.services.resource;


import eu.europa.ec.edelivery.smp.data.dao.DocumentDao;
import eu.europa.ec.edelivery.smp.data.dao.ResourceDao;
import eu.europa.ec.edelivery.smp.data.dao.SubresourceDao;
import eu.europa.ec.edelivery.smp.data.model.doc.DBDocument;
import eu.europa.ec.edelivery.smp.data.model.doc.DBDocumentVersion;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResource;
import eu.europa.ec.edelivery.smp.data.model.doc.DBSubresource;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * The class handles the resource action as creating, updating and reading the resources.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Service
public class ResourceStorage {
    protected static final SMPLogger LOG = SMPLoggerFactory.getLogger(ResourceStorage.class);
    final DocumentDao documentDao;
    final ResourceDao resourceDao;
    final SubresourceDao subresourceDao;

    public ResourceStorage(DocumentDao documentDao, ResourceDao resourceDao, SubresourceDao subresourceDao) {
        this.documentDao = documentDao;
        this.resourceDao = resourceDao;
        this.subresourceDao = subresourceDao;
    }

    public byte[] getDocumentContentForResource(DBResource dbResource) {
        LOG.debug("getDocumentContentForResource: [{}]", dbResource);

        Optional<DBDocumentVersion> documentVersion = this.documentDao.getCurrentDocumentVersionForResource(dbResource);

        return documentVersion.isPresent() ? documentVersion.get().getContent() : null;
    }

    public byte[] getDocumentContentForSubresource(DBSubresource subresource) {
        LOG.debug("getDocumentContentForSubresource: [{}]", subresource);
        Optional<DBDocumentVersion> documentVersion = documentDao.getCurrentDocumentVersionForSubresource(subresource);

        return documentVersion.isPresent() ? documentVersion.get().getContent() : null;
    }


    @Transactional
    public DBResource addDocumentVersionForResource(DBResource resource, DBDocumentVersion version) {
        LOG.debug("addDocumentVersionForResource: [{}]", resource);
        if (resource.getId() == null && resource.getDocument() == null) {
            resource.setDocument(new DBDocument());
        }
        DBResource managedResource = resource.getId() != null ? resourceDao.find(resource.getId()) : resourceDao.merge(resource);
        managedResource.getDocument().addNewDocumentVersion(version);
        return managedResource;
    }

    @Transactional
    public DBSubresource addDocumentVersionForSubresource(DBSubresource subresource, DBDocumentVersion version) {
        LOG.debug("addDocumentVersionForSubresource: [{}]", subresource);
        if (subresource.getId() == null && subresource.getDocument() == null) {
            subresource.setDocument(new DBDocument());
        }
        DBSubresource managedResource = subresource.getId() != null ? subresourceDao.find(subresource.getId()) : subresourceDao.merge(subresource);
        managedResource.getDocument().addNewDocumentVersion(version);
        return managedResource;
    }

    @Transactional
    public void deleteResource(DBResource resource) {
        LOG.debug("deleteResource: [{}]", resource);
        resourceDao.remove(resource);
    }

    public void deleteSubresource(DBSubresource subresource) {
        LOG.debug("deleteSubresource: [{}]", subresource);
        subresourceDao.remove(subresource);
    }

}
