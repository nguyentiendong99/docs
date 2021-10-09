package com.bkav.lk.web.rest;

import com.bkav.lk.service.DoctorScheduleTimeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DoctorScheduleTimeResource {

    private final DoctorScheduleTimeService service;

    public DoctorScheduleTimeResource(DoctorScheduleTimeService service) {
        this.service = service;
    }

}
