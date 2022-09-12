package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "summary_matching_detail", schema = "cd")
public class SummaryMatchingDetail {
    @Id
    @Column(name = "summary_matching_detail_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int summary_matching_detail_id;

    @NotNull
    @Column(name = "status")
    private String status;

    @NotNull
    @Column(name = "summary_matching_id")
    private int summary_matching_id;

    @NotNull
    @Column(name = "ktp_detail_id")
    private int ktp_detail_id;

    @NotNull
    @Column(name = "sdn_entry_id")
    private int sdn_entry_id;


    @NotNull
    @Column(name = "matching_status")
    private String matching_status;


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

    public int getSummary_matching_detail_id() {
        return summary_matching_detail_id;
    }

    public void setSummary_matching_detail_id(int summary_matching_detail_id) {
        this.summary_matching_detail_id = summary_matching_detail_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSummary_matching_id() {
        return summary_matching_id;
    }

    public void setSummary_matching_id(int summary_matching_id) {
        this.summary_matching_id = summary_matching_id;
    }

    public int getKtp_detail_id() {
        return ktp_detail_id;
    }

    public void setKtp_detail_id(int ktp_detail_id) {
        this.ktp_detail_id = ktp_detail_id;
    }

    public int getSdn_entry_id() {
        return sdn_entry_id;
    }

    public void setSdn_entry_id(int sdn_entry_id) {
        this.sdn_entry_id = sdn_entry_id;
    }

    public String getMatching_status() {
        return matching_status;
    }

    public void setMatching_status(String matching_status) {
        this.matching_status = matching_status;
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
