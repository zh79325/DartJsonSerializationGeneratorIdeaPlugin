package github.potato.generator.creation;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zh_zhou
 * created at 2019/06/25 15:39
 * Copyright [2019] [zh_zhou]
 */
public class SqlField {

    boolean primary = false;
    boolean ai = false;
    String sqlToDart;
    String sqlType;



    static Pattern pattern = Pattern.compile("@(.+?)\\((.+?)\\)");

    public static SqlField parse(String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String p1 = matcher.group(1);
        String p2 = matcher.group(2);
        if (!StringUtils.equalsIgnoreCase("PotatoDataField", p1)) {
            return null;
        }
        String[] p2List = p2.split(",");
        boolean primary = false;
        boolean ai = false;
        for (String p : p2List) {
            String[] v = p.split(":");
            if (v.length != 2) {
                continue;
            }
            if ("primary".equalsIgnoreCase(v[0])) {
                primary = parseBoolean(v[1]);
                continue;
            }
            if ("ai".equalsIgnoreCase(v[0])) {
                ai = parseBoolean(v[1]);
                continue;
            }
        }
        SqlField sqlField = new SqlField();
        sqlField.setAi(ai);
        sqlField.setPrimary(primary);
        return sqlField;
    }

    private static boolean parseBoolean(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }
        String v[] = text.split("\\.");
        String n = v.length == 1 ? v[0] : v[1];
        try {
            return Boolean.parseBoolean(n.trim());
        } catch (Exception e) {
            return false;
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

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isAi() {
        return ai;
    }

    public void setAi(boolean ai) {
        this.ai = ai;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public static Pattern getPattern() {
        return pattern;
    }

    public static void setPattern(Pattern pattern) {
        SqlField.pattern = pattern;
    }
}
