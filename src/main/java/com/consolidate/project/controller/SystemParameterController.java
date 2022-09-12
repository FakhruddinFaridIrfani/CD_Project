package com.consolidate.project.controller;


import com.consolidate.project.model.BaseResponse;
import com.consolidate.project.model.Role;
import com.consolidate.project.model.SystemParameter;
import com.consolidate.project.repository.RoleRepository;
import com.consolidate.project.repository.UserRepository;
import com.consolidate.project.service.UserManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("system")
public class SystemParameterController {

    @Autowired
    UserManagementService userManagementService;

    Logger logger = LoggerFactory.getLogger(SystemParameterController.class);

    @PostMapping("/addSystemParameter")
    public BaseResponse<String> addSystemParameter(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Add System Parameter");
        return userManagementService.addSystemParameter(input);
    }

    @PostMapping("/getSystemParameter")
    public BaseResponse<List<SystemParameter>> getSystemParameter(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Get System Parameter");
        return userManagementService.getSystemParameter(input);
    }

    @PostMapping("/updateSystemParameter")
    public BaseResponse<SystemParameter> updateSystemParameter(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Update System Parameter");
        return userManagementService.updateSystemParameter(input);
    }

    @PostMapping("/deleteSystemParameter")
    public BaseResponse<SystemParameter> deleteSystemParameter(@RequestBody String input) throws Exception, SQLException, ParseException {
        logger.info("Delete System Parameter");
        return userManagementService.deleteSystemParameter(input);
    }

}
