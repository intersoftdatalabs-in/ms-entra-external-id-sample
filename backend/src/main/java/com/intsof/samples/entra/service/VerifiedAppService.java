package com.intsof.samples.entra.service;

import org.springframework.stereotype.Service;
import com.intsof.samples.entra.config.SsoConfigProperties;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class VerifiedAppService {

    private final Map<String, List<String>> verifiedApps;

    @Autowired
    public VerifiedAppService(SsoConfigProperties ssoConfigProperties) {
        this.verifiedApps = ssoConfigProperties.getVerifiedApps() != null
                ? ssoConfigProperties.getVerifiedApps()
                : new HashMap<>();
    }

    public List<String> getVerifiedApplicationsForDomain(String domain) {
        if (domain == null) {
            return Collections.emptyList();
        }
        String normalizedDomain = domain.toLowerCase();
        return verifiedApps.getOrDefault(normalizedDomain, Collections.emptyList());
    }
}
