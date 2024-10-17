package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.UserGroupRepository;
import com.example.SpringApi.Repository.CarrierDatabase.UserGroupsUsersMapRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.UserGroup;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.UserGroupsUsersMap;
import org.example.CommonHelpers.HelperUtils;
import org.example.CommonHelpers.Validations;
import org.example.Models.RequestModels.ApiRequestModels.UserGroupRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.UserGroupResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IUserGroupSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class UserGroupDataAccessor extends BaseDataAccessor implements IUserGroupSubTranslator {

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserGroupsUsersMapRepository userGroupsUsersMapRepository;
    private final UserLogDataAccessor userLogDataAccessor;
    @Autowired
    public UserGroupDataAccessor(HttpServletRequest request,
                                 UserLogDataAccessor userLogDataAccessor,
                                 CarrierRepository carrierRepository,
                                 UserRepository userRepository,
                                 UserGroupRepository userGroupRepository,
                                 UserGroupsUsersMapRepository userGroupsUsersMapRepository) {
        super(request, carrierRepository);
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.userLogDataAccessor = userLogDataAccessor;
        this.userGroupsUsersMapRepository = userGroupsUsersMapRepository;
    }

    public Pair<String, Boolean> validateUserGroup(UserGroup userGroup, List<Long> userIds) {
        /*
        * Required fields => name, description, atleast one userId
        * */
        if(!Validations.isValidName(userGroup.getName())){
            return Pair.of(ErrorMessages.UserGroupErrorMessages.ER002, false);
        }
        if(!StringUtils.hasText(userGroup.getDescription())) {
            return Pair.of(ErrorMessages.UserGroupErrorMessages.ER003, false);
        }

        if(userIds == null || userIds.isEmpty()) {
            return Pair.of(ErrorMessages.UserGroupErrorMessages.ER004, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<PaginationBaseResponseModel<UserGroupResponseModel>> getUserGroupsInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // validate the column names
        if(StringUtils.hasText(paginationBaseRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(Arrays.asList("name", "description"));

            if(!validColumns.contains(paginationBaseRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        Page<Object[]> userGroups = userGroupRepository.findPaginatedUserGroups(paginationBaseRequestModel.getColumnName(),
                paginationBaseRequestModel.getCondition(),
                paginationBaseRequestModel.getFilterExpr(),
                paginationBaseRequestModel.isIncludeDeleted(),
                paginationBaseRequestModel.getSelectedIds(),
                PageRequest.of(paginationBaseRequestModel.getStart() / (paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart()),
                        paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart()));

        List<UserGroupResponseModel> userGroupResponseModels = new ArrayList<>();
        for (Object[] result : userGroups.getContent()) {
            UserGroupResponseModel userGroupResponseModel = new UserGroupResponseModel();
            userGroupResponseModel.setUserGroup(HelperUtils.copyFields(result[0], org.example.Models.CommunicationModels.CarrierModels.UserGroup.class));
            userGroupResponseModel.setUserCount((int)(long)result[1]);
            userGroupResponseModels.add(userGroupResponseModel);
        }

        PaginationBaseResponseModel<UserGroupResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(userGroupResponseModels);
        paginationBaseResponseModel.setTotalDataCount(userGroups.getTotalElements());

        return new Response<>(true, SuccessMessages.GroupsSuccessMessages.GetGroups, paginationBaseResponseModel);
    }

    @Override
    public Response<UserGroupResponseModel> getUserGroupDetailsById(long groupId) {
        Optional<UserGroup> existingGroup = userGroupRepository.findById(groupId);
        if(existingGroup.isEmpty()){
            return new Response<>(false, ErrorMessages.UserGroupErrorMessages.InvalidId, null);
        }

        UserGroupResponseModel userGroupResponseModel = new UserGroupResponseModel();
        userGroupResponseModel.setUserGroup(HelperUtils.copyFields(existingGroup.get(), org.example.Models.CommunicationModels.CarrierModels.UserGroup.class));

        List<UserGroupsUsersMap> userGroupsUsersMaps = userGroupsUsersMapRepository.findByUserGroupId(existingGroup.get().getUserGroupId());
        List<Long> userIds = userGroupsUsersMaps.stream()
                .map(UserGroupsUsersMap::getUserId) // Concisely map to userId
                .toList();
        userGroupResponseModel.setUserIds(userIds);

        return new Response<>(true,  SuccessMessages.GroupsSuccessMessages.GetGroup, userGroupResponseModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> createUserGroup(UserGroupRequestModel userGroupRequestModel) {
        UserGroup userGroup = HelperUtils.copyFields(userGroupRequestModel.getUserGroup(), UserGroup.class);

        Pair<String, Boolean> validation = validateUserGroup(userGroup, userGroupRequestModel.getUserIds());
        if(!validation.getValue()) {
            return new Response<>(false, validation.getKey(), null);
        }

        UserGroup group = userGroupRepository.findByName(userGroupRequestModel.getUserGroup().getName().trim().toLowerCase());
        if(group != null) {
            return new Response<>(false, ErrorMessages.UserGroupErrorMessages.GroupNameExists, null);
        }

        UserGroup savedGroup = userGroupRepository.save(userGroup);
        if(userGroupRequestModel.getUserIds() != null && !userGroupRequestModel.getUserIds().isEmpty()){
            List<UserGroupsUsersMap> userGroupsUsersMaps = new ArrayList<UserGroupsUsersMap>();
            for(long userId : userGroupRequestModel.getUserIds()){
                UserGroupsUsersMap userGroupsUsersMap = new UserGroupsUsersMap();
                userGroupsUsersMap.setUserId(userId);
                userGroupsUsersMap.setUserGroupId(savedGroup.getUserGroupId());

                userGroupsUsersMaps.add(userGroupsUsersMap);
            }

            userGroupsUsersMapRepository.saveAll(userGroupsUsersMaps);
        }

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.GroupsSuccessMessages.InsertGroup + " "  + savedGroup.getUserGroupId(),
                ApiRoutes.UserGroupsSubRoute.CREATE_USER_GROUP);
        return new Response<>(true, SuccessMessages.GroupsSuccessMessages.InsertGroup, savedGroup.getUserGroupId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> updateUserGroup(UserGroupRequestModel userGroupRequestModel) {
        UserGroup userGroup = HelperUtils.copyFields(userGroupRequestModel.getUserGroup(), UserGroup.class);

        Pair<String, Boolean> validation = validateUserGroup(userGroup, userGroupRequestModel.getUserIds());
        if(!validation.getValue()) {
            return new Response<>(false, validation.getKey(), null);
        }

        Optional<UserGroup> existingGroup = userGroupRepository.findById(userGroupRequestModel.getUserGroup().getUserGroupId());
        if(existingGroup.isEmpty()){
            return new Response<>(false, ErrorMessages.UserGroupErrorMessages.InvalidId, null);
        }

        // save the user group
        existingGroup.get().setName(userGroup.getName());
        existingGroup.get().setDescription(userGroup.getDescription());
        existingGroup.get().setNotes(userGroup.getNotes());
        userGroupRepository.save(existingGroup.get());

        // delete all the existing mappings
        List<UserGroupsUsersMap> existingUserGroupsUsersMaps = userGroupsUsersMapRepository.findByUserGroupId(userGroupRequestModel.getUserGroup().getUserGroupId());
        if(existingUserGroupsUsersMaps != null && !existingUserGroupsUsersMaps.isEmpty()){
            userGroupsUsersMapRepository.deleteAll(existingUserGroupsUsersMaps);
        }

        // save the new mappings
        if(userGroupRequestModel.getUserIds() != null && !userGroupRequestModel.getUserIds().isEmpty()){
            List<UserGroupsUsersMap> userGroupsUsersMaps = new ArrayList<UserGroupsUsersMap>();
            for(long userId : userGroupRequestModel.getUserIds()){
                UserGroupsUsersMap userGroupsUsersMap = new UserGroupsUsersMap();
                userGroupsUsersMap.setUserId(userId);
                userGroupsUsersMap.setUserGroupId(userGroupRequestModel.getUserGroup().getUserGroupId());

                userGroupsUsersMaps.add(userGroupsUsersMap);
            }

            userGroupsUsersMapRepository.saveAll(userGroupsUsersMaps);
        }

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.GroupsSuccessMessages.UpdateGroup + " "  + existingGroup.get().getUserGroupId(),
                ApiRoutes.UserGroupsSubRoute.UPDATE_USER_GROUP);
        return new Response<>(true, SuccessMessages.GroupsSuccessMessages.UpdateGroup, userGroupRequestModel.getUserGroup().getUserGroupId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> toggleUserGroup(long groupId) {
        Optional<UserGroup> group = userGroupRepository.findById(groupId);
        if(group.isPresent()){
            group.get().setDeleted(!group.get().isDeleted());
            userGroupRepository.save(group.get());
            userLogDataAccessor.logData(getUserId(), SuccessMessages.GroupsSuccessMessages.ToggleGroup + " " + group.get().getUserGroupId(), ApiRoutes.UserGroupsSubRoute.TOGGLE_USER_GROUP);
            return new Response<>(true, SuccessMessages.Success, false);
        }
        else{
            return new Response<>(false, ErrorMessages.UserGroupErrorMessages.InvalidId, null);
        }
    }

    @Override
    public Response<List<Long>> getUserGroupIdsByUserId(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()){
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
        }

        List<Long> userGroupIds = userGroupRepository.getUserGroupIdsFromUserId(userId);
        return new Response<>(true, SuccessMessages.Success, userGroupIds);
    }
}