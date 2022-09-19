package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "dma_detail", schema = "ofac")
public class DMADetail {
    @Id
    @Column(name = "dmadetail_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int dmadetail_id;

    @NotNull
    @Column(name = "dmafile_id")
    private int dmafile_id;

    @NotNull
    @Column(name = "status")
    private String status;

    @NotNull
    @Column(name = "merchant_no")
    private String merchant_no;


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

    public int getDmadetail_id() {
        return dmadetail_id;
    }

    public void setDmadetail_id(int dmadetail_id) {
        this.dmadetail_id = dmadetail_id;
    }

    public int getDmafile_id() {
        return dmafile_id;
    }

    public void setDmafile_id(int dmafile_id) {
        this.dmafile_id = dmafile_id;
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
