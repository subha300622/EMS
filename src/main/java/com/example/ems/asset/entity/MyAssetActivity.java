package com.example.ems.asset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_asset_activities")
public class MyAssetActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private MyAsset asset;

    @Column(nullable = false)
    private String event;

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    private String remarks;

    public MyAssetActivity() {}

    public MyAssetActivity(MyAsset asset, String event, String performedBy, String remarks) {
        this.asset = asset;
        this.event = event;
        this.performedBy = performedBy;
        this.remarks = remarks;
        this.date = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MyAsset getAsset() {
        return asset;
    }

    public void setAsset(MyAsset asset) {
        this.asset = asset;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
