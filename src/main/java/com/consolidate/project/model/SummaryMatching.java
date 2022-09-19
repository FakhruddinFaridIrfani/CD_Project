package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "summary_matching", schema = "ofac")
public class SummaryMatching {
    @Id
    @Column(name = "summary_matching_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int summary_matching_id;


    @NotNull
    @Column(name = "status")
    private String status;

    @NotNull
    @Column(name = "sdn_file_id_sdn")
    int sdnfile_id_sdn;

    @NotNull
    @Column(name = "sdn_file_id_consolidate")
    int sdnfile_id_consolidate;

    @NotNull
    @Column(name = "ktp_file_id")
    int ktp_file_id;


    @NotNull
    @Column(name = "dma_file_id")
    int dma_file_id;

    @Column(name = "sdn_data")
    int sdn_data;

    @Column(name = "consolidate_data")
    int consolidate_data;

    @Column(name = "ktp_data")
    int ktp_data;

    @Column(name = "dma_data")
    int dma_data;

    @Column(name = "screen_data")
    int screen_data;

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


    @Column(name = "count_positive_sdn")
    int count_positive_sdn;

    @Column(name = "count_positive_consolidate")
    int count_positive_consolidate;

    @Column(name = "count_potential_sdn")
    int count_potential_sdn;

    @Column(name = "count_potential_consolidate")
    int count_potential_consolidate;

    @Column(name = "extract_date_sdn")
    String extract_date_sdn;

    @Column(name = "extract_date_consolidate")
    String extract_date_consolidate;


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

    public int getKtp_file_id() {
        return ktp_file_id;
    }

    public void setKtp_file_id(int ktp_file_id) {
        this.ktp_file_id = ktp_file_id;
    }

    public int getDma_file_id() {
        return dma_file_id;
    }

    public void setDma_file_id(int dma_file_id) {
        this.dma_file_id = dma_file_id;
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

    public int getCount_positive_sdn() {
        return count_positive_sdn;
    }

    public void setCount_positive_sdn(int count_positive_sdn) {
        this.count_positive_sdn = count_positive_sdn;
    }

    public int getCount_positive_consolidate() {
        return count_positive_consolidate;
    }

    public void setCount_positive_consolidate(int count_positive_consolidate) {
        this.count_positive_consolidate = count_positive_consolidate;
    }

    public int getCount_potential_sdn() {
        return count_potential_sdn;
    }

    public void setCount_potential_sdn(int count_potential_sdn) {
        this.count_potential_sdn = count_potential_sdn;
    }

    public int getCount_potential_consolidate() {
        return count_potential_consolidate;
    }

    public void setCount_potential_consolidate(int count_potential_consolidate) {
        this.count_potential_consolidate = count_potential_consolidate;
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

    public int getScreen_data() {
        return screen_data;
    }

    public void setScreen_data(int screen_data) {
        this.screen_data = screen_data;
    }

    public String getExtract_date_sdn() {
        return extract_date_sdn;
    }

    public void setExtract_date_sdn(String extract_date_sdn) {
        this.extract_date_sdn = extract_date_sdn;
    }

    public String getExtract_date_consolidate() {
        return extract_date_consolidate;
    }

    public void setExtract_date_consolidate(String extract_date_consolidate) {
        this.extract_date_consolidate = extract_date_consolidate;
    }
}
