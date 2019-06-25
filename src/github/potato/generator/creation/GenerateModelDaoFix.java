package github.potato.generator.creation;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import github.potato.generator.generation.CreateFromJsonFactoryFix;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author zh_zhou
 * created at 2019/06/24 22:45
 * Copyright [2019] [zh_zhou]
 */
public class GenerateModelDaoFix extends BaseCreateFileFix<DartComponent> {
    public GenerateModelDaoFix(@NotNull DartClass dartClass) {
        super(dartClass);
    }

    @Override
    protected void processElements(@NotNull final Project project,
                                   @NotNull final Editor editor,
                                   @NotNull PsiFile second,
                                   @NotNull final Set<DartComponent> components) {
        Properties properties = new Properties();
        String modelFileName = second.getName();
        PsiDirectory directory = second.getParent();
        String ext = FilenameUtils.getExtension(modelFileName);
        String modelClass = myDartClass.getName();
        String daoClass = modelClass + "Dao";
        String abstractDaoClass = modelClass + "AbstractDao";
        String daoFileName = String.format("%s.%s", daoClass, ext);
        String abstractDaoFileName = String.format("%s.%s", abstractDaoClass, ext);
        PsiFile abstractDaoFile = directory.findFile(abstractDaoFileName);
        PsiFile daoFile = directory.findFile(daoFileName);
        FileTemplate fileTemplate = new CustomFileTemplate("Dart", ext);
        PsiElement abstractDaoElement = null, daoElement = null;
        if(abstractDaoFile!=null){
            abstractDaoFile.delete();
        }
        try {
            abstractDaoElement = FileTemplateUtil.createFromTemplate(fileTemplate, abstractDaoClass, properties, directory);
        } catch (Exception ex) {
            HintManager.getInstance().showErrorHint(editor, abstractDaoFileName + "create failed," + directory.getName() + " is not writable");
        }
        writeAbstractDaoClassFile(project, abstractDaoElement, modelClass, abstractDaoClass, daoClass, components);
        if (daoFile == null) {
            try {
                daoElement = FileTemplateUtil.createFromTemplate(fileTemplate, daoClass, properties, directory);
                writeDaoClassFile(project, daoElement, modelClass, abstractDaoClass, daoClass);
            } catch (Exception ex) {
                HintManager.getInstance().showErrorHint(editor, daoFileName + "create failed," + directory.getName() + " is not writable");
            }
        } else {
            HintManager.getInstance().showInformationHint(editor, daoFileName + " already exist,please extend " + abstractDaoClass + " manually");
        }


    }

    private void writeDaoClassFile(@NotNull Project project, PsiElement file, String modelClass, String abstractDaoClass, String daoClass) {
        NavigationUtil.activateFileWithPsiElement(file, true);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        Editor editor = fileEditorManager.getSelectedTextEditor();
        final TemplateManager templateManager = TemplateManager.getInstance(project);
        final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
        template.setToReformat(true);
        template.addTextSegment("import 'package:sqflite/sqlite_api.dart';");
        template.addTextSegment(String.format("import '%s.dart';", modelClass));
        template.addTextSegment(String.format("import '%s.dart';", abstractDaoClass));
        template.addTextSegment(" ");
        template.addTextSegment(String.format("class %s extends %s {", daoClass, abstractDaoClass));
        template.addTextSegment(String.format("%s(Database database):super(database);", daoClass));
        template.addTextSegment("}");
        templateManager.startTemplate(editor, template);
        templateManager.finishTemplate(editor);
    }

    private void writeAbstractDaoClassFile(@NotNull Project project, PsiElement file, String modelClass, String abstractDaoClass, String daoClass, @NotNull Set<DartComponent> components) {

        String tableName = modelClass.toLowerCase();
        List<DartField> fields = getDartFields(components);
        NavigationUtil.activateFileWithPsiElement(file, true);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        Editor editor = fileEditorManager.getSelectedTextEditor();
        TemplateManager templateManager = TemplateManager.getInstance(project);
        Template headTemplate = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
        headTemplate.setToReformat(false);
        headTemplate.addTextSegment("import 'package:sqflite/sqlite_api.dart';");
        headTemplate.addTextSegment("\n");
        headTemplate.addTextSegment(String.format("import '%s.dart';", modelClass));
        headTemplate.addTextSegment("\n");
        headTemplate.addTextSegment("/**");
        headTemplate.addTextSegment("\n");
        headTemplate.addTextSegment(" * DO NOT MODIFY THIS FILE ");
        headTemplate.addTextSegment("\n");
        headTemplate.addTextSegment(" * this is auto generated by Potato Idea Data Generator");
        headTemplate.addTextSegment("\n");
        headTemplate.addTextSegment(" * visit https://github.com/zh79325/DartJsonSerializationGeneratorIdeaPlugin");
        headTemplate.addTextSegment("\n");
        headTemplate.addTextSegment(" */");
        headTemplate.addTextSegment("\n");
        headTemplate.addTextSegment("\n");
        templateManager.startTemplate(editor, headTemplate);
        templateManager.finishTemplate(editor);
        headTemplate.addTextSegment(" ");
        Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
        template.setToReformat(true);
        template.addTextSegment(String.format("abstract  class %s {", abstractDaoClass));
        template.addTextSegment("final Database database;");
        template.addTextSegment("final String tableName;");
        template.addTextSegment(String.format("%s(this.database):tableName=\"%s\";", abstractDaoClass, tableName));

        buildCreateTableFunction(template, tableName, fields);

        buildInsertFunction(template, modelClass, tableName, fields);

        buildParseFunction(template, modelClass, tableName, fields);

        CreateFromJsonFactoryFix.buildFunctionsText(template,myDartClass,components);

        template.addTextSegment("}");
        templateManager.startTemplate(editor, template);
        templateManager.finishTemplate(editor);

    }

    private void buildParseFunction(Template template, String modelClass, String tableName, List<DartField> fields) {
        template.addTextSegment(" ");
        template.addTextSegment(String.format("static %s fromDb(Map<String,dynamic> map) { ", modelClass));
        template.addTextSegment("if(map==null){return null;}");
        template.addTextSegment(String.format(" %s data=new %s();", modelClass, modelClass));
        for (int i = 0; i < fields.size(); i++) {
            DartField field = fields.get(i);
            String templateSql = "data.%s = %s;";
            String sql = String.format(templateSql, field.getFieldName(), field.getSqlField().getSqlToDart());
            template.addTextSegment(sql);

        }
        template.addTextSegment("return data;");
        template.addTextSegment("} ");


        template.addTextSegment(String.format("%s fromDB(Map<String, dynamic> map){",modelClass));
        template.addTextSegment("return fromDb(map);");
        template.addTextSegment("} ");

    }

    private void buildInsertFunction(Template template, String modelClass, String tableName, List<DartField> fields) {
        template.addTextSegment(" ");
        template.addTextSegment(String.format(" Future<int> insert(%s data) async{ ", modelClass));
        template.addTextSegment("List<dynamic> values = List();");
        StringBuilder insertFiledSql = new StringBuilder();
        StringBuilder insertParamSql = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            DartField field = fields.get(i);
            SqlField sqlField= field.getSqlField();
            if(sqlField.isAi()){
                continue;
            }
            String templateSql = "values.add(data.%s);";
            String sql = String.format(templateSql, field.getFieldName());
            template.addTextSegment(sql);
            if (insertFiledSql.length() > 0) {
                insertFiledSql.append(",");
                insertParamSql.append(",");
            }
            insertFiledSql.append("`" + field.getFieldName() + "`");
            insertParamSql.append("?");
        }
        String sqlTpl = "String sql=\"insert into \"+tableName+\" (%s) values (%s)\";";
        String sql = String.format(sqlTpl, insertFiledSql.toString(), insertParamSql.toString());
        template.addTextSegment(sql);
        template.addTextSegment("return database.rawInsert(sql, values);");
        template.addTextSegment("} ");
    }

    private void buildCreateTableFunction(Template template, String tableName, List<DartField> fields) {
        template.addTextSegment(" ");
        template.addTextSegment("static String getCreateSql(){ ");
        template.addTextSegment(" String template=\"CREATE TABLE " + tableName + "(\";");
        for (int i = 0; i < fields.size(); i++) {
            DartField field = fields.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append("`");
            sb.append(field.getFieldName());
            sb.append("`");
            sb.append(" ");
            SqlField sqlField = field.getSqlField();
            sb.append(sqlField.getSqlType());
            sb.append(" ");
            if (sqlField.isPrimary()) {
                sb.append(" PRIMARY KEY ");
            }
            if (sqlField.isAi()) {
                sb.append(" AUTOINCREMENT ");
            }
            if (i < fields.size() - 1) {
                sb.append(",");
            }
            String sql = "template+=\"" + sb.toString() + "\";";
            template.addTextSegment(sql);
        }
        template.addTextSegment(" template+= \")\";");
        template.addTextSegment(" return template;");
        template.addTextSegment("} ");
    }

    private List<DartField> getDartFields(Set<DartComponent> components) {
        List<DartField> dartFieldList = new ArrayList<>();
        for (DartComponent component : components) {
            String fieldName = component.getName();
            DartReturnType returnType = PsiTreeUtil.getChildOfType(component, DartReturnType.class);
            DartMetadata metadata = PsiTreeUtil.getChildOfType(component, DartMetadata.class);
            DartType dartType = PsiTreeUtil.getChildOfType(component, DartType.class);
            String fieldType = returnType == null ? DartPresentableUtil.buildTypeText(component, dartType, null) : DartPresentableUtil.buildTypeText(component, returnType, null);
            boolean isGenericCollection = fieldType.startsWith("Set") || fieldType.startsWith("List");
            if (isGenericCollection) {
                continue;
            }
            SqlField sqlField = null;
            if (metadata != null) {
                String text = metadata.getText();
                sqlField = SqlField.parse(text);
            }
            if (sqlField == null) {
                sqlField = new SqlField();
            }
            String sqlType = null;
            String sqlToDart = null;
            switch (fieldType.toLowerCase()) {
                case "double":
                    sqlType = "REAL";
                    sqlToDart = "map[\"" + fieldName + "\"]";
                    break;
                case "datetime": {
                    sqlType = "INTEGER";
                    sqlToDart = "map[\"" + fieldName + "\"]==null?null:DateTime.fromMillisecondsSinceEpoch(" + "map[\"" + fieldName + "\"]" + ")";
                    break;
                }
                case "int":
                    sqlType = "INTEGER";
                    sqlToDart = "map[\"" + fieldName + "\"]";
                    break;
                case "bool": {
                    sqlType = "INTEGER";
                    sqlToDart = "map[\"" + fieldName + "\"]" + "==null?false:" + "map[\"" + fieldName + "\"]" + "==0";
                    break;
                }
                case "string":
                    sqlToDart = "map[\"" + fieldName + "\"]";
                    sqlType = "TEXT";
                    break;
                default:
                    continue;

            }
            sqlField.setSqlToDart(sqlToDart);
            sqlField.setSqlType(sqlType);
            DartField dartField = new DartField(fieldName, fieldType, sqlField);
            dartFieldList.add(dartField);
        }
        return dartFieldList;
    }


    protected Template buildFunctionsText(TemplateManager templateManager, Set<DartComponent> elementsToProcess) {
        final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
        template.setToReformat(true);
        return template;
    }

    @Override
    protected @NotNull String getNothingFoundMessage() {
        return null;
    }

    @Override
    protected @Nullable Template buildFunctionsText(TemplateManager templateManager, DartComponent dartComponent) {
        return null;
    }


}
