package com.bkav.lk.web.rest;

import com.bkav.lk.domain.User;
import com.bkav.lk.dto.MedicationReminderDTO;
import com.bkav.lk.service.MedicationReminderService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.MedicationReminderMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MedicationReminderResource {

    private static final Logger log = LoggerFactory.getLogger(MedicationReminderResource.class);
    private static final String ENTITY_NAME = "Medication Reminder";

    private final MedicationReminderService service;
    private final UserService userService;
    private final MedicationReminderMapper mapper;

    public MedicationReminderResource(MedicationReminderService service, UserService userService, MedicationReminderMapper mapper) {
        this.service = service;
        this.userService = userService;
        this.mapper = mapper;
    }

    @PostMapping("/medication-reminder")
    public ResponseEntity<MedicationReminderDTO> save(@RequestBody MedicationReminderDTO reminderDTO) {
        Optional<User> user = userService.getUserWithAuthorities();
        if (!user.isPresent()) {
            throw new BadRequestAlertException("A medication reminder cannot save - has not User Id", ENTITY_NAME, "user_id_null");
        }
        if (reminderDTO.getTime() == null) {
            throw new BadRequestAlertException("A medication reminder cannot save - has not Time", ENTITY_NAME, "time_null");
        }
        if (reminderDTO.getBookingCode() == null) {
            throw new BadRequestAlertException("A medication reminder cannot save - has not BookingCode", ENTITY_NAME, "booking_code_null");
        }
        MedicationReminderDTO findRecord = service.findByUserIdAndBookingCodeAndTime(user.get().getId(), reminderDTO.getBookingCode(), reminderDTO.getTime());
        if (findRecord != null) {
            throw new BadRequestAlertException("A medication reminder cannot save - duplicates time", ENTITY_NAME, "duplicates_time");
        }
        reminderDTO.setUserId(user.get().getId());
        reminderDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
        MedicationReminderDTO dto = service.save(reminderDTO);
        return ResponseEntity.ok().body(dto);
    }

    @PutMapping("/medication-reminder")
    public ResponseEntity<MedicationReminderDTO> update(@RequestBody MedicationReminderDTO reminderDTO) {
        if (reminderDTO.getId() == null) {
            throw new BadRequestAlertException("Id is not null", ENTITY_NAME, "id_null");
        }
        if (reminderDTO.getTime() == null) {
            throw new BadRequestAlertException("A medication reminder cannot save - has not Time", ENTITY_NAME, "time_null");
        }
        MedicationReminderDTO dto = service.findById(reminderDTO.getId());
        if (dto == null) {
            throw new BadRequestAlertException("Not found", ENTITY_NAME, "not_found");
        }
        dto.setTime(reminderDTO.getTime());
        dto.setStatus(Constants.ENTITY_STATUS.ACTIVE);
        return ResponseEntity.ok().body(service.save(dto));
    }

    @DeleteMapping("/medication-reminder/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        MedicationReminderDTO dto = service.findById(id);
        if (dto == null) {
            throw new BadRequestAlertException("Not found record", ENTITY_NAME, "not_found_record");
        }
        service.delete(dto.getId());
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, true,
                ENTITY_NAME, dto.getId().toString())).build();
    }

    @GetMapping("/medication-reminder/{bookingCode}")
    public ResponseEntity<List<MedicationReminderDTO>> findAllTimeByBookingCode(@PathVariable String bookingCode) {
        Optional<User> user = userService.getUserWithAuthorities();
        if (!user.isPresent()) {
            throw new BadRequestAlertException("User not found", ENTITY_NAME, "user_not_found");
        }
        List<MedicationReminderDTO> list = service.findAllByBookingCode(user.get().getId(), bookingCode);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/medication-reminder/time")
    public ResponseEntity<List<MedicationReminderDTO>> findAllByWithTime(@RequestParam(required = false) Integer minutes) {
        if (minutes == null) {
            LocalTime localTime = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Integer timeNow = localTime.getHour() * 60 + localTime.getMinute();
            return ResponseEntity.ok().body(service.findAllByTimeEqual(timeNow));
        }
        return ResponseEntity.ok().body(service.findAllByTimeEqual(minutes));
    }
}
