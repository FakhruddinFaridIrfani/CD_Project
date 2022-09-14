package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "report", schema = "cd")
public class Report {
    @Id
    @Column(name = "report_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int report_id;


    @NotNull
    @Column(name = "status")
    private String status;

    @Column(name = "extract_date")
    String extract_date;

    @Column(name = "ofac_list_screened")
    String ofac_list_screened;

    @Column(name = "start_date")
    String start_date;

    @Column(name = "end_date")
    String end_date;

    @Column(name = "positive")
    int positive;

    @Column(name = "potential")
    int potential;

    @Column(name = "total_screened")
    int total_screened;

    @Column(name = "total_data")
    int total_data;


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

    public int getReport_id() {
        return report_id;
    }

    public void setReport_id(int report_id) {
        this.report_id = report_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExtract_date() {
        return extract_date;
    }

    public void setExtract_date(String extract_date) {
        this.extract_date = extract_date;
    }

    public String getOfac_list_screened() {
        return ofac_list_screened;
    }

    public void setOfac_list_screened(String ofac_list_screened) {
        this.ofac_list_screened = ofac_list_screened;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public int getPositive() {
        return positive;
    }

    public void setPositive(int positive) {
        this.positive = positive;
    }

    public int getPotential() {
        return potential;
    }

    public void setPotential(int potential) {
        this.potential = potential;
    }

    public int getTotal_screened() {
        return total_screened;
    }

    public void setTotal_screened(int total_screened) {
        this.total_screened = total_screened;
    }

    public int getTotal_data() {
        return total_data;
    }

    public void setTotal_data(int total_data) {
        this.total_data = total_data;
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
