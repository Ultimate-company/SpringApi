package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.AddressRepository;
import com.example.SpringApi.Repository.CarrierDatabase.PickupLocationRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Address;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.PickupLocation;
import org.example.CommonHelpers.DataCleaner;
import org.example.CommonHelpers.HelperUtils;
import org.example.CommonHelpers.ShippingHelper;
import org.example.CommonHelpers.Validations;
import org.example.Models.RequestModels.ApiRequestModels.PickupLocationRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PickupLocationResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Models.ResponseModels.ShippingResponseModels.AddPickupLocationResponseModel;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPickupLocationSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class PickupLocationDataAccessor extends BaseDataAccessor implements IPickupLocationSubTranslator {

    private final PickupLocationRepository pickupLocationRepository;
    private final AddressRepository addressRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public PickupLocationDataAccessor(HttpServletRequest request,
                                      CarrierRepository carrierRepository,
                                      PickupLocationRepository pickupLocationRepository,
                                      AddressRepository addressRepository,
                                      UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.pickupLocationRepository = pickupLocationRepository;
        this.addressRepository = addressRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    public Pair<String, Boolean> validatePickupLocation(PickupLocation pickupLocation, Address address) {
        /*
        * Required fields -> address nick name,
        * */
        if(!Validations.isValidName(pickupLocation.getAddressNickName())){
            return Pair.of(ErrorMessages.PickupLocationErrorMessages.ER001, false);
        }

        // check mandatory address fields
        Response<Boolean> addressValidationResponse = Validations.isValidAddress(address.getLine1(), address.getState(), address.getCity(), address.getZipCode(), address.getPhoneOnAddress(), address.getNameOnAddress());
        if(!addressValidationResponse.isSuccess()){
            return Pair.of(ErrorMessages.InvalidAddress, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<PaginationBaseResponseModel<PickupLocationResponseModel>> getPickupLocationsInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // validate the column names
        if(StringUtils.hasText(paginationBaseRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(Arrays.asList("locationName", "address", "nameOnAddress", "phoneOnAddress", "emailAtAddress" ));

            if(!validColumns.contains(paginationBaseRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        Page<Object[]> pickupLocations = pickupLocationRepository.findPaginatedPickupLocations(paginationBaseRequestModel.getColumnName(),
                paginationBaseRequestModel.getCondition(),
                paginationBaseRequestModel.getFilterExpr(),
                paginationBaseRequestModel.isIncludeDeleted(),
                PageRequest.of(paginationBaseRequestModel.getStart() / (paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart()),
                        paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart(),
                        Sort.by("pickupLocationId").descending()));

        List<PickupLocationResponseModel> pickupLocationResponseModels = new ArrayList<>();
        for (Object[] result : pickupLocations.getContent()) {
            PickupLocationResponseModel responseModel = new PickupLocationResponseModel();
            responseModel.setPickupLocation(HelperUtils.copyFields(result[0], org.example.Models.CommunicationModels.CarrierModels.PickupLocation.class));
            responseModel.setAddress(HelperUtils.copyFields(result[1], org.example.Models.CommunicationModels.CarrierModels.Address.class));

            pickupLocationResponseModels.add(responseModel);
        }

        PaginationBaseResponseModel<PickupLocationResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(pickupLocationResponseModels);
        paginationBaseResponseModel.setTotalDataCount(pickupLocations.getTotalElements());

        return new Response<>(true, SuccessMessages.PickupLocationSuccessMessages.GetPickupLocation, paginationBaseResponseModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> createPickupLocation(PickupLocationRequestModel pickupLocationRequestModel) throws Exception {
        PickupLocation existingPickupLocation = pickupLocationRepository.findByAddressNickName(pickupLocationRequestModel.getPickupLocation().getAddressNickName());
        if(existingPickupLocation != null){
            return new Response<>(false, ErrorMessages.PickupLocationErrorMessages.DuplicateName, null);
        }

        // validate the pickup location request model
        PickupLocation pickupLocation = HelperUtils.copyFields(pickupLocationRequestModel.getPickupLocation(), PickupLocation.class);
        Address address = HelperUtils.copyFields(pickupLocationRequestModel.getAddress(), Address.class);
        Pair<String, Boolean> validation = validatePickupLocation(pickupLocation, address);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        // clean the data
        address.setPhoneOnAddress(DataCleaner.cleanPhone(address.getPhoneOnAddress()));
        pickupLocationRequestModel.getAddress().setPhoneOnAddress(address.getPhoneOnAddress());

        // add the pickup location in shiprocket
        ShippingHelper shippingHelper = new ShippingHelper(getCarrierDetails().getShipRocketEmail(), getCarrierDetails().getShipRocketPassword());
        Response<String> shipRocketTokenResponse = shippingHelper.getToken();
        if(!shipRocketTokenResponse.isSuccess()) {
            throw new Exception(shipRocketTokenResponse.getMessage());
        }
        Response<AddPickupLocationResponseModel> addPickupLocationResponseModelResponse = shippingHelper.addPickupLocation(shipRocketTokenResponse.getItem(), pickupLocationRequestModel);
        if(!addPickupLocationResponseModelResponse.isSuccess()){
            throw new Exception(addPickupLocationResponseModelResponse.getMessage());
        }

        // add the data in the db
        Address savedAddress = addressRepository.save(address);
        pickupLocation.setPickupLocationAddressId(savedAddress.getAddressId());
        pickupLocation.setShipRocketPickupLocationId(addPickupLocationResponseModelResponse.getItem().getPickup_id());
        PickupLocation newPickupLocation = pickupLocationRepository.save(pickupLocation);
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PickupLocationSuccessMessages.InsertPickupLocation + " " + newPickupLocation.getPickupLocationId(),
                ApiRoutes.PickupLocationsSubRoute.CREATE_PICKUP_LOCATION);

        return new Response<>(true, SuccessMessages.PickupLocationSuccessMessages.InsertPickupLocation, newPickupLocation.getPickupLocationId());
    }

    @Override
    public Response<List<org.example.Models.CommunicationModels.CarrierModels.PickupLocation>> getAllPickupLocations(boolean includeDeleted) {
        if(includeDeleted) {
            return new Response<>(true,
                    SuccessMessages.PickupLocationSuccessMessages.GetPickupLocation,
                    HelperUtils.copyFields(pickupLocationRepository.findAll(), org.example.Models.CommunicationModels.CarrierModels.PickupLocation.class));
        }
        else{
            return new Response<>(true,
                    SuccessMessages.PickupLocationSuccessMessages.GetPickupLocation,
                    HelperUtils.copyFields(pickupLocationRepository.findAllNonDeleted(), org.example.Models.CommunicationModels.CarrierModels.PickupLocation.class));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> updatePickupLocation(PickupLocationRequestModel pickupLocationRequestModel) throws Exception {
        Optional<PickupLocation> existingPickupLocation = pickupLocationRepository.findById(pickupLocationRequestModel.getPickupLocation().getPickupLocationId());
        if(existingPickupLocation.isEmpty()){
            return new Response<>(false, ErrorMessages.PickupLocationErrorMessages.InvalidId, null);
        }

        // validate the pickup location request model
        PickupLocation pickupLocation = HelperUtils.copyFields(pickupLocationRequestModel.getPickupLocation(), PickupLocation.class);
        Address address = HelperUtils.copyFields(pickupLocationRequestModel.getAddress(), Address.class);
        Pair<String, Boolean> validation = validatePickupLocation(pickupLocation, address);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        // clean the data
        address.setPhoneOnAddress(DataCleaner.cleanPhone(address.getPhoneOnAddress()));
        pickupLocationRequestModel.getAddress().setPhoneOnAddress(address.getPhoneOnAddress());

        PickupLocation existingPickupLocationByName = pickupLocationRepository.findByAddressNickName(pickupLocationRequestModel.getPickupLocation().getAddressNickName());
        if(existingPickupLocationByName != null && existingPickupLocationByName.getPickupLocationId() != pickupLocationRequestModel.getPickupLocation().getPickupLocationId()){
            return new Response<>(false, ErrorMessages.PickupLocationErrorMessages.DuplicateName, null);
        }

        // get the existing address
        Optional<Address> addressInDb = addressRepository.findById(existingPickupLocation.get().getPickupLocationAddressId());
        if(addressInDb.isPresent()){
            addressInDb.get().setLine1(pickupLocationRequestModel.getAddress().getLine1());
            addressInDb.get().setLine2(pickupLocationRequestModel.getAddress().getLine2());
            addressInDb.get().setLandmark(pickupLocationRequestModel.getAddress().getLandmark());
            addressInDb.get().setState(pickupLocationRequestModel.getAddress().getState());
            addressInDb.get().setZipCode(pickupLocationRequestModel.getAddress().getZipCode());
            addressInDb.get().setNameOnAddress(pickupLocationRequestModel.getAddress().getNameOnAddress());
            addressInDb.get().setPhoneOnAddress(pickupLocationRequestModel.getAddress().getPhoneOnAddress());
            addressInDb.get().setAddressLabel(pickupLocationRequestModel.getAddress().getAddressLabel());
            addressInDb.get().setEmailAtAddress(pickupLocationRequestModel.getAddress().getEmailAtAddress());
            addressRepository.save(addressInDb.get());
        }
        else{
            return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
        }

        existingPickupLocation.get().setAddressNickName(pickupLocationRequestModel.getPickupLocation().getAddressNickName());

        // add the pickup location in shiprocket
        ShippingHelper shippingHelper = new ShippingHelper(getCarrierDetails().getShipRocketEmail(), getCarrierDetails().getShipRocketPassword());
        Response<String> shipRocketTokenResponse = shippingHelper.getToken();
        if(!shipRocketTokenResponse.isSuccess()) {
            throw new Exception(shipRocketTokenResponse.getMessage());
        }

        pickupLocationRequestModel.setEdit(true);
        Response<AddPickupLocationResponseModel> addPickupLocationResponseModelResponse = shippingHelper.addPickupLocation(shipRocketTokenResponse.getItem(), pickupLocationRequestModel);
        if(!addPickupLocationResponseModelResponse.isSuccess()){
            throw new Exception(addPickupLocationResponseModelResponse.getMessage());
        }

        existingPickupLocation.get().setShipRocketPickupLocationId(addPickupLocationResponseModelResponse.getItem().getPickup_id());
        pickupLocationRepository.save(existingPickupLocation.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PickupLocationSuccessMessages.UpdatePickupLocation + " " + existingPickupLocation.get().getPickupLocationId(),
                ApiRoutes.PickupLocationsSubRoute.UPDATE_PICKUP_LOCATION);

        return new Response<>(true, SuccessMessages.PickupLocationSuccessMessages.UpdatePickupLocation, existingPickupLocation.get().getPickupLocationId());
    }

    @Override
    public Response<PickupLocationResponseModel> getPickupLocationById(long pickupLocationId) {
        Optional<PickupLocation> existingPickupLocation = pickupLocationRepository.findById(pickupLocationId);
        if(existingPickupLocation.isPresent()){
            PickupLocationResponseModel pickupLocationResponseModel = new PickupLocationResponseModel();
            pickupLocationResponseModel.setPickupLocation(HelperUtils.copyFields(existingPickupLocation.get(), org.example.Models.CommunicationModels.CarrierModels.PickupLocation.class));
            Optional<Address> existingAddress = addressRepository.findById(existingPickupLocation.get().getPickupLocationAddressId());
            if(existingAddress.isPresent()) {
                pickupLocationResponseModel.setAddress(HelperUtils.copyFields(existingAddress.get(), org.example.Models.CommunicationModels.CarrierModels.Address.class));
                return new Response<>(true, SuccessMessages.PickupLocationSuccessMessages.GetPickupLocation, pickupLocationResponseModel);
            }
            else{
                return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
            }
        }
        else {
            return new Response<>(false, ErrorMessages.PickupLocationErrorMessages.InvalidId, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> togglePickupLocation(long pickupLocationId) {
        Optional<PickupLocation> existingPickupLocation = pickupLocationRepository.findById(pickupLocationId);
        if(existingPickupLocation.isEmpty()){
            return new Response<>(false, ErrorMessages.PickupLocationErrorMessages.InvalidId, false);
        }

        existingPickupLocation.get().setDeleted(!existingPickupLocation.get().isDeleted());
        pickupLocationRepository.save(existingPickupLocation.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PickupLocationSuccessMessages.TogglePickupLocation + " " + existingPickupLocation.get().getPickupLocationId(),
                ApiRoutes.PickupLocationsSubRoute.TOGGLE_PICKUP_LOCATION);

        return new Response<>(true, SuccessMessages.PickupLocationSuccessMessages.TogglePickupLocation, true);
    }
}