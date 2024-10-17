package com.example.SpringApi.Services.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.*;
import com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import com.example.SpringApi.DatabaseModels.CentralDatabase.UserCarrierMapping;
import com.example.SpringApi.DatabaseModels.CentralDatabase.UserCarrierPermissionMapping;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.AddressRepository;
import com.example.SpringApi.Repository.CarrierDatabase.UserGroupRepository;
import com.example.SpringApi.Repository.CarrierDatabase.PermissionRepository;
import com.example.SpringApi.Repository.CarrierDatabase.UserGroupsUsersMapRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserCarrierMappingRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserCarrierPermissionMappingRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.*;
import org.example.Models.RequestModels.ApiRequestModels.ImportUsersRequestModel;
import org.example.Models.RequestModels.ApiRequestModels.UsersRequestModel;
import org.example.Models.RequestModels.GridRequestModels.GetUsersRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.IUserSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserDataAccessor extends BaseDataAccessor implements IUserSubTranslator {
    private final UserRepository userRepository;
    private final UserLogDataAccessor userLogDataAccessor;
    private final AddressRepository addressRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserCarrierMappingRepository userCarrierMappingRepository;
    private final UserGroupsUsersMapRepository userGroupsUsersMapRepository;
    private final PermissionRepository permissionRepository;
    private final UserCarrierPermissionMappingRepository userCarrierPermissionMappingRepository;
    private EmailTemplates emailTemplates;
    @Autowired
    public UserDataAccessor(HttpServletRequest request,
                            UserRepository userRepository,
                            UserCarrierMappingRepository userCarrierMappingRepository,
                            CarrierRepository carrierRepository,
                            AddressRepository addressRepository,
                            UserGroupRepository userGroupRepository,
                            UserLogDataAccessor userLogDataAccessor,
                            UserGroupsUsersMapRepository userGroupsUsersMapRepository,
                            PermissionRepository permissionRepository,
                            UserCarrierPermissionMappingRepository userCarrierPermissionMappingRepository) {
        super(request, carrierRepository);
        this.userRepository = userRepository;
        this.userCarrierMappingRepository = userCarrierMappingRepository;
        this.userLogDataAccessor = userLogDataAccessor;
        this.addressRepository= addressRepository;
        this.userGroupRepository = userGroupRepository;
        this.userGroupsUsersMapRepository = userGroupsUsersMapRepository;
        this.permissionRepository = permissionRepository;
        this.userCarrierPermissionMappingRepository = userCarrierPermissionMappingRepository;
    }
    private boolean authorizedToFetchThisUserDetails(long requestedUserId){
        // both the user ids should be present in the same carrier.
        UserCarrierMapping userCarrierMapping = userCarrierMappingRepository.findByUserIdAndCarrierId(requestedUserId, getCarrierId());
        return userCarrierMapping != null;
    }

    private Pair<String, Boolean> validateUser(org.example.Models.CommunicationModels.CentralModels.User user,
                                               org.example.Models.CommunicationModels.CarrierModels.Address address,
                                               List<Long> groupIds) {
        /*
        * Required fields in user -> login name, first name, lastname, role, dob and phone
        * */
        if(!StringUtils.hasText(user.getLoginName()) && !Validations.isValidEmail(user.getLoginName())) {
            return Pair.of(ErrorMessages.UserErrorMessages.ER004, false);
        }

        if(!Validations.isValidName(user.getFirstName())) {
            return Pair.of(ErrorMessages.UserErrorMessages.ER005, false);
        }

        if(!Validations.isValidName(user.getLastName())) {
            return Pair.of(ErrorMessages.UserErrorMessages.ER006, false);
        }

        if(!StringUtils.hasText(user.getRole()) && !HelperUtils.getRoles().contains(user.getRole())) {
            return Pair.of(ErrorMessages.UserErrorMessages.ER007 + String.join(",", HelperUtils.getRoles()), false);
        }

        if(user.getDob() == null) {
            return Pair.of(ErrorMessages.UserErrorMessages.ER008, false);
        }

        if(!StringUtils.hasText(user.getPhone()) && !Validations.isValidPhone(user.getPhone())) {
            return Pair.of(ErrorMessages.UserErrorMessages.ER009, false);
        }

        /*
        * Required fields in address -> line1, city, state, zipcode, name on address and phone on address
        * */
        Response<Boolean> addressValidationResponse = Validations.isValidAddress(address.getLine1(), address.getState(), address.getCity(), address.getZipCode(), address.getPhoneOnAddress(), address.getNameOnAddress());
        if(!addressValidationResponse.isSuccess()){
            return Pair.of(ErrorMessages.InvalidAddress, false);
        }

        /*
        *  if list of group ids is not empty then check all the group ids are valid
        * */
        if(userGroupRepository.findAllById(groupIds).size() != groupIds.size()) {
            return Pair.of(ErrorMessages.UserGroupErrorMessages.ER001, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<org.example.Models.CommunicationModels.CarrierModels.Permissions> getUserPermissionsById(long id) {
        // check if the current user is authorized to fetch details for the given user.
        boolean authorized = authorizedToFetchThisUserDetails(id);
        if(!authorized){
            return new Response<>(false, ErrorMessages.UserErrorMessages.Unauthorized, null);
        }

        Optional<User> userInDb = userRepository.findById(id);
        if(userInDb.isEmpty()){
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
        }

        List<UserCarrierPermissionMapping> userCarrierPermissionMappings = userCarrierPermissionMappingRepository.findCarrierPermissionMappingByUserId(id);
        if(userCarrierPermissionMappings != null && !userCarrierPermissionMappings.isEmpty()) {
            Optional<UserCarrierPermissionMapping> userCarrierPermissionMapping = userCarrierPermissionMappings.stream()
                    .filter(mapping -> mapping.getCarrierId() == getCarrierId())
                    .findFirst();
            if(userCarrierPermissionMapping.isPresent()) {
                Optional<Permissions> permission = permissionRepository.findById(userCarrierPermissionMapping.get().getPermissionId());
                if(permission.isPresent()){
                    return new Response<>(true, SuccessMessages.Success, HelperUtils.copyFields(permission.get(), org.example.Models.CommunicationModels.CarrierModels.Permissions.class));
                }
            }
        }

        return new Response<>(false, ErrorMessages.UserErrorMessages.ER003, null);

    }

    @Override
    public Response<org.example.Models.CommunicationModels.CentralModels.User> getUserByEmail(String email) {
        User user = userRepository.findByLoginName(email);
        return user == null ? new Response<>(false, ErrorMessages.UserErrorMessages.InvalidEmail, null)
                : new Response<>(false, SuccessMessages.UserSuccessMessages.GetUser, HelperUtils.copyFields(user, org.example.Models.CommunicationModels.CentralModels.User.class));
    }

    @Override
    public Response<org.example.Models.CommunicationModels.CentralModels.User> getUserById(long userId) {
        // check if the current user is authorized to fetch details for the given user.
        boolean authorized = authorizedToFetchThisUserDetails(userId);
        if(!authorized){
            return new Response<>(false, ErrorMessages.UserErrorMessages.Unauthorized, null);
        }

        Optional<User> user = userRepository.findById(userId);
        return user.map(value -> new Response<>(true, SuccessMessages.UserSuccessMessages.GetUser, HelperUtils.copyFields(value, org.example.Models.CommunicationModels.CentralModels.User.class)))
                .orElseGet(() -> new Response<>(false, ErrorMessages.UserErrorMessages.InvalidEmail, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> createUser(UsersRequestModel usersRequestModel) {
        // check if the user is already in the system or no
        Response<Boolean> isEmailAvailableInSystemResponse = isEmailAvailableInSystem(usersRequestModel.getUser().getLoginName());
        if(isEmailAvailableInSystemResponse.isSuccess()){
            if(!isEmailAvailableInSystemResponse.getItem()){
                return new Response<>(false, ErrorMessages.UserErrorMessages.EmailExists, null);
            }
        }
        else {
            return new Response<>(false, isEmailAvailableInSystemResponse.getMessage(), null);
        }

        // add the required address fields
        usersRequestModel.getAddress().setNameOnAddress(usersRequestModel.getUser().getFirstName() + " " + usersRequestModel.getUser().getLastName());
        usersRequestModel.getAddress().setPhoneOnAddress(usersRequestModel.getUser().getPhone());

        // validate the request data
        Pair<String, Boolean> validation = validateUser(usersRequestModel.getUser(), usersRequestModel.getAddress(), usersRequestModel.getUserGroupIds());
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        // clean the data
        usersRequestModel.getUser().setPhone(DataCleaner.cleanPhone(usersRequestModel.getUser().getPhone()));

        String password = PasswordHelper.getRandomPassword();
        String[] saltAndHash = PasswordHelper.getHashedPasswordAndSalt(password);
        usersRequestModel.getUser().setSalt(saltAndHash[0]);
        usersRequestModel.getUser().setPassword(saltAndHash[1]);
        usersRequestModel.getUser().setApiKey(PasswordHelper.getToken(usersRequestModel.getUser().getLoginName()));
        usersRequestModel.getUser().setToken(PasswordHelper.getToken(usersRequestModel.getUser().getLoginName()));
        usersRequestModel.getUser().setLockedAttempts(5);
        usersRequestModel.getUser().setAuditUserId(getUserId());
        User savedUser = userRepository.save(HelperUtils.copyFields(usersRequestModel.getUser(), com.example.SpringApi.DatabaseModels.CentralDatabase.User.class));

        // add the user to the carrier
        UserCarrierMapping userCarrierMapping = new UserCarrierMapping();
        userCarrierMapping.setCarrierId(getCarrierId());
        userCarrierMapping.setUserId(savedUser.getUserId());
        userCarrierMappingRepository.save(userCarrierMapping);

        //save the address
        usersRequestModel.getAddress().setUserId(savedUser.getUserId());
        usersRequestModel.getAddress().setPhoneOnAddress(DataCleaner.cleanPhone(usersRequestModel.getUser().getPhone()));

        addressRepository.save(HelperUtils.copyFields(usersRequestModel.getAddress(), com.example.SpringApi.DatabaseModels.CarrierDatabase.Address.class));

        // save user group mappings
        List<UserGroupsUsersMap> userGroupsUsersMaps = new ArrayList<>();
        for(UserGroup group : userGroupRepository.findAllById(usersRequestModel.getUserGroupIds())){
            UserGroupsUsersMap userGroupsUsersMap = new UserGroupsUsersMap();
            userGroupsUsersMap.setUserId(savedUser.getUserId());
            userGroupsUsersMap.setUserGroupId(group.getUserGroupId());
            userGroupsUsersMaps.add(userGroupsUsersMap);
        }
        userGroupsUsersMapRepository.saveAll(userGroupsUsersMaps);

        //save permissions and add the user mappings
        Permissions savedPermission = permissionRepository.save(HelperUtils.copyFields(usersRequestModel.getPermissions(), com.example.SpringApi.DatabaseModels.CarrierDatabase.Permissions.class));
        UserCarrierPermissionMapping userCarrierPermissionMapping = new UserCarrierPermissionMapping();
        userCarrierPermissionMapping.setUserId(savedUser.getUserId());
        userCarrierPermissionMapping.setPermissionId(savedPermission.getPermissionId());
        userCarrierPermissionMapping.setCarrierId(getCarrierId());
        userCarrierPermissionMappingRepository.save(userCarrierPermissionMapping);

        // send account confirmation email
        Carrier carrier = getCarrierDetails();
//        this.emailTemplates = new EmailTemplates(carrier.getSendgridSenderName(), carrier.getSendgridEmailAddress(), carrier.getSendgridApikey());
//        Response<Boolean> sendAccountConfirmationEmailResponse = emailTemplates.sendNewUserAccountConfirmation(savedUser.getUserId(),
//                user.getToken(),
//                user.getLoginName(),
//                password);
//        if(!sendAccountConfirmationEmailResponse.isSuccess()) {
//            throw new Exception(sendAccountConfirmationEmailResponse.getMessage());
//        }

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.UserSuccessMessages.InsertUser +  " " + savedUser.getUserId(),
                ApiRoutes.UsersSubRoute.CREATE_USER);
        return new Response<>(true, SuccessMessages.UserSuccessMessages.InsertUser, savedUser.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> updateUser(UsersRequestModel usersRequestModel) {
        // check if the current user is authorized to fetch details for the given user.
        boolean authorized = authorizedToFetchThisUserDetails(usersRequestModel.getUser().getUserId());
        if(!authorized){
            return new Response<>(false, ErrorMessages.UserErrorMessages.Unauthorized, null);
        }

        Optional<User> userInDb = userRepository.findById(usersRequestModel.getUser().getUserId());
        if(userInDb.isEmpty()){
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidEmail, null);
        }

        Pair<String, Boolean> validation = validateUser(usersRequestModel.getUser(), usersRequestModel.getAddress(), usersRequestModel.getUserGroupIds());
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        // clean the data
        userInDb.get().setPhone(DataCleaner.cleanPhone(usersRequestModel.getUser().getPhone()));

        userInDb.get().setFirstName(usersRequestModel.getUser().getFirstName());
        userInDb.get().setLastName(usersRequestModel.getUser().getLastName());
        userInDb.get().setPhone(usersRequestModel.getUser().getPhone());
        userInDb.get().setRole(usersRequestModel.getUser().getRole());
        userInDb.get().setDob(usersRequestModel.getUser().getDob());
        userRepository.save(userInDb.get());

        //save the address
        Address existingAddressForGivenUser = addressRepository.findByUserId(userInDb.get().getUserId());
        if(existingAddressForGivenUser != null){
            existingAddressForGivenUser.setLine1(usersRequestModel.getAddress().getLine1());
            existingAddressForGivenUser.setLine2(usersRequestModel.getAddress().getLine2());
            existingAddressForGivenUser.setLandmark(usersRequestModel.getAddress().getLandmark());
            existingAddressForGivenUser.setState(usersRequestModel.getAddress().getState());
            existingAddressForGivenUser.setCity(usersRequestModel.getAddress().getCity());
            existingAddressForGivenUser.setZipCode(usersRequestModel.getAddress().getZipCode());
            addressRepository.save(existingAddressForGivenUser);
        }
        else {
            // add the new address
            usersRequestModel.getAddress().setUserId(usersRequestModel.getUser().getUserId());
            usersRequestModel.getAddress().setNameOnAddress(usersRequestModel.getUser().getFirstName() + " " + usersRequestModel.getUser().getLastName());
            usersRequestModel.getAddress().setPhoneOnAddress(usersRequestModel.getUser().getPhone());
            addressRepository.save(HelperUtils.copyFields(usersRequestModel.getAddress(), Address.class));
        }

        // delete existing userid - groupid mappings
        List<UserGroupsUsersMap>  userGroupsUsersMaps = userGroupsUsersMapRepository.findUserGroupsUsersMapByGroupIdAndUserId(usersRequestModel.getUserGroupIds(), usersRequestModel.getUser().getUserId());
        if(userGroupsUsersMaps != null && !userGroupsUsersMaps.isEmpty()){
            userGroupsUsersMapRepository.deleteAll(userGroupsUsersMaps);
        }

        // add new userid - groupid mappings
        userGroupsUsersMaps = new ArrayList<>();
        for(UserGroup group : userGroupRepository.findAllById(usersRequestModel.getUserGroupIds())){
            UserGroupsUsersMap userGroupsUsersMap = new UserGroupsUsersMap();
            userGroupsUsersMap.setUserId(userInDb.get().getUserId());
            userGroupsUsersMap.setUserGroupId(group.getUserGroupId());
            userGroupsUsersMaps.add(userGroupsUsersMap);
        }
        userGroupsUsersMapRepository.saveAll(userGroupsUsersMaps);

        //update permissions for user
        permissionRepository.save(HelperUtils.copyFields(usersRequestModel.getPermissions(), Permissions.class)); // permission id has to be passed over here
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.UserSuccessMessages.UpdateUser +  " " + usersRequestModel.getUser().getUserId(),
                ApiRoutes.UsersSubRoute.UPDATE_USER);
        return new Response<>(true, SuccessMessages.UserSuccessMessages.UpdateUser, usersRequestModel.getUser().getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> toggleUser(long userId) {
        // check if the current user is authorized to fetch details for the given user.
        boolean authorized = authorizedToFetchThisUserDetails(userId);
        if(!authorized){
            return new Response<>(false, ErrorMessages.UserErrorMessages.Unauthorized, null);
        }

        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            user.get().setDeleted(!user.get().isDeleted());
            userRepository.save(user.get());
            userLogDataAccessor.logData(getUserId(), SuccessMessages.UserSuccessMessages.ToggleUser +  " " + user.get().getUserId(),
                    ApiRoutes.UsersSubRoute.TOGGLE_USER);
            return new Response<>(true, SuccessMessages.UserSuccessMessages.ToggleUser +  " " + user.get().getUserId(), userId);
        }
        else{
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
        }
    }

    @Override
    public Response<List<org.example.Models.CommunicationModels.CentralModels.User>> fetchUsersInCarrier(boolean includeDeleted) {
        List<User> users = userRepository.findAllWithIncludeDeletedInCarrier(includeDeleted, getCarrierId());
        return new Response<>(true, SuccessMessages.UserSuccessMessages.GotUsers, HelperUtils.copyFields(users, org.example.Models.CommunicationModels.CentralModels.User.class));
    }

    @Override
    public Response<Boolean> isEmailAvailableInSystem(String email) {
        User user = userRepository.findByLoginName(email);
        return new Response<>(true, SuccessMessages.Success, user == null);
    }

    @Override
    public Response<String> importUsers(ImportUsersRequestModel importUsersRequestModel) throws Exception {

        String[][] usersDatatable = ExcelHelper.deSerializeStringTo2DWorkbook(importUsersRequestModel.getUsersDataTable());
        if (usersDatatable.length < 2) {
            return new Response<>(false, ErrorMessages.UserErrorMessages.ER001, null);
        }
        if (usersDatatable.length > 100) {
            return new Response<>(false, ErrorMessages.UserErrorMessages.ER002, null);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<User, String> userPasswordMapping = new HashMap<>();
        for (String[] row : usersDatatable) {
            User newUser = new User();
            Address newAddress = new Address();
            String password = PasswordHelper.getRandomPassword();
            String[] saltAndHash = PasswordHelper.getHashedPasswordAndSalt(password);

            User user = userRepository.findByLoginName(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.Email)]);
            if(user != null){
                throw new Exception(ErrorMessages.LoginErrorMessages.ER010);
            }

            // set the user fields
            newUser.setFirstName(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.FirstName)]);
            newUser.setLastName(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.LastName)]);
            newUser.setLoginName(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.Email)]);
            newUser.setRole(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.Role)]);
            newUser.setDob(dateFormat.parse(dateFormat.format(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.DOB)])));
            newUser.setPhone(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.Phone)]);
            newUser.setSalt(saltAndHash[0]);
            newUser.setPassword(saltAndHash[1]);
            newUser.setApiKey(PasswordHelper.getToken(newUser.getLoginName()));
            newUser.setToken(PasswordHelper.getToken(newUser.getLoginName()));
            newUser.setLockedAttempts(5);
            newUser.setAuditUserId(getUserId());
            User savedUser = userRepository.save(newUser);

            // set the address fields
            newAddress.setLine1(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.AddressLine1)]);
            newAddress.setLine2(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.AddressLine2)]);
            newAddress.setCity(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.City)]);
            newAddress.setState(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.State)]);
            newAddress.setZipCode(row[ImportUsersRequestModel.headers.indexOf(ImportUsersRequestModel.Header.ZipCode)]);
            newAddress.setAuditUserId(getUserId());
            newAddress.setUserId(savedUser.getUserId());

            addressRepository.save(newAddress);
            userPasswordMapping.put(savedUser, password);
        }

        Carrier carrier = getCarrierDetails();
        this.emailTemplates = new EmailTemplates(carrier.getSendgridSenderName(), carrier.getSendgridEmailAddress(), carrier.getSendgridApikey());
        for(Map.Entry<User, String> mapping : userPasswordMapping.entrySet()){
            Response<Boolean> sendAccountConfirmationEmailResponse = emailTemplates.sendNewUserAccountConfirmation(mapping.getKey().getUserId(),
                    mapping.getKey().getToken(),
                    mapping.getKey().getLoginName(),
                    mapping.getValue());

            if(!sendAccountConfirmationEmailResponse.isSuccess()) {
                throw new Exception(sendAccountConfirmationEmailResponse.getMessage());
            }
        }

        return new Response<>(true, "Successfully created users", "Success");
    }

    @Override
    public Response<PaginationBaseResponseModel<org.example.Models.CommunicationModels.CentralModels.User>> fetchUsersInCarrierInBatches(GetUsersRequestModel getUsersRequestModel) {
        // validate the column names
        if(StringUtils.hasText(getUsersRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(Arrays.asList("firstName", "lastName", "loginName", "role", "dob", "phone"));

            if(!validColumns.contains(getUsersRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        Page<User> users = userRepository.findPaginatedUsers(getCarrierId(),
                getUsersRequestModel.getSelectedUsers(),
                getUsersRequestModel.getColumnName(),
                getUsersRequestModel.getCondition(),
                getUsersRequestModel.getFilterExpr(),
                getUsersRequestModel.isIncludeDeleted(),
                PageRequest.of(getUsersRequestModel.getStart() / (getUsersRequestModel.getEnd() - getUsersRequestModel.getStart()),
                        getUsersRequestModel.getEnd() - getUsersRequestModel.getStart(),
                        Sort.by("userId").descending()));

        PaginationBaseResponseModel<org.example.Models.CommunicationModels.CentralModels.User> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(HelperUtils.copyFields(users.getContent(), org.example.Models.CommunicationModels.CentralModels.User.class));
        paginationBaseResponseModel.setTotalDataCount(users.getTotalElements());

        return new Response<>(true, SuccessMessages.UserSuccessMessages.GotUsers, paginationBaseResponseModel);
    }
}
