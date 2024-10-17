package com.example.SpringApi.Services.CentralDatabase;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserLogRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import jakarta.servlet.http.HttpServletRequest;
import com.example.SpringApi.DatabaseModels.CentralDatabase.UserLog;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.RequestModels.GridRequestModels.GetUserLogsRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.IUserLogSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserLogDataAccessor extends BaseDataAccessor implements IUserLogSubTranslator {
    private final UserLogRepository userLogRepository;

    @Autowired
    public UserLogDataAccessor(UserLogRepository userLogRepository,
                               CarrierRepository carrierRepository,
                               HttpServletRequest request){
        super(request, carrierRepository);
        this.userLogRepository = userLogRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> logData(long userId, String change, String oldValue, String newValue) {
        UserLog userLog = new UserLog();
        userLog.setUserId(userId);
        userLog.setChange(change);
        userLog.setOldValue(oldValue);
        userLog.setNewValue(newValue);
        userLogRepository.save(userLog);
        return new Response<>(true, "Successfully saved user log", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> logData(long userId, String newValue, String endPoint) {
        UserLog userLog = new UserLog();
        userLog.setChange(endPoint);
        userLog.setUserId(userId);
        userLog.setNewValue(newValue);
        if(getUserId() == null){
            userLog.setAuditUserId(userId);
        }
        else{
            userLog.setAuditUserId(getUserId());
        }
        userLogRepository.save(userLog);
        return new Response<>(true, "Successfully saved user log", true);
    }


    /**
     * Retrieves user logs based on provided filtering criteria.
     * @param getUserLogsRequestModel The filtering criteria for retrieving user logs.
     * @return A response containing a pagination model with the user logs that match the filtering criteria.
     */
    @Override
    public Response<PaginationBaseResponseModel<org.example.Models.CommunicationModels.CentralModels.UserLog>> fetchUserLogsInBatches(GetUserLogsRequestModel getUserLogsRequestModel) {
        // validate the column names
        if(StringUtils.hasText(getUserLogsRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(Arrays.asList("change", "oldValue", "newValue"));

            if(!validColumns.contains(getUserLogsRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        Page<UserLog> userLogs = userLogRepository.findPaginatedUserLogs(getUserLogsRequestModel.getUserId(),
                getUserLogsRequestModel.getCarrierId(),
                getUserLogsRequestModel.getColumnName(),
                getUserLogsRequestModel.getCondition(),
                getUserLogsRequestModel.getFilterExpr(),
                PageRequest.of(getUserLogsRequestModel.getStart() / (getUserLogsRequestModel.getEnd() - getUserLogsRequestModel.getStart()),
                        getUserLogsRequestModel.getEnd() - getUserLogsRequestModel.getStart(),
                        Sort.by("logId").ascending()));

        PaginationBaseResponseModel<org.example.Models.CommunicationModels.CentralModels.UserLog> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(HelperUtils.copyFields(userLogs.getContent(), org.example.Models.CommunicationModels.CentralModels.UserLog.class));
        paginationBaseResponseModel.setTotalDataCount(userLogs.getTotalElements());

        return new Response<>(true, "Got user logs.", paginationBaseResponseModel);
    }
}