package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.PaymentInfoRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.CommunicationModels.CarrierModels.PaymentInfo;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPaymentInfoSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentInfoDataAccessor extends BaseDataAccessor implements IPaymentInfoSubTranslator {

    private final PaymentInfoRepository paymentInfoRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public PaymentInfoDataAccessor(HttpServletRequest request, CarrierRepository carrierRepository,
                               PaymentInfoRepository paymentInfoRepository,
                               UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.paymentInfoRepository = paymentInfoRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    private Pair<String, Boolean> validatePaymentInfo(PaymentInfo paymentInfo)
    {
        double total = paymentInfo.getTotal();
        double tax = paymentInfo.getTax();
        double serviceFee = paymentInfo.getServiceFee();
        double deliveryFee = paymentInfo.getDeliveryFee();
        double packingFee = paymentInfo.getPackagingFee();
        double subTotal = paymentInfo.getSubTotal();

        if(subTotal + tax + serviceFee + deliveryFee + packingFee != total){
            return Pair.of(ErrorMessages.PaymentInfoErrorMessages.ER001, false);
        }

        return Pair.of("Success", true);
    }

    // dont add transactional since they are already called by transactional functions
    @Override
    public Response<Long> insertPaymentInfo(PaymentInfo paymentInfo) {
        Pair<String, Boolean> validation =  validatePaymentInfo(paymentInfo);
        if(!validation.getValue()) {
            return new Response<>(false, validation.getKey(), null);
        }

        com.example.SpringApi.DatabaseModels.CarrierDatabase.PaymentInfo savedPaymentInfo = paymentInfoRepository.save(HelperUtils.copyFields(paymentInfo, com.example.SpringApi.DatabaseModels.CarrierDatabase.PaymentInfo.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PaymentInfoSuccessMessages.InsertPaymentInfo + " " + savedPaymentInfo.getPaymentId(),
                ApiRoutes.PaymentsSubRoute.INSERT_PAYMENT);
        return new Response<>(true, SuccessMessages.PaymentInfoSuccessMessages.InsertPaymentInfo, savedPaymentInfo.getPaymentId());
    }

    // dont add transactional since they are already called by transactional functions
    @Override
    public Response<Long> updatePaymentInfo(PaymentInfo paymentInfo) {
        Optional<com.example.SpringApi.DatabaseModels.CarrierDatabase.PaymentInfo> existingPaymentInfo = paymentInfoRepository.findById(paymentInfo.getPaymentId());
        if(existingPaymentInfo.isEmpty()) {
            return new Response<>(false, ErrorMessages.PaymentInfoErrorMessages.InvalidId, null);
        }

        Pair<String, Boolean> validation =  validatePaymentInfo(paymentInfo);
        if(!validation.getValue()) {
            return new Response<>(false, validation.getKey(), null);
        }

        com.example.SpringApi.DatabaseModels.CarrierDatabase.PaymentInfo savedPaymentInfo = paymentInfoRepository.save(HelperUtils.copyFields(paymentInfo, com.example.SpringApi.DatabaseModels.CarrierDatabase.PaymentInfo.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PaymentInfoSuccessMessages.InsertPaymentInfo + " " + savedPaymentInfo.getPaymentId(),
                ApiRoutes.PaymentsSubRoute.INSERT_PAYMENT);
        return new Response<>(true, SuccessMessages.PaymentInfoSuccessMessages.InsertPaymentInfo, savedPaymentInfo.getPaymentId());
    }
}
