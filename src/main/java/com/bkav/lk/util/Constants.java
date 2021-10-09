package com.bkav.lk.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface Constants {

    Integer LATEST_DAY_CONFIG_APPLY = 31;
    Integer IMMEDIATELY_DAY_CONFIG_APPLY = 0;
    String UPDATE_VALUE = "Đang cập nhật";

    interface SYT_DEFAULT {
        String code = "SYT";
        String name = "Sở y tế Yên Bái";
        String cityCode = "15";
        String address = "656 Yên Ninh, P.Yên Ninh, Thành phố Yên Bái, Yên Bái";
        String districtCode = "15.132";
        String wardCode = "15.132.04252";
    }

    interface BOOL_NUMBER {
        Integer TRUE = 1;
        Integer FALSE = 0;
    }

    interface ENTITY_STATUS {
        Integer DELETED = 0;
        Integer ACTIVE = 1;
        Integer DEACTIVATE = 2;
        Integer NOTIFICATION_SEEN = 3;
    }

    interface ACTION {
        String CREATE = "create";
        String UPDATE = "update";
        String DELETE = "delete";
    }

    interface DOCTOR_SCHEDULE_STATUS {
        Integer MORNING_WORKING = 1;
        Integer AFTERNOON_WORKING = 2;
        Integer FULL_TIME_WORKING = 3;
        Integer ERROR_WORKING = -1;
    }

    interface DOCTOR_APPOINTMENT_STATUS {
        Integer DELETE_DOCTOR_APPOINTMENT_TEMP_INVALID = -1; // Phuc vu cho viec XOA MEM DoctorAppointment Tam thoi
        Integer REQUEST = 0; // Phuc vu cho TH luu tam thoi (dung cho dat lich thanh toan VNPay)
        Integer WAITING_APPROVE = 1;
        Integer APPROVED = 2;
        Integer DONE = 3;
        Integer CANCEL = 4;
        Integer DENY = 5;
        Integer WAIT_CONFIRM = 6;
    }

    interface ACADEMIC_RANK_FIRST {
        Integer BS = 1;
        Integer ThS = 2;
        Integer TS = 3;
        Integer TSKH = 4;
    }

    interface RELATIONSHIP {
        String ME = "me"; // tôi
        String FATHER = "father";
        String MOTHER = "mother";
        String GRANDFATHER = "grandfather";
        String GRANDMOTHER = "grandmother";
        String SON = "son";
        String DAUGHTER = "daughter";
        String HUSBAND = "husband";
        String WIFE = "wife";
        String OLDER_BROTHER = "older_brother";
        String OLDER_SISTER = "older_sister";
        String YOUNGER_BROTHER = "younger_brother";
        String YOUNGER_SISTER = "younger_sister";
        String UNCLE = "uncle";
        String AUNT = "aunt";
        String OTHER = "other";
    }

    enum RelationShipConstant {
        me("Tôi", "other"),
        father("Bố", "male"),
        mother("Mẹ", "female"),
        grandfather("Ông", "male"),
        grandmother("Bà", "female"),
        son("Con trai", "male"),
        daughter("Con gái", "female"),
        husband("Chồng", "male"),
        wife("Vợ", "female"),
        older_brother("Anh trai", "male"),
        older_sister("Chị gái", "female"),
        younger_brother("Em trai", "male"),
        younger_sister("Em gái", "female"),
        uncle("Cậu/Chú/Bác trai", "male"),
        aunt("Cô/Dì/Bác gái", "female"),
        other("Khác", "other");

        public String value;
        public String gender;

        RelationShipConstant(String value, String gender) {
            this.value = value;
            this.gender = gender;
        }
    }

    interface GENDER {
        String MALE = "male";
        String FEMALE = "female";
        String OTHER = "other";
    }

    interface VI_GENDER {
        String MALE = "Nam";
        String FEMALE = "Nữ";
        String OTHER = "Khác";
    }

    interface ACADEMIC_RANK_SECOND {
        Integer PGS = 1;
        Integer GS = 2;
    }

    interface CONTENT_TYPE {
        Integer DOCTOR_APPOINTMENT = 1;
        Integer DOCTOR_SCHEDULE = 2;
        Integer CLINIC = 3;
        Integer DOCTOR = 4;
        Integer MEDICAL_SERVICE = 5;
        Integer TOPIC = 6;
        Integer HEALTH_FACILITY = 7;
        Integer POSITION = 8;
        Integer USER = 9;
        Integer GROUP = 10;
        Integer DEPARTMENT = 11;
        Integer MEDICAL_SPECIALITY = 12;
        Integer FEEDBACK = 13;
        Integer PATIENT_RECORD = 14;
        Integer DOCTOR_FEEDBACK = 15;
        Integer MEDICAL_DECLARATION_INFO = 16;
        Integer ACADEMIC_RANK = 17;
        Integer DOCTOR_APPOINTMENT_CONFIG = 18;
        Integer SUBCLINICAL_RESULT = 19;
        Integer RE_EXAMINATION = 20;
        Integer SYSTEM_NOTIFICATION = 21;
    }

    interface ACTION_TYPE {
        Integer CREATE = 1;
        Integer UPDATE = 2;
        Integer DELETE = 3;
        Integer APPROVE = 4;
        Integer DENY = 5;
        Integer CONFIRM = 6;
        Integer CANCEL = 7;
        Integer WAITING = 8;
        Integer PROCESSING = 9;
        Integer DONE = 10;
        Integer RETRIEVE = 11;
    }

    interface PEOPLE_REGISTERED {
        Integer MAX = 10;
    }

    interface AREA_LEVEL {
        Integer COUNTRY = 0;
        Integer CITY = 1;
        Integer DISTRICT = 2;
        Integer WARD = 3;
    }

    interface NOTIFICATION_TYPE {
        String DOCTOR_APPOINTMENT = "doctor_appointment";
        String REMINDER_APPOINTMENT = "reminder_appointment";
        String MEDICATION_REMINDER = "medication_reminder";
        String FEEDBACK = "feedback";
        String FEEDBACK_DOCTOR = "feedback_doctor";
        String SUBCLINICAL_RESULT = "subclinical_result";
    }

    enum NotificationConstants {
        APPOINTMENT_FAILED(2, "Thông báo đặt lịch khám thất bại", "appointment_failed"),
        APPOINTMENT_CANCEL(99, "Thông báo hủy lịch khám thành công", "appointment_cancel"),
        APPOINTMENT_DATE_SUCCESS(31, "Thông báo đặt lịch khám theo ngày thành công", "appointment_date"),
        APPOINTMENT_DOCTOR_SUCCESS(32, "Thông báo đặt lịch khám bác sĩ thành công", "appointment_doctor"),
        APPOINTMENT_REMINDER(41, "Thông báo nhắc lịch khám/tái khám", "appointment_reminder"),
        APPOINTMENT_REMINDER_DOCTOR(42, "Thông báo nhắc lịch khám/tái khám", "appointment_reminder_doctor"),
        APPOINTMENT_CHANGE(5, "Thông báo thay đổi lịch khám", "appointment_change"),
        SUGGEST_CLS(12, "Thông báo gợi ý thực hiện CLS", "cls_suggest"),
        GET_RESULT_CLS(13, "Thông báo lấy kết quả CLS", "cls_result"),
        FEEDBACK_ACCEPT(14, "Thông báo xử lý ý kiến đóng góp", "feedback_accept"),
        FEEDBACK_RESPONSE(15, "Thông báo phản hồi bình luận ý kiến đóng góp", "feedback_response"),
        REVIEW_DOCTOR_RESPONSE(16, "Thông báo phản hồi nhận xét bác sĩ", "doctor_response"),
        OTHER(17, "Thông báo từ phía backend", "");

        public String name;
        public int id;
        public String template;

        NotificationConstants(int id, String name, String template) {
            this.id = id;
            this.name = name;
            this.template = template;
        }

    }

    interface DAY_OF_WEEK {
        Integer MONDAY = 1;
        Integer TUESDAY = 2;
        Integer WEDNESDAY = 3;
        Integer THURSDAY = 4;
        Integer FRIDAY = 5;
        Integer SATURDAY = 6;
        Integer SUNDAY = 7;
    }

    interface DOCTOR_APPOINTMENT_CONFIG {
        Long HEALTH_FACILITIES_DEFAULT = 0L;
        Integer MINUTES_PER_APPOINTMENT_SCHEDULE = 30;
        Integer ALLOW_TIME_DEFAULT = 1;
        Integer MAX_REGISTERED_PATIENTS_BY_DAILY = 20;
        Integer MAX_REGISTERED_PATIENTS_BY_DOCTOR = 20;
        Integer CONNECT_WITH_HIS = 1;
        Integer UN_CONNECT_WITH_HIS_APPROVAL_AUTOMATIC = 2;
        Integer UN_CONNECT_WITH_HIS_APPROVAL_MANUAL = 3;
    }

    interface DOCTOR_APPOINTMENT_TYPE {
        Integer BY_DATE = 1;
        Integer BY_DOCTOR = 2;
        Integer BOTH = 3;
        Integer TYPE_ACTIVE = 1;
    }

    interface REPORT_STATUS {
        Integer WAITING = 1;
        Integer PROCESSING = 2;
        Integer DONE = 3;
    }

    interface FACEBOOK_PROFILES {
        String ID = "id";
        String NAME = "name";
        String FIRST_NAME = "first_name";
        String LAST_NAME = "last_name";
        String EMAIL = "email";
    }

    enum DOCTOR_APPOINTMENT_STATUS_LIST {
        WAITING(1, "Đang chờ"),
        APPROVED(2, "Đã duyệt"),
        DONE(3, "Đã khám"),
        CANCEL(4, "Đã hủy"),
        DENY(5, "Từ chối");

        private Integer id;
        private String text;

        DOCTOR_APPOINTMENT_STATUS_LIST(Integer id, String text) {
            this.id = id;
            this.text = text;
        }

        public Integer getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public static DOCTOR_APPOINTMENT_STATUS_LIST getById(int id) {
            for (DOCTOR_APPOINTMENT_STATUS_LIST e : values()) {
                if (e.id == id) return e;
            }
            return null;
        }
    }

    interface FILE_FORMAT {
        Integer WORD = 1;
        Integer EXCEL = 2;
        Integer PDF = 3;
        Integer XML = 4;
    }

    interface NOTIFICATION_STATUS {
        Integer HAVE_NOT_NOTIFIED = 1;
        Integer NOTIFIED = 2;
    }

    enum PAYMENT_METHOD {
        CASH("CASH", "Thanh toán trực tiếp tại CSYT"),
        PATIENT_CARD("PATIENT_CARD", "Thẻ khám bệnh"),
        VISA("VISA", "Thẻ tín dụng/Ghi nợ"),
        ATM("ATM", "Thẻ ATM"),
        E_WALLET("E_WALLET", "Ví điện tử");
        public String value;
        public String code;

        PAYMENT_METHOD(String code, String value) {
            this.code = code;
            this.value = value;
        }
    }

    enum BANK {
        SACOMBANK("SACOMBANK", "Ngân hàng TMCP Sài Gòn Thương Tín (SacomBank)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/sacombank_logo.png"),
        VIETINBANK("VIETINBANK", "Ngân hàng Công thương (Vietinbank)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/vietinbank_logo.png"),
        VIETCOMBANK("VIETCOMBANK", "Ngân hàng Ngoại thương (Vietcombank)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/vietcombank_logo.png"),
        SCB("SCB", "Ngân hàng TMCP Sài Gòn (SCB)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/scb_logo.png"),
        EXIMBANK("EXIMBANK", "Ngân hàng EximBank", "https://sandbox.vnpayment.vn/apis/assets/images/bank/eximbank_logo.png"),
        BIDV("BIDV", "Ngân hàng đầu tư và phát triển Việt Nam (BIDV)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/bidv_logo.png"),
        DONGABANK("DONGABANK", "Ngân hàng Đông Á (DongABank)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/dongabank_logo.png"),
        ACB("ACB", "Ngân hàng ACB", "https://sandbox.vnpayment.vn/apis/assets/images/bank/acb_logo.png"),
        MBBANK("MBBANK", "Ngân hàng thương mại cổ phần Quân đội", "https://sandbox.vnpayment.vn/apis/assets/images/bank/mbbank_logo.png"),
        TECHCOMBANK("TECHCOMBANK", "Ngân hàng Kỹ thương Việt Nam (TechcomBank)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/techcombank_logo.png"),
        VPBANK("VPBANK", "Ngân hàng Việt Nam Thịnh vượng (VPBank)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/vpbank_logo.png"),
        VIB("VIB", "Ngân hàng Thương mại cổ phần Quốc tế Việt Nam (VIB)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/vib_logo.png"),
        HDBANK("HDBANK", "Ngân hàng HDBank", "https://sandbox.vnpayment.vn/apis/assets/images/bank/hdbank_logo.png"),
        OJB("OJB", "Ngân hàng Đại Dương (OceanBank)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/ojb_logo.png"),
        SHB("SHB", "Ngân hàng Thương mại cổ phần Sài Gòn - Hà Nội(SHB)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/shb_logo.png"),
        SEABANK("SEABANK", "Ngân Hàng TMCP Đông Nam Á", "https://sandbox.vnpayment.vn/apis/assets/images/bank/seabank_logo.png"),
        ABBANK("ABBANK", "Ngân hàng thương mại cổ phần An Bình (ABBANK)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/abbank_logo.png"),
        TPBANK("TPBANK", "Ngân hàng Tiên Phong (TPBank)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/tpbank_logo.png"),
        NCB("NCB", "Ngân hàng Quốc Dân (NCB)", "https://sandbox.vnpayment.vn/apis/assets/images/bank/ncb_logo.png");
        public String code;
        public String name;
        public String imgURL;

        BANK(String code, String name, String imgURL) {
            this.code = code;
            this.name = name;
            this.imgURL = imgURL;
        }
    }

    interface PAYMENT_STATUS {
        Integer DELETE_TRANSACTION_TEMP_INVALID = -1; // Phuc vu cho viec XOA MEM Transaction Tam thoi
        Integer REQUEST = 0;  // Phuc vu cho TH luu tam thoi (dung cho dat lich thanh toan VNPay)
        Integer PAID_WATING = 1;
        Integer PAID_SUCCESS = 2;
        Integer REFUNDED_WATTING = 3;
        Integer REFUNDED_SUCCESS = 4;
        Integer FAILED = 5;
    }

    interface TRANSACTION_TYPE_CODE {
        String DEPOSIT = "DEPOSIT";
        String WITHDRAW = "WITHDRAW";
        String REFUND = "REFUND";
    }

    enum MODULE {
        DOCTOR_APPOINTMENT(1, "Quản lý lịch khám"),
        DOCTOR_SCHEDULE(2, "Thời gian khám bệnh"),
        CLINIC(3, "Quản lý danh mục phòng khám"),
        DOCTOR(4, "Quản lý danh mục bác sĩ"),
        MEDICAL_SERVICE(5, "Quản lý danh mục dịch vụ khám"),
        TOPIC(6, "Quản lý danh mục chủ đề ý kiến"),
        HEALTH_FACILITY(7, "Quản lý đơn vị"),
        POSITION(8, "Quản lý chức vụ"),
        USER(9, "Quản lý người dùng"),
        GROUP(10, "Quản lý phân quyền"),
        DEPARTMENT(11, "Quản lý phòng ban"),
        MEDICAL_SPECIALITY(12, "Quản lý danh mục chuyên khoa"),
        FEEDBACK(13, "Quản lý ý kiến đóng góp"),
        PATIENT_RECORD(14, "Hồ sơ bệnh nhân"),
        DOCTOR_FEEDBACK(15, "Quản lý đánh giá bác sĩ"),
        MEDICAL_DECLARATION_INFO(16, "Quản lý thông tin tờ khai y tế"),
        ACADEMIC_RANK(17, "Quản lý danh mục học hàm học vị"),
        DOCTOR_APPOINTMENT_CONFIG(18, "Cấu hình đặt lịch"),
        SUBCLINICAL_RESULT(19, "Thông báo lấy KQ-CLS"),
        RE_EXAMINATION(20, "Hẹn lịch tái khám");
        public int id;
        public String name;

        MODULE(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static MODULE getById(int id) {
            for (MODULE e : values()) {
                if (e.id == id) return e;
            }
            return DOCTOR_APPOINTMENT;
        }
    }

    enum ACTION_NAME {
        CREATE(1, "Thêm mới"),
        UPDATE(2, "Cập nhật"),
        DELETE(3, "Xóa"),
        APPROVE(4, "Xét duyệt"),
        DENY(5, "Từ chối"),
        CONFIRM(6, "Xác nhận"),
        CANCEL(7, "Hủy"),
        WAITING(8, "Đang chờ"),
        PROCESSING(9, "Đang xử lý"),
        DONE(10, "Đã hoàn thành");
        public int id;
        public String name;

        ACTION_NAME(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static ACTION_NAME getById(int id) {
            for (ACTION_NAME e : values()) {
                if (e.id == id) return e;
            }
            return CREATE;
        }
    }

    enum CONTENT {
        DOCTOR_APPOINTMENT(1, "lịch khám"),
        DOCTOR_SCHEDULE(2, "thời gian khám bệnh"),
        CLINIC(3, "phòng khám"),
        DOCTOR(4, "bác sĩ"),
        MEDICAL_SERVICE(5, "dịch vụ khám"),
        TOPIC(6, "chủ đề ý kiến"),
        HEALTH_FACILITY(7, "đơn vị"),
        POSITION(8, "chức vụ"),
        USER(9, "người dùng"),
        GROUP(10, "phân quyền"),
        DEPARTMENT(11, "phòng ban"),
        MEDICAL_SPECIALITY(12, "chuyên khoa"),
        FEEDBACK(13, "ý kiến đóng góp"),
        PATIENT_RECORD(14, "hồ sơ bệnh nhân"),
        DOCTOR_FEEDBACK(15, "đánh giá bác sĩ"),
        MEDICAL_DECLARATION_INFO(16, "thông tin tờ khai y tế"),
        ACADEMIC_RANK(17, "học hàm học vị"),
        DOCTOR_APPOINTMENT_CONFIG(18, "cấu hình đặt lịch"),
        SUBCLINICAL_RESULT(19, "thông báo lấy KQ-CLS"),
        RE_EXAMINATION(20, "lịch tái khám");
        public int id;
        public String name;

        CONTENT(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static CONTENT getById(int id) {
            for (CONTENT e : values()) {
                if (e.id == id) return e;
            }
            return DOCTOR_APPOINTMENT;
        }
    }

    interface CONFIG_PROPERTY {
        // Property Type
        String TEXT_TYPE = "text";
        String NUMBER_TYPE = "number";

        // HIS
        String HIS_HOST = "HIS_HOST";
        String HIS_NAME = "Host";
        String HIS_GROUP = "his";

        // SOCIAL INSURANCE
        String SOCIAL_INSURANCE_HOST = "SOCIAL_INSURANCE_HOST";
        String SOCIAL_INSURANCE_NAME = "Host";
        String SOCIAL_INSURANCE_GROUP = "social_insurance";

        // Social Network SignIn
        String SOCIAL_NETWORK_SIGNIN_GROUP = "social_network_signin";
        String SOCIAL_GOOGLE_NAME = "Google";
        String SOCIAL_FACEBOOK_NAME = "Facebook";
        String SOCIAL_GOOGLE_CODE = "GOOGLE";
        String SOCIAL_FACEBOOK_CODE = "FACEBOOK";

        // Config DoctorAppointment other
        String CONFIG_OTHER_GROUP = "config_other";
        String TIME_ALLOW_BEFORE_BOOKING = "TIME_ALLOW_BEFORE_BOOKING";
        String ALLOW_BOOKING_BEFORE_DAY = "ALLOW_BOOKING_BEFORE_DAY";
        String BLOCK_ACCOUNT_EXCEED_DAY = "BLOCK_ACCOUNT_EXCEED_DAY";
        String BLOCK_ACCOUNT_EXCEED_WEEK = "BLOCK_ACCOUNT_EXCEED_WEEK";
        String BLOCK_ACCOUNT_IN_DAY = "BLOCK_ACCOUNT_IN_DAY";

        Integer STATUS_ACTIVE = 1;
    }

    enum CONFIG_CATEGORY_TYPE {
        DOCTOR(1, "DOCTOR"),
        CLINIC(2, "CLINIC"),
        MEDICAL_SERVICE(3, "MEDICAL_SERVICE"),
        CLS(4, "CLS"),
        TOPIC(5, "TOPIC"),
        ACADEMIC(6, "ACADEMIC"),
        MEDICAL_SPECIALDITY(7, "MEDICAL_SPECIALITY");

        public Integer code;
        public String value;

        CONFIG_CATEGORY_TYPE(Integer code, String value) {
            this.code = code;
            this.value = value;
        }
    }

    enum CONFIG_REPORT_TYPE {
        FEEDBACK(1, "FEEDBACK"),
        DOCTOR_FEEDBACK(2, "DOCTOR_FEEDBACK");

        public Integer code;
        public String value;

        CONFIG_REPORT_TYPE(Integer code, String value) {
            this.code = code;
            this.value = value;
        }
    }

    enum CONFIG_PATIENT_TYPE {
        PATIENT_RECORD(1, "PATIENT_RECORD"),
        SUBCLINICAL_RESULT(2, "SUBCLINICAL_RESULT"),
        RE_EXAMINATION(3, "RE_EXAMINATION");

        public Integer code;
        public String value;

        CONFIG_PATIENT_TYPE(Integer code, String value) {
            this.code = code;
            this.value = value;
        }
    }

    enum CONFIG_DATA_TYPE {
        NUMBER(1, "NUMBER"),
        TEXT(2, "TEXT"),
        LIST(3, "LIST"),
        CHECKBOX(4, "CHECKBOX"),
        RADIO(5, "RADIO"),
        DATE(6, "DATE"),
        SELECT(7, "SELECT");

        public Integer code;
        public String value;

        CONFIG_DATA_TYPE(Integer code, String value) {
            this.code = code;
            this.value = value;
        }
    }

    interface SYSTEM_NOTIFICATION_STATUS {
        Integer DELETE = 0;
        Integer WAITING_APPROVE = 1;
        Integer DEMO = 2;
        Integer DENY = 3;
        Integer APPROVED = 4;
        Integer PUBLISHED = 5;
        Integer CANCEL = 6;
    }

    interface SYS_NOTI_CREATE_TYPE {
        Integer COMPLETE = 1;
        Integer DEMO = 2;
    }

    interface SYS_NOTI_STYLE {
        Integer AFTER_APPROVE = 1;
        Integer ON_DAY = 2;
        Integer FROM_DAY_TO_DAY = 3;
    }

    String[] CATEGORY_FIELD_MAIN_CONFIG_DOCTOR = {"code", "name", "academic_id", "medical_speciality_id", "position_id", "status"};
    String[] CATEGORY_FIELD_MAIN_CONFIG_CLINIC = {"code", "name", "status"};
    String[] CATEGORY_FIELD_MAIN_CONFIG_MEDICAL_SERVICE = {"code", "name", "price", "status"};
    String[] CATEGORY_FIELD_MAIN_CONFIG_CLS = {"cls_code", "cls_name", "cls_price", "status"};
    String[] CATEGORY_FIELD_MAIN_CONFIG_TOPIC = {"code", "name", "status"};
    String[] CATEGORY_FIELD_MAIN_CONFIG_ACADEMIC = {"code", "name", "description", "status"};
    String[] CATEGORY_FIELD_MAIN_CONFIG_MEDICAL_SPECIALITY = {"code", "name", "status"};

    String[] CATEGORY_ICON_DOCTOR_METHOD = {"add", "update", "delete", "export_excel", "add_by_excel"};
    String[] CATEGORY_ICON_CLINIC_METHOD = {"add", "update", "delete", "export_excel", "add_by_excel"};
    String[] CATEGORY_ICON_MEDICAL_SERVICE_METHOD = {"add", "update", "delete", "export_excel", "add_by_excel"};
    String[] CATEGORY_ICON_CLS_METHOD = {"add", "update", "delete", "export_excel", "add_by_excel"};

    String[] REPORT_FIELD_MAIN_CONFIG_FEEDBACK = {"topicName", "content", "createdDate", "feedbackedUnitName", "processingUnitName", "processedBy", "status"};
    String[] REPORT_FIELD_MAIN_CONFIG_DOCTOR_FEEDBACK = {"userName", "doctorName", "healthFacilityName", "rate", "content", "createdDate", "status"};

    String[] REPORT_ICON_FEEDBACK_METHOD = {"detail", "history"};
    String[] REPORT_ICON_DOCTOR_FEEDBACK_METHOD = {"detail", "history"};

    String CONFIG_REPORT_TYPE_STRING = "CONFIG_REPORT_TYPE";

    String[] REPORT_FIELD_MAIN_CONFIG_PATIENT_RECORD = {"patientRecordCode", "name", "gender", "dob", "address", "wardName", "districtName", "phone"};
    String[] REPORT_FIELD_MAIN_CONFIG_SUBCLINICAL_RESULT = {"his_makham", "patientRecordCode", "patientRecordName", "cls_madichvu", "cls_tendichvu",
            "cls_kithuatvien", "cls_phongthuchien", "status"};
    String[] REPORT_FIELD_MAIN_CONFIG_RE_EXAMINATION = {"appointmentCode", "patientCode", "patientName", "reExaminationDate", "doctorName"};

    String[] REPORT_ICON_PATIENT_RECORD_METHOD = {"detail"};
    String[] REPORT_ICON_SUBCLINICAL_RESULT_METHOD = {"notify", "export_excel"};
    String[] REPORT_ICON_RE_EXAMINATION_METHOD = {"re_examination", "export_excel"};

    String CONFIG_PATIENT_TYPE_STRING = "CONFIG_PATIENT_TYPE";


    interface ICON_DEFAULT {
        String ADD = "";
        String UPDATE = "<i class=\"fas fa-pen\"></i>";
        String DELETE = "<i class=\"fas fa-trash\"></i>";
        String EXPORT_EXCEL = "<i class=\"fas fa-file-excel mr-2\"></i>";
        String ADD_BY_EXCEL = "";
        String DETAIL = "<i class=\"fas fa-eye\"></i>";
        String HISTORY = "<i class=\"fas fa-history\"></i>";
        String NOTIFY = "<i class=\"fas fa-paper-plane\"></i>";
        String RE_EXAMINATION = "<i class=\"fas fa-history\"></i>";
    }

    interface SOURCE_DOCTOR_APPOINTMENT {
        Integer WEB = 1;
        Integer MOBILE = 2;
    }

    enum DOCTOR_REQUIRED_FIELD {
        NAME("name", "Tên bác sỹ"),
        STATUS("status", "Trạng thái"),
        ACADEMIC_ID("academicId", "Học hàm/học vị"),
        MEDICAL_SPECIALITY_ID("medicalSpecialityId", "Chuyên khoa"),
        POSITION_ID("positionId", "Chức vụ");

        private static final Map<String, String> BY_FIELD_NAME = new HashMap<>();

        static {
            for (DOCTOR_REQUIRED_FIELD e : values()) {
                BY_FIELD_NAME.put(e.fieldName, e.displayName);
            }
        }

        private final String fieldName;
        private final String displayName;

        DOCTOR_REQUIRED_FIELD(String fieldName, String displayName) {
            this.fieldName = fieldName;
            this.displayName = displayName;
        }

        public static String getDisplayName(String fieldName) {
            return BY_FIELD_NAME.get(fieldName);
        }

        public static Set<String> getRequiredFields() {
            return BY_FIELD_NAME.keySet();
        }
    }

    interface HIS_STATUS_CODE {
        String SUCCESS = "0";
        String ERROR_400 = "400";
    }

    interface DOCTOR_APPOINTMENT_CONFIG_STATUS {
        Integer DELETED = 0;
        Integer ACTIVE = 1;
        Integer PENDING = 2;
    }

    interface SOCIAL_TYPE {
        String GOOGLE = "google";
    }

    interface DEFAULT_SECRET {
        String PASS_WORD = "12345678";
    }

    interface COUNTRY_VN {
        String CODE = "VN";
    }

    interface TIME_OUT_VNPAY {
        Integer TIME_OUT_MINUTE = 35; // Timeout VNPAY = 35 phut (30 phut nhung de 35 phut cho chac)
    }

    interface QUESTION_TYPE {
        String TEXT = "text";
        String BOOLEAN = "boolean";
    }

    interface SYMPTOM {
        String FEVER = "Sốt";
        String DYSPNOEIC = "Khó thở";
        String SORE_THROAT = "Đau họng";
    }

    interface ANSWER_VALUE {
        String YES = "Có";
        String NO = "Không";
    }

    interface CONFIG_INTEGRATED {
        String HIS_CONNECT_CODE = "HIS";
        String HIS_CONNECT_NAME = "HIS";

        String SOCIAL_INSURANCE_CONNECT_CODE = "SOCIAL_INSURANCE";
        String SOCIAL_INSURANCE_CONNECT_NAME = "SOCIAL INSURANCE";

        String USERNAME_DEFAULT = "lichkham";
        String PASSWORD_DEFAULT = "lichkham";
    }
}
