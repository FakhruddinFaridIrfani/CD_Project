package com.consolidate.project.service;

import com.consolidate.project.model.*;
import com.consolidate.project.repository.*;
import com.jcraft.jsch.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.IOUtils;


@Service
public class CdService {

    Logger logger = LoggerFactory.getLogger(CdService.class);

    @Autowired
    SdnFileRepository sdnFileRepository;

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

    @Value("${file.path.sdn}")
    private String filePathSdn;

    @Value("${file.path.consolidate}")
    private String filePathConsolidate;

    @Value("${file.path.report")
    private String filePathReport;

    @Value("${sftp.user.name}")
    private String sftpUser;

    @Value("${sftp.user.password}")
    private String sftpPassword;

    @Value("${sftp.url}")
    private String sftpUrl;


    //SDN SECTION
    public BaseResponse<String> uploadSdnFile(String input) throws Exception {
        BaseResponse response = new BaseResponse();
        Session session = null;
        ChannelSftp channel = null;
        UUID uuid = UUID.randomUUID();
        SdnFile sdnFile = new SdnFile();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            JSONObject jsonInput = new JSONObject(input);
            String fileName = jsonInput.optString("file_name");
            String fileData = jsonInput.optString("file_data");
            String file_type = jsonInput.optString("file_type");
            String fileUploader = jsonInput.optString("file_uploader");

            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            String savedFileName = "";
            //SFTP
            logger.info("Saving " + fileName + "to SFTP . . .  . ");
            BaseResponse<Map<String, String>> fileUploadProcess = addFile(fileName, fileData, "sdn");
            if (fileUploadProcess.isSuccess() == true) {
                savedFileName = fileUploadProcess.getData().get("file");
                logger.info("saved");
            } else {
                response.setStatus("0");
                response.setSuccess(false);
                response.setMessage(fileUploadProcess.getMessage());
                logger.info("failed");
                return response;
            }

            Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            logger.info("Saving File " + fileName + "to DB . . .  . ");
            sdnFile.setFile_name_ori(fileName);
            sdnFile.setFile_name_save(savedFileName);
            sdnFile.setFile_type(file_type);
            sdnFile.setFile_comparison_group("default");
            sdnFile.setStatus("active");
            sdnFile.setCreated_by(fileUploader);
            sdnFile.setUpdated_by(fileUploader);
            sdnFile.setCreated_date(newInputDate);
            sdnFile.setUpdated_date(newInputDate);
            sdnFileRepository.save(sdnFile);
            logger.info("saved");

            int currentSdnFileId = sdnFileRepository.getSdnFileBySavedFileName(savedFileName).getSdnfile_id();
            logger.info("currentSdnFileId :  " + currentSdnFileId);

            //Parsing XML
            byte[] b = Base64.getMimeDecoder().decode(getFile(savedFileName, "sdn").getData().get("file_base64").toString());
            InputStream inputStream = new ByteArrayInputStream(b);
            dbf.setNamespaceAware(false);
            Document doc = dbf.newDocumentBuilder().parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList sdnEntryList = doc.getElementsByTagName("sdnEntry");
            logger.info("sdnEntryList count : " + sdnEntryList.getLength());
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
                    sdnEntry.setCreated_by(fileUploader);
                    sdnEntry.setUpdated_by(fileUploader);
                    sdnEntry.setCreated_date(newInputDate);
                    sdnEntry.setUpdated_date(newInputDate);
                    sdnEntryRepository.save(sdnEntry);
                    int insertedSdnEntryId = sdnEntryRepository.getSdnEntryBySavedUID(Integer.valueOf(sdnEntryUid)).getSdnEntry_id();
                    logger.info("insertedSdnEntryId : " + insertedSdnEntryId);
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
                                sdnProgram.setCreated_by(fileUploader);
                                sdnProgram.setUpdated_by(fileUploader);
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
                                    sdnAka.setCreated_by(fileUploader);
                                    sdnAka.setUpdated_by(fileUploader);
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
                                    sdnAddress.setState_province(stateOrProvince);
                                    sdnAddress.setCity(cityAddress);
                                    sdnAddress.setCountry(countryAddress);
                                    sdnAddress.setAddress_1(addressAddress1);
                                    sdnAddress.setAddress_2(addressAddress2);
                                    sdnAddress.setAddress_3(addressAddress3);
                                    sdnAddress.setPostal_code(postalCode);
                                    sdnAddress.setRemarks(remarksAddress);
                                    sdnAddress.setStatus("active");
                                    sdnAddress.setCreated_by(fileUploader);
                                    sdnAddress.setUpdated_by(fileUploader);
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
                                    sdnDOB.setCreated_by(fileUploader);
                                    sdnDOB.setUpdated_by(fileUploader);
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
                                    sdnPOB.setCreated_by(fileUploader);
                                    sdnPOB.setUpdated_by(fileUploader);
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
                                    sdnID.setCreated_by(fileUploader);
                                    sdnID.setUpdated_by(fileUploader);
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
                                    sdnCitizenship.setCreated_by(fileUploader);
                                    sdnCitizenship.setUpdated_by(fileUploader);
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
                                    sdnNationality.setCreated_by(fileUploader);
                                    sdnNationality.setUpdated_by(fileUploader);
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
            logger.info("Root Element :" + doc.getDocumentElement().getNodeName());
            inputStream.close();

            response.setData(savedFileName);
            response.setStatus("2000");
            response.setSuccess(true);
            response.setMessage("Sdn File uploaded");

        } catch (JSONException | ParseException e) {
            response.setStatus("0");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            logger.info("Exception :" + e.getMessage());
        } catch (Exception e) {
            response.setStatus("0");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            logger.info("Exception :" + e.getMessage());
        }

        return response;
    }

    public BaseResponse<Map<String, String>> addFile(String file_name, String file_content, String folder) throws Exception {
        BaseResponse response = new BaseResponse();
        Session session = null;
        ChannelSftp channel = null;
        Map<String, String> imageAddResult = new HashMap<>();
        try {
            UUID uuid = UUID.randomUUID();
            session = new JSch().getSession(sftpUser, sftpUrl, 22);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            String path = "";
            if (folder.compareToIgnoreCase("sdn") == 0) {
                path = filePathSdn;
            } else if (folder.compareToIgnoreCase("consolidate") == 0) {
                path = filePathConsolidate;
            } else {
                response.setStatus("0");
                response.setSuccess(false);
                response.setMessage("unknown folder");
                return response;
            }
            byte[] b = Base64.getMimeDecoder().decode(file_content);
            InputStream stream = new ByteArrayInputStream(b);
            channel.put(stream, path + uuid + "_" + file_name, 0);

            imageAddResult.put("file", uuid + "_" + file_name);

            response.setData(imageAddResult);
            response.setStatus("2000");
            response.setSuccess(true);
            response.setMessage("File successfully Added");
        } catch (Exception e) {
            response.setData(imageAddResult);
            response.setStatus("0");
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
            session = new JSch().getSession(sftpUser, sftpUrl, 22);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            String path = "";
            if (folder.compareToIgnoreCase("sdn") == 0) {
                path = filePathSdn;
            } else if (folder.compareToIgnoreCase("consolidate") == 0) {
                path = filePathConsolidate;
            } else {
                response.setStatus("0");
                response.setSuccess(false);
                response.setMessage("unknown folder");
                logger.info("unknown folder");
                return response;
            }

            InputStream inputStream = channel.get(path + file_name);
            logger.info("file path : " + path + file_name);
            byte[] bytes = IOUtils.toByteArray(inputStream);
            String base64 = Base64.getEncoder().encodeToString(bytes);

            result.put("file_base64", base64);
            result.put("file_bytes", bytes);


            response.setData(result);
            response.setStatus("2000");
            response.setSuccess(true);
            response.setMessage("Get File success");
        } catch (Exception e) {
            response.setStatus("0");
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

}
