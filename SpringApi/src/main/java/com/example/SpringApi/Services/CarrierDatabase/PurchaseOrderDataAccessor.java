package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.*;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.*;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import com.itextpdf.text.DocumentException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.*;
import com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier;
import org.example.Models.RequestModels.ApiRequestModels.PurchaseOrderRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PurchaseOrderResponseModel;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPurchaseOrderSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderDataAccessor extends BaseDataAccessor implements IPurchaseOrderSubTranslator {
    private final PurchaseOrdersProductQuantityMapRepository purchaseOrdersProductQuantityMapRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AddressRepository addressRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public PurchaseOrderDataAccessor(HttpServletRequest request, CarrierRepository carrierRepository,
                                     PurchaseOrdersProductQuantityMapRepository purchaseOrdersProductQuantityMapRepository,
                                     PurchaseOrderRepository purchaseOrderRepository,
                                     AddressRepository addressRepository,
                                     LeadRepository leadRepository,
                                     UserRepository userRepository,
                                     ProductRepository productRepository,
                                     UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.purchaseOrdersProductQuantityMapRepository = purchaseOrdersProductQuantityMapRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.addressRepository = addressRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    private String formPurchaseOrderPdf(
            Carrier carrier,
            PurchaseOrder purchaseOrder,
            Address shippingAddress,
            User purchaseOrderCreatedBy,
            User purchaseOrderApprovedBy,
            Lead lead,
            List<PurchaseOrdersProductQuantityMap> purchaseOrdersProductQuantityMaps) throws IOException, TemplateException {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDirectoryForTemplateLoading(new File("/Users/nraichura/Desktop/Ultimate Company/spring-api/SpringApi/src/main/resources/templates/Invoices"));
        Template template = cfg.getTemplate("PurchaseOrder.ftl");

        Map<String, Object> templateData = new HashMap<>();

        // fill in the markers
        templateData.put("companyLogo", carrier.getImage());
        templateData.put("companyName", carrier.getName());
        templateData.put("website", carrier.getWebsite());
        templateData.put("fullAddress", carrier.getSendgridEmailAddress());
        templateData.put("purchaseOrder", purchaseOrder);
        templateData.put("shippingAddress", shippingAddress);
        templateData.put("lead", lead);
        templateData.put("purchaseOrderCreatedBy", purchaseOrderCreatedBy);
        templateData.put("purchaseOrderApprovedBy", purchaseOrderApprovedBy);
        templateData.put("purchaseOrdersProductQuantityMaps",  purchaseOrdersProductQuantityMaps);

        StringWriter out = new StringWriter();
        template.process(templateData, out);
        return out.toString();
    }

    private Pair<String, Boolean> validatePurchaseOrder(PurchaseOrder purchaseOrder, Address address, Map<Long, Integer> productIdQuantityMapping){
        // required fields
        if(productIdQuantityMapping == null || productIdQuantityMapping.isEmpty()){
            return Pair.of(ErrorMessages.PurchaseOrderErrorMessages.ER004, false);
        }

        if(purchaseOrder.getTermsConditionsHtml() == null || !StringUtils.hasText(purchaseOrder.getTermsConditionsHtml())){
            return Pair.of(ErrorMessages.PurchaseOrderErrorMessages.ER003, false);
        }

        if(purchaseOrder.getAssignedLeadId() == 0L){
            return Pair.of(ErrorMessages.PurchaseOrderErrorMessages.ER002, false);
        }

        Response<Boolean> addressValidationResponse = Validations.isValidAddress(address.getLine1(), address.getState(), address.getCity(), address.getZipCode(), address.getPhoneOnAddress(), address.getNameOnAddress());
        if(!addressValidationResponse.isSuccess()){
            return Pair.of(addressValidationResponse.getMessage(), false);
        }

        /*
        1. all quantity should be greater than 0
        2. all product id should be present in the productRepository
        */
        if(!productIdQuantityMapping.values().stream().allMatch(quantity -> quantity > 0)
                || !productIdQuantityMapping.keySet().stream().allMatch(productRepository::existsById)){
            return Pair.of(ErrorMessages.PurchaseOrderErrorMessages.ER005, false);
        }

        // optional fields
        if(purchaseOrder.getExpectedShipmentDate() != null
                && !DateHelper.isDateLessThanCurrentUTC(DateHelper.convertLocalDateTimeToDate(purchaseOrder.getExpectedShipmentDate()))) {
            return Pair.of(ErrorMessages.PurchaseOrderErrorMessages.ER001, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<PaginationBaseResponseModel<PurchaseOrderResponseModel>> getPurchaseOrdersInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // validate the column names
        if(StringUtils.hasText(paginationBaseRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(List.of("id", "address", "expectedShipmentDate",
                    "vendorNumber", "orderReceipt", "assignedLead"));

            if(!validColumns.contains(paginationBaseRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        // get the paginated data
        Page<Object[]> purchaseOrders = purchaseOrderRepository.findPaginatedPurchaseOrder(paginationBaseRequestModel.getColumnName(),
                paginationBaseRequestModel.getCondition(),
                paginationBaseRequestModel.getFilterExpr(),
                paginationBaseRequestModel.isIncludeDeleted(),
                PageRequest.of(paginationBaseRequestModel.getStart() / (paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart()),
                        paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart(),
                        Sort.by("purchaseOrderId").descending()));

        // fill in the response model
        List<PurchaseOrderResponseModel> purchaseOrderResponseModels = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        for (Object[] result : purchaseOrders.getContent()) {
            PurchaseOrder purchaseOrder = (PurchaseOrder) result[0];
            PurchaseOrderResponseModel purchaseOrderResponseModel = new PurchaseOrderResponseModel();
            purchaseOrderResponseModel.setPurchaseOrder(HelperUtils.copyFields(purchaseOrder, org.example.Models.CommunicationModels.CarrierModels.PurchaseOrder.class));
            purchaseOrderResponseModel.setLead(HelperUtils.copyFields(result[1], org.example.Models.CommunicationModels.CarrierModels.Lead.class));
            purchaseOrderResponseModel.setAddress(HelperUtils.copyFields(result[2], org.example.Models.CommunicationModels.CarrierModels.Address.class));

            if(purchaseOrder.getApprovedByUserId() != null) {
                Optional<User> approvedByUser = userRepository.findById(purchaseOrder.getApprovedByUserId());
                approvedByUser.ifPresent(user ->
                        purchaseOrderResponseModel.setApprovedByUser(HelperUtils.copyFields(user, org.example.Models.CommunicationModels.CentralModels.User.class))
                );
            }

            purchaseOrderResponseModels.add(purchaseOrderResponseModel);

            userIds.add(purchaseOrder.getCreatedByUserId());
            if(purchaseOrder.getApprovedByUserId() != null){
                userIds.add(purchaseOrder.getApprovedByUserId());
            }
        }

        // mapping between user id and the user model
        Map<Long, User> userIdUserMapping = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // fill in the user models for created by and approved by in my response model
        purchaseOrderResponseModels.forEach(purchaseOrderResponseModel -> {
            long createdByUserId = purchaseOrderResponseModel.getPurchaseOrder().getCreatedByUserId();
            Long approvedByUserId = purchaseOrderResponseModel.getPurchaseOrder().getApprovedByUserId();
            if(approvedByUserId != null && approvedByUserId != 0) {
                purchaseOrderResponseModel.setApprovedByUser(HelperUtils.copyFields(userIdUserMapping.get(approvedByUserId), org.example.Models.CommunicationModels.CentralModels.User.class));
            }
            purchaseOrderResponseModel.setCreatedByUser(HelperUtils.copyFields(userIdUserMapping.get(createdByUserId), org.example.Models.CommunicationModels.CentralModels.User.class));
        });

        PaginationBaseResponseModel<PurchaseOrderResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(purchaseOrderResponseModels);
        paginationBaseResponseModel.setTotalDataCount(purchaseOrders.getTotalElements());

        return new Response<>(true, SuccessMessages.PurchaseOrderSuccessMessages.GetPurchaseOrder, paginationBaseResponseModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> createPurchaseOrder(PurchaseOrderRequestModel purchaseOrderRequestModel) {
        purchaseOrderRequestModel.getAddress().setPhoneOnAddress(DataCleaner.cleanPhone(purchaseOrderRequestModel.getAddress().getPhoneOnAddress()));
        Pair<String, Boolean> validation = validatePurchaseOrder(HelperUtils.copyFields(purchaseOrderRequestModel.getPurchaseOrder(), PurchaseOrder.class),
                HelperUtils.copyFields(purchaseOrderRequestModel.getAddress(), Address.class),
                purchaseOrderRequestModel.getProductIdQuantityMapping());
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        Address savedAddress = addressRepository.save(HelperUtils.copyFields(purchaseOrderRequestModel.getAddress(), Address.class));
        purchaseOrderRequestModel.getPurchaseOrder().setCreatedByUserId(getUserId());
        purchaseOrderRequestModel.getPurchaseOrder().setPurchaseOrderAddressId(savedAddress.getAddressId());

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(HelperUtils.copyFields(purchaseOrderRequestModel.getPurchaseOrder(), PurchaseOrder.class));

        List<PurchaseOrdersProductQuantityMap> purchaseOrdersProductQuantityMaps = new ArrayList<>();
        for(Map.Entry<Long, Integer> productIdQuantityMap  : purchaseOrderRequestModel.getProductIdQuantityMapping().entrySet()){
            PurchaseOrdersProductQuantityMap purchaseOrdersProductQuantityMap = new PurchaseOrdersProductQuantityMap();
            purchaseOrdersProductQuantityMap.setProductId(productIdQuantityMap.getKey());
            purchaseOrdersProductQuantityMap.setQuantity(productIdQuantityMap.getValue());
            purchaseOrdersProductQuantityMap.setPurchaseOrderId(savedPurchaseOrder.getPurchaseOrderId());

            purchaseOrdersProductQuantityMaps.add(purchaseOrdersProductQuantityMap);
        }

        purchaseOrdersProductQuantityMapRepository.saveAll(purchaseOrdersProductQuantityMaps);
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PurchaseOrderSuccessMessages.InsertPurchaseOrder + " " + savedPurchaseOrder.getPurchaseOrderId(),
                ApiRoutes.PurchaseOrderSubRoute.CREATE_PURCHASE_ORDER);
        return new Response<>(true, SuccessMessages.PurchaseOrderSuccessMessages.InsertPurchaseOrder, savedPurchaseOrder.getPurchaseOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> updatePurchaseOrder(PurchaseOrderRequestModel purchaseOrderRequestModel) {
        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(purchaseOrderRequestModel.getPurchaseOrder().getPurchaseOrderId());
        if(purchaseOrder.isEmpty()){
            return new Response<>(false, ErrorMessages.PurchaseOrderErrorMessages.InvalidId, null);
        }

        Pair<String, Boolean> validation = validatePurchaseOrder(HelperUtils.copyFields(purchaseOrderRequestModel.getPurchaseOrder(), PurchaseOrder.class),
                HelperUtils.copyFields(purchaseOrderRequestModel.getAddress(), Address.class),
                purchaseOrderRequestModel.getProductIdQuantityMapping());

        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        // modify the address
        Optional<Address> address = addressRepository.findById(purchaseOrder.get().getPurchaseOrderAddressId());
        if(address.isPresent()){
            address.get().setLine1(purchaseOrderRequestModel.getAddress().getLine1());
            address.get().setLine2(purchaseOrderRequestModel.getAddress().getLine2());
            address.get().setCity(purchaseOrderRequestModel.getAddress().getCity());
            address.get().setState(purchaseOrderRequestModel.getAddress().getState());
            address.get().setZipCode(purchaseOrderRequestModel.getAddress().getZipCode());
            address.get().setNameOnAddress(purchaseOrderRequestModel.getAddress().getNameOnAddress());
            address.get().setPhoneOnAddress(purchaseOrderRequestModel.getAddress().getPhoneOnAddress());
            addressRepository.save(address.get());
        }

        // modify the quantity mapping
        purchaseOrdersProductQuantityMapRepository.deleteAll(purchaseOrdersProductQuantityMapRepository.findByPurchaseOrderId(purchaseOrder.get().getPurchaseOrderId()));
        List<PurchaseOrdersProductQuantityMap> purchaseOrdersProductQuantityMaps = new ArrayList<>();
        for(Map.Entry<Long, Integer> productIdQuantityMap  : purchaseOrderRequestModel.getProductIdQuantityMapping().entrySet()){
            PurchaseOrdersProductQuantityMap purchaseOrdersProductQuantityMap = new PurchaseOrdersProductQuantityMap();
            purchaseOrdersProductQuantityMap.setProductId(productIdQuantityMap.getKey());
            purchaseOrdersProductQuantityMap.setQuantity(productIdQuantityMap.getValue());
            purchaseOrdersProductQuantityMap.setPurchaseOrderId(purchaseOrder.get().getPurchaseOrderId());

            purchaseOrdersProductQuantityMaps.add(purchaseOrdersProductQuantityMap);
        }
        purchaseOrdersProductQuantityMapRepository.saveAll(purchaseOrdersProductQuantityMaps);

        // save the updated purchase order
        purchaseOrderRequestModel.getPurchaseOrder().setCreatedByUserId(purchaseOrder.get().getCreatedByUserId());
        purchaseOrderRequestModel.getPurchaseOrder().setPurchaseOrderAddressId(purchaseOrder.get().getPurchaseOrderAddressId());
        purchaseOrderRequestModel.getPurchaseOrder().setApprovedByUserId(null);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(HelperUtils.copyFields(purchaseOrderRequestModel.getPurchaseOrder(), PurchaseOrder.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PurchaseOrderSuccessMessages. UpdatePurchaseOrder + " " + savedPurchaseOrder.getPurchaseOrderId(),
                ApiRoutes.PurchaseOrderSubRoute.UPDATE_PURCHASE_ORDER);
        return new Response<>(true, SuccessMessages.PurchaseOrderSuccessMessages.UpdatePurchaseOrder, savedPurchaseOrder.getPurchaseOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<PurchaseOrderResponseModel> getPurchaseOrderDetailsById(long purchaseOrderId) {
        PurchaseOrderResponseModel purchaseOrderResponseModel = new PurchaseOrderResponseModel();
        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId);
        if(purchaseOrder.isPresent()){
            purchaseOrderResponseModel.setPurchaseOrder(HelperUtils.copyFields(purchaseOrder.get(), org.example.Models.CommunicationModels.CarrierModels.PurchaseOrder.class));

            Optional<Address> address = addressRepository.findById(purchaseOrder.get().getPurchaseOrderAddressId());
            if(address.isPresent()){
                purchaseOrderResponseModel.setAddress(HelperUtils.copyFields(address.get(), org.example.Models.CommunicationModels.CarrierModels.Address.class));
            }
            else {
                return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
            }

            Optional<Lead> lead = leadRepository.findById(purchaseOrder.get().getAssignedLeadId());
            if(lead.isPresent()){
                purchaseOrderResponseModel.setLead(HelperUtils.copyFields(lead.get(), org.example.Models.CommunicationModels.CarrierModels.Lead.class));
            }
            else {
                return new Response<>(false, ErrorMessages.LeadsErrorMessages.InvalidId, null);
            }

            Optional<User> createdByUser = userRepository.findById(purchaseOrder.get().getCreatedByUserId());
            if(createdByUser.isPresent()){
                purchaseOrderResponseModel.setCreatedByUser(HelperUtils.copyFields(createdByUser.get(), org.example.Models.CommunicationModels.CentralModels.User.class));
            }
            else {
                return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
            }

            if(purchaseOrder.get().getApprovedByUserId() != null) {
                Optional<User> approvedByUser = userRepository.findById(purchaseOrder.get().getApprovedByUserId());
                approvedByUser.ifPresent(user ->
                        purchaseOrderResponseModel.setApprovedByUser(HelperUtils.copyFields(user, org.example.Models.CommunicationModels.CentralModels.User.class))
                );
            }

            // get the product id and quantity mapping
            // get the product id and price mapping
            // get the product id and the discount mapping
            List<PurchaseOrdersProductQuantityMap> purchaseOrdersProductQuantityMaps = purchaseOrdersProductQuantityMapRepository.findByPurchaseOrderId(purchaseOrder.get().getPurchaseOrderId());
            Map<Long, Integer> productIdQuantityMap = new HashMap<>();
            Map<Long, Double> productIdPriceMapping = new HashMap<>();
            Map<Long, Double> productIdDiscountMapping = new HashMap<>();

            for(PurchaseOrdersProductQuantityMap purchaseOrdersProductQuantityMap : purchaseOrdersProductQuantityMaps) {
                productIdQuantityMap.putIfAbsent(purchaseOrdersProductQuantityMap.getProductId(), purchaseOrdersProductQuantityMap.getQuantity());

                Optional<Product> product = productRepository.findById(purchaseOrdersProductQuantityMap.getProductId());
                if(product.isEmpty()) {
                    return new Response<>(false, ErrorMessages.ProductErrorMessages.InvalidId, null);
                }
                productIdPriceMapping.putIfAbsent(purchaseOrdersProductQuantityMap.getProductId(), product.get().getPrice());
                productIdDiscountMapping.putIfAbsent(purchaseOrdersProductQuantityMap.getProductId(), product.get().getDiscount());
            }

            purchaseOrderResponseModel.setProductIdQuantityMapping(productIdQuantityMap);
            purchaseOrderResponseModel.setProductIdPriceMapping(productIdPriceMapping);
            purchaseOrderResponseModel.setProductIdDiscountMapping(productIdDiscountMapping);
        }
        else {
            return new Response<>(false, ErrorMessages.PurchaseOrderErrorMessages.InvalidId, null);
        }

        return new Response<>(true, SuccessMessages.PurchaseOrderSuccessMessages.GetPurchaseOrder, purchaseOrderResponseModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> togglePurchaseOrder(long purchaseOrderId) {
        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId);
        if(purchaseOrder.isEmpty()){
            return new Response<>(false, ErrorMessages.PurchaseOrderErrorMessages.InvalidId, null);
        }

        purchaseOrder.get().setDeleted(!purchaseOrder.get().isDeleted());
        purchaseOrderRepository.save(purchaseOrder.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PurchaseOrderSuccessMessages.GetPurchaseOrder + " " + purchaseOrderId,
                ApiRoutes.PurchaseOrderSubRoute.TOGGLE_PURCHASE_ORDER);
        return new Response<>(true, SuccessMessages.PurchaseOrderSuccessMessages.TogglePurchaseOrder, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> approvedByPurchaseOrder(long purchaseOrderId) {
        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId);
        if(purchaseOrder.isEmpty()){
            return new Response<>(false, ErrorMessages.PurchaseOrderErrorMessages.InvalidId, null);
        }

        // if the purchase order is already approved then we cant approve it again
        if(purchaseOrder.get().isApproved() && purchaseOrder.get().getApprovedByUserId() != null) {
            return new Response<>(false, ErrorMessages.PurchaseOrderErrorMessages.ER006, null);
        }

        // update the purchase order
        purchaseOrder.get().setApproved(true);
        purchaseOrder.get().setApprovedByUserId(getUserId());
        purchaseOrderRepository.save(purchaseOrder.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PurchaseOrderSuccessMessages.SetApprovedByPurchaseOrder + " " + purchaseOrderId,
                ApiRoutes.PurchaseOrderSubRoute.APPROVED_BY_PURCHASE_ORDER);

        return new Response<>(true, SuccessMessages.PurchaseOrderSuccessMessages.TogglePurchaseOrder, true);
    }

    @Override
    public Response<String> getPurchaseOrderPDF(long purchaseOrderId) throws TemplateException, IOException, DocumentException {
        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId);
        if(purchaseOrder.isEmpty()){
            return new Response<>(false, ErrorMessages.PurchaseOrderErrorMessages.InvalidId, null);
        }

        Optional<Address> shippingAddress = addressRepository.findById(purchaseOrder.get().getPurchaseOrderAddressId());
        if(shippingAddress.isEmpty()){
            return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
        }

        Optional<User> purchaseOrderCreatedBy = userRepository.findById(purchaseOrder.get().getCreatedByUserId());
        if(purchaseOrderCreatedBy.isEmpty()){
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
        }

        Optional<User> purchaseOrderApprovedBy = userRepository.findById(purchaseOrder.get().getApprovedByUserId());
        if(purchaseOrderApprovedBy.isEmpty()){
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
        }

        Optional<Lead> lead = leadRepository.findById(purchaseOrder.get().getAssignedLeadId());
        if(lead.isEmpty()){
            return new Response<>(false, ErrorMessages.PaymentInfoErrorMessages.InvalidId, null);
        }

        List<PurchaseOrdersProductQuantityMap> purchaseOrdersProductQuantityMaps = purchaseOrdersProductQuantityMapRepository.findByPurchaseOrderId(purchaseOrder.get().getPurchaseOrderId());

        String htmlContent = formPurchaseOrderPdf(
                getCarrierDetails(),
                purchaseOrder.get(),
                shippingAddress.get(),
                purchaseOrderCreatedBy.get(),
                purchaseOrderApprovedBy.get(),
                lead.get(),
                purchaseOrdersProductQuantityMaps);

        htmlContent = HTMLHelper.replaceBrTags(htmlContent);
        byte[] pdfBytes = PDFHelper.convertHtmlToPdf(htmlContent);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "PurchaseOrder.pdf");
        return new Response<>(true, SuccessMessages.PurchaseOrderSuccessMessages.GetPurchaseOrderPdf, Base64.getEncoder().encodeToString(pdfBytes));
    }
}