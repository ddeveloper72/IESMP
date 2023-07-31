package eu.europa.ec.edelivery.smp.auth;

import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * URLCsrfMatcher matches the request and validates if request can be ignored for CSRF.
 * As example the non session requests (as SMP REST API) should now have the CSRF tokens.
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
public class URLCsrfIgnoreMatcher implements RequestMatcher {

    private static final Logger LOG = SMPLoggerFactory.getLogger(
            URLCsrfIgnoreMatcher.class);
    private final List<RegexRequestMatcher> unprotectedMatcherList = new ArrayList<>();

    public URLCsrfIgnoreMatcher() {
        this(null, null);
    }

    public URLCsrfIgnoreMatcher(List<String> regularExpressions, List<HttpMethod> methods) {
        if (regularExpressions == null || regularExpressions.isEmpty()) {
            return;
        }
        regularExpressions.forEach(regexp -> addIgnoreUrl(regexp, methods));
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        // ignore non ui sites!
        String uri = request.getRequestURI();
        LOG.debug("Test CSRF for uri [{}]", uri);
        if (!StringUtils.startsWithAny(uri, "/ui/", "/smp/ui/")) {
            LOG.debug("URL is not part of the UI  [{}]", uri);
            return false;
        }
        Optional<RegexRequestMatcher> unprotectedMatcher = unprotectedMatcherList.stream().filter(requestMatcher -> requestMatcher.matches(request)).findFirst();
        if (unprotectedMatcher.isPresent()) {
            LOG.debug("Ignore CSRF for: [{}] - [{}] with matcher [{}]!", request.getMethod(), request.getRequestURI(), unprotectedMatcher.get());
        }
        return !unprotectedMatcher.isPresent();
    }


    /**
     * Creates a case-sensitive {@code Pattern} instance to match against the request for  http method(s).
     *
     * @param ignoreUrlPattern the regular expression to match ignore URLs.
     * @param httpMethods      the HTTP method(s) to match. May be null to match all methods.
     */
    public void addIgnoreUrl(String ignoreUrlPattern, HttpMethod... httpMethods) {
        addIgnoreUrl(ignoreUrlPattern, httpMethods == null || httpMethods.length == 0 ? null : Arrays.asList(httpMethods));
    }


    /**
     * Creates a case-sensitive {@code Pattern} instance to match against the request for  http method(s).
     *
     * @param ignoreUrlPattern the regular expression to match ignore URLs.
     * @param httpMethods      list of the HTTP method(s) to match. May be null or empty to match all methods.
     */
    public void addIgnoreUrl(String ignoreUrlPattern, List<HttpMethod> httpMethods) {
        if (httpMethods == null || httpMethods.isEmpty()) {
            unprotectedMatcherList.add(new RegexRequestMatcher(ignoreUrlPattern, null));
        } else {
            httpMethods.forEach(httpMethod -> {
                unprotectedMatcherList.add(new RegexRequestMatcher(ignoreUrlPattern, httpMethod.name()));
            });
        }
    }
}
