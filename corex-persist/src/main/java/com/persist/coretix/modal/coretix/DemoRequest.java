package com.persist.coretix.modal.coretix;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "demo_request")
public class DemoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "clinic_name", nullable = false, length = 150)
    private String clinicName;

    @Column(name = "work_email", nullable = false, length = 150)
    private String workEmail;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "demo_done", nullable = false)
    private boolean demoDone;

    @Column(name = "demo_done_at")
    private Timestamp demoDoneAt;

    @Column(name = "demo_done_by", length = 100)
    private String demoDoneBy;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isDemoDone() {
        return demoDone;
    }

    public void setDemoDone(boolean demoDone) {
        this.demoDone = demoDone;
    }

    public Timestamp getDemoDoneAt() {
        return demoDoneAt;
    }

    public void setDemoDoneAt(Timestamp demoDoneAt) {
        this.demoDoneAt = demoDoneAt;
    }

    public String getDemoDoneBy() {
        return demoDoneBy;
    }

    public void setDemoDoneBy(String demoDoneBy) {
        this.demoDoneBy = demoDoneBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
