package com.consolidate.project.configuration;

import com.consolidate.project.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@ComponentScan(value = "com.consolidate.project")
public class LdapConfiguration {

    @Autowired
    DataService dataService;

    @Bean
    public LdapContextSource contextSource() throws Exception {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(dataService.getSystemParameter().get("ldapUrl"));
        contextSource.setBase(dataService.getSystemParameter().get("ldapBase"));
        contextSource.setUserDn(dataService.getSystemParameter().get("ldapUserDn"));
        contextSource.setPassword(dataService.getSystemParameter().get("ldapPassword"));
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() throws Exception {
        return new LdapTemplate(contextSource());
    }

}