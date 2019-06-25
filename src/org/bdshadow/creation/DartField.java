package org.bdshadow.creation;

/**
 * @author zh_zhou
 * created at 2019/06/25 14:05
 * Copyright [2019] [zh_zhou]
 */
public class DartField {
    final String fieldName;
    final String fieldType;
    final SqlField sqlField;


    public DartField(String fieldName, String fieldType, SqlField sqlField) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.sqlField = sqlField;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public SqlField getSqlField() {
        return sqlField;
    }
}
