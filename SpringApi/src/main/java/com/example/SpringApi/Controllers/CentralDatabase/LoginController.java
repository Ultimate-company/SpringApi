package com.example.SpringApi.Controllers.CentralDatabase;

import com.example.SpringApi.Services.CentralDatabase.LoginDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.CommunicationModels.CentralModels.User;
import org.example.Models.RequestModels.ApiRequestModels.LoginRequestModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.ILoginSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.LOGIN + "/")
public class LoginController {
    private final ILoginSubTranslator accessor;

    @Autowired
    public LoginController(LoginDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PostMapping(ApiRoutes.LoginSubRoute.CONFIRM_EMAIL)
    public ResponseEntity<Response<Boolean>> confirmEmail(@RequestBody LoginRequestModel loginRequestModel) {
        return ResponseEntity.ok(accessor.confirmEmail(loginRequestModel));
    }

    @PostMapping(ApiRoutes.LoginSubRoute.SIGN_IN)
    public ResponseEntity<Response<User>> signIn(@RequestBody LoginRequestModel loginRequestModel) {
        return ResponseEntity.ok(accessor.signIn(loginRequestModel));
    }

    @PutMapping(ApiRoutes.LoginSubRoute.SIGN_UP)
    public ResponseEntity<Response<String>> signUp(@RequestBody User newUser) throws Exception {
        return ResponseEntity.ok(accessor.signUp(newUser));
    }

//    @PostMapping(ApiRoutes.LoginSubRoute.GOOGLE_SIGN_IN)
//    public ResponseEntity<Response<User>> googleSignIn(@RequestBody GoogleUser googleuser) {
//        return ResponseEntity.ok(accessor.googleSignIn(googleuser));
//    }

    @PostMapping(ApiRoutes.LoginSubRoute.RESET_PASSWORD)
    public ResponseEntity<Response<Boolean>> resetPassword(@RequestBody LoginRequestModel loginRequestModel) throws Exception {
        return ResponseEntity.ok(accessor.resetPassword(loginRequestModel));
    }

    @GetMapping(ApiRoutes.LoginSubRoute.GET_TOKEN)
    public ResponseEntity<Response<String>> getToken(@RequestBody LoginRequestModel loginRequestModel) {
        return ResponseEntity.ok(accessor.getToken(loginRequestModel));
    }
}