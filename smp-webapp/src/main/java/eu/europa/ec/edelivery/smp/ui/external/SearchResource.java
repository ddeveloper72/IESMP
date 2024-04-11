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
package eu.europa.ec.edelivery.smp.ui.external;


import eu.europa.ec.edelivery.smp.data.dao.DomainDao;
import eu.europa.ec.edelivery.smp.data.ui.ServiceGroupSearchRO;
import eu.europa.ec.edelivery.smp.data.ui.ServiceResult;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.ui.UIResourceSearchService;
import eu.europa.ec.edelivery.smp.services.ui.filters.ResourceFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static eu.europa.ec.edelivery.smp.ui.ResourceConstants.*;

/**
 * Purpose of the SearchResource is to provide search method public participant capabilities
 *
 * @author Joze Rihtarsic
 * @since 4.1
 */
@RestController
@RequestMapping(path = CONTEXT_PATH_PUBLIC_SEARCH_PARTICIPANT)
public class SearchResource {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(SearchResource.class);

    final UIResourceSearchService uiServiceGroupService;
    final DomainDao domainDao;

    public SearchResource(UIResourceSearchService uiServiceGroupService, DomainDao domainDao) {
        this.uiServiceGroupService = uiServiceGroupService;
        this.domainDao = domainDao;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ServiceResult<ServiceGroupSearchRO> getServiceGroupList(
            @RequestParam(value = PARAM_PAGINATION_PAGE, defaultValue = "0") int page,
            @RequestParam(value = PARAM_PAGINATION_PAGE_SIZE, defaultValue = "10") int pageSize,
            @RequestParam(value = PARAM_PAGINATION_ORDER_BY, required = false) String orderBy,
            @RequestParam(value = PARAM_PAGINATION_ORDER_TYPE, defaultValue = "asc", required = false) String orderType,
            @RequestParam(value = PARAM_QUERY_PARTC_ID, required = false) String participantIdentifier,
            @RequestParam(value = PARAM_QUERY_PARTC_SCHEME, required = false) String participantScheme,
            @RequestParam(value = PARAM_QUERY_DOMAIN_CODE, required = false) String domainCode) {

        String participantIdentifierDecoded = decodeUrlToUTF8(participantIdentifier);
        String participantSchemeDecoded = decodeUrlToUTF8(participantScheme);
        String domainCodeDecoded = decodeUrlToUTF8(domainCode);

        LOG.info("Search for page: {}, page size: {}, part. id: {}, part sch: {}, domain {}", page, pageSize, participantIdentifierDecoded,
                participantSchemeDecoded, domainCodeDecoded);

        ResourceFilter sgf = new ResourceFilter();
        sgf.setIdentifierValueLike(participantIdentifierDecoded);
        sgf.setIdentifierSchemeLike(participantSchemeDecoded);
        // add domain search parameter
        sgf.setDomain(domainDao.validateDomainCode(domainCodeDecoded));

        return uiServiceGroupService.getTableList(page, pageSize, orderBy, orderType, sgf);
    }

    private String decodeUrlToUTF8(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOG.error("Unsupported UTF-8 encoding while converting: " + value, ex);
        }
        return value;
    }
}
