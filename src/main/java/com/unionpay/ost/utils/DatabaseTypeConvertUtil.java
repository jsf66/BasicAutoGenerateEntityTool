package com.unionpay.ost.utils;

/**
 * Created by jsf on 16/8/8..
 */
public class DatabaseTypeConvertUtil {

    public static String dataBaseTypeToJavaTypeForMYSQL(String dateBaseType) {

        switch (dateBaseType) {
            case "VARCHAR":
                return "String";
            case "CHAR":
                return "String";
            case "DATE":
                return "Date";
            case "DATETIME":
                return "Timestamp";
            case "TIMESTAMP":
                return "Timestamp";
            case "DOUBLE":
                return "Double";
            case "TINYINT":
                return "Integer";
            case "SMALLINT":
                return "Integer";
            case "MEDIUMINT":
                return "Integer";
            case "INT":
                return "Integer";
            case "FLOAT":
                return "Float";
            case "DECIMAL":
                return "BigDecimal";
            case "BLOB":
                return "byte[]";
            case "TEXT":
                return "String";
            default:
                return "String";
        }
    }
    public static String dataBaseTypeToJavaTypeForDB2(String dateBaseType){

        switch (dateBaseType) {
            case "BIGINT":
                return "Long";
            case "CHAR":
                return "String";
            case "CLOB":
                return "String";
            case "DATE":
                return "Date";
            case "DATETIME":
                return "Timestamp";
            case "TIMESTAMP":
                return "Timestamp";
            case "TIME":
                return "Time";
            case "DOUBLE":
                return "Double";
            case "TINYINT":
                return "Integer";
            case "SMALLINT":
                return "Integer";
            case "MEDIUMINT":
                return "Integer";
            case "INTEGER":
                return "Integer";
            case "INT":
                return "Integer";
            case "NUMERIC":
                return "BigDecimal";
            case "DECIMAL":
                return "BigDecimal";
            case "BLOB":
                return "byte[]";
            case "TEXT":
                return "String";
            default:
                return "String";
        }
    }
}
