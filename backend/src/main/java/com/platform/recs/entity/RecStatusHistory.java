package com.platform.recs.entity;

import com.platform.recs.enumtype.RecStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rec_status_history")
public class RecStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rec_id", nullable = false)
    private RenewableEnergyCertificate rec;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private RecStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private RecStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(length = 500)
    private String comment;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public RenewableEnergyCertificate getRec() { return rec; }
    public void setRec(RenewableEnergyCertificate rec) { this.rec = rec; }
    public RecStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(RecStatus oldStatus) { this.oldStatus = oldStatus; }
    public RecStatus getNewStatus() { return newStatus; }
    public void setNewStatus(RecStatus newStatus) { this.newStatus = newStatus; }
    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
