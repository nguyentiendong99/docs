package com.bkav.lk.dto;

import com.bkav.lk.config.Constants;
import com.bkav.lk.domain.Authority;
import com.bkav.lk.domain.Group;
import com.bkav.lk.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.restfb.Facebook;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class UserDTO implements Serializable {

    private Long id;

    @Facebook(com.bkav.lk.util.Constants.FACEBOOK_PROFILES.ID)
    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    @Facebook(com.bkav.lk.util.Constants.FACEBOOK_PROFILES.NAME)
    @Size(max = 50)
    private String name;

    @Facebook(com.bkav.lk.util.Constants.FACEBOOK_PROFILES.EMAIL)
    @Email
    @Size(min = 5, max = 254)
    private String email;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean activated;

    private String phoneNumber;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdBy;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Instant createdDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lastModifiedBy;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Instant lastModifiedDate;

    private Set<String> authorities;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer status;

    private String description;

    private String avatar;

    private Long healthFacilityId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long positionId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String positionName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long departmentId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String departmentName;

    private Set<Group> groups = new HashSet<>();

    private Instant dob;

    private Long doctorId;

    private String doctorName;

    private String socialId;

    private String socialType;

    private String cityName;

    @NotEmpty
    private String cityCode;

    private String districtName;

    @NotEmpty
    private String districtCode;

    private String wardName;

    @NotEmpty
    private String wardCode;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.avatar = user.getAvatar();
        this.description = user.getDescription();
        this.dob = user.getDob();
        this.email = user.getEmail();
        this.authorities = getListAuthorityByGroup(user);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public Instant getDob() {
        return dob;
    }

    public void setDob(Instant dob) {
        this.dob = dob;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public String getSocialType() {
        return socialType;
    }

    public void setSocialType(String socialType) {
        this.socialType = socialType;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getWardCode() {
        return wardCode;
    }

    public void setWardCode(String wardCode) {
        this.wardCode = wardCode;
    }

    private Set<String> getListAuthorityByGroup(User user) {
        Set<String> authorities = user.getAuthorities().stream()
                .map(Authority::getName)
                .collect(Collectors.toSet());
        Set<String> groupAuthorities = new HashSet<>();

        if (user.getGroups() != null) {
            user.setGroups(user.getGroups().stream()
                    .filter(o -> o.getStatus() == com.bkav.lk.util.Constants.ENTITY_STATUS.ACTIVE)
                    .collect(Collectors.toSet()));
            for (Group g : user.getGroups()) {
                Set<String> authoritiesGroup = g.getAuthorities().stream()
                        .map(Authority::getName)
                        .collect(Collectors.toSet());
                groupAuthorities.addAll(authoritiesGroup);
            }
        }
        authorities.addAll(groupAuthorities);
        return authorities;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", activated=" + activated +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdDate=" + createdDate +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModifiedDate=" + lastModifiedDate +
                ", authorities=" + authorities +
                ", status=" + status +
                ", description='" + description + '\'' +
                '}';
    }
}
