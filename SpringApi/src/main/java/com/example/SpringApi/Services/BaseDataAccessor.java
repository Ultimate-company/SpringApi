package com.example.SpringApi.Services;

import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import jakarta.servlet.http.HttpServletRequest;
import com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import org.springframework.data.jpa.domain.Specification;

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
        String auditUserId = request.getParameter("AuditUserId");
        if (auditUserId != null) {
            return Long.parseLong(auditUserId);
        } else {
            return null;
        }
    }

    public Long getCarrierId() {  // Return type changed to Long to allow null
        String carrierId = request.getParameter("CarrierId");
        if (carrierId != null) {
            return Long.parseLong(carrierId);
        } else {
            return null;
        }
    }

    public Long getWebTemplateId() {  // Return type changed to Long to allow null
        String webTemplateId = request.getParameter("WebTemplateId");
        if (webTemplateId != null) {
            return Long.parseLong(webTemplateId);
        } else {
            return null;
        }
    }

    protected <T> Specification<T> getWhereConditionForJpa(String columnName, String condition, String filterExpr) {
        return (root, query, cb) -> switch (condition) {
            case "equals" -> cb.equal(root.get(columnName), filterExpr);
            case "notEquals" -> cb.notEqual(root.get(columnName), filterExpr);
            case "startsWith" -> cb.like(root.get(columnName), filterExpr + "%");
            case "endsWith" -> cb.like(root.get(columnName), "%" + filterExpr);
            case "contains" -> cb.like(root.get(columnName), "%" + filterExpr + "%");
            default -> throw new IllegalArgumentException("Unknown condition: " + condition);
        };
    }

    protected Specification<User> getWhereCaseConditionForDeletedItemsForJpa(boolean includeDeleted) {
        return (root, query, cb) -> {
            if (includeDeleted) {
                // If deleted items should be included, return a condition that always evaluates to true.
                return cb.conjunction(); // This is equivalent to '1 = 1' in SQL.
            } else {
                // If deleted items should not be included, return a condition that checks the 'isDeleted' field.
                return cb.isFalse(root.get("IsDeleted"));
            }
        };
    }

    protected Specification<User> getWhereCaseConditionForGuestUsersForJpa(boolean includeGuest) {
        return (root, query, cb) -> {
            if (includeGuest) {
                // If deleted items should be included, return a condition that always evaluates to true.
                return cb.conjunction(); // This is equivalent to '1 = 1' in SQL.
            } else {
                // If deleted items should not be included, return a condition that checks the 'isDeleted' field.
                return cb.isFalse(root.get("IsGuest"));
            }
        };
    }
}
