package com.consolidate.project.controller;


import com.consolidate.project.model.BaseResponse;
import com.consolidate.project.model.Role;
import com.consolidate.project.model.Users;
import com.consolidate.project.repository.RoleRepository;
import com.consolidate.project.repository.UserRepository;
import com.consolidate.project.service.UserManagementService;
import com.jcraft.jsch.JSchException;
import org.apache.catalina.User;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("role")
public class RoleController {

    @Autowired
    UserManagementService userManagementService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    Logger logger = LoggerFactory.getLogger(RoleController.class);

    @PostMapping("/addRole")
    public BaseResponse<String> addRole(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Add role");
        return userManagementService.addRole(input);
    }

    @PostMapping("/getRole")
    public BaseResponse<List<Role>> getRole(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Get Role");
        return userManagementService.getRole(input);
    }
//    @GetMapping("/getRole2")
//    public BaseResponse<List<Role>> getRole2(@RequestHeader(value = "Authorization", required = false) String token) throws Exception, SQLException, ParseException, JSchException, JSONException {
//        logger.info("Get Role List 2");
//        logger.info("Token : " + token);
//        BaseResponse<List<Role>> response = new BaseResponse<>();
//        if (token == null) {
//            response.setSuccess(false);
//            response.setStatus("401");
//            response.setMessage("Missing request header 'Authorization' for method parameter of type String. ");
//            return response;
//        }
//
//        List<Users> users = userRepository.tokenAuth(token.replace("Bearer ", ""));
//        if (users.size() == 0) {
//            response.setSuccess(false);
//            response.setStatus("401");
//            response.setMessage("Authorization Failed. ");
//            return response;
//        }
//        response.setData(roleRepository.findAll());
//        response.setSuccess(true);
//        response.setStatus("200");
//        response.setMessage("Role Listed");
//        return response;
//    }

    @PostMapping("/updateRole")
    public BaseResponse<Role> updateRole(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Update role");
        return userManagementService.updateRole(input);
    }

    @PostMapping("/deleteRole")
    public BaseResponse<Role> deleteRole(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Delete role");
        return userManagementService.deleteRole(input);
    }

}
