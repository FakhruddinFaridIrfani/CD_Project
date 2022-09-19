package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "sdn_entry", schema = "ofac")
public class SdnEntry {
    @Id
    @Column(name = "sdn_entry_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sdnEntry_id;

    @NotNull
    @Column(name = "status")
    private String status;

    @NotNull
    @Column(name = "sdnfile_id")
    private int sdnfile_id;

    @NotNull
    @Column(name = "uid")
    private int uid;


    @NotNull
    @Column(name = "first_name")
    private String first_name;

    @NotNull
    @Column(name = "last_name")
    private String last_name;

    @NotNull
    @Column(name = "sdn_type")
    private String sdn_type;

    @NotNull
    @Column(name = "tittle")
    private String tittle;


    @NotNull
    @Column(name = "remarks")
    private String remarks;

    @NotNull
    @Column(name = "created_by")
    private String created_by;


    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Column(name = "created_date")
    private Date created_date;


    @Column(name = "updated_by")
    private String updated_by;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Column(name = "updated_date")
    private Date updated_date;

    public int getSdnEntry_id() {
        return sdnEntry_id;
    }

    public void setSdnEntry_id(int sdnEntry_id) {
        this.sdnEntry_id = sdnEntry_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSdnfile_id() {
        return sdnfile_id;
    }

    public void setSdnfile_id(int sdnfile_id) {
        this.sdnfile_id = sdnfile_id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getSdn_type() {
        return sdn_type;
    }

    public void setSdn_type(String sdn_type) {
        this.sdn_type = sdn_type;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    public String getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(String updated_by) {
        this.updated_by = updated_by;
    }

    public Date getUpdated_date() {
        return updated_date;
    }

    public void setUpdated_date(Date updated_date) {
        this.updated_date = updated_date;
    }
}
