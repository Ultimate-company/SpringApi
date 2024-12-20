package com.example.SpringApi.Services;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.GoogleCredRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.RequestContext;
import com.example.SpringApi.Services.CarrierDatabase.*;
import com.example.SpringApi.Services.CentralDatabase.UserDataAccessor;
import com.example.SpringApi.SuccessMessages;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServletRequest;
import org.example.Adapters.DateAdapter;
import org.example.Adapters.LocalDateTimeAdapter;
import org.example.CommonHelpers.EmailTemplates;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CentralModels.Carrier;
import org.example.Models.CommunicationModels.CentralModels.GoogleCred;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.Interface.IBulkSubTranslator;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
public class BulkDataAccessor extends BaseDataAccessor implements IBulkSubTranslator {
    private final UserDataAccessor userDataAccessor;
    private final LeadDataAccessor leadDataAccessor;
    private final MessageDataAccessor messageDataAccessor;
    private final PackageDataAccessor packageDataAccessor;
    private final PickupLocationDataAccessor pickupLocationDataAccessor;
    private final ProductDataAccessor productDataAccessor;
    private final PromoDataAccessor promoDataAccessor;
    private final PurchaseOrderDataAccessor purchaseOrderDataAccessor;
    private final SalesOrderDataAccessor salesOrderDataAccessor;
    private final SupportDataAccessor supportDataAccessor;
    private final UserGroupDataAccessor userGroupDataAccessor;
    private final WebTemplatesDataAccessor webTemplatesDataAccessor;
    private final Authorization authorization;
    private final Environment environment;
    private final GoogleCredRepository googleCredRepository;
    private final UserRepository userRepository;
    private final CarrierRepository carrierRepository;

    // Map for permission checking
    private final Map<String, String> BULK_ADD_TYPES = new HashMap<>() {{
        put("User", Authorizations.INSERT_USER_PERMISSION);
        put("Lead", Authorizations.INSERT_LEADS_PERMISSION);
        put("Message", Authorizations.INSERT_MESSAGES_PERMISSION);
        put("Package", Authorizations.INSERT_PACKAGES_PERMISSION);
        put("PickupLocation", Authorizations.INSERT_PICKUP_LOCATIONS_PERMISSION);
        put("Product", Authorizations.INSERT_PRODUCTS_PERMISSION);
        put("Promo", Authorizations.INSERT_PROMOS_PERMISSION);
        put("PurchaseOrder", Authorizations.INSERT_PURCHASE_ORDERS_PERMISSION);
        put("SalesOrder", Authorizations.INSERT_SALES_ORDERS_PERMISSION);
        put("Support", null); // Support doesn't require a permission check
        put("UserGroup", Authorizations.INSERT_GROUPS_PERMISSION);
        put("WebTemplate", Authorizations.INSERT_WEB_TEMPLATE_PERMISSION);
    }};

    public BulkDataAccessor(HttpServletRequest request,
                            CarrierRepository carrierRepository,
                            UserDataAccessor userDataAccessor,
                            LeadDataAccessor leadDataAccessor,
                            MessageDataAccessor messageDataAccessor,
                            PackageDataAccessor packageDataAccessor,
                            PickupLocationDataAccessor pickupLocationDataAccessor,
                            ProductDataAccessor productDataAccessor,
                            PromoDataAccessor promoDataAccessor,
                            PurchaseOrderDataAccessor purchaseOrderDataAccessor,
                            SalesOrderDataAccessor salesOrderDataAccessor,
                            SupportDataAccessor supportDataAccessor,
                            UserGroupDataAccessor userGroupDataAccessor,
                            WebTemplatesDataAccessor webTemplatesDataAccessor,
                            Authorization authorization,
                            Environment environment,
                            GoogleCredRepository googleCredRepository,
                            UserRepository userRepository, CarrierRepository carrierRepository1) {
        super(request, carrierRepository);
        this.userDataAccessor = userDataAccessor;
        this.leadDataAccessor = leadDataAccessor;
        this.messageDataAccessor = messageDataAccessor;
        this.packageDataAccessor = packageDataAccessor;
        this.pickupLocationDataAccessor = pickupLocationDataAccessor;
        this.productDataAccessor = productDataAccessor;
        this.promoDataAccessor = promoDataAccessor;
        this.purchaseOrderDataAccessor = purchaseOrderDataAccessor;
        this.salesOrderDataAccessor = salesOrderDataAccessor;
        this.supportDataAccessor = supportDataAccessor;
        this.userGroupDataAccessor = userGroupDataAccessor;
        this.webTemplatesDataAccessor = webTemplatesDataAccessor;
        this.authorization = authorization;
        this.environment = environment;
        this.googleCredRepository = googleCredRepository;
        this.userRepository = userRepository;
        this.carrierRepository = carrierRepository1;
    }

    // local functions
    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    public static <T, R> Function<T, R> wrapFunction(CheckedFunction<T, R> checkedFunction) {
        return t -> {
            try {
                return checkedFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private <T, R> int processBulkData(String object, Gson gson, TypeToken<List<T>> typeToken,
                                       CheckedFunction<T, Response<R>> dataAccessorFunction,
                                       Map<String, String> errors) {
        List<T> items = gson.fromJson(object, typeToken.getType());
        int totalDataCount = items.size();

        for (T item : items) {
            try {
                // Applying the dataAccessorFunction which can return any Response<R>
                Response<R> response = wrapFunction(dataAccessorFunction).apply(item);
                if (!response.isSuccess()) {
                    errors.putIfAbsent(item.toString(), response.getMessage());
                }
            } catch (RuntimeException e) {
                errors.putIfAbsent(item.toString(), "Data Access Error: " + e.getCause().getMessage());
            } catch (Exception e) {
                errors.putIfAbsent(item.toString(), "JSON Processing Error: " + e.getMessage());
            }
        }
        return totalDataCount;
    }

    private CompletableFuture<Response<Boolean>> sendEmailNotification(String bulkAddType, Map<String, String> errors, int totalDataCountToBeImported, String auditUserId, String carrierId) throws Exception {
        Optional<com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier> carrier = carrierRepository.findById(Long.parseLong(carrierId));
        if (carrier.isEmpty()) {
            throw new Exception(ErrorMessages.CarrierErrorMessages.InvalidId);
        }

        Optional<com.example.SpringApi.DatabaseModels.CentralDatabase.GoogleCred> googleCred = googleCredRepository.findById(carrier.get().getGoogleCredId());
        if (googleCred.isEmpty()) {
            throw new Exception(ErrorMessages.CarrierErrorMessages.ER007);
        }

        Optional<User> user = userRepository.findById(Long.parseLong(auditUserId));
        if (user.isEmpty()) {
            throw new Exception(ErrorMessages.UserErrorMessages.InvalidId);
        }

        new EmailTemplates(
                carrier.get().getSendgridSenderName(),
                carrier.get().getSendgridEmailAddress(),
                carrier.get().getSendgridApikey(),
                environment,
                HelperUtils.copyFields(carrier.get(), Carrier.class),
                HelperUtils.copyFields(googleCred.get(), GoogleCred.class)
        ).sendImportBulkDataResults(
                bulkAddType,
                errors,
                totalDataCountToBeImported,
                user.get().getLoginName()
        );

        return CompletableFuture.completedFuture(new Response<>(true, SuccessMessages.Success, true));
    }
    //

    @Override
    public Response<Boolean> bulkAdd(String bulkAddType, Object object) throws Exception {
        return null;
    }

    @Async("asyncExecutor")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<Response<Boolean>> bulkAddAsync(
            String bulkAddType,
            String object,
            String auditUserId,
            String carrierId,
            String requestToken) throws Exception {

        Map<String, String> errors = new HashMap<>();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Date.class, new DateAdapter())
                .create();

        // Check if bulk add type is valid and has associated permission
        String permission = BULK_ADD_TYPES.get(bulkAddType);
        if (permission != null && !authorization.isAllowed(requestToken, auditUserId, carrierId, permission)) {
            errors.put("Permission", ErrorMessages.Unauthorized);
        }

        if (!errors.isEmpty()) {
            return sendEmailNotification(bulkAddType, errors, 0, auditUserId, carrierId);
        }

        // initalize the request context
        RequestContext.set("auditUserId", auditUserId);
        RequestContext.set("carrierId", carrierId);

        int totalDataCountToBeImported = switch (bulkAddType) {
            case "User" -> processBulkData(object, gson, new TypeToken<>() {}, userDataAccessor::createUser, errors);
            case "Lead" -> processBulkData(object, gson, new TypeToken<>() {}, leadDataAccessor::createLead, errors);
            case "Message" -> processBulkData(object, gson, new TypeToken<>() {}, messageDataAccessor::createMessage, errors);
            case "Package" -> processBulkData(object, gson, new TypeToken<>() {}, packageDataAccessor::createPackage, errors);
            case "PickupLocation" -> processBulkData(object, gson, new TypeToken<>() {}, pickupLocationDataAccessor::createPickupLocation, errors);
            case "Product" -> processBulkData(object, gson, new TypeToken<>() {}, productDataAccessor::addProduct, errors);
            case "Promo" -> processBulkData(object, gson, new TypeToken<>() {}, promoDataAccessor::createPromo, errors);
            case "PurchaseOrder" -> processBulkData(object, gson, new TypeToken<>() {}, purchaseOrderDataAccessor::createPurchaseOrder, errors);
            case "SalesOrder" -> processBulkData(object, gson, new TypeToken<>() {}, salesOrderDataAccessor::createSalesOrder, errors);
            case "Support" -> processBulkData(object, gson, new TypeToken<>() {}, supportDataAccessor::createTicket, errors);
            case "UserGroup" -> processBulkData(object, gson, new TypeToken<>() {}, userGroupDataAccessor::createUserGroup, errors);
            case "WebTemplate" -> processBulkData(object, gson, new TypeToken<>() {}, webTemplatesDataAccessor::insertWebTemplate, errors);
            default -> 0;
        };

        // Send email with the results
        return sendEmailNotification(bulkAddType, errors, totalDataCountToBeImported, auditUserId, carrierId);
    }
}