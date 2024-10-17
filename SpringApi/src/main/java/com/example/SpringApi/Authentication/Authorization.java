package com.example.SpringApi.Authentication;

import com.example.SpringApi.DataSource.CarrierContextHolder;
import com.example.SpringApi.DatabaseModels.CentralDatabase.WebTemplateCarrierMapping;
import com.example.SpringApi.Repository.CarrierDatabase.PermissionRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserCarrierMappingRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Repository.CentralDatabase.WebTemplateCarrierMappingRepository;
import jakarta.servlet.http.HttpServletRequest;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Permissions;
import com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import com.example.SpringApi.DatabaseModels.CentralDatabase.UserCarrierMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service("customAuthorization")
public class Authorization{

    private final HttpServletRequest request;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CarrierRepository carrierRepository;
    private final PermissionRepository permissionRepository;
    private final UserCarrierMappingRepository userCarrierMappingRepository;
    private final WebTemplateCarrierMappingRepository webTemplateCarrierMappingRepository;

    @Autowired
    public Authorization(HttpServletRequest request,
                         JwtTokenProvider jwtTokenProvider,
                         UserRepository userRepository,
                         CarrierRepository carrierRepository,
                         PermissionRepository permissionRepository,
                         UserCarrierMappingRepository userCarrierMappingRepository,
                         WebTemplateCarrierMappingRepository WebTemplateCarrierMappingRepository
    ){
        this.request = request;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.carrierRepository = carrierRepository;
        this.permissionRepository = permissionRepository;
        this.userCarrierMappingRepository = userCarrierMappingRepository;
        this.webTemplateCarrierMappingRepository = WebTemplateCarrierMappingRepository;
    }

    private String getJwtFromRequest() {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
    private long validateUserId(String auditUserIdStr){
        if(!StringUtils.hasText(auditUserIdStr)){
            throw new PermissionException("Audit User Id Id is required in the get params of the request, this will be the user id of the user you are currently logged in as.");
        }
        long auditUserId;
        try {
            auditUserId = Long.parseLong(auditUserIdStr);
            // Now you can use the id variable
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(auditUserIdStr + " cannot be parsed to a long please enter a valid carrier id", e);
        }
        return auditUserId;
    }

    private long validateCarrierId(String carrierIdStr){
        if(!StringUtils.hasText(carrierIdStr)){
            throw new PermissionException("Carrier Id is required in the get params of the request, this will be the carrier id you are currently trying to access.");
        }

        long carrierId;
        try {
            carrierId = Long.parseLong(carrierIdStr);
            // Now you can use the id variable
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(carrierIdStr + " cannot be parsed to a long please enter a valid carrier id", e);
        }
        return carrierId;
    }

    private long validateWebTemplateId(String webTemplateIdStr){
        if(!StringUtils.hasText(webTemplateIdStr)){
            throw new PermissionException("Web Template Id is required.");
        }

        long webTemplateId;
        try {
            webTemplateId = Long.parseLong(webTemplateIdStr);
            // Now you can use the id variable
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(webTemplateIdStr + " cannot be parsed to a long please enter a valid web template id", e);
        }
        return webTemplateId;
    }

    public boolean validatePublicEndpoint() {
        // validate and initialize the carrier
        if(request.getParameter("CarrierId") != null) {
            long carrierId = validateCarrierId(request.getParameter("CarrierId"));

            // get the carrier and user by id
            Optional<Carrier> carrier = carrierRepository.findById(carrierId);

            if(carrier.isPresent()) {
                CarrierContextHolder.setCarrierId(carrierId);
            }
            else {
                throw new PermissionException("Invalid carrier id provided");
            }
        }

        // if the user id is present make sure its valid and present in the db
        if (request.getParameter("AuditUserId") != null) {
            // Try to retrieve the value of "AuditUserId" from the request query parameters
            long auditUserId = Long.parseLong(request.getParameter("AuditUserId"));

            //get the user by id
            Optional<User> user = userRepository.findById(auditUserId);

            if(user.isEmpty()) {
                throw new PermissionException("Invalid user id");
            }
        }

        // get the web template id and the wildcard
        // also validate the token
        if (request.getParameter("WebTemplateId") != null &&
                request.getParameter("WildCard") != null ) {
            long webTemplateId = validateWebTemplateId(request.getParameter("WebTemplateId"));
            String wildCard = request.getParameter("WildCard");

            // validate the webtempalteid and wildcard match
            WebTemplateCarrierMapping webTemplateCarrierMapping = webTemplateCarrierMappingRepository.findByWildCard(wildCard);
            if(webTemplateCarrierMapping == null) {
                throw new PermissionException("Invalid wildcard");
            }
            if(!webTemplateCarrierMapping.getWebTemplateId().equals(webTemplateId)) {
                throw new PermissionException("Invalid web template Id");
            }

            String token = getJwtFromRequest();
            boolean isValidToken = jwtTokenProvider.validateTokenForWebTemplate(token, wildCard, webTemplateCarrierMapping.getApiAccessKey());
            if (!isValidToken) {
                throw new PermissionException("Invalid Bearer token provided");
            }
        }

        return true;
    }

    public boolean validateToken() {
        // Check if the request contains the "AuditUserId" parameter
        if (request.getParameter("AuditUserId") != null) {
            // Try to retrieve the value of "AuditUserId" from the request query parameters
            long auditUserId = validateUserId(request.getParameter("AuditUserId"));

            // get the carrier and user by id
            Optional<User> user = userRepository.findById(auditUserId);

            if(user.isPresent()) {
                // check if the token provided is valid and belongs to the same requesting user.
                String token = getJwtFromRequest();
                boolean isValidToken = jwtTokenProvider.validateToken(token, user.get().getLoginName(), user.get().getApiKey());
                if (!isValidToken) {
                    throw new PermissionException("Invalid Bearer token provided");
                }
            }
        }
        else{
            return false;
        }

        if(request.getParameter("CarrierId") != null) {
            long carrierId = validateCarrierId(request.getParameter("CarrierId"));

            // get the carrier and user by id
            Optional<Carrier> carrier = carrierRepository.findById(carrierId);

            if(carrier.isPresent()) {
                CarrierContextHolder.setCarrierId(carrierId);
            }
            else {
                throw new PermissionException("Invalid carrier id provided");
            }
        }

        return true;
    }

    public boolean hasAuthority(String userPermission) {
        String auditUserId = "";
        String carrierId = "";

        // Check if the request contains the "AuditUserId" parameter
        if (request.getParameter("AuditUserId") != null) {
            // Try to retrieve the value of "AuditUserId" from the request query parameters
            auditUserId = request.getParameter("AuditUserId");
        }

        // Check if the request contains the "CarrierId" parameter
        if (request.getParameter("CarrierId") != null) {
            // Ignore the value of "CarrierId" as it is not used in this method
            carrierId = request.getParameter("CarrierId");
        }
        return isAllowed(auditUserId, carrierId, userPermission);
    }

    private boolean isAllowed(String auditUserIdStr, String carrierIdStr, String userPermission) {
        long carrierId = validateCarrierId(carrierIdStr);
        long auditUserId = validateUserId(auditUserIdStr);

        // get the carrier and user by id
        Optional<Carrier> carrier = carrierRepository.findById(carrierId);
        Optional<User> user = userRepository.findById(auditUserId);

        if(carrier.isPresent() && user.isPresent()) {
            // check if the token provided is valid and belongs to the same requesting user.
            String token = getJwtFromRequest();
            boolean isValidToken = jwtTokenProvider.validateToken(token, user.get().getLoginName(), user.get().getApiKey());
            if(!isValidToken){
                throw new PermissionException("Invalid Bearer token provided");
            }else{
                // establish connection to this carrier db
                CarrierContextHolder.setCarrierId(carrierId);
            }

            //check if the userid and carrier are actually mapped together
            UserCarrierMapping userCarrierMapping = userCarrierMappingRepository.findByUserIdAndCarrierId(user.get().getUserId(), carrier.get().getCarrierId());
            if(userCarrierMapping == null){
                throw new PermissionException("User is not present in the carrier id provided, a user can only access a carrier he is a part of.");
            }

            // fetch the permissions from the token mapping
            Map<String, Integer> carrierPermissionMapping = jwtTokenProvider.getcarrierPermissionMappingFromToken(token, user.get().getApiKey());
            if(carrierPermissionMapping!= null && carrierPermissionMapping.containsKey(String.valueOf(carrierId))){
                // Add each permission to the set
                int permissionId = carrierPermissionMapping.get(String.valueOf(carrierId));
                Optional<Permissions> permission = permissionRepository.findById((long)permissionId);
                if(permission.isEmpty()){
                    throw new PermissionException("Invalid permission Id, please get a new bearer token and try again.");
                }
                Set<String> permissionSet = new HashSet<>();
                permissionSet.addAll(Arrays.asList(permission.get().getUserPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getUserLogPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getGroupsPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getMessagesPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getPromosPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getAddressPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getPickupLocationPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getOrdersPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getPaymentsPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getEventsPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getProductsPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getSupportPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getApiKeyPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getLeadsPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getPurchaseOrderPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getSalesOrderPermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getWebTemplatePermissions().split(",")));
                permissionSet.addAll(Arrays.asList(permission.get().getPackagePermissions().split(",")));

                if(permissionSet.contains(userPermission)){
                    return true;
                }
                else {
                    throw new PermissionException("User is not authorized to do the action.");
                }
            }
            else {
                throw new PermissionException("Given user id does not have any valid permission set for the given carrier in the system.");
            }
        }
        else {
            if(carrier.isEmpty()){
                throw new PermissionException("Invalid carrier id provided, carrier id does not exists in the system.");
            }
            else{
                throw new PermissionException("Invalid user id provided, user id does not exists in the system.");
            }
        }
    }
}