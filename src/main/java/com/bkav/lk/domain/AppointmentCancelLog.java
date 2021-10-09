package com.bkav.lk.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "appointment_cancel_log")
public class AppointmentCancelLog extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_day_canceled")
    private Integer maxDayCanceled;

    @Column(name = "max_week_canceled")
    private Integer maxWeekCanceled;

    @Column(name = "is_blocked")
    private Integer isBlocked;

    @Column(name = "start_blocked_date")
    private Instant startBlockedDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMaxDayCanceled() {
        return maxDayCanceled;
    }

    public void setMaxDayCanceled(Integer dayPermission) {
        this.maxDayCanceled = dayPermission;
    }

    public Integer getMaxWeekCanceled() {
        return maxWeekCanceled;
    }

    public void setMaxWeekCanceled(Integer weekPermission) {
        this.maxWeekCanceled = weekPermission;
    }

    public Integer getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(Integer isBlocked) {
        this.isBlocked = isBlocked;
    }

    public Instant getStartBlockedDate() {
        return startBlockedDate;
    }

    public void setStartBlockedDate(Instant startBlockedDate) {
        this.startBlockedDate = startBlockedDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
