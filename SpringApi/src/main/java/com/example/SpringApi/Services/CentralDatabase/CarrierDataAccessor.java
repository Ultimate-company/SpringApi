package com.example.SpringApi.Services.CentralDatabase;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.DatabaseModels.CentralDatabase.WebTemplateCarrierMapping;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CentralDatabase.WebTemplateCarrierMappingRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.SuccessMessages;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.example.Adapters.DateAdapter;
import org.example.Adapters.LocalDateTimeAdapter;
import org.example.ApiRoutes;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserCarrierMappingRepository;
import com.example.SpringApi.DatabaseModels.CentralDatabase.UserCarrierMapping;
import org.example.CommonHelpers.HelperUtils;
import org.example.CommonHelpers.JiraHelper;
import org.example.Models.CommunicationModels.CentralModels.Carrier;
import org.example.Models.RequestModels.GridRequestModels.GetCarriersRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.CarrierByWebTemplateWildCardResponse;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.JiraResponseModels.CreateIssueTypeResponseModel;
import org.example.Models.ResponseModels.JiraResponseModels.GetIssueTypesResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.ICarrierSubTranslator;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.IUserLogSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarrierDataAccessor extends BaseDataAccessor implements ICarrierSubTranslator {
    private final CarrierRepository carrierRepository;
    private final UserCarrierMappingRepository userCarrierMappingRepository;
    private final WebTemplateCarrierMappingRepository webTemplateCarrierMappingRepository;
    private final IUserLogSubTranslator userLogDataAccessor;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public CarrierDataAccessor(CarrierRepository carrierRepository,
                               UserCarrierMappingRepository userCarrierMappingRepository,
                               WebTemplateCarrierMappingRepository webTemplateCarrierMappingRepository,
                               UserLogDataAccessor userLogDataAccessor,
                               HttpServletRequest request) {
        super(request, carrierRepository);
        this.carrierRepository = carrierRepository;
        this.userCarrierMappingRepository = userCarrierMappingRepository;
        this.userLogDataAccessor = userLogDataAccessor;
        this.webTemplateCarrierMappingRepository = webTemplateCarrierMappingRepository;
        this.jwtTokenProvider = new JwtTokenProvider();
    }

    public boolean syncJiraWithDB(long carrierId) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        // get issue types
        Response<List<GetIssueTypesResponseModel>> getIssuesTypesResponse = jiraHelper.getIssueTypes();
        if (!getIssuesTypesResponse.isSuccess()) {
            return false;
        }

        // Convert the Jira issue types to a Set
        Set<String> jiraIssueTypes = getIssuesTypesResponse.getItem().stream()
                .map(GetIssueTypesResponseModel::getName)
                .filter(name -> !name.contains("[System]"))
                .collect(Collectors.toSet());

        if (!jiraIssueTypes.isEmpty()) {
            // Convert the Set to a comma-separated string
            Optional<com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier> carrier = carrierRepository.findById(carrierId);
            if (carrier.isPresent()) {
                carrier.get().setIssueTypes(String.join(",", jiraIssueTypes));
                carrierRepository.save(carrier.get());
            }
        }

        return true;
    }

    @Override
    public Response<Carrier> getCarrierDetailsById(long carrierId) {
        if (carrierId != getCarrierId()) {
            return new Response<>(false, ErrorMessages.Unauthorized, null);
        }

        return carrierRepository.findById(carrierId)
                .map(carrier -> {
                    syncJiraWithDB(carrierId);
                    Carrier carrierDetails = HelperUtils.copyFields(carrier, Carrier.class);
                    return new Response<>(true, SuccessMessages.CarrierSuccessMessages.GetCarrier, carrierDetails);
                })
                .orElseGet(() -> new Response<>(false, ErrorMessages.CarrierErrorMessages.InvalidId, null));
    }

    @Override
    public Response<Boolean> isUserMappedToCarrier() {
        UserCarrierMapping userCarrierMapping = userCarrierMappingRepository.findByUserIdAndCarrierId(getUserId(), getCarrierId());

        return userCarrierMapping == null ? new Response<>(false, ErrorMessages.CarrierErrorMessages.ER001, false)
                : new Response<>(true, ErrorMessages.CarrierErrorMessages.ER001, true);
    }

    @Override
    public Response<PaginationBaseResponseModel<Carrier>> getCarriersInBatches(GetCarriersRequestModel getCarriersRequestModel) {
        List<com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier> carriers = carrierRepository.findByUserIdAndNameContains(getCarriersRequestModel.getUserId(), getCarriersRequestModel.getFilterExpr());
        long totalDataCount = carrierRepository.countByUserIdAndNameContains(getCarriersRequestModel.getUserId(), getCarriersRequestModel.getFilterExpr());

        PaginationBaseResponseModel<Carrier> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        if (carriers != null && !carriers.isEmpty()) {
            paginationBaseResponseModel.setData(HelperUtils.copyFields(carriers, Carrier.class));
            paginationBaseResponseModel.setTotalDataCount(totalDataCount);
        } else {
            paginationBaseResponseModel.setData(new ArrayList<>());
            paginationBaseResponseModel.setTotalDataCount(0);
        }
        return new Response<>(true, SuccessMessages.CarrierSuccessMessages.GetCarrier, paginationBaseResponseModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> updateApiKeys(Carrier carrier) {
        Optional<com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier> existingCarrier = carrierRepository.findById(carrier.getCarrierId());
        if (existingCarrier.isEmpty()) {
            return new Response<>(false, ErrorMessages.CarrierErrorMessages.InvalidId, false);
        }

        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        // add / delete jira issue types
        {
            // Retrieve existing issue types from the current carrier
            Set<String> existingIssueTypes = new HashSet<>(Arrays.asList(existingCarrier.get().getIssueTypes().split(",")));

            // Retrieve new issue types from the new carrier
            Set<String> newIssueTypes = new HashSet<>(Arrays.asList(carrier.getIssueTypes().split(",")));

            // Determine issue types to create (present in the new carrier but not in the existing carrier)
            Set<String> issueTypesToCreate = new HashSet<>(newIssueTypes);
            issueTypesToCreate.removeAll(existingIssueTypes);

            // Determine issue types to delete (present in the existing carrier but not in the new carrier)
            Set<String> issueTypesToDelete = new HashSet<>(existingIssueTypes);
            issueTypesToDelete.removeAll(newIssueTypes);

            // Create new issue types in Jira
            for (String issueType : issueTypesToCreate) {
                Response<CreateIssueTypeResponseModel> createIssueTypeResponse = jiraHelper.createIssueType(new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .registerTypeAdapter(Date.class, new DateAdapter())
                        .create().toJson(Map.of("name", issueType)));
                if (!createIssueTypeResponse.isSuccess()) {
                    return new Response<>(false, createIssueTypeResponse.getMessage(), false);
                }
            }

            for (String issueType : issueTypesToDelete) {
                Response<Boolean> deleteIssueTypeResponse = jiraHelper.deleteIssueType(issueType);
                if (!deleteIssueTypeResponse.isSuccess()) {
                    return new Response<>(false, deleteIssueTypeResponse.getMessage(), false);
                }
            }
        }

        // get all the issue types from jira
        boolean result = syncJiraWithDB(carrier.getCarrierId());
        if (!result) {
            return new Response<>(false, ErrorMessages.CarrierErrorMessages.ER003, false);
        }

        carrierRepository.save(HelperUtils.copyFields(carrier, com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier.class));
        userLogDataAccessor.logData(getUserId(), SuccessMessages.CarrierSuccessMessages.UpdatedCarrier, ApiRoutes.CarriersSubRoute.UPDATE_API_KEYS);
        return new Response<>(true, SuccessMessages.CarrierSuccessMessages.UpdatedCarrier, true);
    }

    @Override
    public Response<Carrier> getApiKeys() {
        Optional<com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier> carrier = carrierRepository.findById(getCarrierId());

        return carrier.map(value -> new Response<>(true, SuccessMessages.CarrierSuccessMessages.GetCarrier, HelperUtils.copyFields(value, Carrier.class)))
                .orElseGet(() -> new Response<>(false, ErrorMessages.CarrierErrorMessages.ER002, null));
    }

    @Override
    public Response<CarrierByWebTemplateWildCardResponse> getCarrierByWebTemplateWildCard(String wildCard) {
        WebTemplateCarrierMapping webTemplateCarrierMapping = webTemplateCarrierMappingRepository.findByWildCard(wildCard);
        if (webTemplateCarrierMapping != null) {
            Optional<com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier> carrier = carrierRepository.findById(webTemplateCarrierMapping.getCarrierId());
            if (carrier.isEmpty()) {
                return new Response<>(false, ErrorMessages.CarrierErrorMessages.InvalidId, null);
            }

            CarrierByWebTemplateWildCardResponse carrierByWebTemplateWildCardResponse = new CarrierByWebTemplateWildCardResponse()
                    .setCarrier(new Carrier()
                            .setCarrierId(webTemplateCarrierMapping.getCarrierId())
                            .setName(carrier.get().getName())
                            .setImage(carrier.get().getImage())
                            .setWebsite(carrier.get().getWebsite()))
                    .setWildCard(webTemplateCarrierMapping.getWildCard())
                    .setWebTemplateId(webTemplateCarrierMapping.getWebTemplateId())
                    .setApiAccessKey(webTemplateCarrierMapping.getApiAccessKey());

            return new Response<>(true, SuccessMessages.CarrierSuccessMessages.GetCarrier, carrierByWebTemplateWildCardResponse);
        }

        return new Response<>(false, ErrorMessages.CarrierErrorMessages.ER004, null);
    }

    @Override
    public Response<String> getTokenForWebTemplate(org.example.Models.CommunicationModels.CentralModels.WebTemplateCarrierMapping webTemplateCarrierMapping) {
        if (!StringUtils.hasText(webTemplateCarrierMapping.getWildCard())
                || !StringUtils.hasText(webTemplateCarrierMapping.getApiAccessKey())) {
            return new Response<>(false, ErrorMessages.CarrierErrorMessages.ER005, null);
        }

        WebTemplateCarrierMapping dbWebTemplateCarrierMapping = webTemplateCarrierMappingRepository.findByWildCard(webTemplateCarrierMapping.getWildCard());
        if (dbWebTemplateCarrierMapping == null) {
            return new Response<>(false, ErrorMessages.CarrierErrorMessages.ER004, null);
        }

        // check if the apikey is valid -> valid api key gives user access to the spring api
        if(!Objects.equals(webTemplateCarrierMapping.getApiAccessKey(), dbWebTemplateCarrierMapping.getApiAccessKey())) {
            return new Response<>(false, ErrorMessages.CarrierErrorMessages.ER006, null);
        }

        String token = jwtTokenProvider.generateToken(dbWebTemplateCarrierMapping);
        return new Response<>(true, SuccessMessages.Success, token);
    }
}