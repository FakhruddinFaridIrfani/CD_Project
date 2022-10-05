package com.consolidate.project.service;

import com.consolidate.project.model.*;
import com.consolidate.project.repository.*;
import com.jcraft.jsch.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import javax.xml.parsers.DocumentBuilderFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.IOUtils;


@Service
public class DataService {

    Logger logger = LoggerFactory.getLogger(DataService.class);

    @Autowired
    SdnFileRepository sdnFileRepository;

    @Autowired
    KtpFileRepository ktpFileRepository;

    @Autowired
    KtpDetailRepository ktpDetailRepository;

    @Autowired
    DmaFileRepository dmaFileRepository;

    @Autowired
    DmaDetailRepository dmaDetailRepository;

    @Autowired
    SdnEntryRepository sdnEntryRepository;

    @Autowired
    SdnProgramRepository sdnProgramRepository;

    @Autowired
    SdnAkaRepository sdnAkaRepository;

    @Autowired
    SdnAddressRepository sdnAddressRepository;

    @Autowired
    SdnDOBRepository sdnDOBRepository;

    @Autowired
    SdnPOBRepository sdnPOBRepository;

    @Autowired
    SdnIDRepository sdnIDRepository;

    @Autowired
    SdnCitizenshipRepository sdnCitizenshipRepository;

    @Autowired
    SdnNationalityRepository sdnNationalityRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    SdnLoggerRepository sdnLoggerRepository;

    @Autowired
    SystemParameterRepository systemParameterRepository;

    @Autowired
    SummaryMatchingRepository summaryMatchingRepository;

    @Autowired
    SummaryMatchingDetailRepository summaryMatchingDetailRepository;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    SdnNotificationRepository sdnNotificationRepository;


    //FILE SECTION


    public void uploadSdnFile(String input) throws Exception {
        BaseResponse response = new BaseResponse();
        SdnFile sdnFile = new SdnFile();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        int currentSdnFileId = 0;

        try {
            JSONObject jsonInput = new JSONObject(input);
            String fileName = jsonInput.getString("file_name");

            String file_type = jsonInput.getString("file_type");
            logger.info("Upload file : " + file_type.toUpperCase());

            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));
            //Token Auth
            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("401");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                throw new Exception("Token Authentication Failed");
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(jsonInput.getString("file_name"), userOnProcess, "uploadFile");
            String file_type_name = "";
            if (file_type.compareToIgnoreCase("consal") == 0) {
                file_type_name = "Consolidate";
            } else {
                file_type_name = "Sdn";
            }
            createNotification("New " + file_type_name + " file is uploading by " + userOnProcess + " on ");


            Map<String, Object> fileTypeCheck = checkAllowedFileType(file_type);
            if ((Boolean) fileTypeCheck.get("matched") == false) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage(fileTypeCheck.get("message").toString());
                logger.info("error on check file_type process : " + fileTypeCheck.get("message"));
                throw new Exception("error on check file_type process : " + fileTypeCheck.get("message"));
            }

//            List<SdnFile> uploadingFile = sdnFileRepository.getMatchingOrUploadingFile(file_type);
//            if (uploadingFile.size() > 0) {
//                response.setStatus("500");
//                response.setSuccess(false);
//                response.setMessage("Can't upload file, another file (" + uploadingFile.get(0).getFile_name_ori() + ") still on " + uploadingFile.get(0).getStatus() + " process");
//                logger.info("Load file exception : can't upload file, another file is still uploading");
//                return response;
//            }
            BaseResponse canUpload = checkUploadingOrMatchingFile(file_type);
            if (canUpload.isSuccess() == false) {
                throw new Exception(canUpload.getMessage());
            }


            String fileData = jsonInput.getString("file_data");
            if (fileData.isEmpty() || fileData == null) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("file data can't be null or empty");
                logger.info("file data can't be null or empty");
                throw new Exception("file data can't be null or empty");

            }


            String savedFileName = "";
            logger.info("Saving " + fileName + "to SFTP . . .  . ");
            BaseResponse<Map<String, String>> fileUploadProcess = addFile(fileName, fileData, file_type_name);
            if (fileUploadProcess.isSuccess() == true) {
                savedFileName = fileUploadProcess.getData().get("file");
                logger.info("saved");
            } else {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("Failed save file to SFTP : " + fileUploadProcess.getMessage());
                logger.info("failed");
                throw new Exception("Failed save file to SFTP : " + fileUploadProcess.getMessage());
            }

            //CLEARING CURRENT DATA ON DB
            SdnFile onDeleteSdnFile = sdnFileRepository.getFileToBDeleted(file_type);
            if (onDeleteSdnFile != null) {
                logger.info("Clearing last uploaded " + file_type.toUpperCase() + " file data on DB . . .  . ");
                sdnFileRepository.updateFileStatus(onDeleteSdnFile.getSdnfile_id(), "deleted", "-deleted on " + fileName + " uploading process");
                sdnEntryRepository.deleteSdnEntryByFileId(onDeleteSdnFile.getSdnfile_id());
                logger.info("Cleared");
            }


            Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            logger.info("Saving File " + fileName + "to DB . . .  . ");
            sdnFile.setFile_name_ori(fileName);
            sdnFile.setFile_name_save(savedFileName);
            sdnFile.setFile_type(file_type);
            sdnFile.setFile_comparison_group("default");
            sdnFile.setStatus("uploading");
            sdnFile.setCreated_by(userOnProcess);
            sdnFile.setUpdated_by(userOnProcess);
            sdnFile.setCreated_date(newInputDate);
            sdnFile.setUpdated_date(newInputDate);
            sdnFile.setRemarks("");
            sdnFileRepository.save(sdnFile);
            logger.info("saved");

            currentSdnFileId = sdnFileRepository.getSdnFileBySavedFileName(savedFileName).getSdnfile_id();
//            logger.info("currentSdnFileId :  " + currentSdnFileId);

            //Parsing XML
            byte[] b = Base64.getMimeDecoder().decode(getFile(savedFileName, file_type_name).getData().get("file_base64").toString());
            InputStream inputStream = new ByteArrayInputStream(b);
            dbf.setNamespaceAware(false);
            Document doc = dbf.newDocumentBuilder().parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList sdnEntryList = doc.getElementsByTagName("sdnEntry");
            for (int i = 0; i < sdnEntryList.getLength(); i++) {
                Node node = sdnEntryList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String sdnEntryUid = "";
                    String sdnEntryFirstName = "";
                    String sdnEntryLastName = "";
                    String tittle = "";
                    String remarks = "";
                    String sdnEntryType = "";
                    if (element.getElementsByTagName("uid").item(0) != null) {
                        sdnEntryUid = element.getElementsByTagName("uid").item(0).getTextContent();
                    }
                    if (element.getElementsByTagName("firstName").item(0) != null) {
                        sdnEntryFirstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                    }
                    if (element.getElementsByTagName("lastName").item(0) != null) {
                        sdnEntryLastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                    }
                    if (element.getElementsByTagName("sdnType").item(0) != null) {
                        sdnEntryType = element.getElementsByTagName("sdnType").item(0).getTextContent();
                    }
                    if (element.getElementsByTagName("tittle").item(0) != null) {
                        tittle = element.getElementsByTagName("tittle").item(0).getTextContent();
                        tittle = tittle.substring(0, Math.min(tittle.length(), 250));
                    }
                    if (element.getElementsByTagName("remarks").item(0) != null) {
                        remarks = element.getElementsByTagName("remarks").item(0).getTextContent();
                        remarks = remarks.substring(0, Math.min(remarks.length(), 250));
                    }
                    SdnEntry sdnEntry = new SdnEntry();
                    sdnEntry.setSdnfile_id(currentSdnFileId);
                    sdnEntry.setUid(Integer.valueOf(sdnEntryUid));
                    sdnEntry.setFirst_name(sdnEntryFirstName);
                    sdnEntry.setLast_name(sdnEntryLastName);
                    sdnEntry.setStatus("active");
                    sdnEntry.setTittle(tittle);
                    sdnEntry.setSdn_type(sdnEntryType);
                    sdnEntry.setRemarks(remarks);
                    sdnEntry.setCreated_by(userOnProcess);
                    sdnEntry.setUpdated_by(userOnProcess);
                    sdnEntry.setCreated_date(newInputDate);
                    sdnEntry.setUpdated_date(newInputDate);
                    sdnEntryRepository.save(sdnEntry);
                    int insertedSdnEntryId = sdnEntryRepository.getSdnEntryBySavedUIDAndFIleID(Integer.valueOf(sdnEntryUid), currentSdnFileId).getSdnEntry_id();
//                    if (insertedSdnEntryId == 0) {
//                        logger.info("insertedSdnEntryId 0  on : " + sdnEntryUid);
//                    }

//                    logger.info("uid : " + sdnEntryUid);
//                    logger.info("firstName : " + sdnEntryFirstName);
//                    logger.info("lastName : " + sdnEntryLastName);
//                    logger.info("tittle : " + tittle);
//                    logger.info("sdnType : " + sdnEntryType);
//                    logger.info("remarks : " + remarks);


                    NodeList programList = ((Element) sdnEntryList.item(i)).getElementsByTagName("programList");
                    if (programList != null && programList.getLength() > 0) {
//                        logger.info("programList count : " + programList.getLength());
                        for (int j = 0; j < programList.getLength(); j++) {
                            Node programListNode = programList.item(j);
                            if (programListNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element programElement = (Element) programListNode;
                                String programName = programElement.getElementsByTagName("program").item(0).getTextContent();
//                                logger.info("program : " + programName);

                                SdnProgram sdnProgram = new SdnProgram();
                                sdnProgram.setSdnEntry_id(insertedSdnEntryId);
                                sdnProgram.setProgram_name(programName);
                                sdnProgram.setStatus("active");
                                sdnProgram.setCreated_by(userOnProcess);
                                sdnProgram.setUpdated_by(userOnProcess);
                                sdnProgram.setCreated_date(newInputDate);
                                sdnProgram.setUpdated_date(newInputDate);
                                sdnProgramRepository.save(sdnProgram);

                            }
                        }
                    }

                    NodeList akaList = ((Element) sdnEntryList.item(i)).getElementsByTagName("akaList");
//                    logger.info("akaList count : " + akaList.getLength());
                    if (akaList != null && akaList.getLength() > 0) {
                        for (int a = 0; a < akaList.getLength(); a++) {
                            NodeList akaChild = ((Element) akaList.item(a)).getElementsByTagName("aka");
//                            logger.info("akaChild count : " + akaChild.getLength());
                            for (int q = 0; q < akaChild.getLength(); q++) {
                                Node akaChildNode = akaChild.item(q);
                                if (akaChildNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element akaElement = (Element) akaChildNode;
                                    String uidAka = "";
                                    String firstNameAka = "";
                                    String lastNameAka = "";
                                    String typeAka = "";
                                    String categoryAka = "";
                                    if (akaElement.getElementsByTagName("uid").item(0) != null) {
                                        uidAka = akaElement.getElementsByTagName("uid").item(0).getTextContent();
                                    }
                                    if (akaElement.getElementsByTagName("lastName").item(0) != null) {
                                        lastNameAka = akaElement.getElementsByTagName("lastName").item(0).getTextContent();
                                    }
                                    if (akaElement.getElementsByTagName("type").item(0) != null) {
                                        typeAka = akaElement.getElementsByTagName("type").item(0).getTextContent();
                                    }
                                    if (akaElement.getElementsByTagName("category").item(0) != null) {
                                        categoryAka = akaElement.getElementsByTagName("category").item(0).getTextContent();
                                    }
                                    if (akaElement.getElementsByTagName("firstName").item(0) != null) {
                                        firstNameAka = akaElement.getElementsByTagName("firstName").item(0).getTextContent();
                                    }
                                    SdnAka sdnAka = new SdnAka();
                                    sdnAka.setSdnEntry_id(insertedSdnEntryId);
                                    sdnAka.setUid(Integer.valueOf(uidAka));
                                    sdnAka.setStatus("active");
                                    sdnAka.setFirst_name(firstNameAka);
                                    sdnAka.setLast_name(lastNameAka);
                                    sdnAka.setType(typeAka);
                                    sdnAka.setCategory(categoryAka);
                                    sdnAka.setCreated_by(userOnProcess);
                                    sdnAka.setUpdated_by(userOnProcess);
                                    sdnAka.setCreated_date(newInputDate);
                                    sdnAka.setUpdated_date(newInputDate);
                                    sdnAkaRepository.save(sdnAka);

//                                    logger.info("uidAka : " + uidAka);
//                                    logger.info("firstNameAka : " + firstNameAka);
//                                    logger.info("lastNameAka : " + lastNameAka);
//                                    logger.info("categoryAka : " + categoryAka);
//                                    logger.info("typeAka : " + typeAka);
                                }
                            }

                        }
                    }

                    NodeList addressList = ((Element) sdnEntryList.item(i)).getElementsByTagName("addressList");
//                    logger.info("addressList count : " + addressList.getLength());
                    if (addressList != null && addressList.getLength() > 0) {
                        for (int a = 0; a < addressList.getLength(); a++) {
                            NodeList addressChild = ((Element) addressList.item(a)).getElementsByTagName("address");
//                            logger.info("addressChild count : " + addressChild.getLength());
                            for (int q = 0; q < addressChild.getLength(); q++) {
                                Node addressChildNode = addressChild.item(q);
                                if (addressChildNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element addressElement = (Element) addressChildNode;
                                    String uidAddress = "";
                                    String stateOrProvince = "";
                                    String cityAddress = "";
                                    String countryAddress = "";
                                    String addressAddress1 = "";
                                    String addressAddress2 = "";
                                    String addressAddress3 = "";
                                    String postalCode = "";
                                    String remarksAddress = "";
                                    if (addressElement.getElementsByTagName("uid").item(0) != null) {
                                        uidAddress = addressElement.getElementsByTagName("uid").item(0).getTextContent();
                                    }
                                    if (addressElement.getElementsByTagName("stateOrProvince").item(0) != null) {
                                        stateOrProvince = addressElement.getElementsByTagName("stateOrProvince").item(0).getTextContent();
                                    }
                                    if (addressElement.getElementsByTagName("city").item(0) != null) {
                                        cityAddress = addressElement.getElementsByTagName("city").item(0).getTextContent();
                                    }
                                    if (addressElement.getElementsByTagName("country").item(0) != null) {
                                        countryAddress = addressElement.getElementsByTagName("country").item(0).getTextContent();
                                    }
                                    if (addressElement.getElementsByTagName("address1").item(0) != null) {
                                        addressAddress1 = addressElement.getElementsByTagName("address1").item(0).getTextContent();
                                    }
                                    if (addressElement.getElementsByTagName("address2").item(0) != null) {
                                        addressAddress2 = addressElement.getElementsByTagName("address2").item(0).getTextContent();
                                    }
                                    if (addressElement.getElementsByTagName("address3").item(0) != null) {
                                        addressAddress3 = addressElement.getElementsByTagName("address3").item(0).getTextContent();
                                    }
                                    if (addressElement.getElementsByTagName("postalCode").item(0) != null) {
                                        postalCode = addressElement.getElementsByTagName("postalCode").item(0).getTextContent();
                                    }
                                    if (element.getElementsByTagName("remarks").item(0) != null) {
                                        remarksAddress = element.getElementsByTagName("remarks").item(0).getTextContent();
                                        remarksAddress = remarksAddress.substring(0, Math.min(remarksAddress.length(), 250));
                                    }
                                    SdnAddress sdnAddress = new SdnAddress();
                                    sdnAddress.setUid(Integer.valueOf(uidAddress));
                                    sdnAddress.setSdnEntry_id(insertedSdnEntryId);
                                    sdnAddress.setState_province(stateOrProvince);
                                    sdnAddress.setCity(cityAddress);
                                    sdnAddress.setCountry(countryAddress);
                                    sdnAddress.setAddress_1(addressAddress1);
                                    sdnAddress.setAddress_2(addressAddress2);
                                    sdnAddress.setAddress_3(addressAddress3);
                                    sdnAddress.setPostal_code(postalCode);
                                    sdnAddress.setRemarks(remarksAddress);
                                    sdnAddress.setStatus("active");
                                    sdnAddress.setCreated_by(userOnProcess);
                                    sdnAddress.setUpdated_by(userOnProcess);
                                    sdnAddress.setCreated_date(newInputDate);
                                    sdnAddress.setUpdated_date(newInputDate);
                                    sdnAddressRepository.save(sdnAddress);

//                                    logger.info("uidAddress : " + uidAddress);
//                                    logger.info("stateOrProvince : " + stateOrProvince);
//                                    logger.info("cityAddress : " + cityAddress);
//                                    logger.info("countryAddress : " + countryAddress);
//                                    logger.info("address1 : " + addressAddress1);
//                                    logger.info("address2 : " + addressAddress2);
//                                    logger.info("address3 : " + addressAddress3);
//                                    logger.info("postalCode : " + postalCode);
//                                    logger.info("remarksAddress : " + remarksAddress);
                                }
                            }

                        }
                    }
                    NodeList dateOfBirthList = ((Element) sdnEntryList.item(i)).getElementsByTagName("dateOfBirthList");
//                    logger.info("dateOfBirthList count : " + dateOfBirthList.getLength());
                    if (dateOfBirthList != null && dateOfBirthList.getLength() > 0) {
                        for (int a = 0; a < dateOfBirthList.getLength(); a++) {
                            NodeList dateOfBirthItem = ((Element) dateOfBirthList.item(a)).getElementsByTagName("dateOfBirthItem");
//                            logger.info("dateOfBirthItem count : " + dateOfBirthItem.getLength());
                            for (int q = 0; q < dateOfBirthItem.getLength(); q++) {
                                Node dateOfBirthNode = dateOfBirthItem.item(q);
                                if (dateOfBirthNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element dateOfBirthElement = (Element) dateOfBirthNode;
                                    String uidDOB = "";
                                    String dateOfBirth = "";
                                    String mainEntry = "";
                                    if (dateOfBirthElement.getElementsByTagName("uid").item(0) != null) {
                                        uidDOB = dateOfBirthElement.getElementsByTagName("uid").item(0).getTextContent();
                                    }
                                    if (dateOfBirthElement.getElementsByTagName("dateOfBirth").item(0) != null) {
                                        dateOfBirth = dateOfBirthElement.getElementsByTagName("dateOfBirth").item(0).getTextContent();
                                    }
                                    if (dateOfBirthElement.getElementsByTagName("mainEntry").item(0) != null) {
                                        mainEntry = dateOfBirthElement.getElementsByTagName("mainEntry").item(0).getTextContent();
                                    }
                                    SdnDOB sdnDOB = new SdnDOB();
                                    sdnDOB.setUid(Integer.valueOf(uidDOB));
                                    sdnDOB.setSdnEntry_id(insertedSdnEntryId);
                                    sdnDOB.setDob(dateOfBirth);
                                    sdnDOB.setMain_entry(mainEntry);
                                    sdnDOB.setStatus("active");
                                    sdnDOB.setCreated_by(userOnProcess);
                                    sdnDOB.setUpdated_by(userOnProcess);
                                    sdnDOB.setCreated_date(newInputDate);
                                    sdnDOB.setUpdated_date(newInputDate);
                                    sdnDOBRepository.save(sdnDOB);
//                                    logger.info("uidDOB : " + uidDOB);
//                                    logger.info("dateOfBirth : " + dateOfBirth);
//                                    logger.info("mainEntry : " + mainEntry);

                                }
                            }

                        }
                    }

                    NodeList placeOfBirthList = ((Element) sdnEntryList.item(i)).getElementsByTagName("placeOfBirthList");
//                    logger.info("placeOfBirthList count : " + placeOfBirthList.getLength());
                    if (placeOfBirthList != null && placeOfBirthList.getLength() > 0) {
                        for (int a = 0; a < placeOfBirthList.getLength(); a++) {
                            NodeList placeOfBirthItem = ((Element) placeOfBirthList.item(a)).getElementsByTagName("placeOfBirthItem");
//                            logger.info("placeOfBirthItem count : " + placeOfBirthItem.getLength());
                            for (int q = 0; q < placeOfBirthItem.getLength(); q++) {
                                Node placeOfBirthNode = placeOfBirthItem.item(q);
                                if (placeOfBirthNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element placeOfBirthElement = (Element) placeOfBirthNode;
                                    String uidPOB = "";
                                    String placeOfBirth = "";
                                    String mainEntry = "";
                                    if (placeOfBirthElement.getElementsByTagName("uid").item(0) != null) {
                                        uidPOB = placeOfBirthElement.getElementsByTagName("uid").item(0).getTextContent();
                                    }
                                    if (placeOfBirthElement.getElementsByTagName("placeOfBirth").item(0) != null) {
                                        placeOfBirth = placeOfBirthElement.getElementsByTagName("placeOfBirth").item(0).getTextContent();
                                    }
                                    if (placeOfBirthElement.getElementsByTagName("mainEntry").item(0) != null) {
                                        mainEntry = placeOfBirthElement.getElementsByTagName("mainEntry").item(0).getTextContent();
                                    }

                                    SdnPOB sdnPOB = new SdnPOB();
                                    sdnPOB.setUid(Integer.valueOf(uidPOB));
                                    sdnPOB.setSdnEntry_id(insertedSdnEntryId);
                                    sdnPOB.setDob(placeOfBirth);
                                    sdnPOB.setMain_entry(mainEntry);
                                    sdnPOB.setStatus("active");
                                    sdnPOB.setCreated_by(userOnProcess);
                                    sdnPOB.setUpdated_by(userOnProcess);
                                    sdnPOB.setCreated_date(newInputDate);
                                    sdnPOB.setUpdated_date(newInputDate);
                                    sdnPOBRepository.save(sdnPOB);
//                                    logger.info("uidPOB : " + uidPOB);
//                                    logger.info("placeOfBirth : " + placeOfBirth);
//                                    logger.info("mainEntry : " + mainEntry);

                                }
                            }

                        }
                    }

                    NodeList idList = ((Element) sdnEntryList.item(i)).getElementsByTagName("idList");
//                    logger.info("idList count : " + idList.getLength());
                    if (idList != null && idList.getLength() > 0) {
                        for (int a = 0; a < idList.getLength(); a++) {
                            NodeList id = ((Element) idList.item(a)).getElementsByTagName("id");
//                            logger.info("id count : " + id.getLength());
                            for (int q = 0; q < id.getLength(); q++) {
                                Node idNode = id.item(q);
                                if (idNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element idElement = (Element) idNode;
                                    String uidID = "";
                                    String idType = "";
                                    String idNumber = "";
                                    String idCountry = "";
                                    if (idElement.getElementsByTagName("uid").item(0) != null) {
                                        uidID = idElement.getElementsByTagName("uid").item(0).getTextContent();
                                    }
                                    if (idElement.getElementsByTagName("idType").item(0) != null) {
                                        idType = idElement.getElementsByTagName("idType").item(0).getTextContent();
                                    }
                                    if (idElement.getElementsByTagName("idNumber").item(0) != null) {
                                        idNumber = idElement.getElementsByTagName("idNumber").item(0).getTextContent();
                                        idNumber = idNumber.substring(0, Math.min(idNumber.length(), 250));
                                    }
                                    if (idElement.getElementsByTagName("idCountry").item(0) != null) {
                                        idCountry = idElement.getElementsByTagName("idCountry").item(0).getTextContent();
                                    }

                                    SdnID sdnID = new SdnID();
                                    sdnID.setSdnEntry_id(insertedSdnEntryId);
                                    sdnID.setUid(Integer.valueOf(uidID));
                                    sdnID.setId_type(idType);
                                    sdnID.setId_number(idNumber);
                                    sdnID.setStatus("active");
                                    sdnID.setId_country(idCountry);
                                    sdnID.setCreated_by(userOnProcess);
                                    sdnID.setUpdated_by(userOnProcess);
                                    sdnID.setCreated_date(newInputDate);
                                    sdnID.setUpdated_date(newInputDate);
                                    sdnIDRepository.save(sdnID);
//                                    logger.info("uidID : " + uidID);
//                                    logger.info("idType : " + idType);
//                                    logger.info("idNumber : " + idNumber);
//                                    logger.info("idCountry : " + idCountry);

                                }
                            }

                        }
                    }

                    NodeList citizenshipList = ((Element) sdnEntryList.item(i)).getElementsByTagName("citizenshipList");
//                    logger.info("citizenshipList count : " + citizenshipList.getLength());
                    if (citizenshipList != null && citizenshipList.getLength() > 0) {
                        for (int a = 0; a < citizenshipList.getLength(); a++) {
                            NodeList citizenship = ((Element) citizenshipList.item(a)).getElementsByTagName("citizenship");
//                            logger.info("citizenship count : " + citizenship.getLength());
                            for (int q = 0; q < citizenship.getLength(); q++) {
                                Node citizenshipNode = citizenship.item(q);
                                if (citizenshipNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element citizenshipElement = (Element) citizenshipNode;
                                    String uidCitizenShip = "";
                                    String country = "";
                                    String mainEntry = "";
                                    if (citizenshipElement.getElementsByTagName("uid").item(0) != null) {
                                        uidCitizenShip = citizenshipElement.getElementsByTagName("uid").item(0).getTextContent();
                                    }
                                    if (citizenshipElement.getElementsByTagName("country").item(0) != null) {
                                        country = citizenshipElement.getElementsByTagName("country").item(0).getTextContent();
                                    }
                                    if (citizenshipElement.getElementsByTagName("mainEntry").item(0) != null) {
                                        mainEntry = citizenshipElement.getElementsByTagName("mainEntry").item(0).getTextContent();
                                    }
                                    SdnCitizenship sdnCitizenship = new SdnCitizenship();
                                    sdnCitizenship.setSdnEntry_id(insertedSdnEntryId);
                                    sdnCitizenship.setUid(Integer.valueOf(uidCitizenShip));
                                    sdnCitizenship.setCountry(country);
                                    sdnCitizenship.setMain_entry(mainEntry);
                                    sdnCitizenship.setStatus("active");
                                    sdnCitizenship.setCreated_by(userOnProcess);
                                    sdnCitizenship.setUpdated_by(userOnProcess);
                                    sdnCitizenship.setCreated_date(newInputDate);
                                    sdnCitizenship.setUpdated_date(newInputDate);
                                    sdnCitizenshipRepository.save(sdnCitizenship);
//                                    logger.info("uidCitizenShip : " + uidCitizenShip);
//                                    logger.info("country : " + country);
//                                    logger.info("mainEntry : " + mainEntry);

                                }
                            }

                        }
                    }

                    NodeList nationalityList = ((Element) sdnEntryList.item(i)).getElementsByTagName("nationalityList");
//                    logger.info("nationalityList count : " + nationalityList.getLength());
                    if (nationalityList != null && nationalityList.getLength() > 0) {
                        for (int a = 0; a < nationalityList.getLength(); a++) {
                            NodeList nationality = ((Element) nationalityList.item(a)).getElementsByTagName("nationality");
//                            logger.info("nationality count : " + nationality.getLength());
                            for (int q = 0; q < nationality.getLength(); q++) {
                                Node nationalityNode = nationality.item(q);
                                if (nationalityNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element nationalityElement = (Element) nationalityNode;
                                    String uidNationality = "";
                                    String country = "";
                                    String mainEntry = "";
                                    if (nationalityElement.getElementsByTagName("uid").item(0) != null) {
                                        uidNationality = nationalityElement.getElementsByTagName("uid").item(0).getTextContent();
                                    }
                                    if (nationalityElement.getElementsByTagName("country").item(0) != null) {
                                        country = nationalityElement.getElementsByTagName("country").item(0).getTextContent();
                                    }
                                    if (nationalityElement.getElementsByTagName("mainEntry").item(0) != null) {
                                        mainEntry = nationalityElement.getElementsByTagName("mainEntry").item(0).getTextContent();
                                    }
                                    SdnNationality sdnNationality = new SdnNationality();
                                    sdnNationality.setSdnEntry_id(insertedSdnEntryId);
                                    sdnNationality.setUid(Integer.valueOf(uidNationality));
                                    sdnNationality.setCountry(country);
                                    sdnNationality.setMain_entry(mainEntry);
                                    sdnNationality.setStatus("active");
                                    sdnNationality.setCreated_by(userOnProcess);
                                    sdnNationality.setUpdated_by(userOnProcess);
                                    sdnNationality.setCreated_date(newInputDate);
                                    sdnNationality.setUpdated_date(newInputDate);
                                    sdnNationalityRepository.save(sdnNationality);
//                                    logger.info("uidNationality : " + uidNationality);
//                                    logger.info("country : " + country);
//                                    logger.info("mainEntry : " + mainEntry);

                                }
                            }

                        }
                    }
                }
            }
            sdnFileRepository.updateFileStatus(currentSdnFileId, "uploaded", " -uploading file success");
            logger.info("Uploading complete for  :" + fileName);
            createNotification("New " + file_type_name + " file successfully uploaded by " + userOnProcess + " on ");
            inputStream.close();
            summaryMatchingDetailRepository.deleteAll();

            response.setData(savedFileName);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("File upload success");
        } catch (JSONException | ParseException e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            sdnFileRepository.updateFileStatus(currentSdnFileId, "uploading-failed", " -Failed during upload process : " + e.getMessage());
            logger.info("Exception :" + e.getMessage());
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            sdnFileRepository.updateFileStatus(currentSdnFileId, "uploading-failed", " -Failed during upload process : " + e.getMessage());
            logger.info("Exception :" + e.getMessage());
        }
    }

    public BaseResponse uploadKtpFile(String file_name, String file_data) throws Exception {
        BaseResponse response = new BaseResponse();
        KTPFile ktpFile = new KTPFile();
        int currentKTPFileId = 0;
        try {
            String file_type = "ktp";
            createNotification("New KTP File uploading on ");
//            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));
//            //Token Auth
//            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
//                response.setStatus("401");
//                response.setSuccess(false);
//                response.setMessage("Token Authentication Failed");
//                return response;
//            }
//            String userOnProcess = auth.get("user_name").toString();
            createLog(file_name, "SYSTEM-AUTO", "uploadFile");
//
//            BaseResponse canUpload = checkUploadingOrMatchingFile(file_type);
//            if (canUpload.isSuccess() == false) {
//                return canUpload;
//            }
//
//            String fileData = jsonInput.getString("file_data");

            String savedFileName = "";
            logger.info("Saving " + file_name + "to SFTP . . .  . ");
            BaseResponse<Map<String, String>> fileUploadProcess = addFile(file_name, file_data, "ktp");
            if (fileUploadProcess.isSuccess() == true) {
                savedFileName = fileUploadProcess.getData().get("file");
                logger.info("saved");
            } else {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("Failed save file to SFTP : " + fileUploadProcess.getMessage());
                logger.info("failed");
                return response;
            }

            //CLEARING CURRENT DATA ON DB
            KTPFile onDeleteKtpFile = ktpFileRepository.getFileToBDeleted();
            if (onDeleteKtpFile != null) {
                logger.info("Clearing last uploaded " + file_type.toUpperCase() + " file data on DB . . .  . ");
                ktpFileRepository.updateFileStatus(onDeleteKtpFile.getKtp_file_id(), "deleted", "deleted on " + file_name + " uploading process");
                ktpDetailRepository.deleteAll();
                logger.info("Cleared");
            }

            Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            logger.info("Saving File " + file_name + "to DB . . .  . ");
            ktpFile.setFile_name_ori(file_name);
            ktpFile.setFile_name_save(savedFileName);
            ktpFile.setStatus("uploading");
            ktpFile.setCreated_by("SYSTEM-AUTO");
            ktpFile.setUpdated_by("SYSTEM-AUTO");
            ktpFile.setCreated_date(newInputDate);
            ktpFile.setUpdated_date(newInputDate);
            ktpFile.setRemarks("");
            ktpFileRepository.save(ktpFile);
            logger.info("saved");

            currentKTPFileId = ktpFileRepository.getKTPFileBySavedFileName(savedFileName).getKtp_file_id();
//            logger.info("currentKTPFileId :  " + currentKTPFileId);

            //PARSING KTP DATA
            String splitter = "\\|";
            String line = "";
            byte[] b = Base64.getMimeDecoder().decode(getFile(savedFileName, "ktp").getData().get("file_base64").toString());
            InputStream inputStream = new ByteArrayInputStream(b);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                String[] ktpData = line.split(splitter);
//                logger.info("KTP data : " + line);
                if (ktpData[0].matches(".*\\d.*")) {
                    String merchantNo = ktpData[0];
                    String merchantName = ktpData[1];
                    String ktp_1 = ktpData[2].replaceAll("\\.", "");
                    String ktp_2 = ktpData[3].replaceAll("\\.", "");
                    String name_1 = ktpData[4];
                    String name_2 = ktpData[5];
                    String dob1DateTemp = ktp_1.substring(6, 8);
                    String dob1MonthTemp = ktp_1.substring(8, 10);
                    String dob1YearTemp = ktp_1.substring(10, 12);
                    if (Integer.valueOf(dob1DateTemp) > 31) {
                        if (Integer.valueOf(dob1DateTemp) - 40 < 10) {
                            dob1DateTemp = String.format("%02d", (Integer.valueOf(dob1DateTemp) - 40));
                        } else {
                            dob1DateTemp = (Integer.valueOf(dob1DateTemp) - 40) + "";
                        }

                    }
                    String dob1 = new SimpleDateFormat("dd MMM yyyy").format(new SimpleDateFormat("ddMMyy").parse(dob1DateTemp + dob1MonthTemp + dob1YearTemp));
                    String dob2DateTemp = ktp_2.substring(6, 8);
                    String dob2MonthTemp = ktp_2.substring(8, 10);
                    String dob2YearTemp = ktp_2.substring(10, 12);
                    if (Integer.valueOf(dob2DateTemp) > 31) {
                        if (Integer.valueOf(dob2DateTemp) - 40 < 10) {
                            dob2DateTemp = String.format("%02d", (Integer.valueOf(dob2DateTemp) - 40));
                        } else {
                            dob2DateTemp = (Integer.valueOf(dob2DateTemp) - 40) + "";
                        }

                    }
                    String dob2 = new SimpleDateFormat("dd MMM yyy").format(new SimpleDateFormat("ddMMyy").parse(dob2DateTemp + dob2MonthTemp + dob2YearTemp));
                    KTPDetail ktpDetail = new KTPDetail();
                    ktpDetail.setKtp_file_id(currentKTPFileId);
                    ktpDetail.setMerchant_name(merchantName);
                    ktpDetail.setMerchant_no(merchantNo);
                    ktpDetail.setStatus("active");
                    ktpDetail.setName_1(name_1);
                    ktpDetail.setName_2(name_2);
                    ktpDetail.setDob_1(dob1);
                    ktpDetail.setDob_2(dob2);
                    ktpDetail.setKtp_1(ktp_1);
                    ktpDetail.setKtp_2(ktp_2);
                    ktpDetail.setCreated_by("SYSTEM-AUTO");
                    ktpDetail.setUpdated_by("SYSTEM-AUTO");
                    ktpDetail.setCreated_date(newInputDate);
                    ktpDetail.setUpdated_date(newInputDate);
                    ktpDetailRepository.save(ktpDetail);

                }
            }
            br.close();
            inputStream.close();
            ktpFileRepository.updateFileStatus(currentKTPFileId, "uploaded", " -uploading file success");
            logger.info("Uploading complete for  :" + file_name);
            createNotification("New KTP file successfully uploaded on ");


            response.setData(savedFileName);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("File upload success");

        } catch (JSONException | ParseException e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            ktpFileRepository.updateFileStatus(currentKTPFileId, "uploading-failed", " -Failed during upload process : " + e.getMessage());
            logger.info("Exception :" + e.getMessage());
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            ktpFileRepository.updateFileStatus(currentKTPFileId, "uploading-failed", " -Failed during upload process : " + e.getMessage());
            logger.info("Exception :" + e.getMessage());
        }
        return response;
    }

    public BaseResponse uploadDmaFile(String file_name, String file_data) throws Exception {
        BaseResponse response = new BaseResponse();
        DMAFile dmaFile = new DMAFile();
        int currentDMAFileId = 0;
        try {
            String file_type = "dma";
            createNotification("New DMA file uploading on ");
//            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));
//            //Token Auth
//            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
//                response.setStatus("401");
//                response.setSuccess(false);
//                response.setMessage("Token Authentication Failed");
//                return response;
//            }
//            String userOnProcess = auth.get("user_name").toString();
            createLog(file_name, "SYSTEM-AUTO", "uploadFile");
//
//            BaseResponse canUpload = checkUploadingOrMatchingFile(file_type);
//            if (canUpload.isSuccess() == false) {
//                return canUpload;
//            }
//
//            String fileData = jsonInput.getString("file_data");


            String savedFileName = "";
            logger.info("Saving " + file_name + "to SFTP . . .  . ");
            BaseResponse<Map<String, String>> fileUploadProcess = addFile(file_name, file_data, "dma");
            if (fileUploadProcess.isSuccess() == true) {
                savedFileName = fileUploadProcess.getData().get("file");
                logger.info("saved");
            } else {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("Failed save file to SFTP : " + fileUploadProcess.getMessage());
                logger.info("failed");
                return response;
            }

            //CLEARING CURRENT DATA ON DB
            DMAFile onDeleteDMAFile = dmaFileRepository.getFileToBDeleted();
            if (onDeleteDMAFile != null) {
                logger.info("Clearing last uploaded " + file_type.toUpperCase() + " file data on DB . . .  . ");
                dmaFileRepository.updateFileStatus(onDeleteDMAFile.getDmafile_id(), "deleted", "deleted on " + file_name + " uploading process");
                dmaDetailRepository.deleteAll();
                logger.info("Cleared");
            }

            Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            logger.info("Saving File " + file_name + "to DB . . .  . ");
            dmaFile.setFile_name_ori(file_name);
            dmaFile.setFile_name_save(savedFileName);
            dmaFile.setStatus("uploading");
            dmaFile.setCreated_by("SYSTEM-AUTO");
            dmaFile.setUpdated_by("SYSTEM-AUTO");
            dmaFile.setCreated_date(newInputDate);
            dmaFile.setUpdated_date(newInputDate);
            dmaFile.setRemarks("");
            dmaFileRepository.save(dmaFile);
            logger.info("saved");

            currentDMAFileId = dmaFileRepository.getDMAFileBySavedFileName(savedFileName).getDmafile_id();
//            logger.info("currentDMAFileId :  " + currentDMAFileId);

            String splitter = "\\|";
            String line = "";
            byte[] b = Base64.getMimeDecoder().decode(getFile(savedFileName, "dma").getData().get("file_base64").toString());
            InputStream inputStream = new ByteArrayInputStream(b);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                String[] dmaData = line.split(splitter, -1);
                if (dmaData[0].matches(".*\\d.*")) {
                    String merchantNo = dmaData[1];
                    DMADetail dmaDetail = new DMADetail();
                    dmaDetail.setDmafile_id(currentDMAFileId);
                    dmaDetail.setStatus("active");
                    dmaDetail.setMerchant_no(merchantNo);
                    dmaDetail.setCreated_by("SYSTEM-AUTO");
                    dmaDetail.setUpdated_by("SYSTEM-AUTO");
                    dmaDetail.setCreated_date(newInputDate);
                    dmaDetail.setUpdated_date(newInputDate);
                    dmaDetailRepository.save(dmaDetail);


                }
            }
            br.close();
            inputStream.close();
            dmaFileRepository.updateFileStatus(currentDMAFileId, "uploaded", " -uploading file success");
            logger.info("Uploading complete for  :" + file_name);
            createNotification("New DMA file successfully uploaded on ");


            response.setData(savedFileName);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("File upload success");


        } catch (JSONException | ParseException e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            dmaFileRepository.updateFileStatus(currentDMAFileId, "uploading-failed", " -Failed during upload process : " + e.getMessage());
            logger.info("Exception :" + e.getMessage());
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            dmaFileRepository.updateFileStatus(currentDMAFileId, "uploading-failed", " -Failed during upload process : " + e.getMessage());
            logger.info("Exception :" + e.getMessage());
        }
        return response;
    }

    public BaseResponse searchData(String input) throws Exception {
        BaseResponse response = new BaseResponse();
        String first_name = "";
        String last_name = "";
        String id_number = "";
        String dob = "";
        int limit = 0;
        int offset = 0;
        int startingData = 0;
        List<SdnEntry> searchSdn = new ArrayList<>();
        List<SdnEntry> searchConsal = new ArrayList<>();
        List<SdnEntry> searchSdnOri = new ArrayList<>();
        List<SdnEntry> searchConsalOri = new ArrayList<>();
        try {
            JSONObject jsonInput = new JSONObject(input);
            first_name = jsonInput.optString("first_name");
            last_name = jsonInput.optString("last_name");
            id_number = jsonInput.optString("id_number");
            dob = jsonInput.optString("dob");
            limit = jsonInput.getInt("limit");
            offset = jsonInput.getInt("offset");
            startingData = (offset - 1) * limit;
            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));
            //Token Auth
            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("401");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(input, userOnProcess, "searchData");

            if (dob.isEmpty()) {
                searchSdnOri = sdnEntryRepository.searchDataNameId("sdn", first_name, last_name, id_number);
                searchConsalOri = sdnEntryRepository.searchDataNameId("consal", first_name, last_name, id_number);
            } else {
//                dob = new SimpleDateFormat("dd MMM yyyy").format(new SimpleDateFormat("dd-MM-yyy").parse(dob));
                searchSdnOri = sdnEntryRepository.searchDataNameIdDob("sdn", first_name, last_name, id_number, dob);
                searchConsalOri = sdnEntryRepository.searchDataNameIdDob("consal", first_name, last_name, id_number, dob);
            }
//            logger.info("sdn data ori size : " + searchSdnOri.size());
//            logger.info("consal data ori size : " + searchConsalOri.size());
            int maxPageSdn = (int) Math.ceil(searchSdnOri.size() / (limit * 1.0));
            int maxPageConsal = (int) Math.ceil(searchConsalOri.size() / (limit * 1.0));
            if (searchSdnOri.size() > 0) {
                searchSdn = searchSdnOri.subList(startingData, Math.min((startingData + limit), searchSdnOri.size()));
//                logger.info("sdn data from : " + startingData + " to " + Math.min((startingData + limit), searchSdnOri.size()));
            }
            if (searchConsalOri.size() > 0) {
                searchConsal = searchConsalOri.subList(startingData, Math.min((startingData + limit), searchConsalOri.size()));
//                logger.info("consal data from : " + startingData + " to " + Math.min((startingData + limit), searchConsalOri.size()));
            }

//            logger.info("sdn data to retrieve : " + searchSdn.size());
//            logger.info("consal data to retrieve : " + searchConsal.size());


            Map<String, Object> results = new HashMap<>();
            List resultSdn = new ArrayList();
            List resultConsal = new ArrayList();
            List<SdnAddress> sdnAddressList = new ArrayList<>();
            List<SdnAka> sdnAkaList = new ArrayList<>();
            List<SdnCitizenship> sdnCitizenshipList = new ArrayList<>();
            List<SdnDOB> sdnDOBList = new ArrayList<>();
            List<SdnID> sdnIDList = new ArrayList<>();
            List<SdnNationality> sdnNationalityList = new ArrayList<>();
            List<SdnPOB> sdnPOBList = new ArrayList<>();
            List<SdnProgram> sdnProgramsList = new ArrayList<>();
            if (searchSdn.size() > 0) {
                for (int i = 0; i < searchSdn.size(); i++) {
                    Map dataSearch = new HashMap();
                    SdnEntry entry = searchSdn.get(i);
                    sdnAddressList = sdnAddressRepository.getSdnAddressBySdnEntryId(entry.getSdnEntry_id());
                    sdnAkaList = sdnAkaRepository.getSdnAkaBySdnEntryId(entry.getSdnEntry_id());
                    sdnCitizenshipList = sdnCitizenshipRepository.searchCitizenshipBySdnEntryId(entry.getSdnEntry_id());
                    sdnDOBList = sdnDOBRepository.searchDOBBySdnEntryId(entry.getSdnEntry_id());
                    sdnIDList = sdnIDRepository.searchIDBySdnEntryId(entry.getSdnEntry_id());
                    sdnNationalityList = sdnNationalityRepository.searchNationalityBySdnEntryId(entry.getSdnEntry_id());
                    sdnPOBList = sdnPOBRepository.searchPOBBySdnEntryId(entry.getSdnEntry_id());
                    sdnProgramsList = sdnProgramRepository.searchProgramBySdnEntryId(entry.getSdnEntry_id());

                    Map headerData = new HashMap();
                    //first-name
                    headerData.put("first_name", "-");
                    if (entry.getFirst_name().compareToIgnoreCase(first_name) == 0) {
                        headerData.put("first_name", entry.getFirst_name());
                    } else if (entry.getLast_name().compareToIgnoreCase(first_name) == 0) {
                        headerData.put("first_name", entry.getLast_name());
                    }
                    //last-name
                    headerData.put("last_name", "-");
                    if (entry.getFirst_name().compareToIgnoreCase(last_name) == 0) {
                        headerData.put("last_name", entry.getFirst_name());
                    } else if (entry.getLast_name().compareToIgnoreCase(last_name) == 0) {
                        headerData.put("last_name", entry.getLast_name());
                    }
                    if (headerData.get("first_name").toString().compareToIgnoreCase("-") == 0) {
                        if (sdnAkaList.size() > 0) {
                            for (int a = 0; a < sdnAkaList.size(); a++) {
                                SdnAka sdnAka = sdnAkaList.get(a);
                                if (sdnAka.getFirst_name().compareToIgnoreCase(first_name) == 0) {
                                    headerData.put("first_name", sdnAka.getFirst_name());
                                } else if (sdnAka.getLast_name().compareToIgnoreCase(first_name) == 0) {
                                    headerData.put("first_name", sdnAka.getLast_name());
                                }

                            }
                        }
                    }
                    if (headerData.get("last_name").toString().compareToIgnoreCase("-") == 0) {
                        if (sdnAkaList.size() > 0) {
                            for (int a = 0; a < sdnAkaList.size(); a++) {
                                SdnAka sdnAka = sdnAkaList.get(a);
                                if (sdnAka.getFirst_name().compareToIgnoreCase(first_name) == 0) {
                                    headerData.put("last_name", sdnAka.getFirst_name());
                                } else if (sdnAka.getLast_name().compareToIgnoreCase(first_name) == 0) {
                                    headerData.put("last_name", sdnAka.getLast_name());
                                }

                            }
                        }
                    }


                    //dob
                    headerData.put("dob", "-");
                    if (sdnDOBList.size() > 0) {
                        for (int j = 0; j < sdnDOBList.size(); j++) {
                            SdnDOB sdnDOB = sdnDOBList.get(j);
                            if (sdnDOB.getDob().compareToIgnoreCase(dob) == 0) {
                                headerData.put("dob", sdnDOB.getDob());
                            }
                        }
                    }

                    //id
                    headerData.put("id_number", "-");
                    if (sdnIDList.size() > 0) {
                        for (int j = 0; j < sdnIDList.size(); j++) {
                            SdnID sdnID = sdnIDList.get(j);
                            if (sdnID.getId_number().compareToIgnoreCase(id_number) == 0) {
                                headerData.put("id_number", sdnID.getId_number());
                            }
                        }
                    }
                    dataSearch.put("header", headerData);
                    dataSearch.put("entry", entry);
                    dataSearch.put("addrees", sdnAddressList);
                    dataSearch.put("aka", sdnAkaList);
                    dataSearch.put("citizenship", sdnCitizenshipList);
                    dataSearch.put("dob", sdnDOBList);
                    dataSearch.put("id", sdnIDList);
                    dataSearch.put("nationality", sdnNationalityList);
                    dataSearch.put("pob", sdnPOBList);
                    dataSearch.put("program", sdnProgramsList);

                    resultSdn.add(dataSearch);

                }
            }
            if (searchConsal.size() > 0) {
                for (int i = 0; i < searchConsal.size(); i++) {
                    Map dataSearch = new HashMap();
                    SdnEntry entry = searchConsal.get(i);
                    sdnAddressList = sdnAddressRepository.getSdnAddressBySdnEntryId(entry.getSdnEntry_id());
                    sdnAkaList = sdnAkaRepository.getSdnAkaBySdnEntryId(entry.getSdnEntry_id());
                    sdnCitizenshipList = sdnCitizenshipRepository.searchCitizenshipBySdnEntryId(entry.getSdnEntry_id());
                    sdnDOBList = sdnDOBRepository.searchDOBBySdnEntryId(entry.getSdnEntry_id());
                    sdnIDList = sdnIDRepository.searchIDBySdnEntryId(entry.getSdnEntry_id());
                    sdnNationalityList = sdnNationalityRepository.searchNationalityBySdnEntryId(entry.getSdnEntry_id());
                    sdnPOBList = sdnPOBRepository.searchPOBBySdnEntryId(entry.getSdnEntry_id());
                    sdnProgramsList = sdnProgramRepository.searchProgramBySdnEntryId(entry.getSdnEntry_id());

                    Map headerData = new HashMap();
                    //first-name
                    if (entry.getFirst_name().compareToIgnoreCase(first_name) == 0) {
                        headerData.put("first_name", entry.getFirst_name());
                    } else if (entry.getLast_name().compareToIgnoreCase(first_name) == 0) {
                        headerData.put("first_name", entry.getLast_name());
                    } else {
                        headerData.put("first_name", "-");
                    }
                    //last-name
                    if (entry.getFirst_name().compareToIgnoreCase(last_name) == 0) {
                        headerData.put("last_name", entry.getFirst_name());
                    } else if (entry.getLast_name().compareToIgnoreCase(last_name) == 0) {
                        headerData.put("last_name", entry.getLast_name());
                    } else {
                        headerData.put("last_name", "-");
                    }

                    //dob
                    headerData.put("dob", "-");
                    if (sdnDOBList.size() > 0) {
                        for (int j = 0; j < sdnDOBList.size(); j++) {
                            SdnDOB sdnDOB = sdnDOBList.get(j);
                            if (sdnDOB.getDob().compareToIgnoreCase(dob) == 0) {
                                headerData.put("dob", sdnDOB.getDob());
                            }
                        }
                    }

                    //id
                    headerData.put("id_number", "-");
                    if (sdnIDList.size() > 0) {
                        for (int j = 0; j < sdnIDList.size(); j++) {
                            SdnID sdnID = sdnIDList.get(j);
                            if (sdnID.getId_number().compareToIgnoreCase(id_number) == 0) {
                                headerData.put("id_number", sdnID.getId_number());
                            }
                        }
                    }
                    dataSearch.put("header", headerData);
                    dataSearch.put("entry", entry);
                    dataSearch.put("addrees", sdnAddressList);
                    dataSearch.put("aka", sdnAkaList);
                    dataSearch.put("citizenship", sdnCitizenshipList);
                    dataSearch.put("dob", sdnDOBList);
                    dataSearch.put("id", sdnIDList);
                    dataSearch.put("nationality", sdnNationalityList);
                    dataSearch.put("pob", sdnPOBList);
                    dataSearch.put("program", sdnProgramsList);

                    resultConsal.add(dataSearch);

                }
            }
            results.put("sdnData", resultSdn);
            results.put("consalData", resultConsal);
            results.put("maxPageSdn", maxPageSdn);
            results.put("maxPageConsal", maxPageConsal);


            response.setData(results);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Search data success");

        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    public BaseResponse<Map<String, String>> addFile(String file_name, String file_content, String folder) throws Exception {
        BaseResponse response = new BaseResponse();
        Session session = null;
        ChannelSftp channel = null;
        Map<String, String> imageAddResult = new HashMap<>();
        try {
            Map<String, String> systemParameter = parseSystemParameter();
            if (systemParameter.get("errorMessage").compareToIgnoreCase("") != 0) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage(systemParameter.get("errorMessage"));
                return response;
            }

            UUID uuid = UUID.randomUUID();
            session = new JSch().getSession(systemParameter.get("sftpUser"), systemParameter.get("sftpUrl"), Integer.valueOf(systemParameter.get("sftpPort")));
            session.setPassword(systemParameter.get("sftpPassword"));
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            String path = "";
            if (folder.compareToIgnoreCase("sdn") == 0) {
                path = systemParameter.get("pathSaveSdn");
            } else if (folder.compareToIgnoreCase("consolidate") == 0) {
                path = systemParameter.get("pathSaveConsolidate");
            } else if (folder.compareToIgnoreCase("ktp") == 0) {
                path = systemParameter.get("pathSaveKtp");
            } else if (folder.compareToIgnoreCase("dma") == 0) {
                path = systemParameter.get("pathSaveDma");
            } else {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("unknown folder");
                return response;
            }
            byte[] b = Base64.getMimeDecoder().decode(file_content);
            InputStream stream = new ByteArrayInputStream(b);
            channel.put(stream, path + uuid + "_" + file_name, 0);

            imageAddResult.put("file", uuid + "_" + file_name);

            response.setData(imageAddResult);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("File successfully Added");
        } catch (Exception e) {
            response.setData(imageAddResult);
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            logger.info("Exception : " + e.getMessage());
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

    public BaseResponse<Map<String, Object>> getFile(String file_name, String folder) {
        BaseResponse response = new BaseResponse();
        Session session = null;
        ChannelSftp channel = null;
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, String> systemParameter = parseSystemParameter();
            if (systemParameter.get("errorMessage").compareToIgnoreCase("") != 0) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage(systemParameter.get("errorMessage"));
                return response;
            }
            session = new JSch().getSession(systemParameter.get("sftpUser"), systemParameter.get("sftpUrl"), Integer.valueOf(systemParameter.get("sftpPort")));
            session.setPassword(systemParameter.get("sftpPassword"));
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            String path = "";
            if (folder.compareToIgnoreCase("sdn") == 0) {
                path = systemParameter.get("pathSaveSdn");
            } else if (folder.compareToIgnoreCase("consolidate") == 0) {
                path = systemParameter.get("pathSaveConsolidate");
            } else if (folder.compareToIgnoreCase("ktp") == 0) {
                path = systemParameter.get("pathSaveKtp");
            } else if (folder.compareToIgnoreCase("dma") == 0) {
                path = systemParameter.get("pathSaveDma");
            } else {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("unknown folder");
                return response;
            }

            InputStream inputStream = channel.get(path + file_name);
//            logger.info("file path : " + path + file_name);
            byte[] bytes = IOUtils.toByteArray(inputStream);
            String base64 = Base64.getEncoder().encodeToString(bytes);

            result.put("file_base64", base64);
            result.put("file_bytes", bytes);


            response.setData(result);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Get File success");
        } catch (Exception e) {
            response.setStatus("404");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            logger.info(new Date().getTime() + e.getMessage());
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

    public Map<String, Object> checkAllowedFileType(String file_type) {
        Map<String, Object> result = new HashMap();
        List<String> allowedFileType = Arrays.asList("sdn", "consal");
        try {
            if (allowedFileType.contains(file_type.toLowerCase())) {
                result.put("matched", true);
                result.put("message", "file_type allowed");
            } else {
                result.put("matched", false);
                result.put("message", "File_type not allowed. Allowed file_type are : " + allowedFileType.toString());
            }

        } catch (Exception e) {
            result.put("matched", false);
            result.put("message", "error on checking file_type");
        }
        return result;
    }

    public BaseResponse checkUploadingOrMatchingFile(String file_type) throws Exception {
        BaseResponse response = new BaseResponse();
        List uploadingOrMatchingFile = new ArrayList();
        try {
            if (file_type.compareToIgnoreCase("sdn") == 0 || file_type.compareToIgnoreCase("consal") == 0) {
                uploadingOrMatchingFile = sdnFileRepository.getMatchingOrUploadingFile(file_type);
            } else if (file_type.compareToIgnoreCase("ktp") == 0) {
                uploadingOrMatchingFile = ktpFileRepository.getMatchingOrUploadingFile();
            } else if (file_type.compareToIgnoreCase("dma") == 0) {
                uploadingOrMatchingFile = dmaFileRepository.getMatchingOrUploadingFile();
            }
            if (uploadingOrMatchingFile.size() > 0) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("Can't upload file, another file  still on  process");
                logger.info("Load file exception : can't upload file, another file is still on process");
            } else {
                response.setStatus("200");
                response.setSuccess(true);
                response.setMessage("No file on uploading or matching proses, upload process can be done");
                logger.info("No file on uploading or matching proses, upload process can be done");
            }
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage("Service error : " + e.getMessage());
            logger.info("Service error : " + e.getMessage());
        }

        return response;
    }

    public BaseResponse<List<Object>> getNotDeletedFile(String input) {
        BaseResponse<List<Object>> response = new BaseResponse<>();
        List<Object> fileList = new ArrayList<>();
        try {
            JSONObject jsonInput = new JSONObject(input);
            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));
            //Token Auth
            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("401");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(input, userOnProcess, "getUndeletedFile");
            List sdnFileList = sdnFileRepository.getNotDeletedFile();
//            List ktpFileList = ktpFileRepository.getNotDeletedFile();
//            List dmaFileList = dmaFileRepository.getNotDeletedFile();
            fileList.addAll(sdnFileList);
//            fileList.addAll(ktpFileList);
//            fileList.addAll(dmaFileList);

            response.setData(fileList);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("File Listed");
            logger.info("File Listed");

        } catch (Exception e) {
            response.setData(new ArrayList<>());
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage("Server error : " + e.getMessage());
            logger.info("Server error : " + e.getMessage());
        }
        return response;
    }

    //TOKEN AUTH
    public Map<String, Object> tokenAuthentication(String token) {
        Map<String, Object> result = new HashMap();
        List<Users> usersList;
        try {
            usersList = userRepository.tokenAuth(token);
            if (usersList.size() > 0) {
                result.put("valid", true);
                result.put("user_name", usersList.get(0).getUser_name());
            } else {
                result.put("valid", false);
                result.put("user_name", "");
            }

        } catch (Exception e) {
            result.put("valid", false);
            result.put("user_name", "");
        }
        return result;
    }

    //REPORT
    public BaseResponse getReportList(String input) throws Exception {
        BaseResponse response = new BaseResponse();
        List result = new ArrayList();
        Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        try {
            JSONObject jsonInput = new JSONObject(input);

            String start_date = jsonInput.getString("start_date");
            String end_date = jsonInput.getString("end_date");
//            start_date = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd-MM-yyyy").parse(start_date));
//            end_date = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd-MM-yyyy").parse(end_date));
            if (start_date.compareToIgnoreCase("") == 0 || end_date.compareToIgnoreCase("") == 0) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("start date AND end date can't be empty");
                return response;
            }
            reportRepository.deleteAll();
            logger.info("start_date : " + start_date);
            logger.info("end_date : " + end_date);

            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));
            //Token Auth
            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("401");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(input, userOnProcess, "getReport");

            List<SummaryMatching> dataMatching = summaryMatchingRepository.getSummaryMatchingByStartdEndDate(start_date, end_date);
            for (int i = 0; i < dataMatching.size(); i++) {
                SummaryMatching summaryMatching = dataMatching.get(i);
                Report sdnReport = new Report();
                Report consolidateReport = new Report();
                //SDN
                sdnReport.setStatus("active");
                sdnReport.setExtract_date(summaryMatching.getExtract_date_sdn());
                sdnReport.setOfac_list_screened("SDN List");
                sdnReport.setStart_date(summaryMatching.getStart_matching().toString());
                sdnReport.setEnd_date(summaryMatching.getEnd_matching().toString());
                sdnReport.setPositive(summaryMatching.getCount_positive_sdn());
                sdnReport.setPotential(summaryMatching.getCount_potential_sdn());
                sdnReport.setTotal_screened(summaryMatching.getScreen_data());
                sdnReport.setTotal_data(summaryMatching.getSdn_data());
                sdnReport.setCreated_by("SYSTEM-AUTO");
                sdnReport.setUpdated_by("SYSTEM-AUTO");
                sdnReport.setCreated_date(newInputDate);
                sdnReport.setUpdated_date(newInputDate);
                reportRepository.save(sdnReport);

                //CONSOLIDATE
                consolidateReport.setStatus("active");
                consolidateReport.setExtract_date(summaryMatching.getExtract_date_consolidate());
                consolidateReport.setOfac_list_screened("Consolidated List");
                consolidateReport.setStart_date(summaryMatching.getStart_matching().toString());
                consolidateReport.setEnd_date(summaryMatching.getEnd_matching().toString());
                consolidateReport.setPositive(summaryMatching.getCount_positive_consolidate());
                consolidateReport.setPotential(summaryMatching.getCount_potential_consolidate());
                consolidateReport.setTotal_screened(summaryMatching.getScreen_data());
                consolidateReport.setTotal_data(summaryMatching.getConsolidate_data());
                consolidateReport.setCreated_by("SYSTEM-AUTO");
                consolidateReport.setUpdated_by("SYSTEM-AUTO");
                consolidateReport.setCreated_date(newInputDate);
                consolidateReport.setUpdated_date(newInputDate);
                reportRepository.save(consolidateReport);
            }
            result = reportRepository.findAll();


            response.setData(result);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Report Listed");
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            logger.info("Exception : " + e.getMessage());
        }

        return response;
    }

    //LOGGER
    public void createLog(String input, String userOnProcess, String service_name) throws ParseException {
        Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        SdnLogger sdnLogger = new SdnLogger();
        sdnLogger.setService_name(service_name);
        sdnLogger.setService_body(input);
        sdnLogger.setCreated_by(userOnProcess);
        sdnLogger.setCreated_date(newInputDate);
        sdnLoggerRepository.save(sdnLogger);
    }

    //NOTIFICATION
    public void createNotification(String message) throws ParseException {
        Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        SdnNotification sdnNotification = new SdnNotification();
        sdnNotification.setNotification_message(message);
        sdnNotification.setCreated_by("SYSTEM");
        sdnNotification.setCreated_date(newInputDate);
        sdnNotificationRepository.save(sdnNotification);
    }

    //SYSTEM PARAMETER PARSER
    public Map<String, String> parseSystemParameter() throws Exception {
        Map<String, String> result = new HashMap<>();
        List<SystemParameter> systemParameterList = systemParameterRepository.getSystemParameter();
        String sftpUser;
        String sftpPassword;
        String sftpUrl;
        String sftpPort;

        String pathUploadKtp;
        String pathUploadDma;
        String pathSaveDma;
        String pathSaveKtp;
        String pathSaveSdn;
        String pathSaveConsolidate;
        String pathReportFile;

        String ldapUrl;
        String ldapBase;
        try {
            for (SystemParameter parameter : systemParameterList) {
                if (parameter.getParameter_name().compareToIgnoreCase("sftpUser") == 0) {
                    sftpUser = parameter.getParameter_value();
                    result.put("sftpUser", sftpUser);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("sftpPassword") == 0) {
                    sftpPassword = parameter.getParameter_value();
                    result.put("sftpPassword", sftpPassword);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("sftpUrl") == 0) {
                    sftpUrl = parameter.getParameter_value();
                    result.put("sftpUrl", sftpUrl);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("sftpPort") == 0) {
                    sftpPort = parameter.getParameter_value();
                    result.put("sftpPort", sftpPort);
                }

                if (parameter.getParameter_name().compareToIgnoreCase("pathUploadKtp") == 0) {
                    pathUploadKtp = parameter.getParameter_value();
                    result.put("pathUploadKtp", pathUploadKtp);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("pathUploadDma") == 0) {
                    pathUploadDma = parameter.getParameter_value();
                    result.put("pathUploadDma", pathUploadDma);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("pathSaveDma") == 0) {
                    pathSaveDma = parameter.getParameter_value();
                    result.put("pathSaveDma", pathSaveDma);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("pathSaveKtp") == 0) {
                    pathSaveKtp = parameter.getParameter_value();
                    result.put("pathSaveKtp", pathSaveKtp);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("pathSaveSdn") == 0) {
                    pathSaveSdn = parameter.getParameter_value();
                    result.put("pathSaveSdn", pathSaveSdn);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("pathSaveConsolidate") == 0) {
                    pathSaveConsolidate = parameter.getParameter_value();
                    result.put("pathSaveConsolidate", pathSaveConsolidate);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("pathReportFile") == 0) {
                    pathReportFile = parameter.getParameter_value();
                    result.put("pathReportFile", pathReportFile);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("ldapUrl") == 0) {
                    ldapUrl = parameter.getParameter_value();
                    result.put("ldapUrl", ldapUrl);
                }
                if (parameter.getParameter_name().compareToIgnoreCase("ldapBase") == 0) {
                    ldapBase = parameter.getParameter_value();
                    result.put("ldapBase", ldapBase);
                }
                result.put("errorMessage", "");

            }
        } catch (Exception e) {
            result.put("errorMessage", e.getMessage());
        }

        return result;
    }


    //SCHEDULER KTP-DMA
    @Scheduled(cron = "0 0/45 * * * *", zone = "Asia/Jakarta")
    public void scheduledUploadKtpFile() throws Exception {
        Session session = null;
        ChannelSftp channel = null;
        logger.info("Starting auto upload KTP file");

        List<KTPFile> onProcessKtpFile = ktpFileRepository.getMatchingOrUploadingFile();


        if (onProcessKtpFile.size() == 0) {
            try {
                Map<String, String> systemParameter = parseSystemParameter();
                if (systemParameter.get("errorMessage").compareToIgnoreCase("") != 0) {
                    return;
                }
                session = new JSch().getSession(systemParameter.get("sftpUser"), systemParameter.get("sftpUrl"), Integer.valueOf(systemParameter.get("sftpPort")));
                session.setPassword(systemParameter.get("sftpPassword"));
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();
                String fileName = "";
                String filePathUploadKtp = systemParameter.get("filePathUploadKtp");
                Vector<ChannelSftp.LsEntry> dataKtpFile = channel.ls(filePathUploadKtp);
                for (ChannelSftp.LsEntry lsEntry : dataKtpFile) {
                    if (!lsEntry.getAttrs().isDir()) {
                        fileName = lsEntry.getFilename();
                    }
                }

                if (!fileName.isEmpty()) {
                    summaryMatchingDetailRepository.deleteAll();
                    InputStream inputStream = channel.get(filePathUploadKtp + fileName);
                    byte[] bytes = IOUtils.toByteArray(inputStream);
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    BaseResponse uploadKtpResult = uploadKtpFile(fileName, base64);
                    if (uploadKtpResult.isSuccess()) {
                        channel.rm(filePathUploadKtp + fileName);
                    }

                } else {
                    logger.info("No new KTP file to upload");
                }
            } catch (Exception e) {
                logger.info("500 : Error while check KTP file on SFTP");
            } finally {
                if (session.isConnected() || session != null) {
                    session.disconnect();
                }
                if (channel.isConnected() || channel != null) {
                    channel.disconnect();
                }
            }
        }
    }

    @Scheduled(cron = "0 0/45 * * * *", zone = "Asia/Jakarta")
    public void scheduledUploadDmaFile() throws Exception {
        Session session = null;
        ChannelSftp channel = null;
        logger.info("Starting auto upload DMA file");

        List<DMAFile> onProcessDmaFile = dmaFileRepository.getMatchingOrUploadingFile();


        if (onProcessDmaFile.size() == 0) {
            try {
                Map<String, String> systemParameter = parseSystemParameter();
                if (systemParameter.get("errorMessage").compareToIgnoreCase("") != 0) {
                    return;
                }
                session = new JSch().getSession(systemParameter.get("sftpUser"), systemParameter.get("sftpUrl"), Integer.valueOf(systemParameter.get("sftpPort")));
                session.setPassword(systemParameter.get("sftpPassword"));
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();
                String fileName = "";
                String pathUploadDma = systemParameter.get("pathUploadDma");
                Vector<ChannelSftp.LsEntry> dataDmaFile = channel.ls(pathUploadDma);
                for (ChannelSftp.LsEntry lsEntry : dataDmaFile) {
                    if (!lsEntry.getAttrs().isDir()) {
                        fileName = lsEntry.getFilename();
                    }
                }

                if (!fileName.isEmpty()) {
                    summaryMatchingDetailRepository.deleteAll();
                    InputStream inputStream = channel.get(pathUploadDma + fileName);
                    byte[] bytes = IOUtils.toByteArray(inputStream);
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    BaseResponse uploadDmaResult = uploadDmaFile(fileName, base64);
                    if (uploadDmaResult.isSuccess()) {
                        channel.rm(pathUploadDma + fileName);
                    }

                } else {
                    logger.info("No new DMA file to upload");
                }
            } catch (Exception e) {
                logger.info("500 : Error while check DMA file on SFTP");
            } finally {
                if (session.isConnected() || session != null) {
                    session.disconnect();
                }
                if (channel.isConnected() || channel != null) {
                    channel.disconnect();
                }
            }
        }
    }

    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Jakarta")
    public void matchingProcess() throws Exception {
        logger.info("Starting auto matching");
//        int count = 0;
        List<SdnFile> sdnFileUploaded = sdnFileRepository.getFileByStatus("uploaded", "sdn");
        List<SdnFile> consalFileUploaded = sdnFileRepository.getFileByStatus("uploaded", "consal");
        List<DMAFile> dmaFileUploaded = dmaFileRepository.getFileByStatus("uploaded");
        List<KTPFile> ktpFileUploaded = ktpFileRepository.getFileByStatus("uploaded");


        List<SdnFile> sdnFileMatched = sdnFileRepository.getFileByStatus("matched", "sdn");
        List<SdnFile> consalFileMatched = sdnFileRepository.getFileByStatus("matched", "consal");
        List<DMAFile> dmaFileMatched = dmaFileRepository.getFileByStatus("matched");
        List<KTPFile> ktpFileMatched = ktpFileRepository.getFileByStatus("matched");
        if (sdnFileMatched.size() > 0 && consalFileMatched.size() > 0 && dmaFileMatched.size() > 0 && ktpFileMatched.size() > 0) {
            logger.info("No matching process all needed file already matched");
            return;

        }

        if ((sdnFileUploaded.size() > 0 || sdnFileMatched.size() > 0) && (consalFileUploaded.size() > 0 || consalFileMatched.size() > 0)
                && (dmaFileUploaded.size() > 0 || dmaFileMatched.size() > 0) && (ktpFileUploaded.size() > 0) || ktpFileMatched.size() > 0) {
            createNotification("Matching process started on ");

            Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            //FILE - FILE ON MATCHING PROCESS
            int sdnFileId = sdnFileUploaded.get(0).getSdnfile_id();
            if (sdnFileMatched.size() > 0) {
                sdnFileId = sdnFileMatched.get(0).getSdnfile_id();
            }
            int consalFileId = consalFileUploaded.get(0).getSdnfile_id();
            if (consalFileMatched.size() > 0) {
                consalFileId = consalFileMatched.get(0).getSdnfile_id();
            }
            int dmaFileId = dmaFileUploaded.get(0).getDmafile_id();
            if (dmaFileMatched.size() > 0) {
                consalFileId = dmaFileMatched.get(0).getDmafile_id();
            }
            int ktpFileId = ktpFileUploaded.get(0).getKtp_file_id();
            if (ktpFileMatched.size() > 0) {
                consalFileId = ktpFileMatched.get(0).getKtp_file_id();
            }

            //UPDATE STATUS FILES ON PROCESS
            sdnFileRepository.updateFileStatus(sdnFileId, "matching", " -matching process");
            sdnFileRepository.updateFileStatus(consalFileId, "matching", " -matching process");
            dmaFileRepository.updateFileStatus(dmaFileId, "matching", " -matching process");
            ktpFileRepository.updateFileStatus(ktpFileId, "matching", " -matching process");

            //GET COUNT DETAIL DATA
            int countSdnEntry = sdnEntryRepository.getEntryCount(sdnFileId);
            int countConsalEntry = sdnEntryRepository.getEntryCount(consalFileId);
            int countDmaEntry = dmaDetailRepository.getDmaEntryCount();
            int countKtpEntry = ktpDetailRepository.getDmaEntryCount();

            int countScreenData = dmaDetailRepository.getScreenData();

            //Extract Date
            String extract_date_sdn = sdnFileRepository.getFileById(sdnFileId).getCreated_date().toString();
            String extract_date_consolidate = sdnFileRepository.getFileById(consalFileId).getCreated_date().toString();

//            logger.info("matching on process");

            SummaryMatching summaryMatching = new SummaryMatching();
            summaryMatching.setSdnfile_id_sdn(sdnFileId);
            summaryMatching.setSdnfile_id_consolidate(consalFileId);
            summaryMatching.setDma_file_id(dmaFileId);
            summaryMatching.setKtp_file_id(ktpFileId);
            summaryMatching.setStatus("matching");
            summaryMatching.setSdn_data(countSdnEntry);
            summaryMatching.setConsolidate_data(countConsalEntry);
            summaryMatching.setKtp_data(countKtpEntry);
            summaryMatching.setDma_data(countDmaEntry);
            summaryMatching.setScreen_data(countScreenData);
            summaryMatching.setExtract_date_sdn(extract_date_sdn);
            summaryMatching.setExtract_date_consolidate(extract_date_consolidate);
            summaryMatching.setStart_matching(newInputDate);
            summaryMatching.setEnd_matching(newInputDate);
            summaryMatching.setCreated_by("SYSTEM-AUTO");
            summaryMatching.setUpdated_by("SYSTEM-AUTO");
            summaryMatching.setCreated_date(newInputDate);
            summaryMatching.setUpdated_date(newInputDate);
            summaryMatchingRepository.save(summaryMatching);

            summaryMatchingDetailRepository.deleteAll();

            summaryMatchingDetailRepository.matchingPositive();
            summaryMatchingDetailRepository.matchingPotential();

            List<Integer> ktpDetailIdPotential = summaryMatchingDetailRepository.getDistinctMatchingDetailByStatus("potential");
            List<Integer> ktpDetailIdPositive = summaryMatchingDetailRepository.getDistinctMatchingDetailByStatus("positive");
            int sdnMatchPositive = 0;
            int sdnMatchPotential = 0;
            int consolidateMatchPositive = 0;
            int consolidateMatchPotential = 0;

            for (int i = 0; i < ktpDetailIdPositive.size(); i++) {
                SummaryMatchingDetail summaryMatchingDetail = summaryMatchingDetailRepository.getSummaryDetailByKtpDetailId(ktpDetailIdPositive.get(i));
                SdnFile sdnFiles = sdnFileRepository.getSdnFileByEntryId(summaryMatchingDetail.getSdn_entry_id());
                if (sdnFiles.getFile_type().compareToIgnoreCase("sdn") == 0) {
                    sdnMatchPositive++;
                } else if (sdnFiles.getFile_type().compareToIgnoreCase("consal") == 0) {
                    consolidateMatchPositive++;
                }
            }

            for (int i = 0; i < ktpDetailIdPotential.size(); i++) {
                SummaryMatchingDetail summaryMatchingDetail = summaryMatchingDetailRepository.getSummaryDetailByKtpDetailId(ktpDetailIdPotential.get(i));
                SdnFile sdnFiles = sdnFileRepository.getSdnFileByEntryId(summaryMatchingDetail.getSdn_entry_id());
                if (sdnFiles.getFile_type().compareToIgnoreCase("sdn") == 0) {
                    sdnMatchPotential++;
                } else if (sdnFiles.getFile_type().compareToIgnoreCase("consal") == 0) {
                    consolidateMatchPotential++;
                }
            }

//
//            int summaryMatchingDetailPositive = summaryMatchingDetailRepository.getDistinctMatchingDetailByStatus("positive");
//            int summaryMatchingDetailPotential = summaryMatchingDetailRepository.getDistinctMatchingDetailByStatus("potential");
//
            summaryMatchingRepository.updateSummaryMatching(sdnMatchPositive, consolidateMatchPositive, sdnMatchPotential, consolidateMatchPotential, sdnFileId, consalFileId, dmaFileId, ktpFileId);
//
            //UPDATE STATUS FILES ON PROCESS
            sdnFileRepository.updateFileStatus(sdnFileId, "matched", " -matching done");
            sdnFileRepository.updateFileStatus(consalFileId, "matched", " -matching done");
            dmaFileRepository.updateFileStatus(dmaFileId, "matched", " -matching done");
            ktpFileRepository.updateFileStatus(ktpFileId, "matched", " -matching done");


            logger.info("matching done !!");
            createNotification("Matching process finished on ");
        }


    }


}
