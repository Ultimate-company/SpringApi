package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.PickupLocation;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.PromoRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Promo;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPromoSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class PromoDataAccessor extends BaseDataAccessor implements IPromoSubTranslator {
    private final PromoRepository promoRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public PromoDataAccessor(HttpServletRequest request,
                             CarrierRepository carrierRepository,
                             PromoRepository promoRepository,
                             UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.promoRepository = promoRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    public Pair<String, Boolean> validatePromo(org.example.Models.CommunicationModels.CarrierModels.Promo promo) {
        /*
        * Required fields => promo Code, description and discount value should be greater than 0
        * */
        if(!StringUtils.hasText(promo.getPromoCode())){
            return Pair.of(ErrorMessages.PromoErrorMessages.ER001, false);
        }
        if(!StringUtils.hasText(promo.getDescription())){
            return Pair.of(ErrorMessages.PromoErrorMessages.ER002, false);
        }
        if(promo.getDiscountValue() <= 0){
            return Pair.of(ErrorMessages.PromoErrorMessages.ER003, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<PaginationBaseResponseModel<org.example.Models.CommunicationModels.CarrierModels.Promo>> getPromosInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // validate the column names
        if(StringUtils.hasText(paginationBaseRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(Arrays.asList("promoCode", "description", "discountValue"));

            if(!validColumns.contains(paginationBaseRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        Page<Promo> promos = promoRepository.findPaginatedPromos(paginationBaseRequestModel.getColumnName(),
                paginationBaseRequestModel.getCondition(),
                paginationBaseRequestModel.getFilterExpr(),
                paginationBaseRequestModel.isIncludeDeleted(),
                PageRequest.of(paginationBaseRequestModel.getStart() / (paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart()),
                        paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart(),
                        Sort.by("promoId").descending()));

        PaginationBaseResponseModel<org.example.Models.CommunicationModels.CarrierModels.Promo> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(HelperUtils.copyFields(promos.getContent(), org.example.Models.CommunicationModels.CarrierModels.Promo.class));
        paginationBaseResponseModel.setTotalDataCount(promos.getTotalElements());

        return new Response<>(true, SuccessMessages.GroupsSuccessMessages.GetGroups, paginationBaseResponseModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> createPromo(org.example.Models.CommunicationModels.CarrierModels.Promo promo) {
        Promo existingPromo = promoRepository.findPromoByPromoCode(promo.getPromoCode());
        if(existingPromo != null){
            return new Response<>(false, ErrorMessages.PromoErrorMessages.DuplicateName, null);
        }

        // validate the promo before insertion
        Pair<String, Boolean> validation = validatePromo(promo);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        Promo newPromo = promoRepository.save(HelperUtils.copyFields(promo, Promo.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PromoSuccessMessages.InsertPromo + " " + newPromo.getPromoId(),
                ApiRoutes.PromosSubRoute.CREATE_PROMO);

        return new Response<>(true, SuccessMessages.PromoSuccessMessages.InsertPromo, newPromo.getPromoId());
    }

    @Override
    public Response<org.example.Models.CommunicationModels.CarrierModels.Promo> getPromoDetailsById(long promoId) {
        Optional<Promo> promo = promoRepository.findById(promoId);
        return promo.map(value -> new Response<>(true, SuccessMessages.PromoSuccessMessages.GetPromo, HelperUtils.copyFields(value, org.example.Models.CommunicationModels.CarrierModels.Promo.class)))
                .orElseGet(() -> new Response<>(false, ErrorMessages.PromoErrorMessages.InvalidId, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> togglePromo(long promoId) {
        Optional<Promo> promo = promoRepository.findById(promoId);
        if(promo.isEmpty()){
            return new Response<>(false, ErrorMessages.PromoErrorMessages.InvalidId, false);
        }

        promo.get().setDeleted(!promo.get().isDeleted());
        promoRepository.save(promo.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PromoSuccessMessages.TogglePromo + " " + promoId,
                ApiRoutes.PromosSubRoute.TOGGLE_PROMO);
        return new Response<>(true, SuccessMessages.PromoSuccessMessages.TogglePromo, true);
    }

    @Override
    public Response<org.example.Models.CommunicationModels.CarrierModels.Promo> getPromoDetailsByName(String promoCode) {
        Promo promo = promoRepository.findPromoByPromoCode(promoCode);
        if(promo == null){
            return new Response<>(false, ErrorMessages.PromoErrorMessages.InvalidName, null);
        }
        return new Response<>(true, SuccessMessages.PromoSuccessMessages.GetPromo, HelperUtils.copyFields(promo, org.example.Models.CommunicationModels.CarrierModels.Promo.class));
    }
}