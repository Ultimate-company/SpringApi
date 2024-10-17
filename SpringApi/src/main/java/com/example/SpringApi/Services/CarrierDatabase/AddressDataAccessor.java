package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.AddressRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.example.ApiRoutes;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.CommunicationModels.CarrierModels.Address;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IAddressSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AddressDataAccessor extends BaseDataAccessor implements IAddressSubTranslator {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public AddressDataAccessor(HttpServletRequest request,
                               UserLogDataAccessor userLogDataAccessor,
                               UserRepository userRepository,
                               AddressRepository addressRepository,
                               CarrierRepository carrierRepository) {
        super(request, carrierRepository);
        this.userLogDataAccessor = userLogDataAccessor;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> toggleAddress(long addressId) {
        Optional<com.example.SpringApi.DatabaseModels.CarrierDatabase.Address> address = addressRepository.findById(addressId);
        if(address.isEmpty()){
            return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, false);
        }
        addressRepository.delete(address.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.AddressSuccessMessages.ToggleAddress + " " + address.get().getAddressId(),
                ApiRoutes.AddressSubRoute.TOGGLE_ADDRESS);

        return new Response<>(true, SuccessMessages.AddressSuccessMessages.ToggleAddress, true);
    }

    @Override
    public Response<Address> getAddressByUserId(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()){
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
        }

        com.example.SpringApi.DatabaseModels.CarrierDatabase.Address address  = addressRepository.findByUserId(userId);
        if(address == null){
            return new Response<>(false, ErrorMessages.AddressErrorMessages.NotFound, null);
        }
        else {
            return new Response<>(true, SuccessMessages.AddressSuccessMessages.GetAddress, HelperUtils.copyFields(address, Address.class));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> insertAddress(Address address) {
        com.example.SpringApi.DatabaseModels.CarrierDatabase.Address savedAddress = addressRepository.save(HelperUtils.copyFields(address, com.example.SpringApi.DatabaseModels.CarrierDatabase.Address.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.AddressSuccessMessages.InsertAddress + " " + savedAddress.getAddressId(),
                ApiRoutes.AddressSubRoute.INSERT_ADDRESS);
        return new Response<>(true, SuccessMessages.AddressSuccessMessages.InsertAddress, savedAddress.getAddressId());
    }

    @Override
    public Response<Long> updateAddress(Address address) {
        addressRepository.save(HelperUtils.copyFields(address, com.example.SpringApi.DatabaseModels.CarrierDatabase.Address.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.AddressSuccessMessages.UpdateAddress + " " + address.getAddressId(),
                ApiRoutes.AddressSubRoute.UPDATE_ADDRESS);
        return new Response<>(true, SuccessMessages.AddressSuccessMessages.UpdateAddress, address.getAddressId());
    }

    @Override
    public Response<Address> getAddressById(long addressId) {
        Optional<com.example.SpringApi.DatabaseModels.CarrierDatabase.Address> address = addressRepository.findById(addressId);

        return address.map(value -> new Response<>(true, SuccessMessages.AddressSuccessMessages.GetAddress, HelperUtils.copyFields(value, Address.class)))
                .orElseGet(() -> new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null));
    }
}