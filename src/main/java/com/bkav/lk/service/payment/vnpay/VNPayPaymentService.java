package com.bkav.lk.service.payment.vnpay;

import com.bkav.lk.domain.Doctor;
import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.domain.VnpayInformation;
import com.bkav.lk.dto.DoctorAppointmentConfigurationDTO;
import com.bkav.lk.dto.DoctorAppointmentDTO;
import com.bkav.lk.dto.TransactionDTO;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.DoctorAppointmentMapper;
import com.bkav.lk.service.payment.vnpay.model.VNPayConfirmResult;
import com.bkav.lk.service.payment.vnpay.model.VNPayRequest;
import com.bkav.lk.service.payment.vnpay.model.VNPayResult;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import javax.servlet.ServletException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


@Service
public class VNPayPaymentService {
    private final Logger log = LoggerFactory.getLogger(VNPayPaymentService.class);
    @Value("${vnpay.payUrl}")
    private String payUrl; // Url thanh toán môi trường TEST
    @Value("${vnpay.callbackUrl}")
    private String callbackUrl;
    @Value("${vnpay.hashSecret}")
    private String hashSecret; // Secret Key/Chuỗi bí mật tạo checksum
    @Value("${vnpay.tmnCode}")
    private String tmnCode; // Terminal ID/Mã Website*/

    @Autowired
    private DoctorAppointmentService doctorAppointmentService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DoctorAppointmentConfigurationService doctorAppointmentConfigurationService;

    @Autowired
    private VnpayInformationService vnpayInformationService;

    @Autowired
    private DoctorScheduleTimeService doctorScheduleTimeService;

    @Autowired
    private DoctorAppointmentMapper doctorAppointmentMapper;

    public VNPayResult process(VNPayRequest req, Long healthFacilityId, Integer source) throws ServletException, IOException {
        String vnp_TmnCode = "";
        String vnp_hashSecret = "";
        // Tuy theo tung benh vien co cac tai khoan VNPay khac nhau
        VnpayInformation vnpayInformation = vnpayInformationService.findByHealthFacilityId(healthFacilityId);
        if (vnpayInformation != null) {
            vnp_TmnCode = vnpayInformation.getTmnCode();
            vnp_hashSecret = vnpayInformation.getHashSecret();
        } else {
            vnp_TmnCode = tmnCode;
            vnp_hashSecret = hashSecret;
        }

        String vnp_Version = "2.0.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = req.getOrderInfo();
        String orderType = req.getOrderType();
        String vnp_TxnRef = Utils.getRandomNumber(8);
        String vnp_IpAddr = req.getClientIp();
        String vnp_TransactionNo = vnp_TxnRef;
        BigDecimal amount = req.getAmount().multiply(BigDecimal.valueOf(100));
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount.longValue()));
        vnp_Params.put("vnp_CurrCode", "VND");
        String bank_code = req.getBankCode();
        if (bank_code != null && !bank_code.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bank_code);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        String locate = req.getLangCode();
        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }
        String callbackUrlStr = "";
        // Dat tu Web
        if (source == Constants.SOURCE_DOCTOR_APPOINTMENT.WEB) {
            callbackUrlStr = callbackUrl + "/" + healthFacilityId + "/booking/payment";
        } else { // Dat tu Mobile
            callbackUrlStr = callbackUrl + "/" + healthFacilityId + "/booking";
        }
        vnp_Params.put("vnp_ReturnUrl", callbackUrlStr);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        Date dt = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(dt);
        String vnp_CreateDate = dateString;
        String vnp_TransDate = vnp_CreateDate;
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", Long.toString(Long.parseLong(vnp_CreateDate) + 1000));
        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Utils.Sha256(vnp_hashSecret + hashData.toString());
        queryUrl += "&vnp_SecureHashType=SHA256&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = payUrl + "?" + queryUrl;
        VNPayResult vnPayResult = new VNPayResult();
        vnPayResult.setCode("00");
        vnPayResult.setMessage("success");
        vnPayResult.setData(paymentUrl);
        TransactionDTO updateTxnRef = transactionService.findByBookCode(req.getOrderInfo());
        updateTxnRef.setVnpTxnRef(vnp_TxnRef);
        transactionService.save(updateTxnRef);
        return vnPayResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public VNPayConfirmResult processResult(MultiValueMap<String, String> queryParams) {
        VNPayConfirmResult confirmResult = new VNPayConfirmResult();
        try {
            Map<String, String> fields = queryParams.toSingleValueMap();
            String vnp_SecureHash = fields.get("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");
            String signValue = Utils.hashAllFields(hashSecret, fields);
            String responseCode = fields.get("vnp_ResponseCode");
            String transactionNo = fields.get("vnp_TransactionNo");
            String bookingCode = fields.get("vnp_OrderInfo");
            Instant payDate = DateUtils.parseToInstant(fields.get("vnp_PayDate"), "yyyyMMddHHmmss");

            if (!signValue.equals(vnp_SecureHash)) {
                confirmResult.setRspCode("97");
                confirmResult.setMessage("Invalid Signature");
                log.info("Response={}", confirmResult);
                return confirmResult;
            }

            TransactionDTO transactionDTO = transactionService.findByBookCode(bookingCode);
            DoctorAppointmentDTO doctorAppointmentDTO = doctorAppointmentService.findTopByBookingCode(bookingCode);

            if (doctorAppointmentDTO.getOldBookingCode() == null) { // Hoan toan moi
                confirmResult = this.create(doctorAppointmentDTO, transactionDTO, responseCode, transactionNo, payDate);
            } else { // Update
                confirmResult = this.update(doctorAppointmentDTO, transactionDTO, responseCode, transactionNo, payDate);
            }
        } catch (Exception exception) {
            confirmResult.setRspCode("99");
            confirmResult.setMessage("System Error");
            log.error("vnpay error", exception);
        }
        log.info("Response={}", confirmResult);
        return confirmResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public VNPayConfirmResult create(DoctorAppointmentDTO doctorAppointmentDTO, TransactionDTO transactionDTO,
                                     String responseCode, String transactionNo,
                                     Instant payDate) {

        VNPayConfirmResult confirmResult = new VNPayConfirmResult();

        if (!Constants.PAYMENT_STATUS.REQUEST.equals(transactionDTO.getPaymentStatus())
                || !Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST.equals(doctorAppointmentDTO.getStatus())) {
            confirmResult.setRspCode("02");
            confirmResult.setMessage("Order already confirmed");
            log.info("Response={}", confirmResult);
            return confirmResult;
        }

        if (!"00".equals(responseCode)) {
            transactionDTO.setPaymentStatus(Constants.PAYMENT_STATUS.FAILED);
            transactionDTO.setUserId(doctorAppointmentDTO.getUserId());
            transactionDTO.setTransactionCode(com.bkav.lk.service.util.Utils.generateCodeFromId(doctorAppointmentDTO.getUserId()));
            transactionService.save(transactionDTO);
            log.info("Response={}", confirmResult);
            return confirmResult;
        }
        if (Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST.equals(doctorAppointmentDTO.getStatus())) {
            DoctorAppointmentConfigurationDTO configAppointment = doctorAppointmentConfigurationService.findOne(doctorAppointmentDTO.getHealthFacilityId(), Constants.ENTITY_STATUS.ACTIVE);
            if (configAppointment == null) {
                doctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE);
            } else {
                if (Constants.DOCTOR_APPOINTMENT_CONFIG.UN_CONNECT_WITH_HIS_APPROVAL_AUTOMATIC.equals(configAppointment.getConnectWithHis())) {
                    doctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
                } else {
                    doctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE);
                }
            }
            doctorAppointmentDTO.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_SUCCESS);
            doctorAppointmentService.save(doctorAppointmentDTO);
        }
        if (Constants.PAYMENT_STATUS.REQUEST.equals(transactionDTO.getPaymentStatus())) {
            transactionDTO.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_SUCCESS);
            transactionDTO.setUserId(doctorAppointmentDTO.getUserId());
            transactionDTO.setTransactionCode(com.bkav.lk.service.util.Utils.generateCodeFromId(doctorAppointmentDTO.getUserId()));
            transactionDTO.setTransactionNo(transactionNo);
            transactionDTO.setResponseCode(responseCode);
            transactionDTO.setPayDate(payDate);
            transactionService.save(transactionDTO);
        }
        confirmResult.setRspCode("00");
        confirmResult.setMessage("Success");
        return confirmResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public VNPayConfirmResult update(DoctorAppointmentDTO newDoctorAppointmentDTO, TransactionDTO newTransactionDTO,
                                     String responseCode, String transactionNo,
                                     Instant payDate) {

        VNPayConfirmResult confirmResult = new VNPayConfirmResult();

        if (!Constants.PAYMENT_STATUS.REQUEST.equals(newTransactionDTO.getPaymentStatus())
                || !Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST.equals(newDoctorAppointmentDTO.getStatus())) {
            confirmResult.setRspCode("02");
            confirmResult.setMessage("Order already confirmed");
            log.info("Response={}", confirmResult);
            return confirmResult;
        }
        // Tim lich cu -> Huy lich cu
        DoctorAppointmentDTO oldDoctorAppointmentDTO = doctorAppointmentService.findTopByBookingCode(newDoctorAppointmentDTO.getOldBookingCode());
        if (oldDoctorAppointmentDTO.getPaymentStatus().equals(Constants.PAYMENT_STATUS.PAID_SUCCESS)) { // Lich da thanh toan -> VNPAY
            oldDoctorAppointmentDTO.setPaymentStatus(Constants.PAYMENT_STATUS.REFUNDED_WATTING);
        } else { // Lich chua thanh toan -> Thanh toan tai quay
            oldDoctorAppointmentDTO.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_WATING);
        }
        oldDoctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.DELETE_DOCTOR_APPOINTMENT_TEMP_INVALID);
        doctorAppointmentService.save(oldDoctorAppointmentDTO);

        // Tim Transaction cu -> Huy Transaction cu
        TransactionDTO oldTransactionDTO = transactionService.findByBookCode(newDoctorAppointmentDTO.getOldBookingCode());
        if (oldTransactionDTO != null) {
            oldTransactionDTO.setTypeCode(Constants.TRANSACTION_TYPE_CODE.WITHDRAW);
            oldTransactionDTO.setPaymentStatus(Constants.PAYMENT_STATUS.REFUNDED_WATTING);
            oldTransactionDTO.setContent("Hoàn tiền: " + oldTransactionDTO.getTotalAmount() + " VNĐ");
            transactionService.save(oldTransactionDTO);
        }

        // Luu lai lich moi
        if (Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST.equals(newDoctorAppointmentDTO.getStatus())) {
            DoctorAppointmentConfigurationDTO configAppointment = doctorAppointmentConfigurationService.findOne(newDoctorAppointmentDTO.getHealthFacilityId(), Constants.ENTITY_STATUS.ACTIVE);
            if (configAppointment == null) {
                newDoctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE);
            } else {
                if (Constants.DOCTOR_APPOINTMENT_CONFIG.UN_CONNECT_WITH_HIS_APPROVAL_AUTOMATIC.equals(configAppointment.getConnectWithHis())) {
                    newDoctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
                } else {
                    newDoctorAppointmentDTO.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE);
                }
            }
            newDoctorAppointmentDTO.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_SUCCESS);
            doctorAppointmentService.save(newDoctorAppointmentDTO);
        }
        // Luu lai Transaction moi
        if (Constants.PAYMENT_STATUS.REQUEST.equals(newTransactionDTO.getPaymentStatus())) {
            newTransactionDTO.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_SUCCESS);
            newTransactionDTO.setUserId(newDoctorAppointmentDTO.getUserId());
            newTransactionDTO.setTransactionCode(com.bkav.lk.service.util.Utils.generateCodeFromId(newDoctorAppointmentDTO.getUserId()));
            newTransactionDTO.setTransactionNo(transactionNo);
            newTransactionDTO.setResponseCode(responseCode);
            newTransactionDTO.setPayDate(payDate);
            transactionService.save(newTransactionDTO);
        }
        // Tru di luot cu neu khac khung gio
        List<DoctorAppointment> listOld = getListByIds(Collections.singletonList(oldDoctorAppointmentDTO.getId()));
        if (!newDoctorAppointmentDTO.getStartTime().equals(oldDoctorAppointmentDTO.getStartTime())) {
            listOld.forEach(item -> {
                if (Objects.isNull(item.getDoctor()) || item.getDoctor().getId() == null) {
                    doctorScheduleTimeService.minusSubscriptions(null, item.getStartTime(), item.getEndTime(), 1, item.getHealthFacilityId());
                } else {
                    doctorScheduleTimeService.minusSubscriptions(item.getDoctor().getId(), item.getStartTime(), item.getEndTime(), 1, item.getHealthFacilityId());
                }
            });
        }
        confirmResult.setRspCode("00");
        confirmResult.setMessage("Success");
        return confirmResult;
    }


    private List<DoctorAppointment> getListByIds(List<Long> ids) {
        List<DoctorAppointment> listOldDoctorAppointment = new ArrayList<>();
        for (Long id : ids) {
            Optional<DoctorAppointmentDTO> optional = doctorAppointmentService.findOne(id);
            if (optional.isPresent()) {
                DoctorAppointment doctorAppointment = doctorAppointmentMapper.toEntity(optional.get());
                Doctor doctor = new Doctor();
                if (optional.get().getDoctorId() != null) {
                    doctor.setId(optional.get().getDoctorId());
                    doctorAppointment.setDoctor(doctor);
                }
                listOldDoctorAppointment.add(doctorAppointment);
            }
        }
        return listOldDoctorAppointment;
    }
}
