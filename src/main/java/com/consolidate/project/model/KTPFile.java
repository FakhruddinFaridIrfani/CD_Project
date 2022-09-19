package com.consolidate.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "ktp_file", schema = "ofac")
public class KTPFile {
    @Id
    @Column(name = "ktp_file_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ktp_file_id;

    @NotNull
    @Column(name = "status")
    private String status;


    @NotNull
    @Column(name = "file_name_ori")
    private String file_name_ori;


    @NotNull
    @Column(name = "file_name_save")
    private String file_name_save;


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

    public String getFile_name_ori() {
        return file_name_ori;
    }

    public void setFile_name_ori(String file_name_ori) {
        this.file_name_ori = file_name_ori;
    }

    public String getFile_name_save() {
        return file_name_save;
    }

    public void setFile_name_save(String file_name_save) {
        this.file_name_save = file_name_save;
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
