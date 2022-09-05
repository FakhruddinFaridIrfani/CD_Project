package com.consolidate.project.controller;


import com.consolidate.project.model.BaseResponse;
import com.consolidate.project.service.DataService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("data")
public class DataController {

    @Value("${sftp.user.name}")
    private String sftpUser;

    @Value("${sftp.user.password}")
    private String sftpPassword;

    @Value("${sftp.url}")
    private String sftpUrl;

    @Autowired
    DataService dataService;
    Logger logger = LoggerFactory.getLogger(DataController.class);


    @PostMapping("/uploadFile")
    public BaseResponse<String> uploadFile(@RequestBody String input) throws Exception, SQLException, ParseException, JSchException, JSONException {

        BaseResponse response = new BaseResponse();
        try {
            JSONObject jsonInput = new JSONObject(input);
            String file_type = jsonInput.getString("file_type");
            logger.info("Upload file : " + file_type.toUpperCase());
            if (file_type.compareToIgnoreCase("sdn") == 0 || file_type.compareToIgnoreCase("consal") == 0) {
                response = dataService.uploadSdnFile(input);
            } else if (file_type.compareToIgnoreCase("ktp") == 0) {
                response = dataService.uploadKtpFile(input);
            } else if (file_type.compareToIgnoreCase("dma") == 0) {
                response = dataService.uploadDmaFile(input);
            } else {
                response.setSuccess(false);
                response.setStatus("500");
                response.setMessage("Failed to upload : Unknown file type");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setStatus("500");
            response.setMessage("Failed to upload : " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/checkUpload")
    public BaseResponse<String> checkUpload(@RequestBody String input) throws Exception, SQLException, ParseException, JSchException, JSONException {
        logger.info("Check file on process");
        String file_type = new JSONObject(input).getString("file_type");
        return dataService.checkUploadingOrMatchingFile(file_type);
    }

    @GetMapping("/ping")
    public BaseResponse<String> ping() throws Exception, SQLException, ParseException, JSchException, JSONException {
        BaseResponse response = new BaseResponse();
        logger.info("PING !!");
        response.setMessage("Service Up");
        response.setStatus("2000");
        response.setSuccess(true);
        return response;
    }


    @GetMapping("/sftpTest")
    public BaseResponse<String> sftpTest() throws Exception, SQLException, ParseException, JSchException, JSONException {
        BaseResponse response = new BaseResponse();
        logger.info("Test SFTP !!");
        Session session = null;
        ChannelSftp channel = null;
        String message = "";
        try {
            session = new JSch().getSession(sftpUser, sftpUrl, 22);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            if (session.isConnected()) {
                message += " session is connected -";
            }
            if (channel.isConnected()) {
                message += " channel is connected";
            }
            response.setData(message);
            response.setMessage("SFTP Connected");
            response.setStatus("2000");
            response.setSuccess(true);
        } catch (Exception e) {
            logger.info("Exception : " + e.getMessage());
            response.setMessage("cannot connect to SFTP");
            response.setStatus("0");
            response.setSuccess(false);
        } finally {
            if (session.isConnected() || session != null) {
                session.disconnect();
            }
            if (channel.isConnected() || channel != null) {
                channel.disconnect();
            }
        }
        return response;
    }


    @PostMapping("/addFile")
    public BaseResponse<Map<String, String>> addFile(@RequestBody String input) throws Exception {
        logger.info(new Date().getTime() + " : Add File test");
        BaseResponse baseResponse = new BaseResponse<>();
        JSONObject jsonObject = new JSONObject(input);
        return dataService.addFile(jsonObject.optString("file_name"), jsonObject.optString("file_content"), jsonObject.optString("folder"));
    }
}