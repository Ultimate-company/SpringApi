package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.*;
import com.example.SpringApi.DatabaseModels.CentralDatabase.WebTemplateCarrierMapping;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.*;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.WebTemplateCarrierMappingRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.HelperUtils;
import org.example.CommonHelpers.PasswordHelper;
import org.example.Models.RequestModels.ApiRequestModels.WebTemplateRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.WebTemplateResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IWebTemplateSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WebTemplatesDataAccessor extends BaseDataAccessor implements IWebTemplateSubTranslator {

    private final WebTemplatesRepository webTemplatesRepository;
    private final WebTemplateFontStyleRepository webTemplateFontStyleRepository;
    private final UserLogDataAccessor userLogDataAccessor;
    private final ProductRepository productRepository;
    private final WebTemplateCarrierMappingRepository webTemplateCarrierMappingRepository;
    private final WebTemplateUserCartMappingRepository webTemplateUserCartMappingRepository;
    private final WebTemplateUserLikedItemsMappingRepository webTemplateUserLikedItemsMappingRepository;

    @Autowired
    public WebTemplatesDataAccessor(HttpServletRequest request,
                                    CarrierRepository carrierRepository,
                                    WebTemplatesRepository webTemplatesRepository,
                                    WebTemplateFontStyleRepository webTemplateFontStyleRepository,
                                    ProductRepository productRepository,
                                    UserLogDataAccessor userLogDataAccessor,
                                    WebTemplateCarrierMappingRepository webTemplateCarrierMappingRepository,
                                    WebTemplateUserCartMappingRepository webTemplateUserCartMappingRepository,
                                    WebTemplateUserLikedItemsMappingRepository webTemplateUserLikedItemsMappingRepository
    ) {
        super(request, carrierRepository);
        this.webTemplatesRepository = webTemplatesRepository;
        this.webTemplateFontStyleRepository = webTemplateFontStyleRepository;
        this.userLogDataAccessor = userLogDataAccessor;
        this.productRepository = productRepository;
        this.webTemplateCarrierMappingRepository = webTemplateCarrierMappingRepository;
        this.webTemplateUserCartMappingRepository = webTemplateUserCartMappingRepository;
        this.webTemplateUserLikedItemsMappingRepository = webTemplateUserLikedItemsMappingRepository;
    }

    private Pair<String, Boolean> Validations(WebTemplateRequestModel webTemplateRequestModel)
    {
        // 1. At least one sort option should be present and should be valid
        if(webTemplateRequestModel.getSortOptions() == null ||
            webTemplateRequestModel.getSortOptions().isEmpty() ||
                !new HashSet<>(HelperUtils.getSortOptions()).containsAll(webTemplateRequestModel.getSortOptions())){
            return Pair.of(ErrorMessages.WebTemplatesErrorMessages.ER001, false);
        }

        // 2. At least one product id should be present and should be valid
        List<Long> productIds = productRepository.findAll().stream().map(Product:: getProductId).toList();
        if(webTemplateRequestModel.getSelectedProductIds() == null ||
                webTemplateRequestModel.getSelectedProductIds().isEmpty() ||
                !new HashSet<>(productIds).containsAll(webTemplateRequestModel.getSelectedProductIds())){
            return Pair.of(ErrorMessages.WebTemplatesErrorMessages.ER002, false);
        }

        // 3. At least one filter option should be present and should be valid
        if(webTemplateRequestModel.getFilterOptions() == null ||
                webTemplateRequestModel.getFilterOptions().isEmpty() ||
                !new HashSet<>(HelperUtils.getFilterOptions()).containsAll(webTemplateRequestModel.getFilterOptions())){
            return Pair.of(ErrorMessages.WebTemplatesErrorMessages.ER003, false);
        }

        // 3. At least one accepted payment option should be present and should be valid
        HashSet<String> paymentOptions = HelperUtils.getPaymentOptions().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()  // Stream the list of TreeMaps
                        .flatMap(innerMap -> innerMap.entrySet().stream()  // Stream the entries of each TreeMap
                                .map(innerEntry -> entry.getKey() + ":" + innerEntry.getValue())))  // Format as "type:value"
                .collect(Collectors.toCollection(HashSet::new));
        if(webTemplateRequestModel.getAcceptedPaymentOptions() == null ||
                webTemplateRequestModel.getAcceptedPaymentOptions().isEmpty() ||
                !paymentOptions.containsAll(webTemplateRequestModel.getAcceptedPaymentOptions())){
            return Pair.of(ErrorMessages.WebTemplatesErrorMessages.ER004, false);
        }

        //4. url should be present and should be a wildcard/subdomain company
        String regex = "^(http|https)://[a-zA-Z0-9-.]+\\.ultimatecompany\\."+currentEnvironment+"\\.com(?:/[^\\s]*)?$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        if (webTemplateRequestModel.getWebTemplate() == null ||
                !StringUtils.hasText(webTemplateRequestModel.getWebTemplate().getUrl()) ||
                !pattern.matcher(webTemplateRequestModel.getWebTemplate().getUrl()).matches()) {
            return Pair.of(ErrorMessages.WebTemplatesErrorMessages.ER008, false);
        }

        // 5. card header font style, card subtext font style, header font style are required
        if(webTemplateRequestModel.getCardHeaderFontStyle() == null ||
            webTemplateRequestModel.getCardSubTextFontStyle() == null ||
            webTemplateRequestModel.getHeaderFontStyle() == null ||
                !StringUtils.hasText(webTemplateRequestModel.getCardHeaderFontStyle().getFontStyle()) ||
                !StringUtils.hasText(webTemplateRequestModel.getCardHeaderFontStyle().getFontColor()) ||
                webTemplateRequestModel.getCardHeaderFontStyle().getFontSize() <=0 ||
                    !StringUtils.hasText(webTemplateRequestModel.getCardSubTextFontStyle().getFontStyle()) ||
                    !StringUtils.hasText(webTemplateRequestModel.getCardSubTextFontStyle().getFontColor()) ||
                    webTemplateRequestModel.getCardSubTextFontStyle().getFontSize() <=0  ||
                        !StringUtils.hasText(webTemplateRequestModel.getHeaderFontStyle().getFontStyle()) ||
                        !StringUtils.hasText(webTemplateRequestModel.getHeaderFontStyle().getFontColor()) ||
                        webTemplateRequestModel.getHeaderFontStyle().getFontSize() <=0 ){
            return Pair.of(ErrorMessages.WebTemplatesErrorMessages.ER009, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<WebTemplateResponseModel> getWebTemplateById(long webTemplateId) {
        Optional<WebTemplate> webTemplate = webTemplatesRepository.findById(webTemplateId);
        if(webTemplate.isPresent()){
            Optional<WebTemplatesFontStyle> cardHeaderFontStyle = webTemplateFontStyleRepository.findById(webTemplate.get().getCardHeaderFontStyleId());
            Optional<WebTemplatesFontStyle> cardSubTextFontStyle = webTemplateFontStyleRepository.findById(webTemplate.get().getCardSubTextFontStyleId());
            Optional<WebTemplatesFontStyle> headerFontStyle = webTemplateFontStyleRepository.findById(webTemplate.get().getHeaderFontStyleId());
            if(cardHeaderFontStyle.isEmpty() || cardSubTextFontStyle.isEmpty() || headerFontStyle.isEmpty()) {
                return new Response<>(false, ErrorMessages.WebTemplatesErrorMessages.InvalidId, null);
            }

            WebTemplateResponseModel webTemplateResponseModel = new WebTemplateResponseModel();
            webTemplateResponseModel.setWebTemplate(HelperUtils.copyFields(webTemplate.get(), org.example.Models.CommunicationModels.CarrierModels.WebTemplate.class));
            webTemplateResponseModel.setCardHeaderFontStyle(HelperUtils.copyFields(cardHeaderFontStyle.get(), org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle.class));
            webTemplateResponseModel.setCardSubTextFontStyle(HelperUtils.copyFields(cardSubTextFontStyle.get(), org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle.class));
            webTemplateResponseModel.setHeaderFontStyle(HelperUtils.copyFields(headerFontStyle.get(), org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle.class));

            return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.GetWebTemplate, webTemplateResponseModel);
        }
        else {
            return new Response<>(false, ErrorMessages.WebTemplatesErrorMessages.InvalidId, null);
        }
    }

    @Override
    public Response<PaginationBaseResponseModel<WebTemplateResponseModel>> getWebTemplatesInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // validate the column names
        if(StringUtils.hasText(paginationBaseRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(List.of("Url"));

            if(!validColumns.contains(paginationBaseRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        Pageable pageable = PageRequest.of(paginationBaseRequestModel.getStart(),
                paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart());

        Page<Object[]> webTemplates = webTemplatesRepository.findPaginatedWebTemplates(paginationBaseRequestModel.getColumnName(),
                paginationBaseRequestModel.getCondition(),
                paginationBaseRequestModel.getFilterExpr(),
                paginationBaseRequestModel.isIncludeDeleted(),
                pageable);

        List<WebTemplateResponseModel> webTemplateResponseModels = new ArrayList<>();
        for (Object[] result : webTemplates.getContent()) {
            WebTemplateResponseModel responseModel = new WebTemplateResponseModel();
            responseModel.setWebTemplate(HelperUtils.copyFields(result[0], org.example.Models.CommunicationModels.CarrierModels.WebTemplate.class));
            responseModel.setCardHeaderFontStyle(HelperUtils.copyFields(result[1], org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle.class));
            responseModel.setCardSubTextFontStyle(HelperUtils.copyFields(result[2], org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle.class));
            responseModel.setHeaderFontStyle(HelperUtils.copyFields(result[3], org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle.class));
            webTemplateResponseModels.add(responseModel);
        }

        PaginationBaseResponseModel<WebTemplateResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(webTemplateResponseModels);
        paginationBaseResponseModel.setTotalDataCount(webTemplates.getTotalElements());

        return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.GetWebTemplate, paginationBaseResponseModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> insertWebTemplate(WebTemplateRequestModel webTemplateRequestModel) throws JsonProcessingException {
        Pair<String, Boolean> validation = Validations(webTemplateRequestModel);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        // url should be unique
        WebTemplate webTemplate = webTemplatesRepository.findByUrl(webTemplateRequestModel.getWebTemplate().getUrl().trim().toLowerCase());
        if(webTemplate != null){
            return new Response<>(false, ErrorMessages.WebTemplatesErrorMessages.UrlExists, null);
        }

        // add the font styles
        WebTemplatesFontStyle cardHeaderFontStyle = webTemplateFontStyleRepository.save(HelperUtils.copyFields(webTemplateRequestModel.getCardHeaderFontStyle(), WebTemplatesFontStyle.class));
        WebTemplatesFontStyle cardSubTextFontStyle = webTemplateFontStyleRepository.save(HelperUtils.copyFields(webTemplateRequestModel.getCardSubTextFontStyle(), WebTemplatesFontStyle.class));
        WebTemplatesFontStyle headerFontStyle = webTemplateFontStyleRepository.save(HelperUtils.copyFields(webTemplateRequestModel.getHeaderFontStyle(), WebTemplatesFontStyle.class));

        // stringify values before storing it in the db
        webTemplateRequestModel.getWebTemplate().setSortOptions(String.join(",", webTemplateRequestModel.getSortOptions()));
        webTemplateRequestModel.getWebTemplate().setFilterOptions(String.join(",", webTemplateRequestModel.getFilterOptions()));
        webTemplateRequestModel.getWebTemplate().setSelectedProducts(webTemplateRequestModel.getSelectedProductIds().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")));
        webTemplateRequestModel.getWebTemplate().setAcceptedPaymentOptions(String.join(",", webTemplateRequestModel.getAcceptedPaymentOptions()));
        webTemplateRequestModel.getWebTemplate().setStateCitiesMapping(new ObjectMapper().writeValueAsString(webTemplateRequestModel.getStateCityMapping()));

        // set the font style ids
        webTemplateRequestModel.getWebTemplate().setCardHeaderFontStyleId(cardHeaderFontStyle.getWebTemplateFontStyleId());
        webTemplateRequestModel.getWebTemplate().setCardSubTextFontStyleId(cardSubTextFontStyle.getWebTemplateFontStyleId());
        webTemplateRequestModel.getWebTemplate().setHeaderFontStyleId(headerFontStyle.getWebTemplateFontStyleId());

        // extract the wildcard
        String regex = "^https://([a-zA-Z0-9-.]+)\\.ultimatecompany\\."+currentEnvironment+"\\.com$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(webTemplateRequestModel.getWebTemplate().getUrl());
        String wildCard = "";
        if (matcher.matches()) {
            wildCard = matcher.group(1);
        }

        // write to db
        WebTemplate savedWebTemplate = webTemplatesRepository.save(HelperUtils.copyFields(webTemplateRequestModel.getWebTemplate(), WebTemplate.class));
        webTemplateCarrierMappingRepository.save(new WebTemplateCarrierMapping()
                .setWebTemplateId(savedWebTemplate.getWebTemplateId())
                .setCarrierId(getCarrierId())
                .setWildCard(wildCard)
                .setApiAccessKey(PasswordHelper.getToken(wildCard))
        );

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.WebTemplatesSuccessMessages.InsertWebTemplate + " " + savedWebTemplate.getWebTemplateId(),
                ApiRoutes.WebTemplateSubRoute.INSERT_WEB_TEMPLATE);
        return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.InsertWebTemplate, savedWebTemplate.getWebTemplateId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> toggleWebTemplate(long webTemplateId) {
        Optional<WebTemplate> webTemplate = webTemplatesRepository.findById(webTemplateId);
        if(webTemplate.isEmpty()){
            return new Response<>(false, ErrorMessages.WebTemplatesErrorMessages.InvalidId, null);
        }

        webTemplate.get().setDeleted(!webTemplate.get().isDeleted());
        webTemplatesRepository.save(webTemplate.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.WebTemplatesSuccessMessages.ToggleWebTemplate + " " + webTemplateId,
                ApiRoutes.WebTemplateSubRoute.TOGGLE_WEB_TEMPLATE);
        return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.ToggleWebTemplate, true);
    }

    @Override
    public Response<Boolean> deployWebTemplate(long webTemplateId) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> updateWebTemplate(WebTemplateRequestModel webTemplateRequestModel) throws JsonProcessingException {
        Optional<WebTemplate> existingWebTemplate = webTemplatesRepository.findById(webTemplateRequestModel.getWebTemplate().getWebTemplateId());
        if(existingWebTemplate.isEmpty()){
            return new Response<>(false, ErrorMessages.WebTemplatesErrorMessages.InvalidId, null);
        }

        Pair<String, Boolean> validation = Validations(webTemplateRequestModel);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), 0L);
        }

        // url should be unique
        WebTemplate webTemplate = webTemplatesRepository.findByUrl(webTemplateRequestModel.getWebTemplate().getUrl().trim().toLowerCase());
        if(webTemplate != null && webTemplate.getWebTemplateId() != existingWebTemplate.get().getWebTemplateId()){
            return new Response<>(false, ErrorMessages.WebTemplatesErrorMessages.UrlExists, null);
        }

        // update the font styles
        Optional<WebTemplatesFontStyle> cardHeaderFontStyle = webTemplateFontStyleRepository.findById(existingWebTemplate.get().getCardHeaderFontStyleId());
        Optional<WebTemplatesFontStyle> cardSubTextFontStyle = webTemplateFontStyleRepository.findById(existingWebTemplate.get().getCardSubTextFontStyleId());
        Optional<WebTemplatesFontStyle> headerFontStyle = webTemplateFontStyleRepository.findById(existingWebTemplate.get().getHeaderFontStyleId());

        if(cardHeaderFontStyle.isPresent()){
            cardHeaderFontStyle.get().setFontStyle(webTemplateRequestModel.getCardHeaderFontStyle().getFontStyle());
            cardHeaderFontStyle.get().setFontColor(webTemplateRequestModel.getCardHeaderFontStyle().getFontColor());
            cardHeaderFontStyle.get().setFontSize(webTemplateRequestModel.getCardHeaderFontStyle().getFontSize());
            webTemplateFontStyleRepository.save(cardHeaderFontStyle.get());
        }
        if(cardSubTextFontStyle.isPresent()){
            cardSubTextFontStyle.get().setFontStyle(webTemplateRequestModel.getCardSubTextFontStyle().getFontStyle());
            cardSubTextFontStyle.get().setFontColor(webTemplateRequestModel.getCardSubTextFontStyle().getFontColor());
            cardSubTextFontStyle.get().setFontSize(webTemplateRequestModel.getCardSubTextFontStyle().getFontSize());
            webTemplateFontStyleRepository.save(cardSubTextFontStyle.get());
        }
        if(headerFontStyle.isPresent()){
            headerFontStyle.get().setFontStyle(webTemplateRequestModel.getHeaderFontStyle().getFontStyle());
            headerFontStyle.get().setFontColor(webTemplateRequestModel.getHeaderFontStyle().getFontColor());
            headerFontStyle.get().setFontSize(webTemplateRequestModel.getHeaderFontStyle().getFontSize());
            webTemplateFontStyleRepository.save(headerFontStyle.get());
        }

        // stringify values before storing it in the db
        existingWebTemplate.get().setSortOptions(String.join(",", webTemplateRequestModel.getSortOptions()));
        existingWebTemplate.get().setFilterOptions(String.join(",", webTemplateRequestModel.getFilterOptions()));
        existingWebTemplate.get().setSelectedProducts(webTemplateRequestModel.getSelectedProductIds().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")));
        existingWebTemplate.get().setAcceptedPaymentOptions(String.join(",", webTemplateRequestModel.getAcceptedPaymentOptions()));
        existingWebTemplate.get().setStateCitiesMapping(new ObjectMapper().writeValueAsString(webTemplateRequestModel.getStateCityMapping()));

        // update the other fields
        existingWebTemplate.get().setUrl(webTemplateRequestModel.getWebTemplate().getUrl());

        // extract the wildcard
        String regex = "^https://([a-zA-Z0-9-.]+)\\.ultimatecompany\\."+currentEnvironment+"\\.com$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(webTemplateRequestModel.getWebTemplate().getUrl());
        String wildCard = "";
        if (matcher.matches()) {
            wildCard = matcher.group(1);
        }

        // write to db
        webTemplatesRepository.save(existingWebTemplate.get());

        // update to central db
        WebTemplateCarrierMapping webTemplateCarrierMapping = webTemplateCarrierMappingRepository.findByCarrierId(getCarrierId());
        webTemplateCarrierMapping.setWildCard(wildCard);
        webTemplateCarrierMappingRepository.save(webTemplateCarrierMapping);

        // log the changes
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.WebTemplatesSuccessMessages.UpdateWebTemplate + " " + webTemplateRequestModel.getWebTemplate().getWebTemplateId(),
                ApiRoutes.WebTemplateSubRoute.UPDATE_WEB_TEMPLATE);
        return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.UpdateWebTemplate, webTemplateRequestModel.getWebTemplate().getWebTemplateId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> updateUserCart(WebTemplateRequestModel webTemplateRequestModel) {
        // delete the current mapping
        if(webTemplateRequestModel.getQuantity() == 0) {
            Optional<WebTemplateUserCartMapping> webTemplateUserCartMapping = webTemplateUserCartMappingRepository.findByUserIdAndProductId(webTemplateRequestModel.getUserId(), webTemplateRequestModel.getProductId()).stream().findFirst();
            if(webTemplateUserCartMapping.isPresent()){
                webTemplateUserCartMappingRepository.delete(webTemplateUserCartMapping.get());

                userLogDataAccessor.logData(getUserId(),
                        SuccessMessages.WebTemplatesSuccessMessages.UpdateUserCart + " " + + webTemplateUserCartMapping.get().getWebTemplateUserCartMappingId() + " Mapping was deleted",
                        ApiRoutes.WebTemplateSubRoute.UPDATE_USER_CART);
                return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.UpdateUserCart, null);
            }
            else {
                return new Response<>(false, ErrorMessages.WebTemplatesErrorMessages.ER010, null);
            }
        }

        WebTemplateUserCartMapping newWebTemplateUserCartMapping;
        Optional<WebTemplateUserCartMapping> webTemplateUserCartMapping = webTemplateUserCartMappingRepository.findByUserIdAndProductId(webTemplateRequestModel.getUserId(), webTemplateRequestModel.getProductId()).stream().findFirst();
        // add new
        if(webTemplateUserCartMapping.isEmpty()) {
            newWebTemplateUserCartMapping = webTemplateUserCartMappingRepository.save(new WebTemplateUserCartMapping()
                    .setUserId(webTemplateRequestModel.getUserId())
                    .setProductId(webTemplateRequestModel.getProductId())
                    .setWebTemplateId(webTemplateRequestModel.getWebTemplateId())
                    .setQuantity(webTemplateRequestModel.getQuantity()));
        }
        // update quantity for existing
        else {
            webTemplateUserCartMapping.get().setQuantity(webTemplateRequestModel.getQuantity());
            newWebTemplateUserCartMapping = webTemplateUserCartMappingRepository.save(webTemplateUserCartMapping.get());
        }

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.WebTemplatesSuccessMessages.UpdateUserCart + " " + newWebTemplateUserCartMapping.getWebTemplateUserCartMappingId(),
                ApiRoutes.WebTemplateSubRoute.UPDATE_USER_CART);
        return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.UpdateUserCart, newWebTemplateUserCartMapping.getWebTemplateUserCartMappingId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> updateUserLikedItems(WebTemplateRequestModel webTemplateRequestModel) {
        // delete the current mapping
        if(webTemplateRequestModel.getQuantity() == 0) {
            Optional<WebTemplateUserLikedItemsMapping> webTemplateUserLikedItemsMapping = webTemplateUserLikedItemsMappingRepository.findByUserIdAndProductId(webTemplateRequestModel.getUserId(), webTemplateRequestModel.getProductId()).stream().findFirst();
            if(webTemplateUserLikedItemsMapping.isPresent()){
                webTemplateUserLikedItemsMappingRepository.delete(webTemplateUserLikedItemsMapping.get());

                userLogDataAccessor.logData(getUserId(),
                        SuccessMessages.WebTemplatesSuccessMessages.UpdateUserCart + " " + + webTemplateUserLikedItemsMapping.get().getWebTemplateUserLikedItemsMappingId() + " Mapping was deleted",
                        ApiRoutes.WebTemplateSubRoute.UPDATE_USER_LIKED_ITEMS);
                return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.UpdateUserLikedItems, null);
            }
            else {
                return new Response<>(false, ErrorMessages.WebTemplatesErrorMessages.ER011, null);
            }
        }

        Optional<WebTemplateUserLikedItemsMapping> webTemplateUserLikedItemsMapping = webTemplateUserLikedItemsMappingRepository.findByUserIdAndProductId(webTemplateRequestModel.getUserId(), webTemplateRequestModel.getProductId()).stream().findFirst();
        if(webTemplateUserLikedItemsMapping.isEmpty()) {
            WebTemplateUserLikedItemsMapping newWebTemplateUserLikedItemsMapping = webTemplateUserLikedItemsMappingRepository.save(new WebTemplateUserLikedItemsMapping()
                    .setUserId(webTemplateRequestModel.getUserId())
                    .setProductId(webTemplateRequestModel.getProductId())
                    .setWebTemplateId(webTemplateRequestModel.getWebTemplateId()));

            userLogDataAccessor.logData(getUserId(),
                    SuccessMessages.WebTemplatesSuccessMessages.UpdateUserLikedItems + " " + newWebTemplateUserLikedItemsMapping.getWebTemplateUserLikedItemsMappingId(),
                    ApiRoutes.WebTemplateSubRoute.UPDATE_USER_LIKED_ITEMS);
            return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.UpdateUserLikedItems, newWebTemplateUserLikedItemsMapping.getWebTemplateUserLikedItemsMappingId());
        }

        return new Response<>(true, SuccessMessages.WebTemplatesSuccessMessages.UpdateUserLikedItems, webTemplateUserLikedItemsMapping.get().getWebTemplateUserLikedItemsMappingId());
    }
}