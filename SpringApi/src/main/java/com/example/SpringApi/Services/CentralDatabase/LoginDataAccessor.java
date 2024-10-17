package com.example.SpringApi.Services.CentralDatabase;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CentralDatabase.GoogleUserRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserCarrierPermissionMappingRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.SuccessMessages;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.EmailTemplates;
import org.example.CommonHelpers.HelperUtils;
import org.example.CommonHelpers.PasswordHelper;
import com.example.SpringApi.DatabaseModels.CentralDatabase.GoogleUser;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import com.example.SpringApi.DatabaseModels.CentralDatabase.UserCarrierPermissionMapping;
import org.example.Models.RequestModels.ApiRequestModels.LoginRequestModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.ILoginSubTranslator;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.IUserLogSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class LoginDataAccessor implements ILoginSubTranslator {
    private final UserRepository userRepository;
    private final GoogleUserRepository googleUserRepository;
    private final UserCarrierPermissionMappingRepository userCarrierPermissionMappingRepository;
    private final EmailTemplates emailTemplates;
    private final JwtTokenProvider jwtTokenProvider;
    private  final IUserLogSubTranslator userLogDataAccessor;

    @Autowired
    public LoginDataAccessor(UserRepository userRepository,
                             GoogleUserRepository googleUserRepository,
                             UserCarrierPermissionMappingRepository userCarrierPermissionMappingRepository,
                             UserLogDataAccessor userLogDataAccessor) {
        this.userRepository = userRepository;
        this.googleUserRepository = googleUserRepository;
        this.userCarrierPermissionMappingRepository = userCarrierPermissionMappingRepository;
        this.userLogDataAccessor = userLogDataAccessor;
        this.emailTemplates = new EmailTemplates("", "" , "");
        this.jwtTokenProvider = new JwtTokenProvider();
    }

    private Pair<String, Boolean> validateSignUp(org.example.Models.CommunicationModels.CentralModels.User user) {
        /*
        * required fields: LoginName, password, firstname, lastname, phone, dob
        * */

        if(!StringUtils.hasText(user.getLoginName())
        || !StringUtils.hasText(user.getPassword())
        || !StringUtils.hasText(user.getFirstName())
        || !StringUtils.hasText(user.getLastName())
        || !StringUtils.hasText(user.getPhone())
        || user.getDob() == null) {
            return Pair.of(ErrorMessages.LoginErrorMessages.ER013, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> confirmEmail(LoginRequestModel loginRequestModel) {
        Optional<User> userResponse = userRepository.findById(loginRequestModel.getUserId());
        if(userResponse.isPresent())
        {
            User user = userResponse.get();
            if(user.getApiKey().equals(loginRequestModel.getToken())){
                user.setEmailConfirmed(true);
                userRepository.save(user);
                userLogDataAccessor.logData(loginRequestModel.getUserId(), SuccessMessages.LoginSuccessMessages.UserEmailConfirmed, ApiRoutes.LoginSubRoute.CONFIRM_EMAIL);
                return new Response<>(true, SuccessMessages.LoginSuccessMessages.UserEmailConfirmed, true);
            }
            else{
                return new Response<>(false, ErrorMessages.LoginErrorMessages.InvalidToken, false);
            }
        }
        else {
            return new Response<>(false, ErrorMessages.LoginErrorMessages.InvalidId, false);
        }
    }

    @Override
    public Response<org.example.Models.CommunicationModels.CentralModels.User> signIn(LoginRequestModel loginRequestModel) {
        User user = userRepository.findByLoginName(loginRequestModel.getLoginName());

        if(!StringUtils.hasText(loginRequestModel.getLoginName())
        || !StringUtils.hasText(loginRequestModel.getPassword())){
            return new Response<>(false, ErrorMessages.LoginErrorMessages.ER012, null);
        }

        //check if the user was found
        if(user == null) {
            return new Response<>(false, ErrorMessages.LoginErrorMessages.InvalidEmail, null);
        }

        // check if the user's email has been confirmed
        if(!user.isEmailConfirmed()) {
            return new Response<>(false, ErrorMessages.LoginErrorMessages.ER005, null);
        }

        // check if the user account is locked
        if(user.isLocked()) {
            return new Response<>(false, ErrorMessages.LoginErrorMessages.ER006, null);
        }

        // check if the user has a password
        if(user.getPassword() == null || user.getPassword().isBlank() || user.getPassword().isEmpty()){
            return new Response<>(false, "Please use Oauth to sign in", null);
        }

        // check if the password is correct
        if(PasswordHelper.checkPassword(loginRequestModel.getPassword(), user.getPassword(), user.getSalt())){
            userLogDataAccessor.logData(user.getUserId(), SuccessMessages.LoginSuccessMessages.SuccessSignIn, ApiRoutes.LoginSubRoute.SIGN_IN);
            return new Response<>(true, SuccessMessages.LoginSuccessMessages.SuccessSignIn, HelperUtils.copyFields(user, org.example.Models.CommunicationModels.CentralModels.User.class));
        }

        // do the procedure for invalid login
        user.setLockedAttempts(user.getLockedAttempts() - 1);
        if(user.getLockedAttempts() == 0) {
            user.setLocked(true);
            userRepository.save(user);
            return new Response<>(false, ErrorMessages.LoginErrorMessages.ER007, null);
        }
        else {
            userRepository.save(user);
            return new Response<>(false, ErrorMessages.LoginErrorMessages.InvalidCredentials, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> signUp(org.example.Models.CommunicationModels.CentralModels.User newUser) throws Exception {
        Pair<String, Boolean> validation = validateSignUp(newUser);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

         /*
        1. if email exits and password == null and isguest == false -> user signed up using oauth
        2. is email exists and password == null and isguest == true -> user is a customer and did a guest checkout sometime in the system
        3. if email exists and password != null and isguest == false -> user already exists in the system
        */
        User user = userRepository.findByLoginName(newUser.getLoginName());
        GoogleUser googleUser = googleUserRepository.findByEmail(newUser.getLoginName());

        // if the user has an account already then they cant make an account with the same email
        if(googleUser != null || user != null) {
            return new Response<>(false, ErrorMessages.LoginErrorMessages.ER008, "");
        }

         /*
        Algorithm:
        1. generate user password and salt
        3. send in the confirmation email
        */

        // set the default user attributes
        String[] saltAndHash = PasswordHelper.getHashedPasswordAndSalt(newUser.getPassword());
        newUser.setSalt(saltAndHash[0]);
        newUser.setPassword(saltAndHash[1]);
        newUser.setApiKey(PasswordHelper.getToken(newUser.getLoginName()));
        newUser.setToken(PasswordHelper.getToken(newUser.getLoginName()));
        newUser.setLockedAttempts(5);

        User savedUser = userRepository.save(HelperUtils.copyFields(newUser, User.class));
        Response<Boolean> sendAccountConfirmationEmailResponse = emailTemplates.sendAccountConfirmationEmail(savedUser.getUserId(), newUser.getToken(), newUser.getLoginName());
        if(!sendAccountConfirmationEmailResponse.isSuccess()) {
            throw new Exception(sendAccountConfirmationEmailResponse.getMessage());
        }

        userLogDataAccessor.logData(savedUser.getUserId(), SuccessMessages.LoginSuccessMessages.SuccessSignedUp, ApiRoutes.LoginSubRoute.SIGN_UP);
        return new Response<>(true, SuccessMessages.LoginSuccessMessages.SuccessSignedUp, newUser.getApiKey());
    }

    @Override
    public Response<org.example.Models.CommunicationModels.CentralModels.User> googleSignIn(org.example.Models.CommunicationModels.CentralModels.GoogleUser googleUser) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> resetPassword(LoginRequestModel loginRequestModel) throws Exception {
        if(!StringUtils.hasText(loginRequestModel.getLoginName())){
            return new Response<>(false, ErrorMessages.LoginErrorMessages.ER014, false);
        }

        User user = userRepository.findByLoginName(loginRequestModel.getLoginName());
        if(user != null){
            if(user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()){
                return new Response<>(false, ErrorMessages.LoginErrorMessages.ER003, false);
            }
            String randomPassword = PasswordHelper.getRandomPassword();
            String[] saltAndHash = PasswordHelper.getHashedPasswordAndSalt(randomPassword);

            // set user defaults
            user.setSalt(saltAndHash[0]);
            user.setPassword(saltAndHash[1]);
            user.setLockedAttempts(5);
            user.setLocked(false);

            Response<Boolean> sendResetPasswordEmail = emailTemplates.sendResetPasswordEmail(user.getLoginName(), user.getPassword());
            if(!sendResetPasswordEmail.isSuccess()) {
                throw new Exception(sendResetPasswordEmail.getMessage());
            }

            userLogDataAccessor.logData(user.getUserId(), "Password Reset Successfully", ApiRoutes.LoginSubRoute.RESET_PASSWORD);
            return new Response<>(true, "Password Reset Successfully", false);
        }
        else{
            return new Response<>(false, ErrorMessages.LoginErrorMessages.InvalidEmail, false);
        }
    }

    @Override
    public Response<String> getToken(LoginRequestModel loginRequestModel) {
        if(!StringUtils.hasText(loginRequestModel.getLoginName())
        || !StringUtils.hasText(loginRequestModel.getApiKey())){
            return new Response<>(false, ErrorMessages.LoginErrorMessages.ER015, null);
        }

        // Find the user with the specified email
        User user = userRepository.findByLoginName(loginRequestModel.getLoginName());
        if(user == null){
            return new Response<>(false, ErrorMessages.LoginErrorMessages.InvalidCredentials, null);
        }

        // check if the apikey is valid -> valid api key gives user access to the spring api
        if(!StringUtils.hasText(user.getApiKey()) || !Objects.equals(user.getApiKey(), loginRequestModel.getApiKey())) {
            return new Response<>(false, ErrorMessages.LoginErrorMessages.InvalidCredentials, null);
        }

        // Generate a Jwt token
        List<UserCarrierPermissionMapping> userCarrierPermissionMapping = userCarrierPermissionMappingRepository.findCarrierPermissionMappingByUserId(user.getUserId());
        Map<Long, Long> carrierPermissionMapping = new HashMap<>();
        for(UserCarrierPermissionMapping mapping : userCarrierPermissionMapping){
            carrierPermissionMapping.put(mapping.getCarrierId(), mapping.getPermissionId());
        }

        String token = jwtTokenProvider.generateToken(user, carrierPermissionMapping, loginRequestModel.getApiKey());
        userLogDataAccessor.logData(user.getUserId(), "Successfully got token", ApiRoutes.LoginSubRoute.GET_TOKEN);
        return new Response<>(true, "Successfully got token", token);
    }
}