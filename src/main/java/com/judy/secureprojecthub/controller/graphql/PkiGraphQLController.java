package com.judy.secureprojecthub.controller.graphql;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class PkiGraphQLController {

    @QueryMapping
    @PreAuthorize("hasRole('PKI_USER')")
    public Map<String, Object> pkiHealth(Principal principal) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("authentication", principal == null ? null : principal.getName());
        return response;
    }

    @QueryMapping
    @PreAuthorize("hasRole('PKI_USER')")
    public Map<String, Object> certificateInfo(Principal principal) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("principal", principal == null ? null : principal.getName());
        // TODO: If your REST PKI controller reads X509Certificate from request attributes,
        // inject HttpServletRequest here and reuse that logic.
        return response;
    }

    @QueryMapping
    @PreAuthorize("hasRole('PKI_USER')")
    public Map<String, Object> verifyCertificate(Principal principal) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valid", principal != null);
        response.put("principal", principal == null ? null : principal.getName());
        return response;
    }
}
