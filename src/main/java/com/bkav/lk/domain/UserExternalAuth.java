package com.bkav.lk.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;


@Entity
@Table(name = "user_external_login")
public class UserExternalAuth extends AbstractAuditingEntity implements Serializable {

    private static long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(name = "type", length = 100)
    private String type;

    @NotNull
    @Column(name = "external_user_id")
    private String externalUserId;

    @NotNull
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "status")
    private Integer status;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserExternalAuth postType = (UserExternalAuth) o;
        return getId().equals(postType.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
