package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.UserGridPreference;
import com.example.SpringApi.Repository.CarrierDatabase.UserGridPreferenceRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.example.ApiRoutes;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.RequestModels.ApiRequestModels.GridPreferenceRequestModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IGridSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GridDataAccessor extends BaseDataAccessor implements IGridSubTranslator {

    private final UserGridPreferenceRepository userGridPreferenceRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public GridDataAccessor(HttpServletRequest request,
                            CarrierRepository carrierRepository,
                            UserGridPreferenceRepository userGridPreferenceRepository,
                            UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.userGridPreferenceRepository = userGridPreferenceRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    @Override
    public Response<Boolean> updateGridVisibilityPreference(GridPreferenceRequestModel gridPreferenceRequestModel) {
        UserGridPreference userGridPreference = userGridPreferenceRepository
                .findUserGridPreferenceByUserIdAndGridId(getUserId(), gridPreferenceRequestModel.getGridId());

        if(userGridPreference != null) {
            userGridPreference.setVisibilityModel(gridPreferenceRequestModel.getVisibilityJsonBody());
            userGridPreferenceRepository.save(userGridPreference);
        }
        else {
            userGridPreference = new UserGridPreference()
                    .setUserId(getUserId())
                    .setGridId(gridPreferenceRequestModel.getGridId())
                    .setVisibilityModel(gridPreferenceRequestModel.getVisibilityJsonBody());

            userGridPreferenceRepository.save(userGridPreference);
        }

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.UserGridPreferenceSuccessMessages.UpdateUserGridPreference + " " + userGridPreference.getUserGridPreferenceId(),
                ApiRoutes.GridSubRoute.UPDATE_GRID_VISIBILITY_PREFERENCE);
        return new Response<>(true, SuccessMessages.UserGridPreferenceSuccessMessages.UpdateUserGridPreference, true);
    }

    @Override
    public Response<Boolean> updateGridDensityVisibilityPreference(GridPreferenceRequestModel gridPreferenceRequestModel) {
        UserGridPreference userGridPreference = userGridPreferenceRepository
                .findUserGridPreferenceByUserIdAndGridId(getUserId(), gridPreferenceRequestModel.getGridId());

        if(userGridPreference != null) {
            userGridPreference.setDensity(gridPreferenceRequestModel.getDensity());
            userGridPreferenceRepository.save(userGridPreference);
        }
        else {
            userGridPreference = new UserGridPreference()
                    .setUserId(getUserId())
                    .setGridId(gridPreferenceRequestModel.getGridId())
                    .setDensity(gridPreferenceRequestModel.getDensity());

            userGridPreferenceRepository.save(userGridPreference);
        }

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.UserGridPreferenceSuccessMessages.UpdateUserGridPreference + " " + userGridPreference.getUserGridPreferenceId(),
                ApiRoutes.GridSubRoute.UPDATE_GRID_DENSITY_PREFERENCE);
        return new Response<>(true, SuccessMessages.UserGridPreferenceSuccessMessages.UpdateUserGridPreference, true);
    }

    @Override
    public Response<Boolean> updateRowsPerPagePreference(GridPreferenceRequestModel gridPreferenceRequestModel) {
        UserGridPreference userGridPreference = userGridPreferenceRepository
                .findUserGridPreferenceByUserIdAndGridId(getUserId(), gridPreferenceRequestModel.getGridId());

        if(userGridPreference != null) {
            userGridPreference.setRowsPerPage(gridPreferenceRequestModel.getRowsPerPage());
            userGridPreferenceRepository.save(userGridPreference);
        }
        else {
            userGridPreference = new UserGridPreference()
                    .setUserId(getUserId())
                    .setGridId(gridPreferenceRequestModel.getGridId())
                    .setRowsPerPage(gridPreferenceRequestModel.getRowsPerPage());

            userGridPreferenceRepository.save(userGridPreference);
        }

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.UserGridPreferenceSuccessMessages.UpdateUserGridPreference + " " + userGridPreference.getUserGridPreferenceId(),
                ApiRoutes.GridSubRoute.UPDATE_ROWS_PER_PAGE_PREFERENCE);
        return new Response<>(true, SuccessMessages.UserGridPreferenceSuccessMessages.UpdateUserGridPreference, true);
    }

    @Override
    public Response<org.example.Models.CommunicationModels.CarrierModels.UserGridPreference> getGridVisibilityPreference(int gridId) {
        UserGridPreference userGridPreference = userGridPreferenceRepository
                .findUserGridPreferenceByUserIdAndGridId(getUserId(), gridId);

        return new Response<>(true,
                SuccessMessages.UserGridPreferenceSuccessMessages.GetUserGridPreference,
                HelperUtils.copyFields(userGridPreference, org.example.Models.CommunicationModels.CarrierModels.UserGridPreference.class));
    }
}