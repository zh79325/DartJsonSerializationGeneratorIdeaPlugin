package org.bdshadow.creation;

/**
 * @author zh_zhou
 * created at 2019/06/25 14:05
 * Copyright [2019] [zh_zhou]
 */
public class DartField {
    final String fieldName;
    final String dartType;
    final String sqlType;
    final String sqlToDart;


    public DartField(String fieldName, String dartType, String sqlType, String sqlToDart) {
        this.fieldName = fieldName;
        this.dartType = dartType;
        this.sqlType = sqlType;
        this.sqlToDart = sqlToDart;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDartType() {
        return dartType;
    }

    public String getSqlType() {
        return sqlType;
    }

    public String getSqlToDart() {
        return sqlToDart;
    }
}
