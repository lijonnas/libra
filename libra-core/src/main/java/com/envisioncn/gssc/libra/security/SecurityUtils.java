package com.envisioncn.gssc.libra.security;

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.shared.ApplicationConstants;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author zhongshuangli
 * @date 2021-04-01
 */
public final class SecurityUtils {

    private static Boolean enabled = null;
    /**
     * Tests if the request is an internal framework request. The test consists of
     * checking if the request parameter is present and if its value is consistent
     * with any of the request types know.
     *
     * @param request
     *            {@link HttpServletRequest}
     * @return true if is an internal framework request. False otherwise.
     */
    public static boolean isFrameworkInternalRequest(HttpServletRequest request) {
        final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return parameterValue != null
                && Stream.of(HandlerHelper.RequestType.values()).anyMatch(r -> r.getIdentifier().equals(parameterValue));
    }
    public static boolean isAccessGranted(Class<?> securedClass) {
        if (!isSecurityEnabled()) {
            return true;
        }
        // Allow if no roles are required.
        Secured secured = AnnotationUtils.findAnnotation(securedClass, Secured.class);
        if (secured == null) {
            return true; //
        }

        // lookup needed role in user roles
        List<String> allowedRoles = Arrays.asList(secured.value());
        Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (userAuthentication == null) {
            return false;
        }

        return userAuthentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(allowedRoles::contains);
    }
    public static boolean isUserInRole(String role) {
        if (!isSecurityEnabled()) {
            return true;
        }

        String roleWithPrefix = "ROLE_"+role;
        Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();

        if (userAuthentication == null) {
            return false;
        }

        return userAuthentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(roleWithPrefix::equals);
    }


    /**
     * Tests if some user is authenticated. As Spring Security always will create an {@link AnonymousAuthenticationToken}
     * we have to ignore those tokens explicitly.
     */
    public static boolean isUserLoggedIn() {
        if (!isSecurityEnabled()) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }
    public static String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }

        return null;
    }

    public static boolean isSecurityEnabled() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }
}

