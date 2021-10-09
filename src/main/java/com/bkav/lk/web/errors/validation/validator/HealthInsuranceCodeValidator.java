package com.bkav.lk.web.errors.validation.validator;

public class HealthInsuranceCodeValidator {

    private static final String HEALTH_INSURANCE_CODE_REGEX_PATTERN =
            "^(DN|HX|CH|NN|TK|HC|XK|HT|TB|NO|CT|XB|TN|CS|QN|CA|CY|XN|MS|CC" +
                    "|CK|CB|KC|HD|TE|BT|HN|DT|DK|XD|TS|TC|TQ|TV|TA|TY|HG|LS|PV|CN|HS|SV|GB|GD)[1-5][0-9]{12}$";

    public static final String ERROR_DEFAULT_MESSAGE = "Wrong health insurance code format";

    public static boolean isValid(String s) {
        return s == null || s.matches(HEALTH_INSURANCE_CODE_REGEX_PATTERN);
    }
}
