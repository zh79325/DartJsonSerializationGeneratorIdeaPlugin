package github.potato.generator.creation;

import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
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
    private boolean skip;

    public static SqlField parse(DartMetadata meta) {

        Collection<DartNamedArgument> arguments = PsiTreeUtil.findChildrenOfType(meta, DartNamedArgument.class);
        SqlField sqlField = new SqlField();

        for (DartNamedArgument argument : arguments) {
            DartParameterNameReferenceExpression name = PsiTreeUtil.getChildOfType(argument, DartParameterNameReferenceExpression.class);
            DartLiteralExpression value = PsiTreeUtil.getChildOfType(argument, DartLiteralExpression.class);
            if (name == null && value == null) {
                continue;
            }
            String nameText = name.getText();
            String valueText = value.getText();
            if("primary".equals(nameText)){
                sqlField.setPrimary(parseBoolean(valueText));
            }else if("ai".equalsIgnoreCase(nameText)){
                sqlField.setAi(parseBoolean(valueText));
            }
            else if("skip".equalsIgnoreCase(nameText)){
                sqlField.setSkip(parseBoolean(valueText));
            }
        }
        return sqlField;
    }

    private static boolean parseBoolean(String v) {
        if (StringUtils.isEmpty(v)) {
            return false;
        }
        try {
            return Boolean.parseBoolean(v);
        } catch (Exception e) {
            return false;
        }
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


    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean getSkip() {
        return skip;
    }
}
