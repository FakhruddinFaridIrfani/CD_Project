package com.consolidate.project.controller;


import com.consolidate.project.model.*;
import com.consolidate.project.repository.*;
import com.consolidate.project.service.DataService;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import org.postgresql.copy.*;
//import org.postgresql.copy.CopyManager;


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

    @Autowired
    SystemParameterRepository systemParameterRepository;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    SummaryMatchingDetailRepository summaryMatchingDetailRepository;

    @Autowired
    KtpDetailRepository ktpDetailRepository;

    @Autowired
    SdnFileRepository sdnFileRepository;

    @Autowired
    SdnNotificationRepository sdnNotificationRepository;

    Logger logger = LoggerFactory.getLogger(DataController.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();


    @PostMapping("/uploadFile")
    public BaseResponse uploadFile(@RequestBody String input) throws Exception, ParseException, JSchException, JSONException {
        BaseResponse response = new BaseResponse();
        response.setStatus("200");
        response.setSuccess(true);
        response.setMessage("File upload on progress");
        logger.info("Upload file");
        executor.submit(() -> {
            try {
                dataService.uploadSdnFile(input);
            } catch (Exception e) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage(e.getMessage());
            }
        });
        return response;
    }


    @PostMapping("/uploadFileManual")
    public BaseResponse<String> uploadManualKTP_DMA(@RequestBody String input) throws Exception, SQLException, ParseException, JSchException, JSONException {

        BaseResponse response = new BaseResponse();
        try {
            JSONObject jsonInput = new JSONObject(input);
            String file_type = jsonInput.getString("file_type");
            if (file_type.compareToIgnoreCase("ktp") == 0) {
                dataService.scheduledUploadKtpFile();
            } else if (file_type.compareToIgnoreCase("dma") == 0) {
                dataService.scheduledUploadDmaFile();
            }
            response.setSuccess(true);
            response.setStatus("200");
            response.setMessage("manual upload success");
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
        logger.info("Add File test");
        BaseResponse baseResponse = new BaseResponse<>();
        JSONObject jsonObject = new JSONObject(input);
        return dataService.addFile(jsonObject.optString("file_name"), jsonObject.optString("file_content"), jsonObject.optString("folder"));
    }

    @PostMapping("/getFileList")
    public BaseResponse<List<Object>> getExistingFile(@RequestBody String input) throws Exception {
        logger.info("Get List file ");
        return dataService.getNotDeletedFile(input);
    }

    @PostMapping("/searchData")
    public BaseResponse searchData(@RequestBody String input) throws Exception, SQLException, ParseException, JSchException, JSONException {
        logger.info("Search Data");
        return dataService.searchData(input);
    }

    @PostMapping("/matchingProcess")
    public BaseResponse matchingProcess() throws Exception, SQLException, ParseException, JSchException, JSONException {
        logger.info("matching manual");
        BaseResponse response = new BaseResponse();
        try {
            dataService.matchingProcess();
            response.setSuccess(true);
            response.setStatus("200");
            response.setMessage("manual matching success");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setStatus("500");
            response.setMessage("Failed to matching : " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/getReportList")
    public BaseResponse getReportList(@RequestBody String input) throws Exception {
        logger.info("Get report");
        return dataService.getReportList(input);
    }

    @PostMapping(value = "/downloadReport", produces = "application/octet-stream")
    public Object downloadReport(@RequestBody String input) throws Exception {
        Session session = null;
        ChannelSftp channel = null;
        BufferedWriter writer = null;
        ByteArrayResource resource = null;
        HttpHeaders headers = new HttpHeaders();
        String fileName = "Merchant Automation Screening OFAC - Summary Report - ";
        String header = "EXTRACT DATE|OFAC LIST SCREENED|START DATE|END DATE|POTENTIAL|POSITIVE|TOTAL SCREENED|TOTAL DATA";
        String value = "";
        String path = "";
        int report_id = 0;
        List<SystemParameter> systemParameterList = systemParameterRepository.getSystemParameter();
        List<Report> report = new ArrayList<>();
        for (SystemParameter parameter : systemParameterList) {
            if (parameter.getParameter_name().compareToIgnoreCase("pathReportFile") == 0) {
                path = parameter.getParameter_value();
            }
        }
        try {
            JSONObject jsonInput = new JSONObject(input);
            report_id = jsonInput.getInt("report_id");
            Map<String, Object> auth = dataService.tokenAuthentication(jsonInput.optString("user_token"));
            //Token Auth
            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                BaseResponse response = new BaseResponse();
                response.setStatus("401");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            dataService.createLog(input, userOnProcess, "downloadReport");

            report = reportRepository.getReportById(report_id);
            String finalFilename = "";
            if (report.size() > 0) {
                value = value.concat(report.get(0).getExtract_date()).concat(",")
                        .concat(report.get(0).getOfac_list_screened()).concat(",").concat(report.get(0).getStart_date().concat(","))
                        .concat(report.get(0).getEnd_date().concat(",")).concat((report.get(0).getPotential()) + ",").concat((report.get(0).getPositive()) + ",")
                        .concat(report.get(0).getTotal_screened() + ",").concat(report.get(0).getTotal_data() + "");
                finalFilename = fileName.concat(report.get(0).getOfac_list_screened()).concat(" - ").concat(report.get(0).getExtract_date()).concat(".csv");

            } else {
                return ResponseEntity.notFound();
            }

            session = new JSch().getSession(sftpUser, sftpUrl, 22);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            writer = new BufferedWriter(new OutputStreamWriter(channel.put(path + finalFilename, 0)));
            writer.write(header + "\r\n");
            writer.flush();
            writer.write(value);
            writer.close();

            resource = new ByteArrayResource(IOUtils.toByteArray(channel.get(path + finalFilename)));
//
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("Content-Disposition", "attachment;filename=" + finalFilename);
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            return ResponseEntity.ok()
                    .headers(headers).contentLength(resource.contentLength())
                    .contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
        } catch (Exception e) {
            BaseResponse response = new BaseResponse();
            logger.info("Exception : " + e.getMessage());
            response.setMessage("cannot connect to SFTP");
            response.setStatus("0");
            response.setSuccess(false);
            return response;
        } finally {
            if (session.isConnected() || session != null) {
                session.disconnect();
            }
            if (channel.isConnected() || channel != null) {
                channel.disconnect();
            }

        }

    }

    @GetMapping(value = "/getReportFile/{report_id}/{user_token}", produces = "application/octet-stream")
    public ResponseEntity<?> getReportFile(@PathVariable("report_id") int report_id, @PathVariable("user_token") String user_token) throws Exception {
        Session session = null;
        ChannelSftp channel = null;
        OutputStream writerxlxs = null;
        ByteArrayResource resource = null;
        HttpHeaders headers = new HttpHeaders();
        String fileName = "Merchant Automation Screening OFAC - Summary Report - ";
        String header = "EXTRACT DATE,OFAC LIST SCREENED,START DATE,END DATE,POTENTIAL,POSITIVE,TOTAL SCREENED,TOTAL DATA";
        String value = "";
        String path = "";
        List<SystemParameter> systemParameterList = systemParameterRepository.getSystemParameter();
        List<Report> report = new ArrayList<>();
        for (SystemParameter parameter : systemParameterList) {
            if (parameter.getParameter_name().compareToIgnoreCase("pathReportFile") == 0) {
                path = parameter.getParameter_value();
            }
        }
        try {
            Map<String, Object> auth = dataService.tokenAuthentication(user_token);
            //Token Auth
            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
            }
            String userOnProcess = auth.get("user_name").toString();
            dataService.createLog(report_id + " - " + user_token, userOnProcess, "downloadReport");
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet1 = workbook.createSheet("Matching Summary");
            XSSFRow row1;
            int keyItem1 = 1;
            Map<Integer, Object[]> summaryData = new TreeMap<Integer, Object[]>();
            //SHEET SDN
            summaryData.put(keyItem1, new Object[]{"SUMMARY REPORT"});
            keyItem1++;
            summaryData.put(keyItem1, new Object[]{""});
            keyItem1++;
            summaryData.put(keyItem1, new Object[]{"EXTRACT DATE", "OFAC LIST SCREENED", "START DATE", "END DATE", "POTENTIAL", "POSITIVE", "TOTAL SCREENED", "TOTAL DATA"});
            keyItem1++;
            report = reportRepository.getReportById(report_id);
            String finalFilename = "";
            if (report.size() > 0) {
                summaryData.put(keyItem1, new Object[]{report.get(0).getExtract_date(), report.get(0).getOfac_list_screened(), report.get(0).getStart_date(), report.get(0).getEnd_date(),
                        report.get(0).getPositive(), report.get(0).getPotential(), report.get(0).getTotal_screened(), report.get(0).getTotal_data()});
                keyItem1++;
//                value = value.concat(report.get(0).getExtract_date()).concat(",")
//                        .concat(report.get(0).getOfac_list_screened()).concat(",").concat(report.get(0).getStart_date().concat(","))
//                        .concat(report.get(0).getEnd_date().concat(",")).concat((report.get(0).getPotential()) + ",").concat((report.get(0).getPositive()) + ",")
//                        .concat(report.get(0).getTotal_screened() + ",").concat(report.get(0).getTotal_data() + "");
                finalFilename = fileName.concat(report.get(0).getOfac_list_screened()).concat(" - ").concat(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(report.get(0).getExtract_date().toString())).concat(".xlsx"));
            } else {
                return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
            }

            //SET DATA TO SHEET
            Set<Integer> keyId1 = summaryData.keySet();

            int rowId1 = 0;
            CellStyle cellStyle = workbook.createCellStyle();
            for (int key : keyId1) {
                row1 = sheet1.createRow(rowId1++);
                Object[] objects = summaryData.get(key);
                int cellId = 0;
                cellStyle.setWrapText(true);
                for (Object object : objects) {
                    Cell cell = row1.createCell(cellId++);
                    if (object instanceof String) {
                        cell.setCellValue((String) object);
                    } else if (object instanceof Integer) {
                        cell.setCellValue((Integer) object);
                    }
                }
            }

            session = new JSch().getSession(sftpUser, sftpUrl, 22);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            writerxlxs = channel.put(path + finalFilename, 0);
            workbook.write(writerxlxs);
            writerxlxs.close();
            workbook.close();

            resource = new ByteArrayResource(IOUtils.toByteArray(channel.get(path + finalFilename)));
//
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("Content-Disposition", "attachment;filename=" + finalFilename);
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            return ResponseEntity.ok()
                    .headers(headers).contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        } catch (Exception e) {
            return new ResponseEntity<>("BAD_REQUEST", HttpStatus.BAD_REQUEST);
        } finally {
            if (session.isConnected() || session != null) {
                session.disconnect();
            }
            if (channel.isConnected() || channel != null) {
                channel.disconnect();
            }

        }

    }

    @GetMapping(value = "/getDetailReportFile/{user_token}", produces = "application/octet-stream")
    public ResponseEntity<?> getDetailReportFile(@PathVariable("user_token") String user_token) throws Exception {
        Session session = null;
        ChannelSftp channel = null;
        BufferedWriter writer = null;
        OutputStream writerxlxs = null;
        ByteArrayResource resource = null;
        HttpHeaders headers = new HttpHeaders();
        String fileName = "Merchant Automation Screening OFAC - Detailed Report  - ";
        String path = "";
        List<SystemParameter> systemParameterList = systemParameterRepository.getSystemParameter();
        List<Report> report = new ArrayList<>();
        for (SystemParameter parameter : systemParameterList) {
            if (parameter.getParameter_name().compareToIgnoreCase("pathReportFile") == 0) {
                path = parameter.getParameter_value();
            }
        }

        try {
            Map<String, Object> auth = dataService.tokenAuthentication(user_token);
            //Token Auth
            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
            }
            String userOnProcess = auth.get("user_name").toString();
            dataService.createLog(" - " + user_token, userOnProcess, "downloadReportDetail");

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet1 = workbook.createSheet("SDN");
            XSSFSheet sheet2 = workbook.createSheet("CONSOLIDATE");
            XSSFRow row1;
            XSSFRow row2;
            int keyItem1 = 1;
            int keyItem2 = 1;
            String finalFilename = "";
            List<SummaryMatchingDetail> summaryMatchingDetailList = summaryMatchingDetailRepository.findAll();
            if (summaryMatchingDetailList.size() == 0) {
                return new ResponseEntity<>("No detailed report found", HttpStatus.NOT_FOUND);
            }
            finalFilename = fileName.concat(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(summaryMatchingDetailList.get(0).getCreated_date().toString())).concat(".xlsx"));

            Map<Integer, Object[]> dataSdn = new TreeMap<Integer, Object[]>();
            Map<Integer, Object[]> dataConsolidate = new TreeMap<Integer, Object[]>();

            //SHEET SDN
            dataSdn.put(keyItem1, new Object[]{"DETAILED REPORT SDN"});
            keyItem1++;
            dataSdn.put(keyItem1, new Object[]{""});
            keyItem1++;
            dataSdn.put(keyItem1, new Object[]{"MERCHANT ID", "PROCESS DATE", "MERCHANT NAME", "MERCHANT ACCOUNT NAME 1", "MERCHANT ACCOUNT NAME 2",
                    "ID NUMBER 1", "ID NUMBER 2", "DOB 1", "DOB 2", "POTENTIAL", "POSITIVE"});
            keyItem1++;

            //SHEET CONSOLIDATE
            dataConsolidate.put(keyItem2, new Object[]{"DETAILED REPORT CONSOLIDATE"});
            keyItem2++;
            dataConsolidate.put(keyItem2, new Object[]{""});
            keyItem2++;
            dataConsolidate.put(keyItem2, new Object[]{"MERCHANT ID", "PROCESS DATE", "MERCHANT NAME", "MERCHANT ACCOUNT NAME 1", "MERCHANT ACCOUNT NAME 2",
                    "ID NUMBER 1", "ID NUMBER 2", "DOB 1", "DOB 2", "POTENTIAL", "POSITIVE"});
            keyItem2++;

            //DATA PROCESS
            for (int i = 0; i < summaryMatchingDetailList.size(); i++) {
                SummaryMatchingDetail summaryMatchingDetail = summaryMatchingDetailList.get(i);
                KTPDetail ktpDetail = ktpDetailRepository.getKTPDetailById(summaryMatchingDetail.getKtp_detail_id());
                SdnFile sdnFile = sdnFileRepository.getSdnFileByEntryId(summaryMatchingDetail.getSdn_entry_id());
                if (sdnFile.getFile_type().compareToIgnoreCase("sdn") == 0) {
                    if (summaryMatchingDetail.getMatching_status().compareToIgnoreCase("positive") == 0) {
                        dataSdn.put(keyItem1, new Object[]{ktpDetail.getMerchant_no(), summaryMatchingDetail.getCreated_date().toString(), ktpDetail.getMerchant_name(),
                                ktpDetail.getName_1(), ktpDetail.getName_2(), ktpDetail.getKtp_1(), ktpDetail.getKtp_2(), ktpDetail.getDob_1(), ktpDetail.getDob_2(), "N", "Y"});
                    } else {
                        dataSdn.put(keyItem1, new Object[]{ktpDetail.getMerchant_no(), summaryMatchingDetail.getCreated_date().toString(), ktpDetail.getMerchant_name(),
                                ktpDetail.getName_1(), ktpDetail.getName_2(), ktpDetail.getKtp_1(), ktpDetail.getKtp_2(), ktpDetail.getDob_1(), ktpDetail.getDob_2(), "Y", "N"});
                    }
                    keyItem1++;
                } else {
                    if (summaryMatchingDetail.getMatching_status().compareToIgnoreCase("positive") == 0) {
                        dataConsolidate.put(keyItem2, new Object[]{ktpDetail.getMerchant_no(), summaryMatchingDetail.getCreated_date().toString(), ktpDetail.getMerchant_name(),
                                ktpDetail.getName_1(), ktpDetail.getName_2(), ktpDetail.getKtp_1(), ktpDetail.getKtp_2(), ktpDetail.getDob_1(), ktpDetail.getDob_2(), "N", "Y"});
                    } else {
                        dataConsolidate.put(keyItem2, new Object[]{ktpDetail.getMerchant_no(), summaryMatchingDetail.getCreated_date().toString(), ktpDetail.getMerchant_name(),
                                ktpDetail.getName_1(), ktpDetail.getName_2(), ktpDetail.getKtp_1(), ktpDetail.getKtp_2(), ktpDetail.getDob_1(), ktpDetail.getDob_2(), "Y", "N"});
                    }
                    keyItem2++;
                }
            }
            //SET VALUE TO EXCEL CELL
            Set<Integer> keyId1 = dataSdn.keySet();
            Set<Integer> keyId2 = dataConsolidate.keySet();

            int rowId1 = 0;
            int rowId2 = 0;
            CellStyle cellStyle = workbook.createCellStyle();
            for (int key : keyId1) {
                row1 = sheet1.createRow(rowId1++);
                Object[] objects = dataSdn.get(key);
                int cellId = 0;
                cellStyle.setWrapText(true);
                for (Object object : objects) {
                    Cell cell = row1.createCell(cellId++);
                    if (object instanceof String) {
                        cell.setCellValue((String) object);
                    } else if (object instanceof Integer) {
                        cell.setCellValue((Integer) object);
                    }
                }
            }
            for (int key : keyId2) {
                row2 = sheet2.createRow(rowId2++);
                Object[] objects = dataConsolidate.get(key);
                int cellId = 0;
                cellStyle.setWrapText(true);
                for (Object object : objects) {
                    Cell cell = row2.createCell(cellId++);
                    if (object instanceof String) {
                        cell.setCellValue((String) object);
                    } else if (object instanceof Integer) {
                        cell.setCellValue((Integer) object);
                    }
                }
            }


            session = new JSch().getSession(sftpUser, sftpUrl, 22);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            writerxlxs = channel.put(path + finalFilename, 0);
            workbook.write(writerxlxs);
            writerxlxs.close();
            workbook.close();

            resource = new ByteArrayResource(IOUtils.toByteArray(channel.get(path + finalFilename)));
//
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("Content-Disposition", "attachment;filename=" + finalFilename);
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            return ResponseEntity.ok()
                    .headers(headers).contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        } catch (Exception e) {
            return new ResponseEntity<>("BAD_REQUEST", HttpStatus.BAD_REQUEST);
        } finally {
            if (session.isConnected() || session != null) {
                session.disconnect();
            }
            if (channel.isConnected() || channel != null) {
                channel.disconnect();
            }

        }

    }

    @GetMapping("/getNotification")
    public BaseResponse<List<Object>> getNotification() throws Exception {
        logger.info("Get notification  ");
        BaseResponse response = new BaseResponse();
        List<SdnNotification> sdnNotifications = new ArrayList<>();
        try {
            sdnNotifications = sdnNotificationRepository.getNotification();
            for (int i = 0; i < sdnNotifications.size(); i++) {
                String timeOfExecution = sdnNotifications.get(i).getCreated_date().toString();
                String originalMessage = sdnNotifications.get(i).getNotification_message();
                sdnNotifications.get(i).setNotification_message(originalMessage + timeOfExecution);
            }
            response.setData(sdnNotifications);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Notification Listed");
        } catch (Exception e) {
            response.setData(new ArrayList<>());
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }

        return response;
    }
}
