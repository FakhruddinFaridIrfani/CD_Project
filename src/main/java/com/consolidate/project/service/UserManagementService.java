package com.consolidate.project.service;

import com.consolidate.project.model.*;
import com.consolidate.project.repository.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;


@Service
public class UserManagementService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SdnLoggerRepository sdnLoggerRepository;

    @Autowired
    SystemParameterRepository systemParameterRepository;

    @Autowired
    PrivilegeRepository privilegeRepository;

    @Autowired
    DataService dataService;

    @Autowired
    private LdapTemplate ldapTemplate;

    Logger logger = LoggerFactory.getLogger(UserManagementService.class);

    //User Section
    public BaseResponse<String> addUsers(String input) throws Exception {
        BaseResponse response = new BaseResponse();
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
            createLog(input, userOnProcess, "addUser");
            String user_name = jsonInput.optString("user_name");
            String user_full_name = jsonInput.optString("user_full_name");
            String user_password = jsonInput.optString("user_password");
            int role_id = jsonInput.optInt("role_id");
            String user_organization = jsonInput.optString("user_organization");
            String user_token = Base64.getEncoder().encodeToString((user_name + Long.toHexString(new Date().getTime()) + RandomStringUtils.random(10, true, true)).getBytes());
            //user_name check
            List<Users> userNameCheckResult = userRepository.getUsersByName(user_name);
            if (userNameCheckResult.size() > 0) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("User name already exist / used");
                return response;
            }
            userRepository.save(user_name, user_password, role_id, user_organization, user_full_name, userOnProcess, user_token);

            response.setData(new ArrayList<>());
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("User successfully Added");

        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    public BaseResponse<List<Map<String, Object>>> getUsers(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse<>();
        List<Map<String, Object>> result = new ArrayList<>();
        JSONObject jsonInput;
        String user_name;
        String user_full_name;
        String status;
        String user_organization;
        String role_id;
        try {
            jsonInput = new JSONObject(input);
            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));
            //Token Auth
            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("401");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(input, userOnProcess, "getUser");
            user_name = "%" + jsonInput.optString("user_name") + "%";
            user_organization = "%" + jsonInput.optString("user_organization") + "%";
            user_full_name = "%" + jsonInput.optString("user_full_name") + "%";
            status = jsonInput.optString("status");
            if (status.isEmpty()) {
                status = "%%";
            }
            role_id = jsonInput.optInt("role_id") + "";
            if (role_id.compareToIgnoreCase("null") == 0 || role_id.compareToIgnoreCase("0") == 0) {
                role_id = "%%";
            }
            List<Users> getUserResult = userRepository.getUsersList(user_name, user_organization, role_id, status, user_full_name);
            for (int i = 0; i < getUserResult.size(); i++) {
                Map resultMap = new HashMap();
                List<Role> roles = roleRepository.getRoleById(getUserResult.get(i).getRole_id());
                resultMap.put("User", getUserResult.get(i));
                resultMap.put("Role", roles.get(0));
                result.add(resultMap);
            }
            for (int i = 0; i < getUserResult.size(); i++) {
                getUserResult.get(i).setUser_password("null");
            }
            response.setData(result);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("User Listed");
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public BaseResponse<Users> updateUsers(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();
        String user_full_name;
        String status;
        String user_organization;
        int role_id;
        int user_id;
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
            createLog(input, userOnProcess, "updateUser");

            role_id = jsonInput.optInt("role_id");
            user_organization = jsonInput.optString("user_organization");
            user_full_name = jsonInput.optString("user_full_name");
            status = jsonInput.optString("status");
            user_id = jsonInput.optInt("user_id");

            userRepository.updateUser(role_id, user_organization, status, user_full_name, userOnProcess, user_id);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("User successfully Updated");


        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }


        return response;
    }

    public BaseResponse<Users> deleteUsers(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();
        int user_id;
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
            createLog(input, userOnProcess, "deleteUser");
            user_id = jsonInput.optInt("user_id");
            userRepository.deleteUser(user_id, userOnProcess);

            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("User successfully deleted");
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public BaseResponse<Map<String, Object>> loginUser(String user) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();
        Map<String, Object> result = new HashMap<>();
        List<Users> dataLoginUser;

        try {

            //user status check
            List<Users> userNameCheckResult = userRepository.getUsersByName(user);
            if (userNameCheckResult.size() > 0) {
                if (userNameCheckResult.get(0).getStatus().compareToIgnoreCase("active") != 0) {
                    response.setStatus("404");
                    response.setSuccess(false);
                    response.setMessage("Failed to login. User no longer exist");
                    return response;
                }
            }
            dataLoginUser = userRepository.loginUser(user);
            if (dataLoginUser.size() == 0) {
                response.setStatus("401");
                response.setSuccess(false);
                response.setMessage("User not found in ofac application. contact administrator.");
                return response;
            }

            for (int i = 0; i < dataLoginUser.size(); i++) {
                List<Role> roles = roleRepository.getRoleById(dataLoginUser.get(i).getRole_id());
                List<Privilege> privileges = privilegeRepository.getPrivilegeByRoleId(roles.get(0).getRole_id());
                List<String> menuNames = new ArrayList();
                String menu_name = privileges.get(0).getMenu_name();
                menu_name = menu_name.replace("[", "").replace("]", "");
                String[] menuArray = menu_name.split(",");
                menuNames = Arrays.asList(menuArray);
                result.put("User", dataLoginUser.get(i));
                result.put("Role", roles.get(0));
                result.put("Privilege", menuNames);
            }
            for (int i = 0; i < dataLoginUser.size(); i++) {
                dataLoginUser.get(i).setUser_password("null");
            }
            response.setData(result);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Login Success !!");
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public BaseResponse<Map<String, Object>> loginLDAP2(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();
        Map<String, Object> result = new HashMap<>();
        String message = "Line : ";
        List<Users> dataLoginUser;
        String user_name;
        String user_password;
        String ldapServer;
        String ldapBaseDN;
        try {
            JSONObject jsonInput = new JSONObject(input);
            ldapServer = jsonInput.optString("ldapServer");
            ldapBaseDN = jsonInput.optString("ldapBaseDN");
            user_name = jsonInput.optString("user_name");
            user_password = jsonInput.optString("user_password");
            String ldapPrincipal = ldapBaseDN.replace("user_name", user_name);

            Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.PROVIDER_URL, ldapServer);
            if (ldapPrincipal != null) {
                message = "line : 342";
                env.put(Context.SECURITY_PRINCIPAL, ldapPrincipal);
            }
            if (user_password != null) {
                message = "line : 345";
                env.put(Context.SECURITY_CREDENTIALS, user_password);
            }

            message = "line : 350";
            InitialDirContext ctx = new InitialDirContext(env);
            if (ctx != null) {
                logger.info("Auth :  true");
                message = "line : 354";
                result.put("Admin / Base Auth", true);

            } else {
                logger.info("Auth :  false");
                message = "line : 359";
                result.put("Admin / Base Auth", false);
            }

            response.setData(result);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage(message + " - " + "Login LDAP");
        } catch (NamingException e) {
            result.put("Auth", false);
            response.setData(result);
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(message + " - " + e.getMessage());
        } catch (Exception e) {
            result.put("Auth", false);
            response.setData(result);
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(message + " - " + e.getMessage());
        }
        return response;
    }

    public BaseResponse<Map<String, Object>> loginLDAP(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();
        BaseResponse responseLogin = new BaseResponse();
        Map<String, Object> result = new HashMap<>();
        String message = "Line : ";
        String user_name;
        String user_password;
        String ldapServer;
        String ldapBase;
        Map<String, String> systemParameter = parseSystemParameter();
        try {
            JSONObject jsonInput = new JSONObject(input);
            ldapServer = systemParameter.get("ldapUrl");
            ldapBase = systemParameter.get("ldapBase");
            user_name = jsonInput.getString("user_name");
            user_password = jsonInput.optString("user_password");

            String ldapPrincipal = ldapBase.replace("username", user_name);

            Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.PROVIDER_URL, ldapServer);
            if (ldapPrincipal != null) {
                message = "line : 359";
                env.put(Context.SECURITY_PRINCIPAL, ldapPrincipal);
            }
            if (user_password != null) {
                message = "line : 363";
                env.put(Context.SECURITY_CREDENTIALS, user_password);
            } else {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("user password can't be empty string");
                return response;
            }

            message = "line : 372";
            InitialDirContext ctx = new InitialDirContext(env);
            if (ctx != null) {
                logger.info("Auth :  true");
                result.put("Auth", true);
                responseLogin = loginUser(user_name);
                response = responseLogin;

            } else {
                logger.info("Auth :  false");
                message = "line : 382";
                result.put("Auth", false);
                response.setData(result);
                response.setStatus("401");
                response.setSuccess(false);
            }


        } catch (NamingException e) {
            result.put("Auth", false);
            response.setData(result);
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(message + " - " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("invalid credentials")) {
                response.setStatus("401");
                response.setMessage("wrong username - password combination");
            }
//            if (e.getMessage().toLowerCase().contains("expired")) {
//                responseLogin = loginUser(new JSONObject(input).getString("user_name"));
//                response = responseLogin;
//            }

        } catch (Exception e) {
            result.put("Auth", false);
            response.setData(result);
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(message + " - " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("invalid credentials")) {
                response.setStatus("401");
                response.setMessage("wrong username - password combination");
            }
//            if (e.getMessage().toLowerCase().contains("expired")) {
//                responseLogin = loginUser(new JSONObject(input).getString("user_name"));
//                response = responseLogin;
//            }
        }
        return response;
    }
//    public BaseResponse<Map<String, Object>> loginUser2(String input) throws Exception, SQLException {
//        BaseResponse response = new BaseResponse();
//        Map<String, Object> result = new HashMap<>();
//        List<Users> dataLoginUser;
//        String user_name;
//        String user_password;
//        try {
//            JSONObject jsonInput = new JSONObject(input);
//            user_name = jsonInput.optString("user_name");
//            user_password = jsonInput.optString("user_password");
//            //user status check
//            List<Users> userNameCheckResult = userRepository.getUsersByName(user_name);
//            if (userNameCheckResult.size() > 0) {
//                if (userNameCheckResult.get(0).getStatus().compareToIgnoreCase("active") != 0) {
//                    response.setStatus("404");
//                    response.setSuccess(false);
//                    response.setMessage("Failed to login. User no longer exist");
//                    return response;
//                }
//            }
//            dataLoginUser = userRepository.loginUser(user_name, user_password);
//            String user_token;
//            if (dataLoginUser.size() == 0) {
//                response.setStatus("401");
//                response.setSuccess(false);
//                response.setMessage("Failed to login. wrong User Name, Email, or Password");
//                return response;
//            } else {
//                user_token = addNewUserToken(user_name, user_password);
//            }
//
//            for (int i = 0; i < dataLoginUser.size(); i++) {
//                List<Role> roles = roleRepository.getRoleById(dataLoginUser.get(i).getRole_id());
//                result.put("User", dataLoginUser.get(i));
//                result.put("Role", roles.get(0));
//            }
//            for (int i = 0; i < dataLoginUser.size(); i++) {
//                dataLoginUser.get(i).setUser_password("null");
//                dataLoginUser.get(i).setUser_token(user_token);
//            }
//            response.setData(result);
//            response.setStatus("200");
//            response.setSuccess(true);
//            response.setMessage("Login Success !!");
//        } catch (Exception e) {
//            response.setStatus("500");
//            response.setSuccess(false);
//            response.setMessage(e.getMessage());
//        }
//        return response;
//    }

    public BaseResponse<String> changeUsersPassword(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();
        List<Users> dataLoginUser;
        String user_name;
        String user_password;
        String new_user_password;
        if (1 < 2) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage("Can't change password here, contact your system administrator");
            return response;
        }
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
            createLog(input, userOnProcess, "changeUserPassword");
            user_name = jsonInput.optString("user_name");
            user_password = jsonInput.optString("user_password");
            new_user_password = jsonInput.optString("new_user_password");
            dataLoginUser = userRepository.loginUser(user_name);
            if (dataLoginUser.size() == 0) {
                response.setStatus("401");
                response.setSuccess(false);
                response.setMessage("Wrong current password");
                return response;
            }
            int user_id = dataLoginUser.get(0).getUser_id();
            userRepository.changeUsersPassword(user_id, new_user_password);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Password Changed !!");
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

//    public String addNewUserToken(String user_name, String user_password) {
//        String userToken = Base64.getEncoder().encodeToString((user_name + Long.toHexString(new Date().getTime()) + RandomStringUtils.random(10, true, true)).getBytes());
//        userRepository.updateUserToken(user_name, user_password, userToken);
//        return userToken;
//    }

    //Role Section
    public BaseResponse<String> addRole(String input) throws Exception {
        BaseResponse response = new BaseResponse();
        String role_name;
        Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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
            createLog(input, userOnProcess, "addRole");
            role_name = jsonInput.optString("role_name");
            //Existing Role Name check
            List<Role> roleNameCheckResult = roleRepository.getRoleByName(role_name);
            if (roleNameCheckResult.size() > 0) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("Role name already exist / used");
                return response;
            }
            Role roles = new Role();
            roles.setRole_name(role_name);
            roles.setStatus("active");
            roles.setCreated_by(userOnProcess);
            roles.setUpdated_by(userOnProcess);
            roles.setCreated_date(newInputDate);
            roles.setUpdated_date(newInputDate);
            roleRepository.save(roles);

            //SET DEFAULT PRIVILEGE TO INSERTED ROLE
            Role roleInserted = roleRepository.getRoleByName(role_name).get(0);
            int roleIdInserted = roleInserted.getRole_id();
            privilegeRepository.insertGeneralMenuName(roleIdInserted);

            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Role successfully Added");

        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }


        return response;
    }

    public BaseResponse<List<Role>> getRole(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse<>();
        JSONObject jsonInput;
        String role_name;
        try {
            jsonInput = new JSONObject(input);
            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));

            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("0");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(input, userOnProcess, "getRole");
            role_name = "%" + jsonInput.optString("role_name") + "%";
            List<Role> getRoleResult = roleRepository.getRole(role_name);

            response.setData(getRoleResult);
            response.setStatus("2000");
            response.setSuccess(true);
            response.setMessage("Role Listed");
        } catch (Exception e) {
            response.setStatus("0");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public BaseResponse<Role> updateRole(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();
        String role_name;
        int role_id;
        String status;
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
            createLog(input, userOnProcess, "updateRole");

            role_name = jsonInput.optString("role_name");
            role_id = jsonInput.optInt("role_id");
            status = jsonInput.optString("status");
            //Existing Role Name check
            List<Role> roleNameCheckResult = roleRepository.getRoleByNameExceptId(role_name, role_id);
            if (roleNameCheckResult.size() > 0) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("Role name already exist / used");
                return response;
            }
            roleRepository.updateRole(role_name, status, userOnProcess, role_id);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Role successfully Updated");
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public BaseResponse<Role> deleteRole(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();

        try {
            JSONObject jsonInput = new JSONObject(input);
            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));

            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("0");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(input, userOnProcess, "deleteRole");
            List<Users> usedRoleOnUser = userRepository.getUserByRoleId(jsonInput.optInt("role_id"));
            if (usedRoleOnUser.size() > 0) {
                response.setStatus("0");
                response.setSuccess(false);
                response.setMessage("The role still used by " + usedRoleOnUser.size() + " user(s)");
                return response;
            }

            roleRepository.deleteRole(jsonInput.optInt("role_id"), userOnProcess);
            response.setStatus("2000");
            response.setSuccess(true);
            response.setMessage("Role successfully deleted");
        } catch (Exception e) {
            response.setStatus("0");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    //SYSTEM PARAMETER
    public BaseResponse<String> addSystemParameter(String input) throws Exception {
        BaseResponse response = new BaseResponse();
        String parameter_name;
        String parameter_value;
        Date newInputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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
            createLog(input, userOnProcess, "addSystemParameter");
            parameter_name = jsonInput.optString("parameter_name");
            parameter_value = jsonInput.optString("parameter_value");
            //Existing Role Name check
            List<SystemParameter> parameterNameCheck = systemParameterRepository.getSystemParameterByName(parameter_name);
            if (parameterNameCheck.size() > 0) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("Parameter name already exist / used");
                return response;
            }
            SystemParameter systemParameter = new SystemParameter();
            systemParameter.setParameter_name(parameter_name);
            systemParameter.setParameter_value(parameter_value);
            systemParameter.setStatus("active");
            systemParameter.setCreated_by(userOnProcess);
            systemParameter.setUpdated_by(userOnProcess);
            systemParameter.setCreated_date(newInputDate);
            systemParameter.setUpdated_date(newInputDate);
            systemParameterRepository.save(systemParameter);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("System parameter successfully Added");

        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }


        return response;
    }

    public BaseResponse<List<SystemParameter>> getSystemParameter(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse<>();
        JSONObject jsonInput;
        try {
            jsonInput = new JSONObject(input);
            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));

            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("0");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(input, userOnProcess, "getSystemParameter");
            List<SystemParameter> getSystemParameterResult = systemParameterRepository.getSystemParameter();

            response.setData(getSystemParameterResult);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("System Parameter Listed");
        } catch (Exception e) {
            response.setStatus("0");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public BaseResponse<SystemParameter> updateSystemParameter(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();
        String parameter_name;
        String parameter_value;
        String status;
        int systemparameter_id;
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
            createLog(input, userOnProcess, "updateSystemParameter");

            parameter_name = jsonInput.optString("parameter_name");
            parameter_value = jsonInput.optString("parameter_value");
            status = jsonInput.optString("status");
            systemparameter_id = jsonInput.optInt("systemparameter_id");
            //Existing parameter name check
            List<SystemParameter> parameterNameCheck = systemParameterRepository.getParameterByNameExceptId(parameter_name, systemparameter_id);
            if (parameterNameCheck.size() > 0) {
                response.setStatus("500");
                response.setSuccess(false);
                response.setMessage("Parameter name already exist / used");
                return response;
            }
            systemParameterRepository.updateSystemParameter(parameter_value, userOnProcess, status, systemparameter_id);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("System parameter successfully Updated");
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public BaseResponse<SystemParameter> deleteSystemParameter(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();

        try {
            JSONObject jsonInput = new JSONObject(input);
            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));

            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("0");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(input, userOnProcess, "deleteSystemParameter");

            systemParameterRepository.deleteSystemParameter(jsonInput.optInt("systemparameter_id"), userOnProcess);
            response.setStatus("2000");
            response.setSuccess(true);
            response.setMessage("System parameter successfully deleted");
        } catch (Exception e) {
            response.setStatus("0");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }


    //PRIVILEGE
    public BaseResponse<List<Map<String, Object>>> getAllPrivilege(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse<>();
        List result = new ArrayList();
        JSONObject jsonInput;
        try {
            jsonInput = new JSONObject(input);
            Map<String, Object> auth = tokenAuthentication(jsonInput.optString("user_token"));

            if (Boolean.valueOf(auth.get("valid").toString()) == false) {
                response.setStatus("0");
                response.setSuccess(false);
                response.setMessage("Token Authentication Failed");
                return response;
            }
            String userOnProcess = auth.get("user_name").toString();
            createLog(input, userOnProcess, "getAllPrivilege");
            List<Role> getAllRole = roleRepository.getRole("%%");

            for (int i = 0; i < getAllRole.size(); i++) {
                Map<String, Object> resultMap = new HashMap<>();
                List<Privilege> privileges = privilegeRepository.getPrivilegeByRoleId(getAllRole.get(i).getRole_id());
                List<String> menuNames = new ArrayList();
                String menu_name = privileges.get(0).getMenu_name();
                menu_name = menu_name.replace("[", "").replace("]", "");
                String[] menuArray = menu_name.split(",");
                menuNames = Arrays.asList(menuArray);
                resultMap.put("Role", getAllRole.get(i));
                resultMap.put("Privilege", menuNames);
                result.add(resultMap);
            }

            response.setData(result);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Privilege Listed");
        } catch (Exception e) {
            response.setStatus("0");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public BaseResponse<Privilege> updatePrivilege(String input) throws Exception, SQLException {
        BaseResponse response = new BaseResponse();
        String menu_name;
        int role_id;
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
            createLog(input, userOnProcess, "updatePrivilege");

            menu_name = jsonInput.optString("menu_name");
            role_id = jsonInput.optInt("role_id");

            privilegeRepository.updatePrivilegeMenuName(menu_name, userOnProcess, role_id);
            response.setStatus("200");
            response.setSuccess(true);
            response.setMessage("Previlege successfully Updated");
        } catch (Exception e) {
            response.setStatus("500");
            response.setSuccess(false);
            response.setMessage(e.getMessage());
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

}
