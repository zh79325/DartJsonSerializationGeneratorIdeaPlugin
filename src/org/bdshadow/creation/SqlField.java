package org.bdshadow.creation;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zh_zhou
 * created at 2019/06/25 15:39
 * Copyright [2019] [zh_zhou]
 */
public class SqlField {
    String type;
    String sqlToDart;
    int length;

    static Pattern pattern = Pattern.compile("@(.+?)\\((.+?)\\)");

    public static SqlField parse(String fieldName, String fieldType, String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String p1 = matcher.group(1);
        String p2 = matcher.group(2);
        if (!StringUtils.equalsIgnoreCase("DataField", p1)) {
            return null;
        }
        String[] p2List = p2.split(",");
        String type = null;
        int length = 0;
        for (String p : p2List) {
            String[] v = p.split(":");
            if (v.length != 2) {
                continue;
            }
            if ("type".equalsIgnoreCase(v[0])) {
                type = parseType(v[1]);
                continue;
            }
            if ("length".equalsIgnoreCase(v[0])) {
                length = parseLength(v[1]);
                continue;
            }
        }
        if (StringUtils.isEmpty(type) && length <= 0) {
            return null;
        }
        if (StringUtils.isEmpty(type)) {
            type = fieldType;
        }
        SqlField sqlField = new SqlField();
        sqlField.setLength(length);
        sqlField.setType(type);
        switch (type.toLowerCase()) {
            case "text":
            case "string":
                sqlField.setSqlToDart("map[\"" + fieldName + "\"]");
                break;
            default:
                return null;
        }
        return sqlField;
    }

    private static int parseLength(String text) {
        if (StringUtils.isEmpty(text)) {
            return 0;
        }
        String v[] = text.split("\\.");
        String n = v.length == 1 ? v[0] : v[1];
        try {
            return Integer.parseInt(n);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String parseType(String text) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        String v[] = text.split("\\.");
        return v.length == 1 ? v[0] : v[1];
    }

    public String getSqlToDart() {
        return sqlToDart;
    }

    public void setSqlToDart(String sqlToDart) {
        this.sqlToDart = sqlToDart;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String buildSqlType() {
        if (length > 0) {
            return type + "(" + length + ")";
        } else {
            return type;
        }
    }
}
