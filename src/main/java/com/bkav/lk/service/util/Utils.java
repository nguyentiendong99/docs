package com.bkav.lk.service.util;

import com.bkav.lk.config.Constants;
import com.bkav.lk.domain.Authority;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.bkav.lk.util.Constants.*;

public class Utils {
    public static final int SIZE_OF_TRANSACTIONCODE = 8;

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static String removeImage(String[] source, String image) {
        List<String> tmp = Arrays.stream(source).filter(s -> {
            return !s.equalsIgnoreCase(image);
        }).collect(Collectors.toList());
        return String.join(",", tmp);
    }

    public static String getSafeFileName(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != '/' && c != '\\' && c != 0) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String dateUnitFormat(String input) {
        if (input.length() == 1) {
            return "0" + input;
        } else {
            return input;
        }
    }

    public static String genExternalId() {
        //need fix for add partition info
        return UUID.randomUUID().toString();
    }

    public static String getExtension(String fileName) {
        String filename = Utils.getSafeFileName(fileName);
        int last = filename.lastIndexOf('.');
        return filename.substring(last + 1).toLowerCase();
    }

    public static boolean validateExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        int last = fileName.lastIndexOf(Constants.DOT);
        if (last < 0) {
            return false;
        }
        String fileType = fileName.substring(last + 1);
        return Constants.getValidExtensions().contains(fileType.toLowerCase());
    }

    public static String getSQLOperator(int statusOperator) {
        String rs;
        switch (statusOperator) {
            case 1:
                rs = " < ";
                break;
            case 2:
                rs = " > ";
                break;
            case 3:
                rs = " <= ";
                break;
            case 4:
                rs = " >= ";
                break;
            case 5:
                rs = " <> ";
                break;
            default:
                rs = " = ";
                break;
        }
        return rs;
    }

    public static String generateCodeFromId(long id) {
        String str = "123456789qwertyuiopasdfghjklzxcvbnm".toUpperCase();
        String code = "";
        int length = str.length();
        long index = id;
        while (true) {
            int lastIndex = (int) (index % length) - 1;
            if (lastIndex < 0) {
                lastIndex = str.length() - 1;
            }
            code = str.charAt(lastIndex) + code;
            if (index > length) {
                index = index / length;
                if (index < length) {
                    code = str.charAt((int) (index - 1)) + code;
                    break;
                }
            } else {
                break;
            }
        }
        code = StringUtils.leftPad(code, SIZE_OF_TRANSACTIONCODE, RandomStringUtils.randomNumeric(10));
        return code;
    }

    public static String createOrderCode(Long id) {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("MM-yyyy");
        String strDate = dateFormat.format(date);
        return String.format("0%s-%06d", strDate, id);
    }

    public static String getRelationshipName(String relationship) {
        String constantName = null;
        Class<com.bkav.lk.util.Constants.RELATIONSHIP> c = com.bkav.lk.util.Constants.RELATIONSHIP.class;
        for (Field f : c.getDeclaredFields()) {
            int mod = c.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
                try {
                    if (f.get(null).toString().equalsIgnoreCase(relationship)) {
                        constantName = f.getName().toLowerCase();
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error: ", e);
                }
            }
        }
        return constantName;
    }

    public static String getAppointmentCode(Long id) {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
        String strDate = dateFormat.format(date);
        return String.format("%s%06d", strDate, id);
    }

    public static String autoInitializationCode(String name) {
        StringBuilder toFirstCharacterCase = new StringBuilder();
        name = name.replaceAll("\\s\\s+", " ").trim();
        String[] arrW = name.trim().split(" ");
        for (String word : arrW) {
            String[] b = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
            if (StringUtils.indexOfAny(word, b) != -1) {
                toFirstCharacterCase.append(word);
            } else {
                toFirstCharacterCase.append(word.toUpperCase().charAt(0));
            }
        }
        String result = StringUtils.stripAccents(toFirstCharacterCase.toString());
        result = StringUtils.replace(result, "Đ", "D");

        return result;
    }

    public static String autoInitializationCodeForDoctor(String doctorName) {
        StringBuilder toFirstCharacterCase = new StringBuilder();
        doctorName = doctorName.replaceAll("\\s\\s+", " ").trim();
        String[] arrW = doctorName.trim().split(" ");
        for (String word : arrW) {
            String[] b = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
            if (StringUtils.indexOfAny(word, b) != -1) {
                toFirstCharacterCase.append(word);
            } else {
                toFirstCharacterCase.append(word.toUpperCase().charAt(0));
            }
        }
        String result = StringUtils.stripAccents(toFirstCharacterCase.toString());
        result = StringUtils.replace(result, "Đ", "D");

        return result;
    }

    public static String autoInitializationCodeForClinic(String clinicName) {
        StringBuilder toFirstCharacterCase = new StringBuilder();
        clinicName = clinicName.replaceAll("\\s\\s+", " ").trim();
        String[] arrW = clinicName.trim().split(" ");
        for (String word : arrW) {
            String[] b = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
            if (StringUtils.indexOfAny(word, b) != -1) {
                toFirstCharacterCase.append(word);
            } else {
                toFirstCharacterCase.append(word.toUpperCase().charAt(0));
            }
        }
        String result = StringUtils.stripAccents(toFirstCharacterCase.toString());
        result = StringUtils.replace(result, "Đ", "D");

        return "PK-" + result;
    }

    public static String autoInitializationCodeForSystemNotification(String title) {
        StringBuilder toFirstCharacterCase = new StringBuilder();
        title = title.replaceAll("\\s\\s+", " ").trim();
        String[] arrW = title.trim().split(" ");
        for (String word : arrW) {
            if (!word.equals(arrW[arrW.length - 1])) {
                toFirstCharacterCase = toFirstCharacterCase.append(word.toUpperCase().charAt(0));
            } else {
                StringBuilder temporary = new StringBuilder();
                temporary = temporary.append(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
                toFirstCharacterCase = temporary.append(toFirstCharacterCase);

            }
        }
        String result = StringUtils.stripAccents(toFirstCharacterCase.toString());
        result = StringUtils.replace(result, "Đ", "D");

        return result;
    }

    public static String getAuthoritiesNames(Set<Authority> authorities) {
        Set<String> authorityNames = authorities.stream().map(authority -> authority.getName()).collect(Collectors.toSet());
        if (!authorities.isEmpty())
            return StringUtils.join(authorityNames, ",");
        return null;
    }

    public static String getDoctorAppointmentStatusName(Integer id) {
        DOCTOR_APPOINTMENT_STATUS_LIST constants = Arrays.stream(DOCTOR_APPOINTMENT_STATUS_LIST.values())
                .filter(o -> o.getId().equals(id)).findFirst().orElse(null);
        return Objects.nonNull(constants) ? constants.getText() : null;

    }

    public static String autoInitializationCodeForHealthFacilities(String name) {
        StringBuilder toFirstCharacterCase = new StringBuilder();
        String[] arrW = name.trim().split("\\s+");
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(arrW));
        // Toi da 10 ky tu
        if (list.size() > 10) {
            list = list.subList(0, 10);
        }
        // 1 ky tu thi cho them "BV" vao truoc
        if (list.size() == 1) {
            list.add("V");
            list.add("B");
            Collections.reverse(list);
        }
        for (String word : list) {
            String[] b = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
            if (StringUtils.indexOfAny(word, b) != -1) {
                toFirstCharacterCase.append(word);
            } else {
                toFirstCharacterCase.append(word.toUpperCase().charAt(0));
            }
        }
        String result = StringUtils.stripAccents(toFirstCharacterCase.toString());
        result = StringUtils.replace(result, "Đ", "D");
        return result;
    }

    public static String getFeedbackStatusName(Integer status) {
        String constantName = null;
        if (REPORT_STATUS.WAITING.equals(status)) {
            constantName = "Chờ xử lý";
        } else if (REPORT_STATUS.PROCESSING.equals(status)) {
            constantName = "Đang xử lý";
        } else {
            constantName = "Đã xử lý";
        }
        return constantName;
    }

    public static String getStatusName(Integer status) {
        String constantName = null;
        if (ENTITY_STATUS.ACTIVE.equals(status)) {
            constantName = "Đang hoạt động";
        } else if (ENTITY_STATUS.DEACTIVATE.equals(status)) {
            constantName = "Dừng hoạt động";
        } else {
            constantName = "Đã xóa";
        }
        return constantName;
    }

    public static String getGenderName(String gender) {
        String genderName = null;
        if (GENDER.MALE.equalsIgnoreCase(gender)) {
            genderName = "Nam";
        } else if (GENDER.FEMALE.equalsIgnoreCase(gender)) {
            genderName = "Nữ";
        } else {
            genderName = "Khác";
        }
        return genderName;
    }

    public static String getWorkingTime(Integer time) {
        String workingTime = null;
        if (DOCTOR_SCHEDULE_STATUS.MORNING_WORKING.equals(time)) {
            workingTime = "Sáng";
        } else if (DOCTOR_SCHEDULE_STATUS.AFTERNOON_WORKING.equals(time)) {
            workingTime = "Chiều";
        } else if (DOCTOR_SCHEDULE_STATUS.FULL_TIME_WORKING.equals(time)) {
            workingTime = "Cả ngày";
        } else {
            workingTime = "";
        }
        return workingTime;
    }

    public static String getNotificationStatus(Integer status) {
        String notificationStatus = null;
        if (NOTIFICATION_STATUS.HAVE_NOT_NOTIFIED.equals(status)) {
            notificationStatus = "Chưa thông báo";
        } else if (NOTIFICATION_STATUS.NOTIFIED.equals(status)) {
            notificationStatus = "Đã thông báo";
        } else {
            notificationStatus = "";
        }
        return notificationStatus;
    }

    public static Integer getAge(Instant dob) {
        return ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).getYear() - dob.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).getYear();
    }

    /**
     * Compare data between 2 simple object with same type (not contain child object or collection)
     *
     * @param original
     * @param update
     * @param <T>
     * @return
     */
    public static <T> boolean equalOriginal(T original, T update) {
        boolean isOriginal = true;
        Field originalField = null;
        Field updateField = null;

        Field[] originalFields = original.getClass().getDeclaredFields();
        Field[] updateFields = update.getClass().getDeclaredFields();

        try {
            for (int i = 0; i < originalFields.length; i++) {
                originalField = originalFields[i];
                updateField = updateFields[i];
                originalField.setAccessible(true);
                updateField.setAccessible(true);
                if (Objects.isNull(originalField) && Objects.isNull(updateField)) {
                    continue;
                } else if (Objects.nonNull(originalField)) {
                    if (!originalField.get(original).equals(updateField.get(update))) {
                        isOriginal = false;
                    }
                } else {
                    isOriginal = false;
                }

                if (!isOriginal)
                    break;
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return isOriginal;
    }

    public static boolean validTimeRange(String startTimeStr, String endTimeStr) {
        String[] startTime = startTimeStr.split(":");
        String[] endTime = endTimeStr.split(":");
        boolean isValidRangeTime = false;
        try {
            Integer startHour = Integer.parseInt(startTime[0]);
            Integer endHour = Integer.parseInt(endTime[0]);
            Integer startMinutes = Integer.parseInt(startTime[1]);
            Integer endMinutes = Integer.parseInt(endTime[1]);
            if (startHour < endHour) {
                isValidRangeTime = true;
            } else if (startHour == endHour) {
                if (startMinutes < endMinutes) {
                    isValidRangeTime = true;
                }
            }
        } catch (Exception ex) {
            throw new BadRequestAlertException("Time must be format: HH:mm", "", "time-config.wrong-format");
        }
        return isValidRangeTime;
    }

    // Ham chuyen co dau ve khong dau trong Tieng Viet
    public static String removeAccent(String str) {
        str = str.replace("đ", "d");
        String t = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(t).replaceAll("");
    }
}
