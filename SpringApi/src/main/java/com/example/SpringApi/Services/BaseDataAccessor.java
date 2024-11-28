package com.example.SpringApi.Services;

import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier;

import java.util.Optional;


public class BaseDataAccessor {
    private final HttpServletRequest request;
    private final CarrierRepository carrierRepository;
    protected final String currentEnvironment = "Dev";

    public BaseDataAccessor(HttpServletRequest request,
                            CarrierRepository carrierRepository) {
        this.request = request;
        this.carrierRepository = carrierRepository;
    }

    public Carrier getCarrierDetails() {
        Long carrierId = getCarrierId();
        if (carrierId == null) {
            return null;
        } else {
            Optional<Carrier> carrier = carrierRepository.findById(carrierId);
            return carrier.orElse(null);
        }
    }

    public Long getUserId() {  // Return type changed to Long to allow null
        String auditUserId = null;

        // Try getting AuditUserId from the request
        try {
            auditUserId = request.getParameter("AuditUserId");
        } catch (Exception e) {
            // Handle potential exception or log it if needed
        }

        // If not found in request, get it from RequestContext
        if (auditUserId == null) {
            auditUserId = RequestContext.get("auditUserId");
        }

        // If AuditUserId is found, return it as Long, otherwise return null
        if (auditUserId != null) {
            return Long.parseLong(auditUserId);
        } else {
            return null;
        }
    }

    public Long getCarrierId() {  // Return type changed to Long to allow null
        String carrierId = null;

        // Try getting CarrierId from the request
        try {
            carrierId = request.getParameter("CarrierId");
        } catch (Exception e) {
            // Handle potential exception or log it if needed
        }

        // If not found in request, get it from RequestContext
        if (carrierId == null) {
            carrierId = RequestContext.get("carrierId");
        }

        // If CarrierId is found, return it as Long, otherwise return null
        if (carrierId != null) {
            return Long.parseLong(carrierId);
        } else {
            return null;
        }
    }

    public Long getWebTemplateId() {  // Return type changed to Long to allow null
        String webTemplateId = null;

        // Try getting WebTemplateId from the request
        try {
            webTemplateId = request.getParameter("WebTemplateId");
        } catch (Exception e) {
            // Handle potential exception or log it if needed
        }

        // If not found in request, get it from RequestContext
        if (webTemplateId == null) {
            webTemplateId = RequestContext.get("webTemplateId");
        }

        // If WebTemplateId is found, return it as Long, otherwise return null
        if (webTemplateId != null) {
            return Long.parseLong(webTemplateId);
        } else {
            return null;
        }
    }
}
