package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.UserAuthService;
import com.upgrad.quora.service.business.UserService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    UserAuthService userAuthService;

    @RequestMapping(method = RequestMethod.POST, path = "signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> createUser(final SignupUserRequest userRequest) throws SignUpRestrictedException {

        UserEntity existingUser = userService.getUserByUserName(userRequest.getUser_name());
        if (existingUser != null) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }

        UserEntity existingUserWithEmail = userService.getUserByEmail(userRequest.getEmail_address());
        if (existingUserWithEmail != null) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(userRequest.getFirst_name());
        userEntity.setLastName(userRequest.getLast_name());
        userEntity.setUserName(userRequest.getUser_name());
        userEntity.setEmail(userRequest.getEmail_address());
        userEntity.setPassword(userRequest.getPassword());
        userEntity.setCountry(userRequest.getCountry());
        userEntity.setAboutMe(userRequest.getAboutMe());
        userEntity.setDob(userRequest.getDob());
        userEntity.setContactNumber(userRequest.getContact_number());
        userEntity.setRole("nonadmin");

        UserEntity createdUserEntity = userService.createUser(userEntity);

        SignupUserResponse signupUserResponse = new SignupUserResponse();
        signupUserResponse.setId(createdUserEntity.getUuid());
        signupUserResponse.setStatus("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<>(signupUserResponse, HttpStatus.CREATED);
    }

//    public static void main(String[] args) { //dmVlbGE6a2VlbGE=
//        System.out.println(Base64.getEncoder().encodeToString("username:password".getBytes()));
//    }

    @RequestMapping(method = RequestMethod.POST, path = "signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signInUser(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {

        byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        UserAuthEntity userAuthToken = userAuthService.authenticate(decodedArray[0], decodedArray[1]);
        UserEntity user = userAuthToken.getUser();

        SigninResponse signinResponse = new SigninResponse();
        signinResponse.setId(user.getUuid());
        signinResponse.setMessage("SIGNED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", userAuthToken.getAccessToken());
        return new ResponseEntity<>(signinResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signOutUser(@RequestHeader("authorization") final String accessToken) throws SignOutRestrictedException {

        UserAuthEntity existingUserByToken = userAuthService.signOutUser(accessToken);
        UserEntity signedInUser = existingUserByToken.getUser();
        SignoutResponse signoutResponse = new SignoutResponse();
        signoutResponse.setId(signedInUser.getUuid());
        signoutResponse.setMessage("SIGNED OUT SUCCESSFULLY");
        return new ResponseEntity<>(signoutResponse, HttpStatus.OK);
    }

}
