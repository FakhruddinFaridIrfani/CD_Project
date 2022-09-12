package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "summary_matching", schema = "cd")
public class SummaryMatching {
    @Id
    @Column(name = "summary_matching_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int summary_matching_id;


    @NotNull
    @Column(name = "status")
    private String status;

    @NotNull
    @Column(name = "sdnfile_id_sdn")
    int sdnfile_id_sdn;

    @NotNull
    @Column(name = "sdnfile_id_consolidate")
    int sdnfile_id_consolidate;

    @NotNull
    @Column(name = "ktpfile_id")
    int ktpfile_id;


    @NotNull
    @Column(name = "dmafile_id")
    int dmafile_id;

    @Column(name = "sdn_data")
    int sdn_data;

    @Column(name = "consolidate_data")
    int consolidate_data;

    @Column(name = "ktp_data")
    int ktp_data;

    @Column(name = "dma_data")
    int dma_data;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Column(name = "start_matching")
    private Date start_matching;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Column(name = "end_matching")
    private Date end_matching;


    @Column(name = "count_positive")
    int count_positive;

    @Column(name = "count_potential")
    int count_potential;


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

    public int getSummary_matching_id() {
        return summary_matching_id;
    }

    public void setSummary_matching_id(int summary_matching_id) {
        this.summary_matching_id = summary_matching_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSdnfile_id_sdn() {
        return sdnfile_id_sdn;
    }

    public void setSdnfile_id_sdn(int sdnfile_id_sdn) {
        this.sdnfile_id_sdn = sdnfile_id_sdn;
    }

    public int getSdnfile_id_consolidate() {
        return sdnfile_id_consolidate;
    }

    public void setSdnfile_id_consolidate(int sdnfile_id_consolidate) {
        this.sdnfile_id_consolidate = sdnfile_id_consolidate;
    }

    public int getKtpfile_id() {
        return ktpfile_id;
    }

    public void setKtpfile_id(int ktpfile_id) {
        this.ktpfile_id = ktpfile_id;
    }

    public int getDmafile_id() {
        return dmafile_id;
    }

    public void setDmafile_id(int dmafile_id) {
        this.dmafile_id = dmafile_id;
    }

    public int getSdn_data() {
        return sdn_data;
    }

    public void setSdn_data(int sdn_data) {
        this.sdn_data = sdn_data;
    }

    public int getConsolidate_data() {
        return consolidate_data;
    }

    public void setConsolidate_data(int consolidate_data) {
        this.consolidate_data = consolidate_data;
    }

    public int getKtp_data() {
        return ktp_data;
    }

    public void setKtp_data(int ktp_data) {
        this.ktp_data = ktp_data;
    }

    public int getDma_data() {
        return dma_data;
    }

    public void setDma_data(int dma_data) {
        this.dma_data = dma_data;
    }

    public Date getStart_matching() {
        return start_matching;
    }

    public void setStart_matching(Date start_matching) {
        this.start_matching = start_matching;
    }

    public Date getEnd_matching() {
        return end_matching;
    }

    public void setEnd_matching(Date end_matching) {
        this.end_matching = end_matching;
    }

    public int getCount_positive() {
        return count_positive;
    }

    public void setCount_positive(int count_positive) {
        this.count_positive = count_positive;
    }

    public int getCount_potential() {
        return count_potential;
    }

    public void setCount_potential(int count_potential) {
        this.count_potential = count_potential;
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
