package com.bkav.lk.service.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.sun.crypto.provider.SunJCE;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.security.Security;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class DataUtils {

    private static final String PHONE_PATTERN = "^[0-9]*$";
    private static final String PHONE_PATTERN_STANDARD = "(\\b)*((\\+84[3|5|7|8|9][0-9]{8})|(03[2-9][0-9]{7})|(05[6|8|9][0-9]{7})|(07[0|{6-9}][0-9]{7})|(08[8|9|{1-6}][0-9]{7})|(09[0|1|2|3|4|6|7|8|9][0-9]{7}))(\\b)*";
    private static final String saltSHA256 = "1";
    private static final int numberPhoneCanHide = 3;
    private static final String AES = "AES";
    private static final String DES = "DES";

    public DataUtils() {
    }

    public static String getStardardIsdnToIN(String isdn) {
        return isdn == null ? "" : (isdn.startsWith("0") ? "84" + isdn.substring(1) : (isdn.startsWith("84") ? isdn : "84" + isdn));
    }

    public static String addZeroToString(String input, int strLength) {
        String result = input;

        for (int i = 1; i <= strLength - input.length(); ++i) {
            result = "0" + result;
        }

        return result;
    }

    public static String forwardPage(String pageName) {
        return !isNullOrEmpty((CharSequence) pageName) ? "pretty:" + pageName.trim() : "";
    }

    public static boolean isNullOrZero(Long value) {
        return value == null || value.equals(0L);
    }

    public static boolean isNullOrOneNavigate(Long value) {
        return value == null || value.equals(-1L);
    }

    public static boolean isNullOrZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    public static String getHibernateName(String columnName) {
        String temp = columnName.toLowerCase();
        String[] arrs = temp.split("_");
        String method = "";
        String[] var4 = arrs;
        int var5 = arrs.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            String arr = var4[var6];
            method = method + upperFirstChar(arr);
        }

        return method;
    }

    public static String upperFirstChar(String input) {
        return isNullOrEmpty((CharSequence) input) ? input : input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String lowerFirstChar(String input) {
        return isNullOrEmpty((CharSequence) input) ? input : input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    public static Long safeToLong(Object obj1, Long defaultValue) throws Exception {
        Long result = defaultValue;
        if (obj1 != null) {
            if (obj1 instanceof BigDecimal) {
                return Long.valueOf(((BigDecimal) obj1).longValue());
            }

            if (obj1 instanceof BigInteger) {
                return Long.valueOf(((BigInteger) obj1).longValue());
            }

            result = Long.valueOf(Long.parseLong(obj1.toString()));
        }

        return result;
    }

    public static Long safeToLong(Object obj1) {
        try {
            return safeToLong(obj1, Long.valueOf(0L));
        } catch (Exception e) {
            return Long.valueOf(0L);
        }
    }

    public static Double safeToDouble(Object obj1, Double defaultValue) throws Exception {
        Double result = defaultValue;
        if (obj1 != null) {
            double temp = Double.parseDouble(obj1.toString());
            if (temp > 99) {
                result = BigDecimal.valueOf(temp).setScale(0, RoundingMode.HALF_UP).doubleValue();
            } else if (temp > 9 && temp <= 100) {
                result = BigDecimal.valueOf(temp).setScale(1, RoundingMode.HALF_UP).doubleValue();
            } else {
                result = BigDecimal.valueOf(temp).setScale(1, RoundingMode.HALF_UP).doubleValue();
            }
        }

        return result;
    }

    public static Double safeToDouble(Object obj1) {
        try {
            return safeToDouble(obj1, Double.valueOf(0.0D));
        } catch (Exception e) {
            return Double.valueOf(0.0D);
        }
    }

    public static String customFormat(String pattern, double value) {
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        return decimalFormat.format(value);
    }

    public static Short safeToShort(Object obj1, Short defaultValue) throws Exception {
        Short result = defaultValue;
        if (obj1 != null) {
            result = Short.valueOf(Short.parseShort(obj1.toString()));
        }
        return result;
    }

    public static Short safeToShort(Object obj1) throws Exception {
        return safeToShort(obj1, Short.valueOf((short) 0));
    }

    public static int safeToInt(Object obj1, int defaultValue) throws Exception {
        int result = defaultValue;
        if (obj1 != null) {
            result = Integer.parseInt(obj1.toString());
        }

        return result;
    }

    public static int safeToInt(Object obj1) {
        try {
            return safeToInt(obj1, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String safeToString(Object obj1, String defaultValue) {
        return obj1 == null ? defaultValue : obj1.toString();
    }

    public static String safeToLower(String obj1) {
        return obj1 == null ? null : obj1.trim().toLowerCase();
    }

    public static String safeToUpper(String obj1) {
        return obj1 == null ? null : obj1.trim().toUpperCase();
    }

    public static String safeToString(Object obj1) {
        return safeToString(obj1, "");
    }

    public static boolean safeEqual(Long obj1, Long obj2) {
        return obj1 == obj2 ? true : obj1 != null && obj2 != null && obj1.compareTo(obj2) == 0;
    }

    public static boolean safeEqual(BigInteger obj1, BigInteger obj2) {
        return obj1 == obj2 ? true : obj1 != null && obj2 != null && obj1.equals(obj2);
    }

    public static boolean safeEqual(Short obj1, Short obj2) {
        return obj1 == obj2 ? true : obj1 != null && obj2 != null && obj1.compareTo(obj2) == 0;
    }

    public static boolean safeEqual(String obj1, String obj2) {
        return obj1 == obj2 ? true : obj1 != null && obj2 != null && obj1.equals(obj2);
    }

    public static boolean isNullOrEmpty(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNullOrEmpty(Object[] collection) {
        return collection == null || collection.length == 0;
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <T> T defaultIfNull(T object, T defaultValue) {
        return object != null ? object : defaultValue;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static boolean isStringNullOrEmpty(Object obj1) {
        return obj1 == null || "".equals(obj1.toString().trim());
    }

    public static BigDecimal safeToBigDecimal(Object obj1) throws Exception {
        if (obj1 == null) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(obj1.toString());
        }
    }

    public static BigInteger safeToBigInteger(Object obj1, BigInteger defaultValue) throws Exception {
        if (obj1 == null) {
            return defaultValue;
        } else {
            return new BigInteger(obj1.toString());
        }
    }

    public static BigInteger safeToBigInteger(Object obj1) throws Exception {
        if (obj1 instanceof BigInteger) {
            return (BigInteger) obj1;
        } else {
            return new BigInteger(obj1.toString());
        }
    }

    public static BigInteger length(BigInteger from, BigInteger to) {
        return to.subtract(from).add(BigInteger.ONE);
    }

    public static String getFormattedString4Digits(String number, String pattern) throws Exception {
        double amount = Double.parseDouble(number);
        DecimalFormat ex = new DecimalFormat(pattern);
        return ex.format(amount);
    }

    public static Character safeToCharacter(Object value) {
        return safeToCharacter(value, Character.valueOf('0'));
    }

    public static Character safeToCharacter(Object value, Character defaulValue) {
        return value == null ? defaulValue : Character.valueOf(String.valueOf(value).charAt(0));
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static <T> List<T> collectProperty(Collection<?> source, String propertyName, Class<T> returnClass) throws Exception {
        ArrayList propertyValues = Lists.newArrayList();
        String e = "get" + upperFirstChar(propertyName);
        Iterator var5 = source.iterator();

        while (var5.hasNext()) {
            Object x = var5.next();
            Class clazz = x.getClass();
            Method getMethod = clazz.getMethod(e, new Class[0]);
            Object propertyValue = getMethod.invoke(x, new Object[0]);
            if (propertyValue != null && returnClass.isAssignableFrom(propertyValue.getClass())) {
                propertyValues.add(returnClass.cast(propertyValue));
            }
        }

        return propertyValues;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static <T> Set<T> collectUniqueProperty(Collection<?> source, String propertyName, Class<T> returnClass) throws Exception {
        List propertyValues = collectProperty(source, propertyName, returnClass);
        return (Set<T>) Sets.newHashSet(propertyValues);
    }

    public static boolean isDelete(Character isDelete) {
        return isDelete != null && !isNullOrEmpty((CharSequence) String.valueOf(isDelete)) && Objects.equals(isDelete, Integer.valueOf(0));
    }

    public static boolean isActive(Character status, Character isDelete) {
        return Objects.equals(status, Character.valueOf('1')) && (isDelete == null || isDelete.equals(Character.valueOf('0')));
    }

    public static boolean isNullObject(Object obj1) {
        return obj1 == null ? true : (obj1 instanceof String ? isNullOrEmpty((CharSequence) obj1.toString()) : false);
    }

    public static String getProvisionParam(String param, int operator) throws Exception {
        String ex = "";
        if (isNullOrEmpty((CharSequence) param)) {
            return "";
        } else {
            Long lmoney = Long.valueOf(Long.parseLong(param));
            lmoney = Long.valueOf(lmoney.longValue() / (long) operator);
            ex = lmoney.toString();
            return ex;
        }
    }

    public static String toUpper(String input) {
        return isNullOrEmpty((CharSequence) input) ? input : input.toUpperCase();
    }

    public static boolean validateStringByPattern(String value, String regex) {
        if (!isNullOrEmpty((CharSequence) regex) && !isNullOrEmpty((CharSequence) value)) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);
            return matcher.matches();
        } else {
            return false;
        }
    }

    public static Map<String, String> convertStringToMap(String temp, String regex, String regexToMap) {
        if (isNullOrEmpty((CharSequence) temp)) {
            return null;
        } else {
            String[] q = temp.split(regex);
            HashMap lstParam = new HashMap();

            for (int i = 0; i < q.length; ++i) {
                String a = q[i];
                String key = a.substring(0, a.indexOf(regexToMap) < 0 ? 1 : a.indexOf(regexToMap));
                String value = a.substring(a.indexOf(regexToMap) + 1);
                lstParam.put(key.trim(), value.trim());
            }

            return lstParam;
        }
    }

    public static String replaceSpaceSolr(String inputLocation) {
        if (inputLocation != null && !inputLocation.trim().isEmpty()) {
            String[] arr = inputLocation.split(" ");
            String result = "";
            String[] var3 = arr;
            int var4 = arr.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                String s = var3[var5];
                if (!"".equals(s.trim())) {
                    if (!"".equals(result)) {
                        result = result + "\\ ";
                    }

                    result = result + s.trim();
                }
            }

            return result;
        } else {
            return "";
        }
    }

    public static boolean isNumber(String string) {
        return !isNullOrEmpty((CharSequence) string) && string.trim().matches("^\\d+$");
    }

    public static String checkIsdnAddPrefix(String isdn) throws Exception {
        if (!isNullObject(isdn)) {
            String e = isdn.replaceFirst("^0+(?!$)|^84(?!$)", "");
            return e.replaceFirst("^0+(?!$)|^84(?!$)", "");
        } else {
            return null;
        }
    }

    public static String addIsdn84(String isdn) {
        if (!isNullOrEmpty((CharSequence) isdn) && isdn.length() >= 2) {
            if (!"84".equals(isdn.substring(0, 2))) {
                isdn = "0".equals(isdn.substring(0, 1)) ? "84" + isdn.substring(1) : "84" + isdn;
            }

            return isdn;
        } else {
            return isdn;
        }
    }

    public static boolean isValidFraction(String str) throws Exception {
        if (str != null) {
            String[] tmp = str.split("/");
            if (tmp.length == 2 && safeToLong(tmp[0]).longValue() < safeToLong(tmp[1]).longValue()) {
                return true;
            }
        }

        return false;
    }

    public static <T> List<T> subtract(Collection<T> a, Collection<T> b) {
        if (a != null && b != null) {
            ArrayList list = new ArrayList(a);
            list.removeAll(b);
            return list;
        } else {
            return new ArrayList();
        }
    }

    public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
        if (a != null && b != null) {
            ArrayList list = new ArrayList(a);
            list.retainAll(b);
            return list;
        } else {
            return new ArrayList();
        }
    }

    public static String removeStartingZeroes(String number) {
        return isNullOrEmpty((CharSequence) number) ? "" : CharMatcher.anyOf("0").trimLeadingFrom(number);
    }

    public static String trim(String needToTrimString) {
        return needToTrimString == null ? "" : CharMatcher.whitespace().trimFrom(needToTrimString);
    }

    public static String trim(String str, String alt) {
        return str == null ? alt : str.trim();
    }

    public static String trimAndLowerCase(String needToTrimString) {
        return org.apache.commons.lang3.StringUtils.lowerCase(trim(needToTrimString));
    }

    public static BigDecimal defaultIfSmallerThanZero(BigDecimal value) {
        return defaultIfSmallerThanZero(value, BigDecimal.ZERO);
    }

    public static BigDecimal defaultIfSmallerThanZero(BigDecimal value, BigDecimal defaultValue) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0 ? value : defaultValue;
    }

    public static Object convertNullToEmpty(Object value) {
        return value == null ? "" : value;
    }

    public static String getTrailingNumber(String textContainNumber) {
        String prefix = CharMatcher.javaDigit().trimTrailingFrom(textContainNumber);
        return org.apache.commons.lang3.StringUtils.removeStart(textContainNumber, prefix);
    }

    public static String apList2String(List lstAPModel) {
        String result = "";
        if (lstAPModel != null && !isNullOrEmpty((Collection) lstAPModel)) {
            result = Joiner.on("@").skipNulls().join(lstAPModel) + "@";
        }

        return result;
    }

    public static boolean safeEqual(Object obj1, Object obj2) {
        return obj1 != null && obj2 != null && obj2.toString().equals(obj1.toString());
    }

    public static Object convertCommaToDot(Object value) {
        if (!(value instanceof Number)) {
            return value;
        } else {
            DecimalFormat formatter = new DecimalFormat("###,###");
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator('.');
            formatter.setDecimalFormatSymbols(symbols);
            return formatter.format(value);
        }
    }

    public static String formatSerial(int serialLength, BigInteger serial) {
        int prefix = serialLength - serial.toString().length();
        String serialFormat = prefix == 0 ? "%d" : "%0" + String.valueOf(serialLength) + "d";
        return String.format(serialFormat, new Object[]{serial});
    }

    public static String getMimeType(String fileExtension) {
        byte var2 = -1;
        switch (fileExtension.hashCode()) {
            case 97669:
                if ("bmp".equals(fileExtension)) {
                    var2 = 3;
                }
                break;
            case 102340:
                if ("gif".equals(fileExtension)) {
                    var2 = 4;
                }
                break;
            case 105439:
                if ("jpe".equals(fileExtension)) {
                    var2 = 5;
                }
                break;
            case 105441:
                if ("jpg".equals(fileExtension)) {
                    var2 = 2;
                }
                break;
            case 110834:
                if ("pdf".equals(fileExtension)) {
                    var2 = 0;
                }
                break;
            case 111145:
                if ("png".equals(fileExtension)) {
                    var2 = 1;
                }
                break;
            case 3268712:
                if ("jpeg".equals(fileExtension)) {
                    var2 = 6;
                }
        }

        switch (var2) {
            case 0:
                return "application/pdf";
            case 1:
                return "image/png";
            case 2:
                return "image/jpeg";
            case 3:
                return "image/bmp";
            case 4:
                return "image/gif";
            case 5:
                return "image/jpeg";
            case 6:
                return "image/jpeg";
            default:
                return "";
        }
    }

    public static boolean checkPhone(String input) {
        boolean isOk = true;
        return isNullOrEmpty((CharSequence) input) ? isOk : validateStringByPattern(input, "^[0-9]*$");
    }

    public static boolean compareTwoObj(Object oldObj, Object newObj) throws Exception {
        if ((oldObj != null || newObj == null) && (oldObj == null || newObj != null)) {
            if (oldObj == null && newObj == null) {
                return true;
            } else if (!safeEqual(oldObj.getClass().getName(), newObj.getClass().getName())) {
                return false;
            } else {
                Method[] e = oldObj.getClass().getDeclaredMethods();
                Method tempMethod = null;

                for (int i = 0; i < e.length; ++i) {
                    tempMethod = e[i];
                    if (tempMethod.getName().startsWith("get")) {
                        Object oldBO = null;
                        if (oldObj != null) {
                            oldBO = tempMethod.invoke(oldObj, new Object[0]);
                        }

                        Object newBO = tempMethod.invoke(newObj, new Object[0]);
                        String oldValue = "";
                        if (oldBO != null) {
                            if (!(oldBO instanceof Date) && !(oldBO instanceof java.sql.Date)) {
                                oldValue = oldBO.toString();
                            } else {
                                SimpleDateFormat newValue = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                oldValue = newValue.format(oldBO);
                            }
                        }

                        String var11 = "";
                        if (newBO != null) {
                            if (!(newBO instanceof Date) && !(newBO instanceof java.sql.Date)) {
                                var11 = newBO.toString();
                            } else {
                                SimpleDateFormat yyyyMMdd = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                var11 = yyyyMMdd.format(newBO);
                            }
                        }

                        if (!oldValue.equals(var11)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    public static String getEndPoint() throws Exception {
        MBeanServer ex = ManagementFactory.getPlatformMBeanServer();
        Set objs = ex.queryNames(new ObjectName("*:type=Connector,*"), Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
        String hostname = InetAddress.getLocalHost().getHostName();
        InetAddress addresses = InetAddress.getByName(hostname);
        Iterator i = objs.iterator();
        if (i.hasNext()) {
            ObjectName obj = (ObjectName) i.next();
            String port = obj.getKeyProperty("port");
            String host = addresses.getHostAddress();
            return host + ":" + port;
        }
        return "";
    }

    public static String safeSubString(String s, int lastIndex) {
        if (isNullOrEmpty((CharSequence) s)) {
            return s;
        } else {
            lastIndex = lastIndex > s.length() ? s.length() : lastIndex;
            return s.substring(0, lastIndex);
        }
    }

    public static boolean checkDigit(String str) {
        return str.matches("(\\d+)");
    }

    public static String encryptByAES(String str, String strKey) throws Exception {
        Security.addProvider(new SunJCE());
        byte[] ex = getKeyAES(strKey.getBytes());
        SecretKeySpec keySpec = new SecretKeySpec(ex, AES);
        Cipher ecipher = Cipher.getInstance(AES);
        ecipher.init(1, keySpec);
        byte[] utf8 = str.getBytes("UTF8");
        byte[] enc = ecipher.doFinal(utf8);
        return Base64.getMimeEncoder().encodeToString(enc);
    }

    public static String decryptByAES(String str, String strKey) throws Exception {
        Security.addProvider(new SunJCE());
        byte[] ex = getKeyAES(strKey.getBytes());
        SecretKeySpec keySpec = new SecretKeySpec(ex, AES);
        Cipher dcipher = Cipher.getInstance(AES);
        dcipher.init(2, keySpec);
        byte[] dec = Base64.getDecoder().decode(str);
        byte[] utf8 = dcipher.doFinal(dec);
        return new String(utf8, "UTF8");
    }

//    public static String encryptionCodeSHA256(String input, String salt) throws Exception {
//        MessageDigest ex = MessageDigest.getInstance("SHA-256");
//        byte[] key = getKeyAES(salt.getBytes());
//        ex.reset();
//        ex.update(key);
//        return org.apache.commons.codec.binary.Base64.(ex.digest(input.getBytes("UTF-8")));
//    }

    public static String encryptByDES64(String str, String strKey) throws Exception {
        Security.addProvider(new SunJCE());
        byte[] ex = getKeyDES(strKey.getBytes());
        SecretKeySpec keySpec = new SecretKeySpec(ex, DES);
        Cipher ecipher = Cipher.getInstance(DES);
        ecipher.init(1, keySpec);
        byte[] utf8 = str.getBytes("UTF8");
        byte[] enc = ecipher.doFinal(utf8);
        return Base64.getMimeEncoder().encodeToString(enc);
    }

    public static String decryptByDES64(String str, String strKey) throws Exception {
        Security.addProvider(new SunJCE());
        byte[] ex = getKeyDES(strKey.getBytes());
        SecretKeySpec keySpec = new SecretKeySpec(ex, DES);
        Cipher dcipher = Cipher.getInstance(DES);
        dcipher.init(2, keySpec);
        byte[] dec = Base64.getDecoder().decode(str);
        byte[] utf8 = dcipher.doFinal(dec);
        return new String(utf8, "UTF8");
    }

    private static byte[] getKeyAES(byte[] arrBTmp) throws Exception {
        byte[] arrB = new byte[16];

        for (int i = 0; i < arrBTmp.length && i < arrB.length; ++i) {
            arrB[i] = arrBTmp[i];
        }

        return arrB;
    }

    private static byte[] getKeyDES(byte[] arrBTmp) throws Exception {
        byte[] arrB = new byte[8];

        for (int i = 0; i < arrBTmp.length && i < arrB.length; ++i) {
            arrB[i] = arrBTmp[i];
        }

        return arrB;
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

    public static String detectEncodePhone(String value) {
        Pattern pattern = Pattern.compile(PHONE_PATTERN_STANDARD);
        Matcher matcher = pattern.matcher(value);
        int count = 0;
        while (matcher.find()) {
            if (matcher.group(count) == null) {
                continue;
            }
            value = encodeStringChildren(value, matcher.group(count));
//            count++;
//            return matcher.replaceAll("**********");
        }
        return value;
    }

    public static boolean isDetectPhone(String value) {
        Pattern pattern = Pattern.compile(PHONE_PATTERN_STANDARD);
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }

    public static String encodeStringChildren(String originValue, String stringFind) {
        if (stringFind != null && stringFind.trim().length() >= 10) {
            String start = stringFind.substring(0, stringFind.length() - DataUtils.numberPhoneCanHide);
            String end = stringFind.substring(stringFind.length() - DataUtils.numberPhoneCanHide);
            StringBuilder resultEncode = new StringBuilder();
            for (char ch : end.toCharArray()) {
                resultEncode.append("*");
            }
            return originValue.replace(stringFind, start + resultEncode.toString());
        }
        return originValue;
    }
}
