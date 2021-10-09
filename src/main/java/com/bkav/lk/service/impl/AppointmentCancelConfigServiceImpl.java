package com.bkav.lk.service.impl;

import com.bkav.lk.domain.AppointmentCancelLog;
import com.bkav.lk.domain.Config;
import com.bkav.lk.domain.User;
import com.bkav.lk.repository.AppointmentCancelConfigRepository;
import com.bkav.lk.service.AppointmentCancelConfigService;
import com.bkav.lk.service.ConfigService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentCancelConfigServiceImpl implements AppointmentCancelConfigService {

    private final AppointmentCancelConfigRepository appointmentCancelConfigRepository;

    private final UserService userService;

    private final ConfigService configService;

    @Autowired
    public AppointmentCancelConfigServiceImpl(
            AppointmentCancelConfigRepository appointmentCancelConfigRepository,
            UserService userService, ConfigService configService) {
        this.appointmentCancelConfigRepository = appointmentCancelConfigRepository;
        this.userService = userService;
        this.configService = configService;
    }

    @Override
    public AppointmentCancelLog save() {
        User currentUser = userService.getUserWithAuthorities()
                .orElseThrow(() -> new BadRequestAlertException("Not found current login user", "user", "user.notfound"));
        Config exceedDay = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_DAY);
        Config exceedWeek = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_WEEK);
        AppointmentCancelLog acc = new AppointmentCancelLog();
        acc.setMaxDayCanceled(Integer.parseInt(exceedDay.getPropertyValue()) - 1);
        acc.setMaxWeekCanceled(Integer.parseInt(exceedWeek.getPropertyValue()) - 1);
        acc.setUser(currentUser);
        acc.setIsBlocked(Constants.BOOL_NUMBER.FALSE);
        acc.setStartBlockedDate(null);
        return appointmentCancelConfigRepository.save(acc);
    }

    @Override
    public AppointmentCancelLog update(AppointmentCancelLog appointmentCancelLog) {
        return appointmentCancelConfigRepository.save(appointmentCancelLog);
    }

    public List<AppointmentCancelLog> updateAll(List<AppointmentCancelLog> appointmentCancelLogs) {
        return appointmentCancelConfigRepository.saveAll(appointmentCancelLogs);
    }

    @Override
    public AppointmentCancelLog findByUserId(Long userId) {
        List<AppointmentCancelLog> accs = appointmentCancelConfigRepository.findByUserId(userId);
        return !accs.isEmpty() ? accs.get(0) : null;
    }

    @Override
    public AppointmentCancelLog findByCurrentUser() {
        User currentUser = userService.getUserWithAuthorities()
                .orElseThrow(() -> new BadRequestAlertException("Not found current login user", "user", "user.notfound"));
        return findByUserId(currentUser.getId());
    }

    @Override
    public List<AppointmentCancelLog> findAll() {
        return appointmentCancelConfigRepository.findAll();
    }

    @Override
    public void deleteAll(List<Long> ids) {
        List<AppointmentCancelLog> appointmentCancelLogs = appointmentCancelConfigRepository.findAllById(ids);
        if (!appointmentCancelLogs.isEmpty())
            appointmentCancelConfigRepository.deleteAll(appointmentCancelLogs);
    }

    @Override
    public Boolean hasBlocked() {
        User result = userService.getUserWithAuthorities()
                .orElseThrow(() -> new BadRequestAlertException("Not found current login user", "user", "user.notfound"));
        return appointmentCancelConfigRepository.existsByUserIdAndIsBlocked(result.getId(), Constants.BOOL_NUMBER.TRUE);
    }
}
