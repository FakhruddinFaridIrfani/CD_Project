package com.consolidate.project.controller;


import com.consolidate.project.model.BaseResponse;
import com.consolidate.project.model.Users;
import com.consolidate.project.repository.UserRepository;
import com.consolidate.project.service.UserManagementService;
import com.jcraft.jsch.JSchException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("user")
public class UserController {


    @Autowired
    UserManagementService userManagementService;

    Logger logger = LoggerFactory.getLogger(UserController.class);


    @PostMapping("/getUser")
    public BaseResponse<List<Map<String, Object>>> getUserWithParams(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Get user list with param : " + input);
        return userManagementService.getUsers(input);
    }

    @PostMapping("/addUser")
    public BaseResponse<String> addNewUsers(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Add user : " + input);
        return userManagementService.addUsers(input);
    }

    @PostMapping("/updateUser")
    public BaseResponse<Users> updateUsers(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Update user : " + input);
        return userManagementService.updateUsers(input);
    }

    @PostMapping("/deleteUser")
    public BaseResponse<Users> deleteUsersById(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Delete user : " + input);
        return userManagementService.deleteUsers(input);
    }

    @PostMapping("/loginUser")
    public BaseResponse<Map<String, Object>> loginUser(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Login user : " + new JSONObject(input).optString("user_name"));
        return userManagementService.loginLDAP(input);
    }

//    @PostMapping("/loginLDAP")
//    public BaseResponse<Map<String, Object>> loginLDAP(@RequestBody String input) throws Exception, SQLException, ParseException {
//        logger.info("Login user LDAP : " + new JSONObject(input).optString("user_name"));
//        return userManagementService.loginLDAP(input);
//    }

//    @PostMapping("/loginLDAP2")
//    public BaseResponse<Map<String, Object>> loginLDAP2(@RequestBody String input) throws Exception, SQLException, ParseException {
//        logger.info("Login user LDAP 2 : " + new JSONObject(input).optString("user_name"));
//        return userManagementService.loginLDAP2(input);
//    }

//    @PostMapping("/loginUser2")
//    public BaseResponse<Map<String, Object>> loginUser2(@RequestBody String input) throws Exception, SQLException, ParseException {
//        logger.info("Login user : " + new JSONObject(input).optString("user_name"));
//        return userManagementService.loginUser2(input);
//    }

    @PostMapping("/changeUserPassword")
    public BaseResponse<String> changeUserPassword(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Change user password for : " + new JSONObject(input).optString("user_name"));
        return userManagementService.changeUsersPassword(input);
    }

}
