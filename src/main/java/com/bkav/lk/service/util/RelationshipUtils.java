package com.bkav.lk.service.util;

import com.bkav.lk.domain.PatientRecord;
import com.bkav.lk.util.Constants;

public class RelationshipUtils {

//    public static String getOppositeRelationshipName(String relationship, PatientRecord record) {
//        String negativeRelationship = null;
//        if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(relationship)) {
//            if (Constants.GENDER.MALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.SON;
//            } else if (Constants.GENDER.FEMALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.DAUGHTER;
//            } else {
//                negativeRelationship = Constants.RELATIONSHIP.OTHER;
//            }
//        } else if (Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(relationship)) {
//            if (Constants.GENDER.MALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.SON;
//            } else if (Constants.GENDER.FEMALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.DAUGHTER;
//            } else {
//                negativeRelationship = Constants.RELATIONSHIP.OTHER;
//            }
//        } else if (Constants.RELATIONSHIP.GRANDFATHER.equalsIgnoreCase(relationship)) {
//            negativeRelationship = Constants.RELATIONSHIP.OTHER;
//        } else if (Constants.RELATIONSHIP.GRANDMOTHER.equalsIgnoreCase(relationship)) {
//            negativeRelationship = Constants.RELATIONSHIP.OTHER;
//        } else if (Constants.RELATIONSHIP.SON.equalsIgnoreCase(relationship)) {
//            if (Constants.GENDER.MALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.FATHER;
//            } else if (Constants.GENDER.FEMALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.MOTHER;
//            } else {
//                negativeRelationship = Constants.RELATIONSHIP.OTHER;
//            }
//        } else if (Constants.RELATIONSHIP.DAUGHTER.equalsIgnoreCase(relationship)) {
//            if (Constants.GENDER.MALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.FATHER;
//            } else if (Constants.GENDER.FEMALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.MOTHER;
//            } else {
//                negativeRelationship = Constants.RELATIONSHIP.OTHER;
//            }
//        } else if (Constants.RELATIONSHIP.HUSBAND.equalsIgnoreCase(relationship)) {
//            negativeRelationship = Constants.RELATIONSHIP.WIFE;
//        } else if (Constants.RELATIONSHIP.WIFE.equalsIgnoreCase(relationship)) {
//            negativeRelationship = Constants.RELATIONSHIP.HUSBAND;
//        } else if (Constants.RELATIONSHIP.OLDER_BROTHER.equalsIgnoreCase(relationship)) {
//            if (Constants.GENDER.MALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.YOUNGER_BROTHER;
//            } else if (Constants.GENDER.FEMALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.YOUNGER_SISTER;
//            } else {
//                negativeRelationship = Constants.RELATIONSHIP.OTHER;
//            }
//        } else if (Constants.RELATIONSHIP.OLDER_SISTER.equalsIgnoreCase(relationship)) {
//            if (Constants.GENDER.MALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.YOUNGER_BROTHER;
//            } else if (Constants.GENDER.FEMALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.YOUNGER_SISTER;
//            } else {
//                negativeRelationship = Constants.RELATIONSHIP.OTHER;
//            }
//        } else if (Constants.RELATIONSHIP.YOUNGER_BROTHER.equalsIgnoreCase(relationship)) {
//            if (Constants.GENDER.MALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.OLDER_BROTHER;
//            } else if (Constants.GENDER.FEMALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.OLDER_SISTER;
//            } else {
//                negativeRelationship = Constants.RELATIONSHIP.OTHER;
//            }
//        } else if (Constants.RELATIONSHIP.YOUNGER_SISTER.equalsIgnoreCase(relationship)) {
//            if (Constants.GENDER.MALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.OLDER_BROTHER;
//            } else if (Constants.GENDER.FEMALE.equalsIgnoreCase(record.getGender())) {
//                negativeRelationship = Constants.RELATIONSHIP.OLDER_SISTER;
//            } else {
//                negativeRelationship = Constants.RELATIONSHIP.OTHER;
//            }
//        } else if (Constants.RELATIONSHIP.UNCLE.equalsIgnoreCase(relationship)) {
//            negativeRelationship = Constants.RELATIONSHIP.OTHER;
//        } else if (Constants.RELATIONSHIP.AUNT.equalsIgnoreCase(relationship)) {
//            negativeRelationship = Constants.RELATIONSHIP.OTHER;
//        } else {
//            negativeRelationship = Constants.RELATIONSHIP.OTHER;
//        }
//        return negativeRelationship;
//    }
//
//    public static String getRelativeRelationshipName(String currentRelativeRelationship,
//                                                     String newRelativeRelationship,
//                                                     PatientRecord currentRelativePatientRecord,
//                                                     PatientRecord newRelativePatientRecord) {
//        String relativeRelationship = null;
//        Integer gender = Constants.GENDER.MALE.equalsIgnoreCase(currentRelativePatientRecord.getGender()) ? 1 : 0;
//        // level:
//        // first (grant-parent); second (parent); third (uncle-aunt);
//        // fourth (brother-sister); fifth (husband-wife); sixth (son-daughter);
//        if (Constants.RELATIONSHIP.GRANDFATHER.equalsIgnoreCase(currentRelativeRelationship)
//                || Constants.RELATIONSHIP.GRANDMOTHER.equalsIgnoreCase(currentRelativeRelationship)) {
//            relativeRelationship = getFirstLevelRelativeRelationshipName(gender, newRelativeRelationship);
//        } else if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(currentRelativeRelationship)
//                || Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(currentRelativeRelationship)) {
//            relativeRelationship = getSecondLevelRelativeRelationshipName(gender, newRelativeRelationship,
//                    currentRelativePatientRecord, newRelativePatientRecord);
//        } else if (Constants.RELATIONSHIP.UNCLE.equalsIgnoreCase(currentRelativeRelationship)
//                || Constants.RELATIONSHIP.AUNT.equalsIgnoreCase(currentRelativeRelationship)) {
//            relativeRelationship = getThirdLevelRelativeRelationshipName(gender, newRelativeRelationship,
//                    currentRelativePatientRecord, newRelativePatientRecord);
//        } else if (Constants.RELATIONSHIP.YOUNGER_BROTHER.equalsIgnoreCase(currentRelativeRelationship)
//                || Constants.RELATIONSHIP.YOUNGER_SISTER.equalsIgnoreCase(currentRelativeRelationship)
//                || Constants.RELATIONSHIP.OLDER_BROTHER.equalsIgnoreCase(currentRelativeRelationship)
//                || Constants.RELATIONSHIP.OLDER_SISTER.equalsIgnoreCase(currentRelativeRelationship)) {
//            relativeRelationship = getFourthLevelRelativeRelationshipName(gender, newRelativeRelationship,
//                    currentRelativePatientRecord, newRelativePatientRecord);
//        } else if (Constants.RELATIONSHIP.HUSBAND.equalsIgnoreCase(currentRelativeRelationship)
//                || Constants.RELATIONSHIP.WIFE.equalsIgnoreCase(currentRelativeRelationship)) {
//            relativeRelationship = getFifthRelativeRelationshipName(gender, newRelativeRelationship,
//                    currentRelativePatientRecord, newRelativePatientRecord);
//        } else if (Constants.RELATIONSHIP.SON.equalsIgnoreCase(currentRelativeRelationship)
//                || Constants.RELATIONSHIP.DAUGHTER.equalsIgnoreCase(currentRelativeRelationship)) {
//            relativeRelationship = getSixthRelativeRelationshipName(newRelativeRelationship,
//                    currentRelativePatientRecord, newRelativePatientRecord);
//        } else {
//            relativeRelationship = Constants.RELATIONSHIP.OTHER;
//        }
//        return relativeRelationship;
//    }
//
//    private static String getFirstLevelRelativeRelationshipName(
//            Integer gender, String newRelativeRelationship) {
//        String relativeRelationship = null;
//        if (Constants.RELATIONSHIP.GRANDFATHER.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.GRANDMOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            if (gender == 1) {
//                if (Constants.RELATIONSHIP.GRANDFATHER.equalsIgnoreCase(newRelativeRelationship)) {
//                    relativeRelationship = Constants.RELATIONSHIP.OTHER;
//                } else if (Constants.RELATIONSHIP.GRANDMOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//                    relativeRelationship = Constants.RELATIONSHIP.HUSBAND;
//                }
//            } else {
//                if (Constants.RELATIONSHIP.GRANDFATHER.equalsIgnoreCase(newRelativeRelationship)) {
//                    relativeRelationship = Constants.RELATIONSHIP.WIFE;
//                } else if (Constants.RELATIONSHIP.GRANDMOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//                    relativeRelationship = Constants.RELATIONSHIP.OTHER;
//                }
//            }
//        } else if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.SON;
//        } else if (Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.DAUGHTER;
//        } else if (Constants.RELATIONSHIP.UNCLE.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.SON;
//        } else if (Constants.RELATIONSHIP.AUNT.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.DAUGHTER;
//        } else {
//            relativeRelationship = Constants.RELATIONSHIP.OTHER;
//        }
//        return relativeRelationship;
//    }
//
//    private static String getSecondLevelRelativeRelationshipName(
//            Integer gender, String newRelativeRelationship,
//            PatientRecord currentRelativePatientRecord, PatientRecord newRelativePatientRecord) {
//        String relativeRelationship = null;
//        if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            if (gender == 1) {
//                if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(newRelativeRelationship)) {
//                    relativeRelationship = Constants.RELATIONSHIP.OTHER;
//                } else if (Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//                    relativeRelationship = Constants.RELATIONSHIP.HUSBAND;
//                }
//            } else {
//                if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(newRelativeRelationship)) {
//                    relativeRelationship = Constants.RELATIONSHIP.WIFE;
//                } else if (Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//                    relativeRelationship = Constants.RELATIONSHIP.OTHER;
//                }
//            }
//        } else if (Constants.RELATIONSHIP.GRANDFATHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.FATHER;
//        } else if (Constants.RELATIONSHIP.GRANDMOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.MOTHER;
//        } else if (Constants.RELATIONSHIP.HUSBAND.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.SON;
//        } else if (Constants.RELATIONSHIP.WIFE.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.DAUGHTER;
//        } else if (Constants.RELATIONSHIP.OLDER_BROTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.SON;
//        } else if (Constants.RELATIONSHIP.OLDER_SISTER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.DAUGHTER;
//        } else if (Constants.RELATIONSHIP.YOUNGER_BROTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.SON;
//        } else if (Constants.RELATIONSHIP.YOUNGER_SISTER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.DAUGHTER;
//        } else if (Constants.RELATIONSHIP.UNCLE.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob()))
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_BROTHER;
//            else
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_BROTHER;
//        } else if (Constants.RELATIONSHIP.AUNT.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob()))
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_SISTER;
//            else
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_SISTER;
//        } else {
//            relativeRelationship = Constants.RELATIONSHIP.OTHER;
//        }
//        return relativeRelationship;
//    }
//
//    private static String getThirdLevelRelativeRelationshipName(
//            Integer gender, String newRelativeRelationship,
//            PatientRecord currentRelativePatientRecord, PatientRecord newRelativePatientRecord) {
//        String relativeRelationship = null;
//        if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob()))
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_BROTHER;
//            else
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_BROTHER;
//        } else if (Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob()))
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_SISTER;
//            else
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_SISTER;
//        } else if (Constants.RELATIONSHIP.GRANDFATHER.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.GRANDMOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = gender == 1 ? Constants.RELATIONSHIP.SON : Constants.RELATIONSHIP.DAUGHTER;
//        } else if (Constants.RELATIONSHIP.HUSBAND.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.WIFE.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = gender == 1 ? Constants.RELATIONSHIP.UNCLE : Constants.RELATIONSHIP.AUNT;
//        } else if (Constants.RELATIONSHIP.UNCLE.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob()))
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_BROTHER;
//            else
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_BROTHER;
//        } else if (Constants.RELATIONSHIP.AUNT.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob()))
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_SISTER;
//            else
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_SISTER;
//        } else if (Constants.RELATIONSHIP.OTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.OTHER;
//        }
//        return relativeRelationship;
//    }
//
//    private static String getFourthLevelRelativeRelationshipName(
//            Integer gender, String newRelativeRelationship,
//            PatientRecord currentRelativePatientRecord, PatientRecord newRelativePatientRecord) {
//        String relativeRelationship = null;
//        if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = gender == 1 ? Constants.RELATIONSHIP.SON : Constants.RELATIONSHIP.DAUGHTER;
//        } else if (Constants.RELATIONSHIP.SON.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.SON;
//        } else if (Constants.RELATIONSHIP.DAUGHTER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.DAUGHTER;
//        } else if (Constants.RELATIONSHIP.OLDER_BROTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.OLDER_BROTHER;
//        } else if (Constants.RELATIONSHIP.OLDER_SISTER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.OLDER_SISTER;
//        } else if (Constants.RELATIONSHIP.YOUNGER_BROTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.YOUNGER_BROTHER;
//        } else if (Constants.RELATIONSHIP.YOUNGER_SISTER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.YOUNGER_SISTER;
//        } else if (Constants.RELATIONSHIP.UNCLE.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.UNCLE;
//        } else if (Constants.RELATIONSHIP.AUNT.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.AUNT;
//        } else {
//            relativeRelationship = Constants.RELATIONSHIP.OTHER;
//        }
//        return relativeRelationship;
//    }
//
//    private static String getFifthRelativeRelationshipName(
//            Integer gender, String newRelativeRelationship,
//            PatientRecord currentRelativePatientRecord, PatientRecord newRelativePatientRecord) {
//        String relativeRelationship = null;
//        if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = gender == 1 ? Constants.RELATIONSHIP.SON : Constants.RELATIONSHIP.DAUGHTER;
//        } else if (Constants.RELATIONSHIP.SON.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.DAUGHTER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = gender == 1 ? Constants.RELATIONSHIP.UNCLE : Constants.RELATIONSHIP.AUNT;
//        } else if (Constants.RELATIONSHIP.HUSBAND.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.WIFE.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob())) {
//                relativeRelationship = gender == 1 ? Constants.RELATIONSHIP.OLDER_BROTHER : Constants.RELATIONSHIP.OLDER_SISTER;
//            } else {
//                relativeRelationship = gender == 1 ? Constants.RELATIONSHIP.YOUNGER_BROTHER : Constants.RELATIONSHIP.YOUNGER_SISTER;
//            }
//        } else if (Constants.RELATIONSHIP.OLDER_BROTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob())) {
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_BROTHER;
//            } else {
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_BROTHER;
//            }
//        } else if (Constants.RELATIONSHIP.OLDER_SISTER.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob())) {
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_SISTER;
//            } else {
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_SISTER;
//            }
//        } else if (Constants.RELATIONSHIP.YOUNGER_BROTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob())) {
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_BROTHER;
//            } else {
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_BROTHER;
//            }
//        } else if (Constants.RELATIONSHIP.YOUNGER_SISTER.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob())) {
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_SISTER;
//            } else {
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_SISTER;
//            }
//        } else if (Constants.RELATIONSHIP.UNCLE.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.UNCLE;
//        } else if (Constants.RELATIONSHIP.AUNT.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.AUNT;
//        } else {
//            relativeRelationship = Constants.RELATIONSHIP.OTHER;
//        }
//        return relativeRelationship;
//    }
//
//    private static String getSixthRelativeRelationshipName(String newRelativeRelationship,
//                                                           PatientRecord currentRelativePatientRecord, PatientRecord newRelativePatientRecord) {
//        String relativeRelationship = null;
//        if (Constants.RELATIONSHIP.FATHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.GRANDFATHER;
//        } else if (Constants.RELATIONSHIP.MOTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.GRANDMOTHER;
//        } else if (Constants.RELATIONSHIP.SON.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob())) {
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_BROTHER;
//            } else {
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_BROTHER;
//            }
//        } else if (Constants.RELATIONSHIP.DAUGHTER.equalsIgnoreCase(newRelativeRelationship)) {
//            if (currentRelativePatientRecord.getDob().isBefore(newRelativePatientRecord.getDob())) {
//                relativeRelationship = Constants.RELATIONSHIP.YOUNGER_SISTER;
//            } else {
//                relativeRelationship = Constants.RELATIONSHIP.OLDER_SISTER;
//            }
//        } else if (Constants.RELATIONSHIP.HUSBAND.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.FATHER;
//        } else if (Constants.RELATIONSHIP.WIFE.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.MOTHER;
//        } else if (Constants.RELATIONSHIP.OLDER_BROTHER.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.YOUNGER_BROTHER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.UNCLE;
//        } else if (Constants.RELATIONSHIP.OLDER_SISTER.equalsIgnoreCase(newRelativeRelationship)
//                || Constants.RELATIONSHIP.YOUNGER_SISTER.equalsIgnoreCase(newRelativeRelationship)) {
//            relativeRelationship = Constants.RELATIONSHIP.AUNT;
//        } else {
//            relativeRelationship = Constants.RELATIONSHIP.OTHER;
//        }
//        return relativeRelationship;
//    }

}
