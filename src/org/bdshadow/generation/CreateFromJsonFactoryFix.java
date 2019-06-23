package org.bdshadow.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReturnType;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

import static org.yaml.snakeyaml.nodes.NodeId.anchor;

public class CreateFromJsonFactoryFix extends BaseCreateMethodsFix<DartComponent> {

    public CreateFromJsonFactoryFix(@NotNull DartClass dartClass) {
        super(dartClass);
    }

    @Override
    protected void processElements(@NotNull final Project project,
                                   @NotNull final Editor editor,
                                   @NotNull final Set<DartComponent> elementsToProcess) {
        final TemplateManager templateManager = TemplateManager.getInstance(project);
        anchor = doAddMethodsForOne(editor, templateManager, buildFunctionsText(templateManager, elementsToProcess), anchor);
    }

    @NotNull
    @Override
    protected String getNothingFoundMessage() {
        return ""; // can't be called actually because processElements() is overridden
    }

    protected Template buildFunctionsText(TemplateManager templateManager, Set<DartComponent> elementsToProcess) {
        final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
        template.setToReformat(true);

        template.addTextSegment("factory ");
        template.addTextSegment(this.myDartClass.getName());
        template.addTextSegment(".fromJson");
        template.addTextSegment("(Map<String, dynamic> json)");
        template.addTextSegment(" {");
        template.addTextSegment("if(json==null){");
        template.addTextSegment("return null;");
        template.addTextSegment("}");
//        template.addTextSegment("return ");
//        template.addTextSegment(this.myDartClass.getName());
//        template.addTextSegment("(");

        String className=this.myDartClass.getName();
        String classParamName=className.substring(0,1).toLowerCase()+className.substring(1);
        String newElementLine=String.format("%s %s = new %s();",className,classParamName,className);
        template.addTextSegment(newElementLine);
        for (Iterator<DartComponent> iterator = elementsToProcess.iterator(); iterator.hasNext(); ) {
            DartComponent component = iterator.next();
            String fieldName=component.getName();
            String param=String.format("%s.%s",classParamName,fieldName);
            String jsonParam=String.format("json[\"%s\"]",fieldName);
            String scriptScript="%s = %s;";
            String normalScript="if(%s is %s){\n%s = %s;\n}else{\n%s = %s.parse(%s);\n}";
            String boolScript="if(%s is %s){%s = %s;}else{%s = %s.toLowerCase() == 'true';}";
            String otherType="%s = %s.fromJson(%s);";
            DartReturnType returnType = PsiTreeUtil.getChildOfType(component, DartReturnType.class);
            DartType dartType = PsiTreeUtil.getChildOfType(component, DartType.class);
            String typeText = returnType == null ? DartPresentableUtil.buildTypeText(component, dartType, null) : DartPresentableUtil.buildTypeText(component, returnType, null);

            boolean isGenericCollection = typeText.startsWith("Set") || typeText.startsWith("List");

            String nullCheckParam=String.format("if(%s == null){%s=null;}else{",jsonParam,param);
            template.addTextSegment(nullCheckParam);

            if (isGenericCollection) {
                addCollection(template, component, typeText,param,jsonParam);
                template.addTextSegment("}");
                continue;
            }

            String lineScript=null;
            switch (typeText) {
                case "int":
                case "double":
                case "DateTime": {
                    lineScript=String.format(normalScript,jsonParam,typeText,param,jsonParam,param,typeText,jsonParam);
                    break;
                }
                case "bool": {
                    lineScript=String.format(boolScript,jsonParam,typeText,param,jsonParam,param,jsonParam);
                    break;
                }
                case "": //var
                case "String": {
//                    addJsonRetrieval(template, component);
                    lineScript=String.format(scriptScript,param,jsonParam);
                    break;
                }
                default:
//                    template.addTextSegment(typeText);
//                    template.addTextSegment(".fromJson(");
//                    addJsonRetrieval(template, component);
//                    template.addTextSegment(")");
                    lineScript=String.format(otherType,param,typeText,jsonParam);
                    break;

            }
            template.addTextSegment(lineScript);
            template.addTextSegment("}");
        }
        template.addTextSegment(String.format("return %s;",classParamName));
        template.addTextSegment("}\n");

        template.addEndVariable();
        template.addTextSegment(" "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed
        return template;
    }



    private void addCollection(Template template, DartComponent component, String typeText, String paramName, String jsonParamName) {
        int genericBracketIndex = typeText.indexOf("<");
        String collectionType = genericBracketIndex == -1 ? typeText : typeText.substring(0, genericBracketIndex);
        String genericType = genericBracketIndex == -1 ? "" : typeText.substring(genericBracketIndex + 1, typeText.lastIndexOf(">"));
//        template.addTextSegment(collectionType);
//        template.addTextSegment(".of(");
//        addJsonRetrieval(template, component);
//        template.addTextSegment(").map((i) => ");

        String scriptTemplate="%s = %s.of(%s).map((i){if(i==null){return null;}else{ %s}}).to%s();";
        String valueTemplate="  if (i == null) {\n" +
                "          return null;\n" +
                "        }\n" +
                "        if(i is %s){\n" +
                "          return i;\n" +
                "        }else{\n" +
                "          return %s;\n" +
                "        }";
        String realValueTemplate=null;
        switch (genericType) {
            case "int":
            case "double":
            case "DateTime": {
//                template.addTextSegment(genericType);
//                template.addTextSegment(".parse(i)");
                realValueTemplate=String.format("%s.parse(i)",genericType);
                break;
            }
            case "bool": {
//                template.addTextSegment("i.toLowerCase() == 'true'");
                realValueTemplate="i.toLowerCase() == 'true'";
                break;
            }
            case "":
            case "String": {
                realValueTemplate="i";
                break;
            }
            default:
                realValueTemplate=String.format("%s.fromJson(i)",genericType);
                break;
        }
        String value=String.format(valueTemplate,genericType,realValueTemplate);
        String line=String.format(scriptTemplate,paramName,collectionType,jsonParamName,value,collectionType);
        template.addTextSegment(line);
    }

    /**
     * Adds <code>json["componentName"] to the template</code>
     * @param template
     * @param component
     */
    private void addJsonRetrieval(Template template, DartComponent component) {
        template.addTextSegment("json[\"");
        template.addTextSegment(component.getName());
        template.addTextSegment("\"]");
    }

    @Nullable
    @Override
    protected Template buildFunctionsText(TemplateManager templateManager, DartComponent dartComponent) {
        //ignore
        return null;
    }
}
