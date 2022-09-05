package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "sdn_logger", schema = "cd")
public class SdnLogger {
    @Id
    @Column(name = "sdn_logger_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sdn_logger_id;

    @NotNull
    @Column(name = "service_name")
    private String service_name;

    @NotNull
    @Column(name = "service_body")
    private String service_body;

    @NotNull
    @Column(name = "created_by")
    private String created_by;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Column(name = "created_date")
    private Date created_date;

    public int getSdn_logger_id() {
        return sdn_logger_id;
    }

    public void setSdn_logger_id(int sdn_logger_id) {
        this.sdn_logger_id = sdn_logger_id;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getService_body() {
        return service_body;
    }

    public void setService_body(String service_body) {
        this.service_body = service_body;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Date created_date) {
        this.created_date = created_date;
    }
}
