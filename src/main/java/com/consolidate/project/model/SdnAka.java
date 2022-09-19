package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "sdn_aka", schema = "ofac")
public class SdnAka {
    @Id
    @Column(name = "sdn_aka_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sdn_aka_id;

    @NotNull
    @Column(name = "status")
    private String status;


    @NotNull
    @Column(name = "sdn_entry_id")
    private int sdnEntry_id;


    @NotNull
    @Column(name = "uid")
    private int uid;

    @NotNull
    @Column(name = "type")
    private String type;

    @NotNull
    @Column(name = "category")
    private String category;


    @NotNull
    @Column(name = "first_name")
    private String first_name;

    @NotNull
    @Column(name = "last_name")
    private String last_name;


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

    public int getSdn_aka_id() {
        return sdn_aka_id;
    }

    public void setSdn_aka_id(int sdn_aka_id) {
        this.sdn_aka_id = sdn_aka_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSdnEntry_id() {
        return sdnEntry_id;
    }

    public void setSdnEntry_id(int sdnEntry_id) {
        this.sdnEntry_id = sdnEntry_id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
