package com.consolidate.project.controller;


import com.consolidate.project.model.BaseResponse;
import com.consolidate.project.model.Role;
import com.consolidate.project.repository.RoleRepository;
import com.consolidate.project.service.CdService;
import com.jcraft.jsch.JSchException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("role")
public class RoleController {

    @Autowired
    CdService cdService;

    @Autowired
    RoleRepository roleRepository;
    Logger logger = LoggerFactory.getLogger(RoleController.class);

    @PostMapping("/getRole")
    public BaseResponse<List<Role>> getRole() throws Exception, SQLException, ParseException, JSchException, JSONException {
        logger.info("Gert Role List");
        BaseResponse<List<Role>> response = new BaseResponse<>();
        response.setData(roleRepository.findAll());
        response.setSuccess(true);
        response.setStatus("2000");
        response.setMessage("Role Listed");
        return response;
    }

    @PostMapping("/getRole")
    public BaseResponse<List<Role>> deleteRole(@RequestBody String input) throws Exception, SQLException, ParseException, JSchException, JSONException {
        logger.info("Gert Role List");
        BaseResponse<List<Role>> response = new BaseResponse<>();
        try {
            JSONObject jsonInput = new JSONObject(input);
            roleRepository.deleteById(jsonInput.getInt("role_id"));
            response.setSuccess(true);
            response.setStatus("2000");
            response.setMessage("Role Deleted");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setStatus("0");
            response.setMessage("Failed delete role : " + e.getMessage());
        }
        return response;
    }

}
