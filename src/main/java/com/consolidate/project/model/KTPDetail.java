package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "ktp_detail", schema = "cd")
public class KTPDetail {
    @Id
    @Column(name = "ktp_detail_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ktp_detail_id;

    @NotNull
    @Column(name = "ktp_file_id")
    private int ktp_file_id;

    @NotNull
    @Column(name = "status")
    private String status;


    @Column(name = "merchant_no")
    private String merchant_no;


    @Column(name = "merchant_name")
    private String merchant_name;


    @Column(name = "name_1")
    private String name_1;


    @Column(name = "name_2")
    private String name_2;

    @Column(name = "dob_1")
    private String dob_1;


    @Column(name = "dob_2")
    private String dob_2;

    @Column(name = "ktp_1")
    private String ktp_1;


    @Column(name = "ktp_2")
    private String ktp_2;


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


    public int getKtp_detail_id() {
        return ktp_detail_id;
    }

    public void setKtp_detail_id(int ktp_detail_id) {
        this.ktp_detail_id = ktp_detail_id;
    }

    public int getKtp_file_id() {
        return ktp_file_id;
    }

    public void setKtp_file_id(int ktp_file_id) {
        this.ktp_file_id = ktp_file_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMerchant_no() {
        return merchant_no;
    }

    public void setMerchant_no(String merchant_no) {
        this.merchant_no = merchant_no;
    }

    public String getMerchant_name() {
        return merchant_name;
    }

    public void setMerchant_name(String merchant_name) {
        this.merchant_name = merchant_name;
    }

    public String getName_1() {
        return name_1;
    }

    public void setName_1(String name_1) {
        this.name_1 = name_1;
    }

    public String getName_2() {
        return name_2;
    }

    public void setName_2(String name_2) {
        this.name_2 = name_2;
    }

    public String getDob_1() {
        return dob_1;
    }

    public void setDob_1(String dob_1) {
        this.dob_1 = dob_1;
    }

    public String getDob_2() {
        return dob_2;
    }

    public void setDob_2(String dob_2) {
        this.dob_2 = dob_2;
    }

    public String getKtp_1() {
        return ktp_1;
    }

    public void setKtp_1(String ktp_1) {
        this.ktp_1 = ktp_1;
    }

    public String getKtp_2() {
        return ktp_2;
    }

    public void setKtp_2(String ktp_2) {
        this.ktp_2 = ktp_2;
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
