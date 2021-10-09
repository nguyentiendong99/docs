package com.bkav.lk.service;

public interface SchedulingJobService {

    /***
     * Neu qua ngay kham thi cap nhat trang thai Cho duyet, Tu choi, Cho kham, Huy => Huy
     */
    void schedulingDoctorAppointment();

    /*void schedulingNotificationReminder();

    void schedulingMedicationReminder();
   */
    // thay doi trang thai cua feedback neu nguoi dung 30 ngay ko tuong tac;
    void schedulingFeedbackReminder();

    /**
     * Job chay nhac lich kham.
     * */
    void schedulingPushNotifyDoctorAppointment();
}
