package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.*;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Package;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.HelperUtils;
import org.example.CommonHelpers.PDFHelper;
import org.example.CommonHelpers.ShippingHelper;
import org.example.CommonHelpers.Validations;
import com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import org.example.Models.Enums.SalesOrderStatus;
import org.example.Models.RequestModels.ApiRequestModels.SalesOrderRequestModel;
import org.example.Models.RequestModels.GridRequestModels.GetSalesOrdersRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PurchaseOrderResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.SalesOrderResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Models.ResponseModels.ShippingResponseModels.*;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPaymentInfoSubTranslator;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPurchaseOrderSubTranslator;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.ISalesOrderSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SalesOrderDataAccessor extends BaseDataAccessor implements ISalesOrderSubTranslator {
    private final SalesOrdersProductQuantityMapRepository salesOrdersProductQuantityMapRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PickupLocationRepository pickupLocationRepository;
    private final PaymentInfoRepository paymentInfoRepository;
    private final AddressRepository addressRepository;
    private final IPurchaseOrderSubTranslator purchaseOrderDataAccessor;
    private final UserLogDataAccessor userLogDataAccessor;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final IPaymentInfoSubTranslator paymentInfoDataAccessor;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final SalesOrderPackagingAndShipRocketMappingRepository salesOrderPackagingAndShipRocketMappingRepository;
    private final EntityManager entityManager;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public SalesOrderDataAccessor(HttpServletRequest request,
                                  CarrierRepository carrierRepository,
                                  SalesOrdersProductQuantityMapRepository salesOrdersProductQuantityMapRepository,
                                  ProductRepository productRepository,
                                  SalesOrderRepository salesOrderRepository,
                                  PaymentInfoRepository paymentInfoRepository,
                                  AddressRepository addressRepository,
                                  PurchaseOrderDataAccessor purchaseOrderDataAccessor,
                                  UserLogDataAccessor userLogDataAccessor,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  PaymentInfoDataAccessor paymentInfoDataAccessor,
                                  LeadRepository leadRepository,
                                  PackageRepository packageRepository,
                                  SalesOrderPackagingAndShipRocketMappingRepository salesOrderPackagingAndShipRocketMappingRepository,
                                  UserRepository userRepository,
                                  PickupLocationRepository pickupLocationRepository,
                                  @Qualifier("multiEntityManager") EntityManagerFactory entityManagerFactory,
                                  @Qualifier("multiTransactionManager") PlatformTransactionManager transactionManager) {
        super(request, carrierRepository);
        this.salesOrdersProductQuantityMapRepository = salesOrdersProductQuantityMapRepository;
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.paymentInfoRepository = paymentInfoRepository;
        this.addressRepository = addressRepository;
        this.purchaseOrderDataAccessor = purchaseOrderDataAccessor;
        this.userLogDataAccessor = userLogDataAccessor;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.paymentInfoDataAccessor = paymentInfoDataAccessor;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
        this.pickupLocationRepository = pickupLocationRepository;
        this.salesOrderPackagingAndShipRocketMappingRepository = salesOrderPackagingAndShipRocketMappingRepository;
        this.entityManager = entityManagerFactory.createEntityManager();
        this.transactionManager = transactionManager;
    }

    private String formSalesOrderPdf(
            Carrier carrier,
            SalesOrder salesOrder,
            PurchaseOrder purchaseOrder,
            Address billingAddress,
            Address shippingAddress,
            User salesOrderCreatedBy,
            User purchaseOrderCreatedBy,
            PaymentInfo paymentInfo,
            Lead lead,
            List<SalesOrdersProductQuantityMap> salesOrdersProductQuantityMaps) throws IOException, TemplateException {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDirectoryForTemplateLoading(new File("/Users/nraichura/Desktop/Ultimate Company/spring-api/SpringApi/src/main/resources/templates/Invoices"));
        Template template = cfg.getTemplate("SalesOrder.ftl");

        Map<String, Object> templateData = new HashMap<>();

        // fill in the markers
        templateData.put("companyLogo", carrier.getImage());
        templateData.put("companyName", carrier.getName());
        templateData.put("fullAddress", carrier.getSendgridEmailAddress());
        templateData.put("salesOrder", salesOrder);
        templateData.put("purchaseOrder", purchaseOrder);
        templateData.put("billingAddress", billingAddress);
        templateData.put("shippingAddress", shippingAddress);
        templateData.put("lead", lead);
        templateData.put("salesOrderCreatedBy", salesOrderCreatedBy);
        templateData.put("purchaseOrderCreatedBy", purchaseOrderCreatedBy);
        templateData.put("paymentInfo", paymentInfo);
        templateData.put("salesOrdersProductQuantityMaps",  salesOrdersProductQuantityMaps);

        StringWriter out = new StringWriter();
        template.process(templateData, out);
        return out.toString();
    }

    private Pair<String, Boolean> validateSalesOrder(SalesOrder salesOrder,
                                                     Address billingAddress,
                                                     Address shippingAddress,
                                                     List<SalesOrdersProductQuantityMap> salesOrdersProductQuantityMaps,
                                                     List<PackagingEstimateResponseModel> packagingEstimateResponseModels,
                                                     List<SalesOrderRequestModel.SelectedCourier> selectedCouriers) {
        // required fields
        if(salesOrder.getTermsAndConditionsHtml() == null || !StringUtils.hasText(salesOrder.getTermsAndConditionsHtml())) {
            return Pair.of(ErrorMessages.SalesOrderErrorMessages.ER003, false);
        }

        Response<Boolean> billingAddressValidation = Validations.isValidAddress(billingAddress.getLine1(), billingAddress.getState(), billingAddress.getCity(), billingAddress.getZipCode(), billingAddress.getPhoneOnAddress(), billingAddress.getNameOnAddress());
        if(!billingAddressValidation.isSuccess()) {
            return Pair.of(billingAddressValidation.getMessage(), false);
        }

        Response<Boolean> shippingAddressValidation = Validations.isValidAddress(shippingAddress.getLine1(), shippingAddress.getState(), shippingAddress.getCity(), shippingAddress.getZipCode(), shippingAddress.getPhoneOnAddress(), shippingAddress.getNameOnAddress());
        if(!shippingAddressValidation.isSuccess()) {
            return Pair.of(shippingAddressValidation.getMessage(), false);
        }

        // optional fields
        if(salesOrder.getPurchaseOrderId() == 0L || purchaseOrderRepository.findById(salesOrder.getPurchaseOrderId()).isEmpty()) {
            return Pair.of(ErrorMessages.SalesOrderErrorMessages.ER002, false);
        }

        // validate sales order quantity map
        /* at least one sales order quantity map is required */
        if(salesOrdersProductQuantityMaps == null || salesOrdersProductQuantityMaps.isEmpty()) {
            return Pair.of(ErrorMessages.SalesOrderErrorMessages.ER004, false);
        }
        /* product id is required, quantity can be 0 */
        if(!salesOrdersProductQuantityMaps.stream().allMatch(map -> productRepository.existsById(map.getProductId()))){
            return Pair.of(ErrorMessages.SalesOrderErrorMessages.ER005, false);
        }

        // packing estimate and selected courier id should not be null
        if(packagingEstimateResponseModels == null || packagingEstimateResponseModels.isEmpty()){
            return Pair.of(ErrorMessages.SalesOrderErrorMessages.ER007, false);
        }
        if(selectedCouriers== null ||
                selectedCouriers.isEmpty() ||
                selectedCouriers.size() != packagingEstimateResponseModels.size() ||
                selectedCouriers.stream().anyMatch(c -> !StringUtils.hasText(c.getAvailableCourierId()))
        ) {
            return Pair.of(ErrorMessages.SalesOrderErrorMessages.ER008, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<PaginationBaseResponseModel<SalesOrderResponseModel>> getSalesOrdersInBatches(GetSalesOrdersRequestModel getSalesOrdersRequestModel) {
        // validate the column names
        if(StringUtils.hasText(getSalesOrdersRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(List.of("id", "billingAddress", "shippingAddress",
                    "purchaseOrderCreatedBy", "approvedBy", "assignedLead"));

            if(!validColumns.contains(getSalesOrdersRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        // get paginated data
        Page<Object[]> salesOrder = salesOrderRepository.findPaginatedSalesOrder(getSalesOrdersRequestModel.getColumnName(),
                getSalesOrdersRequestModel.getCondition(),
                getSalesOrdersRequestModel.getFilterExpr(),
                getSalesOrdersRequestModel.isIncludeDeleted(),
                getSalesOrdersRequestModel.getSalesOrderStatus().stream().mapToInt(SalesOrderStatus::getCode).toArray(),
                PageRequest.of(getSalesOrdersRequestModel.getStart() / (getSalesOrdersRequestModel.getEnd() - getSalesOrdersRequestModel.getStart()),
                        getSalesOrdersRequestModel.getEnd() - getSalesOrdersRequestModel.getStart(),
                        Sort.by("salesOrderId").ascending()));

        List<SalesOrderResponseModel> salesOrderResponseModels = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();

        for (Object[] result : salesOrder.getContent()) {
            SalesOrderResponseModel salesOrderResponseModel = new SalesOrderResponseModel();

            PurchaseOrder purchaseOrder = (PurchaseOrder) result[1];
            salesOrderResponseModel.setSalesOrder(HelperUtils.copyFields(result[0], org.example.Models.CommunicationModels.CarrierModels.SalesOrder.class));
            salesOrderResponseModel.setPurchaseOrder(HelperUtils.copyFields(purchaseOrder, org.example.Models.CommunicationModels.CarrierModels.PurchaseOrder.class));
            salesOrderResponseModel.setPaymentInfo(HelperUtils.copyFields(result[2], org.example.Models.CommunicationModels.CarrierModels.PaymentInfo.class));
            salesOrderResponseModel.setBillingAddress(HelperUtils.copyFields(result[3], org.example.Models.CommunicationModels.CarrierModels.Address.class));
            salesOrderResponseModel.setShippingAddress(HelperUtils.copyFields(result[4], org.example.Models.CommunicationModels.CarrierModels.Address.class));
            salesOrderResponseModel.setLead(HelperUtils.copyFields(result[5], org.example.Models.CommunicationModels.CarrierModels.Lead.class));
            salesOrderResponseModel.setPurchaseOrderAddress(HelperUtils.copyFields(result[6], org.example.Models.CommunicationModels.CarrierModels.Address.class));
            salesOrderResponseModel.setSalesOrderPackagingMappings(HelperUtils.copyFields((List<SalesOrderPackagingAndShipRocketMapping>)result[7], org.example.Models.CommunicationModels.CarrierModels.SalesOrderPackagingAndShipRocketMapping.class));

            userIds.add(purchaseOrder.getCreatedByUserId());
            if(purchaseOrder.getApprovedByUserId() != null){
                userIds.add(purchaseOrder.getApprovedByUserId());
            }

            salesOrderResponseModels.add(salesOrderResponseModel);
        }

        // mapping between user id and the user model
        Map<Long, User> userIdUserMapping = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // fill in the user models for created by and approved by in my response model
        salesOrderResponseModels.forEach(salesOrderResponseModel -> {
            long createdByUserId = salesOrderResponseModel.getPurchaseOrder().getCreatedByUserId();
            Long approvedByUserId = salesOrderResponseModel.getPurchaseOrder().getApprovedByUserId();
            if(approvedByUserId != null) {
                salesOrderResponseModel.setPurchaseOrderApprovedBy(HelperUtils.copyFields(userIdUserMapping.get(approvedByUserId), org.example.Models.CommunicationModels.CentralModels.User.class));
            }
            salesOrderResponseModel.setPurchaseOrderCreatedBy(HelperUtils.copyFields(userIdUserMapping.get(createdByUserId), org.example.Models.CommunicationModels.CentralModels.User.class));
        });

        PaginationBaseResponseModel<SalesOrderResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(salesOrderResponseModels);
        paginationBaseResponseModel.setTotalDataCount(salesOrder.getTotalElements());

        return new Response<>(true, SuccessMessages.SalesOrderSuccessMessages.GetSalesOrder, paginationBaseResponseModel);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // we're going to handle transactions manually
    public Response<Boolean> updateSalesOrderPickupAddress(long salesOrderId, long shipRocketOrderId, long pickupLocationId) {
        Optional<SalesOrder> salesOrder = salesOrderRepository.findById(salesOrderId);
        if (salesOrder.isEmpty()) {
            return new Response<>(false, ErrorMessages.SalesOrderErrorMessages.InvalidId, false);
        }

        Optional<PickupLocation> pickupLocation = pickupLocationRepository.findById(pickupLocationId);
        if (pickupLocation.isEmpty()) {
            return new Response<>(false, ErrorMessages.PickupLocationErrorMessages.InvalidId, false);
        }
        List<SalesOrderPackagingAndShipRocketMapping> salesOrderPackagingAndShipRocketMappings = salesOrderPackagingAndShipRocketMappingRepository.findAllBySalesOrderId(salesOrder.get().getSalesOrderId());

        // get ship rocket token
        ShippingHelper shippingHelper = new ShippingHelper(getCarrierDetails().getShipRocketEmail(), getCarrierDetails().getShipRocketPassword());
        Response<String> shipRocketTokenResponse = shippingHelper.getToken();
        if (!shipRocketTokenResponse.isSuccess()) {
            return new Response<>(false, shipRocketTokenResponse.getMessage(), false);
        }

        // validate order modifiable state
        Pair<String, Boolean> validateOrderCancellationStatus = shippingHelper.checkIfOrderCanBeUpdated(shippingHelper,
                HelperUtils.copyFields(salesOrderPackagingAndShipRocketMappings, org.example.Models.CommunicationModels.CarrierModels.SalesOrderPackagingAndShipRocketMapping.class),
                shipRocketTokenResponse.getItem());
        if (!validateOrderCancellationStatus.getValue()) {
            return new Response<>(false, validateOrderCancellationStatus.getKey(), false);
        }

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        String transactionStatus = transactionTemplate.execute(status -> {

            // update the pickup location on the db
            {
                salesOrderPackagingAndShipRocketMappings.stream()
                        .filter(so -> so.getShipRocketOrderId() == shipRocketOrderId)
                        .toList()
                        .forEach(so -> so.setPickupLocationId(pickupLocationId));

                salesOrderPackagingAndShipRocketMappings.forEach(entityManager::merge);
            }

            // update the pickup location on shipRocket
            {
                Response<UpdatePickupLocationResponseModel> updatePickupLocationResponse = shippingHelper.updatePickupLocationForShipRocketOrder(shipRocketTokenResponse.getItem(),
                        Collections.singletonList(shipRocketOrderId),
                        pickupLocation.get().getShipRocketPickupLocationId());
                if(!updatePickupLocationResponse.isSuccess()) {
                    status.setRollbackOnly();
                    return updatePickupLocationResponse.getMessage();
                }
            }

            return SuccessMessages.Success;
        });

        if(Objects.equals(transactionStatus, SuccessMessages.Success)) {
            return new Response<>(true, SuccessMessages.SalesOrderSuccessMessages.UpdatedPickupLocation, true);
        }
        else {
            return new Response<>(false, transactionStatus, false);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // we're going to handle transactions manually
    public Response<Boolean> updateCustomerDeliveryAddress(long salesOrderId, org.example.Models.CommunicationModels.CarrierModels.Address addressToUpdate) {
        // validate the new address
        Response<Boolean> shippingAddressValidation = Validations.isValidAddress(addressToUpdate.getLine1(), addressToUpdate.getState(), addressToUpdate.getCity(), addressToUpdate.getZipCode(), addressToUpdate.getPhoneOnAddress(), addressToUpdate.getNameOnAddress());
        if(!shippingAddressValidation.isSuccess()) {
            return new Response<>(false, shippingAddressValidation.getMessage(), false);
        }

        Optional<SalesOrder> salesOrder = salesOrderRepository.findById(salesOrderId);
        if(salesOrder.isEmpty()) {
            return new Response<>(false, ErrorMessages.SalesOrderErrorMessages.InvalidId, false);
        }
        List<SalesOrderPackagingAndShipRocketMapping> salesOrderPackagingAndShipRocketMappings = salesOrderPackagingAndShipRocketMappingRepository.findAllBySalesOrderId(salesOrder.get().getSalesOrderId());

        // get ship rocket token
        ShippingHelper shippingHelper = new ShippingHelper(getCarrierDetails().getShipRocketEmail(), getCarrierDetails().getShipRocketPassword());
        Response<String> shipRocketTokenResponse = shippingHelper.getToken();
        if(!shipRocketTokenResponse.isSuccess()) {
            return new Response<>(false, shipRocketTokenResponse.getMessage(), false);
        }

        // validate order modifiable state
        Pair<String, Boolean> validateOrderCancellationStatus = shippingHelper.checkIfOrderCanBeUpdated(shippingHelper,
                HelperUtils.copyFields(salesOrderPackagingAndShipRocketMappings, org.example.Models.CommunicationModels.CarrierModels.SalesOrderPackagingAndShipRocketMapping.class),
                shipRocketTokenResponse.getItem());
        if(!validateOrderCancellationStatus.getValue()) {
            return new Response<>(false, validateOrderCancellationStatus.getKey(), false);
        }

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        String transactionStatus = transactionTemplate.execute(status -> {

            // update in db
            {
                Optional<Address> address = addressRepository.findById(salesOrder.get().getShippingAddressId());
                if(address.isPresent()) {
                    address.get().setLine1(addressToUpdate.getLine1());
                    address.get().setLine2(addressToUpdate.getLine2());
                    address.get().setLandmark(addressToUpdate.getLandmark());
                    address.get().setCity(addressToUpdate.getCity());
                    address.get().setState(addressToUpdate.getState());
                    address.get().setZipCode(addressToUpdate.getZipCode());
                    address.get().setNameOnAddress(addressToUpdate.getNameOnAddress());
                    address.get().setPhoneOnAddress(addressToUpdate.getPhoneOnAddress());
                    address.get().setEmailAtAddress(addressToUpdate.getEmailAtAddress());
                    entityManager.merge(address.get());
                }
            }

            // update the customer delivery address on all shipRocket orders associated with the sales order
            {
                for(SalesOrderPackagingAndShipRocketMapping salesOrderPackagingAndShipRocketMapping : salesOrderPackagingAndShipRocketMappings) {
                    Response<Boolean> updateCustomerDeliveryAddressResponse = shippingHelper.updateCustomerDeliveryAddress(
                            shipRocketTokenResponse.getItem(),
                            salesOrderPackagingAndShipRocketMapping.getShipRocketOrderId(),
                            addressToUpdate
                    );
                    if(!updateCustomerDeliveryAddressResponse.isSuccess()) {
                        status.setRollbackOnly();
                        return updateCustomerDeliveryAddressResponse.getMessage();
                    }
                }
            }

            return SuccessMessages.Success;
        });

        if(Objects.equals(transactionStatus, SuccessMessages.Success)) {
            return new Response<>(true, SuccessMessages.SalesOrderSuccessMessages.UpdatedCustomerDeliveryAddress, true);
        }
        else {
            return new Response<>(false, transactionStatus, false);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // we're going to handle transactions manually
    public Response<Boolean> cancelSalesOrder(long salesOrderId) {
        Optional<SalesOrder> salesOrder = salesOrderRepository.findById(salesOrderId);
        if(salesOrder.isEmpty()) {
            return new Response<>(false, ErrorMessages.SalesOrderErrorMessages.InvalidId, false);
        }
        List<SalesOrderPackagingAndShipRocketMapping> salesOrderPackagingAndShipRocketMappings = salesOrderPackagingAndShipRocketMappingRepository.findAllBySalesOrderId(salesOrder.get().getSalesOrderId());

        // get ship rocket token
        ShippingHelper shippingHelper = new ShippingHelper(getCarrierDetails().getShipRocketEmail(), getCarrierDetails().getShipRocketPassword());
        Response<String> shipRocketTokenResponse = shippingHelper.getToken();
        if(!shipRocketTokenResponse.isSuccess()) {
            return new Response<>(false, shipRocketTokenResponse.getMessage(), false);
        }

        // validate order modifiable state
        Pair<String, Boolean> validateOrderCancellationStatus = shippingHelper.checkIfOrderCanBeUpdated(shippingHelper,
                HelperUtils.copyFields(salesOrderPackagingAndShipRocketMappings, org.example.Models.CommunicationModels.CarrierModels.SalesOrderPackagingAndShipRocketMapping.class),
                shipRocketTokenResponse.getItem());
        if(!validateOrderCancellationStatus.getValue()) {
            return new Response<>(false, validateOrderCancellationStatus.getKey(), false);
        }

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        String transactionStatus = transactionTemplate.execute(status -> {

            // update the db
            {
                salesOrder.get().setSalesOrderStatus(SalesOrderStatus.CANCELLED.getCode());
                entityManager.merge(salesOrder.get());
            }

            // call the shipRocket api and update the shipRocket order
            {
                Response<CancelShipmentResponseModel> cancelShipmentResponseModelResponse = shippingHelper.cancelShipment(
                        shipRocketTokenResponse.getItem(),
                        salesOrderPackagingAndShipRocketMappings.stream()
                                .map(SalesOrderPackagingAndShipRocketMapping::getShipRocketGeneratedAWB)
                                .collect(Collectors.toList())
                );

                if(!cancelShipmentResponseModelResponse.isSuccess()) {
                    status.setRollbackOnly();
                    return cancelShipmentResponseModelResponse.getMessage();
                }
            }

            return SuccessMessages.Success;
        });

        if(Objects.equals(transactionStatus, SuccessMessages.Success)) {
            return new Response<>(true, SuccessMessages.SalesOrderSuccessMessages.CancelOrder, true);
        }
        else {
            return new Response<>(false, transactionStatus, false);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // we're going to handle transactions manually
    public Response<Long> createSalesOrder(SalesOrderRequestModel salesOrderRequestModel) throws RuntimeException {
        /*
        * Create Sales Order Algorithm Steps:
        * 1. Validate our sales order data
        * 2. Save the billing and shipper address for sales order
        * 3. Save the payment info for sales order
        * 4. Save the sales order
        * 5. Save the Sales order product id quantity mapping
        * 6. Update the product quantity and the package quantity used
        * 7. Update the purchase order by setting the sales order id FK
        * 8. Generate different shiprocket sales orders for different pickup locations
        * 9. Generate awbs for each shiprocket sales order
        * 10. Save all the shiprocket data, productids, packageid, salesorderid for the salesorder.
        * */

        Pair<String, Boolean> validateSalesOrder = validateSalesOrder(HelperUtils.copyFields(salesOrderRequestModel.getSalesOrder(), SalesOrder.class),
                HelperUtils.copyFields(salesOrderRequestModel.getBillingAddress(), Address.class),
                HelperUtils.copyFields(salesOrderRequestModel.getBillingAddress(), Address.class),
                salesOrderRequestModel.getSalesOrdersProductQuantityMaps()
                        .stream()
                        .map(salesOrdersProductQuantityMap -> HelperUtils.copyFields(salesOrdersProductQuantityMap, SalesOrdersProductQuantityMap.class))
                        .collect(Collectors.toList()),
                salesOrderRequestModel.getPackagingEstimateResponseModels(),
                salesOrderRequestModel.getSelectedCouriers());

        if(!validateSalesOrder.getValue()) {
            return new Response<>(false, validateSalesOrder.getKey(), null);
        }

        // initialize data
        ShippingHelper shippingHelper = new ShippingHelper(getCarrierDetails().getShipRocketEmail(), getCarrierDetails().getShipRocketPassword());
        List<Object[]> productPickupLocationAddressModels = productRepository.getProductPickupLocationAddressModels(salesOrderRequestModel
                .getSalesOrdersProductQuantityMaps()
                .stream()
                .map(org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap::getProductId)  // This line must correctly reference the getProductId method
                .toList());
        Map<Long, org.example.Models.CommunicationModels.CarrierModels.PickupLocation> productIdPickupLocationMapping = productPickupLocationAddressModels.stream()
                .collect(Collectors.toMap(
                        result -> ((Product) result[0]).getProductId(),
                        result -> HelperUtils.copyFields((PickupLocation) result[1], org.example.Models.CommunicationModels.CarrierModels.PickupLocation.class)
                ));
        Map<Long, org.example.Models.CommunicationModels.CarrierModels.Product> productIdProductMapping = productPickupLocationAddressModels.stream()
                .collect(Collectors.toMap(
                        result -> ((Product) result[0]).getProductId(),
                        result -> HelperUtils.copyFields((Product) result[0], org.example.Models.CommunicationModels.CarrierModels.Product.class)
                ));
        List<Package> packages = packageRepository.findByDeleted(false);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        String transactionStatus = Objects.requireNonNull(transactionTemplate.execute(status -> {
            /* ***** ALGORITHM START ***** */
            // save the addresses
            {
                Address savedBillingAddress = HelperUtils.copyFields(salesOrderRequestModel.getBillingAddress(), Address.class);
                Address savedShippingAddress = HelperUtils.copyFields(salesOrderRequestModel.getShippingAddress(), Address.class);

                entityManager.persist(savedBillingAddress);
                entityManager.persist(savedShippingAddress);

                salesOrderRequestModel.getSalesOrder().setBillingAddressId(savedBillingAddress.getAddressId());
                salesOrderRequestModel.getSalesOrder().setShippingAddressId(savedShippingAddress.getAddressId());
            }

            // save the payment info
            {
                Response<Long> paymentInfoResponse = paymentInfoDataAccessor.insertPaymentInfo(salesOrderRequestModel.getPaymentInfo());
                if (!paymentInfoResponse.isSuccess()) {
                    return paymentInfoResponse;
                }
                salesOrderRequestModel.getSalesOrder().setPaymentId(paymentInfoResponse.getItem());
            }

            // save the sales order
            SalesOrder savedSalesOrder = HelperUtils.copyFields(salesOrderRequestModel.getSalesOrder(), SalesOrder.class);
            {
                salesOrderRequestModel.getSalesOrder().setSalesOrderStatus(SalesOrderStatus.ORDER_RECEIVED.getCode());
                salesOrderRequestModel.getSalesOrder().setCreatedByUserId(getUserId());

                entityManager.persist(savedSalesOrder);
            }

            // save the sales order product id quantity mapping
            {
                salesOrderRequestModel.getSalesOrdersProductQuantityMaps().forEach(map -> map.setSalesOrderId(savedSalesOrder.getSalesOrderId()));
                List<SalesOrdersProductQuantityMap> salesOrdersProductQuantityMaps = salesOrderRequestModel.getSalesOrdersProductQuantityMaps()
                        .stream()
                        .map(salesOrdersProductQuantityMap -> HelperUtils.copyFields(salesOrdersProductQuantityMap, SalesOrdersProductQuantityMap.class))
                        .toList();

                salesOrdersProductQuantityMaps.forEach(entityManager::persist);
            }

            // update the products and packages used for the sales order
            {
                List<Product> productsToUpdate = new ArrayList<>();
                for (org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap salesOrdersProductQuantityMap : salesOrderRequestModel.getSalesOrdersProductQuantityMaps()) {
                    Optional<Product> product = productRepository.findById(salesOrdersProductQuantityMap.getProductId());
                    if (product.isPresent()) {
                        Product localProduct = product.get();
                        localProduct.setAvailableStock(product.get().getAvailableStock() - salesOrdersProductQuantityMap.getQuantity());
                        productsToUpdate.add(localProduct);
                    }
                }
                productsToUpdate.forEach(entityManager::merge);

                List<Package> packagesToUpdate = new ArrayList<>();
                for (PackagingEstimateResponseModel packagingEstimateResponseModel : salesOrderRequestModel.getPackagingEstimateResponseModels()) {
                    Optional<Package> _package = packageRepository.findById(packagingEstimateResponseModel.getPackageId());
                    if (_package.isPresent()) {
                        Package localPackage = _package.get();
                        localPackage.setQuantity(_package.get().getQuantity() - 1);
                        packagesToUpdate.add(localPackage);
                    }
                }
                packagesToUpdate.forEach(entityManager::merge);
            }

            // update the purchase order set the purchase order id
            {
                Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(salesOrderRequestModel.getSalesOrder().getPurchaseOrderId());
                purchaseOrder.ifPresent(existingPO -> {
                    existingPO.setSalesOrderId(savedSalesOrder.getSalesOrderId());
                    entityManager.merge(existingPO);
                });
            }

            /* ************ SHIPROCKET STUFF ************** */
            // STEP 1 ======  get the shiprocket token
            Response<String> shipRocketTokenResponse = shippingHelper.getToken();
            if (!shipRocketTokenResponse.isSuccess()) {
                status.setRollbackOnly();
                return shipRocketTokenResponse.getMessage();
            }

            // STEP 2 ====== create the shiprocket order
            Response<List<OrderEstimateResponseModel>> getOrderEstimatesFromSalesOrderRequestResponse = shippingHelper.getOrderEstimatesFromSalesOrderRequest(salesOrderRequestModel,
                    productIdPickupLocationMapping,
                    productIdProductMapping,
                    HelperUtils.copyFields(packages.stream().filter(p -> p.getQuantity() > 0).toList(), org.example.Models.CommunicationModels.CarrierModels.Package.class),
                    salesOrderRequestModel.getPackagingEstimateResponseModels());

            if (!getOrderEstimatesFromSalesOrderRequestResponse.isSuccess()) {
                status.setRollbackOnly();
                return getOrderEstimatesFromSalesOrderRequestResponse.getMessage();
            }
            Response<List<PlaceOrderResponseModel>> createShippingForSalesOrderOrderResponse = shippingHelper.createShippingForSalesOrderOrder(shipRocketTokenResponse.getItem(), savedSalesOrder.getSalesOrderId(), getOrderEstimatesFromSalesOrderRequestResponse.getItem());
            if (!createShippingForSalesOrderOrderResponse.isSuccess()) {
                status.setRollbackOnly();
                return createShippingForSalesOrderOrderResponse.getMessage();
            }

            // STEP 3 ====== generate the awb
            Map<Long, String> shipmentIdCourierIdMapping = createShippingForSalesOrderOrderResponse.getItem().stream()
                    .collect(Collectors.toMap(
                            PlaceOrderResponseModel::getShipment_id,
                            value -> salesOrderRequestModel.getSelectedCouriers()
                                    .stream()
                                    .filter(c -> c.getPickupLocationId() == value.getPickupLocationId())
                                    .findFirst()
                                    .map(SalesOrderRequestModel.SelectedCourier::getAvailableCourierId)
                                    .orElse("")
                    ));
            Response<List<GenerateAWBResponseModel>> generateAWBForShipmentResponse = shippingHelper.generateAWBForShipment(shipRocketTokenResponse.getItem(),
                    createShippingForSalesOrderOrderResponse.getItem().stream().map(PlaceOrderResponseModel::getShipment_id).toList(),
                    shipmentIdCourierIdMapping,
                    savedSalesOrder.getSalesOrderId());
            if (!generateAWBForShipmentResponse.isSuccess()) {
                status.setRollbackOnly();
                return generateAWBForShipmentResponse.getMessage();
            }

            //STEP 4 ====== Request for shipment pickup on shiprocket
            Map<Long, Date> shipmentIdPickupDateMapping = createShippingForSalesOrderOrderResponse.getItem().stream()
                    .collect(Collectors.toMap(
                            PlaceOrderResponseModel::getShipment_id,
                            value -> salesOrderRequestModel.getSelectedCouriers()
                                    .stream()
                                    .filter(c -> c.getPickupLocationId() == value.getPickupLocationId())
                                    .findFirst()
                                    .map(SalesOrderRequestModel.SelectedCourier::getShipmentPickupDate)
                                    .orElse(new Date())
                    ));

            Response<List<ShipmentPickupResponseModel>> requestForShipmentPickupResponse = shippingHelper.requestForShipmentPickupRequest(shipRocketTokenResponse.getItem(),
                    createShippingForSalesOrderOrderResponse.getItem().stream().map(PlaceOrderResponseModel::getShipment_id).toList(),
                    shipmentIdPickupDateMapping,
                    savedSalesOrder.getSalesOrderId());
            if (!requestForShipmentPickupResponse.isSuccess()) {
                throw new RuntimeException(requestForShipmentPickupResponse.getMessage());
            }

            // STEP 5 ====== Generate Manifest for the shipments
            Response<GenerateManifestResponseModel> generateManifestResponseModelResponse = shippingHelper.generateManifest(shipRocketTokenResponse.getItem(),
                    createShippingForSalesOrderOrderResponse.getItem().stream().map(PlaceOrderResponseModel::getShipment_id).toList());
            if(!generateManifestResponseModelResponse.isSuccess()) {
                status.setRollbackOnly();
                return generateManifestResponseModelResponse.getMessage();
            }

            // STEP 6 ====== Generate Print Version of Manifest for all the shipments
            Response<PrintManifestResponseModel> generatePrintManifestResponseModelResponse = shippingHelper.generatePrintManifest(shipRocketTokenResponse.getItem(),
                    createShippingForSalesOrderOrderResponse.getItem().stream().map(PlaceOrderResponseModel::getOrder_id).toList());
            if(!generatePrintManifestResponseModelResponse.isSuccess()) {
                status.setRollbackOnly();
                return generatePrintManifestResponseModelResponse.getMessage();
            }

            // STEP 7 ====== Generate label for all the shipments
            Response<GenerateLabelResponseModel> generateLabelResponseModelResponse = shippingHelper.generateLabel(shipRocketTokenResponse.getItem(),
                    createShippingForSalesOrderOrderResponse.getItem().stream().map(PlaceOrderResponseModel::getShipment_id).toList());
            if(!generateLabelResponseModelResponse.isSuccess()) {
                status.setRollbackOnly();
                return generateLabelResponseModelResponse.getMessage();
            }

            // STEP 8 ====== Generate Invoice for all the shipments
            Response<GenerateInvoiceResponseModel> generateInvoiceResponseModelResponse = shippingHelper.generateInvoice(shipRocketTokenResponse.getItem(),
                    createShippingForSalesOrderOrderResponse.getItem().stream().map(PlaceOrderResponseModel::getOrder_id).toList());
            if(!generateInvoiceResponseModelResponse.isSuccess()) {
                status.setRollbackOnly();
                return generateInvoiceResponseModelResponse.getMessage();
            }

            // STEP 9 ====== save SalesOrderPackagingMapping to db.
            List<SalesOrderPackagingAndShipRocketMapping> salesOrderPackagingAndShipRocketMappings = new ArrayList<>();
            for (OrderEstimateResponseModel orderEstimateResponseModel : getOrderEstimatesFromSalesOrderRequestResponse.getItem()) {
                SalesOrderPackagingAndShipRocketMapping salesOrderPackagingAndShipRocketMapping = new SalesOrderPackagingAndShipRocketMapping();
                salesOrderPackagingAndShipRocketMapping.setProductIds(String.join(", ", orderEstimateResponseModel.getProducts().stream()
                        .map(product -> Long.toString(product.getProductId()))
                        .toList()));
                salesOrderPackagingAndShipRocketMapping.setSalesOrderId(orderEstimateResponseModel.getSystemOrderId());
                salesOrderPackagingAndShipRocketMapping.setPackageId(orderEstimateResponseModel.get_package().getPackageId());

                // save the shiprocket shipment id
                Optional<PlaceOrderResponseModel> placeOrderResponseModel = createShippingForSalesOrderOrderResponse.getItem().stream().filter(o -> o.getSystemOrderId() == orderEstimateResponseModel.getSystemOrderId()).findFirst();
                placeOrderResponseModel.ifPresent(orderResponseModel -> salesOrderPackagingAndShipRocketMapping.setShipRocketShipmentId(orderResponseModel.getShipment_id()));

                // save the shiprocket awb
                Optional<GenerateAWBResponseModel> generateAWBResponseModel = generateAWBForShipmentResponse.getItem().stream().filter(o -> o.getSystemOrderId() == orderEstimateResponseModel.getSystemOrderId()).findFirst();
                generateAWBResponseModel.ifPresent(awbResponseModel -> salesOrderPackagingAndShipRocketMapping.setShipRocketGeneratedAWB(awbResponseModel.getResponse().getData().getAwb_code()));

                // save the pickup request token number
                Optional<ShipmentPickupResponseModel> shipmentPickupResponseModel = requestForShipmentPickupResponse.getItem().stream().filter(o -> o.getSystemOrderId() == orderEstimateResponseModel.getSystemOrderId()).findFirst();
                shipmentPickupResponseModel.ifPresent(pickupResponseModel -> salesOrderPackagingAndShipRocketMapping.setShipRocketPickupTokenNumber(pickupResponseModel.getResponse().getPickupTokenNumber()));

                salesOrderPackagingAndShipRocketMappings.add(salesOrderPackagingAndShipRocketMapping);
            }

            salesOrderPackagingAndShipRocketMappings.forEach(entityManager::merge);

            userLogDataAccessor.logData(getUserId(),
                    SuccessMessages.SalesOrderSuccessMessages.InsertSalesOrder + " " + savedSalesOrder.getSalesOrderId(),
                    ApiRoutes.SalesOrderSubRoute.CREATE_SALES_ORDER);

            return SuccessMessages.Success;
        })).toString();

        if(Objects.equals(transactionStatus, SuccessMessages.Success)) {
            return new Response<>(true, SuccessMessages.SalesOrderSuccessMessages.InsertSalesOrder, 1L);
        }
        else {
            return new Response<>(false, transactionStatus, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> updateSalesOrder(SalesOrderRequestModel salesOrderRequestModel) {
        Optional<SalesOrder> salesOrder = salesOrderRepository.findById(salesOrderRequestModel.getSalesOrder().getSalesOrderId());
        if(salesOrder.isEmpty()){
            return new Response<>(false, ErrorMessages.SalesOrderErrorMessages.InvalidId, null);
        }

        Pair<String, Boolean> validateSalesOrder = validateSalesOrder(HelperUtils.copyFields(salesOrderRequestModel.getSalesOrder(), SalesOrder.class),
                HelperUtils.copyFields(salesOrderRequestModel.getBillingAddress(), Address.class),
                HelperUtils.copyFields(salesOrderRequestModel.getBillingAddress(), Address.class),
                salesOrderRequestModel.getSalesOrdersProductQuantityMaps()
                        .stream()
                        .map(salesOrdersProductQuantityMap -> HelperUtils.copyFields(salesOrdersProductQuantityMap, SalesOrdersProductQuantityMap.class))
                        .collect(Collectors.toList()),
                salesOrderRequestModel.getPackagingEstimateResponseModels(),
                salesOrderRequestModel.getSelectedCouriers());

        if(!validateSalesOrder.getValue()) {
            return new Response<>(false, validateSalesOrder.getKey(), null);
        }

        // update the payment info
        {
            Optional<PaymentInfo> paymentInfo = paymentInfoRepository.findById(salesOrder.get().getPaymentId());
            if(paymentInfo.isPresent()) {
                Response<Long> updatePaymentInfoResponse = paymentInfoDataAccessor.updatePaymentInfo(salesOrderRequestModel.getPaymentInfo());
                if(!updatePaymentInfoResponse.isSuccess()){
                    return updatePaymentInfoResponse;
                }
            }
            else {
                return new Response<>(false, ErrorMessages.PaymentInfoErrorMessages.InvalidId, null);
            }
            salesOrderRequestModel.getSalesOrder().setPaymentId(paymentInfo.get().getPaymentId());
        }

        // update the billing addresses
        {
            Optional<Address> billingAddress = addressRepository.findById(salesOrder.get().getBillingAddressId());
            if(billingAddress.isPresent()){
                billingAddress.get().setLine1(salesOrderRequestModel.getBillingAddress().getLine1());
                billingAddress.get().setLine2(salesOrderRequestModel.getBillingAddress().getLine2());
                billingAddress.get().setCity(salesOrderRequestModel.getBillingAddress().getCity());
                billingAddress.get().setState(salesOrderRequestModel.getBillingAddress().getState());
                billingAddress.get().setZipCode(salesOrderRequestModel.getBillingAddress().getZipCode());
            }
            else {
                return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
            }
            salesOrderRequestModel.getSalesOrder().setBillingAddressId(billingAddress.get().getAddressId());
        }

        // update the shipping address
        {
            Optional<Address> shippingAddress = addressRepository.findById(salesOrder.get().getShippingAddressId());
            if(shippingAddress.isPresent()){
                shippingAddress.get().setLine1(salesOrderRequestModel.getShippingAddress().getLine1());
                shippingAddress.get().setLine2(salesOrderRequestModel.getShippingAddress().getLine2());
                shippingAddress.get().setCity(salesOrderRequestModel.getShippingAddress().getCity());
                shippingAddress.get().setState(salesOrderRequestModel.getShippingAddress().getState());
                shippingAddress.get().setZipCode(salesOrderRequestModel.getShippingAddress().getZipCode());
            }
            else {
                return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
            }
            salesOrderRequestModel.getSalesOrder().setShippingAddressId(shippingAddress.get().getAddressId());
        }


        // update the sales order product - quantity mappings
        List<SalesOrdersProductQuantityMap> salesOrdersProductQuantityMaps = salesOrdersProductQuantityMapRepository.findBySalesOrderId(salesOrder.get().getSalesOrderId());
        salesOrdersProductQuantityMapRepository.deleteAll(salesOrdersProductQuantityMaps);
        salesOrderRequestModel.getSalesOrdersProductQuantityMaps().forEach(map -> map.setSalesOrderId(salesOrder.get().getSalesOrderId()));
        salesOrdersProductQuantityMapRepository.saveAll(salesOrderRequestModel.getSalesOrdersProductQuantityMaps()
                .stream()
                .map(salesOrdersProductQuantityMap -> HelperUtils.copyFields(salesOrdersProductQuantityMap, SalesOrdersProductQuantityMap.class))
                .collect(Collectors.toList()));

        // update the sales order
        SalesOrder savedSalesOrder = salesOrderRepository.save(HelperUtils.copyFields(salesOrderRequestModel.getSalesOrder(), SalesOrder.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.SalesOrderSuccessMessages.UpdateSalesOrder + " " + savedSalesOrder.getSalesOrderId(),
                ApiRoutes.SalesOrderSubRoute.UPDATE_SALES_ORDER);

        return new Response<>(true, SuccessMessages.SalesOrderSuccessMessages.UpdateSalesOrder, savedSalesOrder.getSalesOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<SalesOrderResponseModel> getSalesOrderDetailsById(long salesOrderId) {
        SalesOrderResponseModel salesOrderResponseModel = new SalesOrderResponseModel();
        Optional<SalesOrder> salesOrder = salesOrderRepository.findById(salesOrderId);
        if(salesOrder.isPresent()){
            salesOrderResponseModel.setSalesOrder(HelperUtils.copyFields(salesOrder.get(), org.example.Models.CommunicationModels.CarrierModels.SalesOrder.class));

            Optional<PaymentInfo> paymentInfo = paymentInfoRepository.findById(salesOrder.get().getPaymentId());
            if(paymentInfo.isPresent()){
                salesOrderResponseModel.setPaymentInfo(HelperUtils.copyFields(paymentInfo.get(), org.example.Models.CommunicationModels.CarrierModels.PaymentInfo.class));
            }
            else {
                return new Response<>(false, ErrorMessages.PaymentInfoErrorMessages.InvalidId, null);

            }

            Optional<Address> billingAddress = addressRepository.findById(salesOrder.get().getBillingAddressId());
            if(billingAddress.isPresent()){
                salesOrderResponseModel.setBillingAddress(HelperUtils.copyFields(billingAddress.get(), org.example.Models.CommunicationModels.CarrierModels.Address.class));
            }
            else{
                return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
            }

            Optional<Address> shippingAddress = addressRepository.findById(salesOrder.get().getShippingAddressId());
            if(shippingAddress.isPresent()){
                salesOrderResponseModel.setShippingAddress(HelperUtils.copyFields(shippingAddress.get(), org.example.Models.CommunicationModels.CarrierModels.Address.class));
            }
            else{
                return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
            }

            Response<PurchaseOrderResponseModel> getPurchaseOrderByIdResponse = purchaseOrderDataAccessor.getPurchaseOrderDetailsById(salesOrder.get().getPurchaseOrderId());
            if(getPurchaseOrderByIdResponse.isSuccess()){
                salesOrderResponseModel.setPurchaseOrder(getPurchaseOrderByIdResponse.getItem().getPurchaseOrder());
                salesOrderResponseModel.setPurchaseOrderAddress(getPurchaseOrderByIdResponse.getItem().getAddress());
                salesOrderResponseModel.setLead(getPurchaseOrderByIdResponse.getItem().getLead());
                salesOrderResponseModel.setPurchaseOrderCreatedBy(getPurchaseOrderByIdResponse.getItem().getCreatedByUser());
                salesOrderResponseModel.setPurchaseOrderApprovedBy(getPurchaseOrderByIdResponse.getItem().getApprovedByUser());
            }
            else {
                return new Response<>(false, getPurchaseOrderByIdResponse.getMessage(), null);
            }
        }
        else {
            return new Response<>(false, ErrorMessages.SalesOrderErrorMessages.InvalidId, null);
        }

        return new Response<>(true, SuccessMessages.SalesOrderSuccessMessages.GetSalesOrder, salesOrderResponseModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> toggleSalesOrder(long salesOrderId) {
        Optional<SalesOrder> salesOrder = salesOrderRepository.findById(salesOrderId);
        if(salesOrder.isEmpty()){
            return new Response<>(false, ErrorMessages.SalesOrderErrorMessages.InvalidId, null);
        }

        salesOrder.get().setDeleted(!salesOrder.get().isDeleted());
        salesOrderRepository.save(salesOrder.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.SalesOrderSuccessMessages.GetSalesOrder + " " + salesOrderId,
                ApiRoutes.SalesOrderSubRoute.TOGGLE_SALES_ORDER);
        return new Response<>(true, SuccessMessages.SalesOrderSuccessMessages.ToggleSalesOrder, true);
    }

    @Override
    public Response<byte[]> getSalesOrderPDF(long salesOrderId) throws TemplateException, IOException, DocumentException {
        Optional<SalesOrder> salesOrder = salesOrderRepository.findById(salesOrderId);
        if(salesOrder.isEmpty()){
            return new Response<>(false, ErrorMessages.SalesOrderErrorMessages.InvalidId, null);
        }

        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(salesOrder.get().getPurchaseOrderId());
        if(purchaseOrder.isEmpty()){
            return new Response<>(false, ErrorMessages.PurchaseOrderErrorMessages.InvalidId, null);
        }

        Optional<Address> billingAddress = addressRepository.findById(salesOrder.get().getBillingAddressId());
        if(billingAddress.isEmpty()){
            return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
        }

        Optional<Address> shippingAddress = addressRepository.findById(salesOrder.get().getShippingAddressId());
        if(shippingAddress.isEmpty()){
            return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
        }

        Optional<User> salesOrderCreatedBy = userRepository.findById(salesOrder.get().getCreatedByUserId());
        if(salesOrderCreatedBy.isEmpty()){
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
        }

        Optional<User> purchaseOrderCreatedBy = userRepository.findById(purchaseOrder.get().getCreatedByUserId());
        if(purchaseOrderCreatedBy.isEmpty()){
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
        }

        Optional<PaymentInfo> paymentInfo = paymentInfoRepository.findById(salesOrder.get().getPaymentId());
        if(paymentInfo.isEmpty()){
            return new Response<>(false, ErrorMessages.PaymentInfoErrorMessages.InvalidId, null);
        }

        Optional<Lead> lead = leadRepository.findById(purchaseOrder.get().getAssignedLeadId());
        if(lead.isEmpty()){
            return new Response<>(false, ErrorMessages.PaymentInfoErrorMessages.InvalidId, null);
        }

        List<SalesOrdersProductQuantityMap> salesOrdersProductQuantityMaps = salesOrdersProductQuantityMapRepository.findBySalesOrderId(salesOrder.get().getSalesOrderId());

        String htmlContent = formSalesOrderPdf(
                getCarrierDetails(),
                salesOrder.get(),
                purchaseOrder.get(),
                billingAddress.get(),
                shippingAddress.get(),
                salesOrderCreatedBy.get(),
                purchaseOrderCreatedBy.get(),
                paymentInfo.get(),
                lead.get(),
                salesOrdersProductQuantityMaps);

        byte[] pdfBytes = PDFHelper.convertHtmlToPdf(htmlContent);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "SalesOrder.pdf");
        return new Response<>(true, SuccessMessages.SalesOrderSuccessMessages.GetSalesOrderPdf, pdfBytes);
    }
}