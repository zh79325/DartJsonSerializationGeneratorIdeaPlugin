package github.potato.generator.generation;

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
            DartReturnType returnType = PsiTreeUtil.getChildOfType(component, DartReturnType.class);
            DartType dartType = PsiTreeUtil.getChildOfType(component, DartType.class);
            String typeText = returnType == null ? DartPresentableUtil.buildTypeText(component, dartType, null) : DartPresentableUtil.buildTypeText(component, returnType, null);

            boolean isGenericCollection = typeText.startsWith("Set") || typeText.startsWith("List");


            if (isGenericCollection) {
                addCollection(template, component, typeText,param,jsonParam);
                template.addTextSegment("}");
                continue;
            }

            String lineScript = buildLineScript(typeText,param, jsonParam);
            template.addTextSegment(lineScript);
        }
        template.addTextSegment(String.format("return %s;",classParamName));
        template.addTextSegment("}\n");

        template.addEndVariable();
        template.addTextSegment(" "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed
        return template;
    }

    private String buildLineScript(String typeText,String param, String jsonParam) {
        String lineScript;
        switch (typeText.toLowerCase()) {
            case "int":
                lineScript=String.format("%s = PotatoDataParser.parseIntValue(%s);",param,jsonParam);
                break;
            case "double":
                lineScript=String.format("%s = PotatoDataParser.parseDoubleValue(%s);",param,jsonParam);
                break;
            case "datetime": {
                lineScript=String.format("%s = PotatoDataParser.parseDateTime(%s);",param,jsonParam);
                break;
            }
            case "bool": {
                lineScript=String.format("%s = PotatoDataParser.parseBool(%s);",param,jsonParam);
                break;
            }
            case "":
            case "string": {
                lineScript=String.format("%s = %s;",param,jsonParam);
                break;
            }
            default:
                lineScript=String.format("%s = %s.fromJson(%s);",param,typeText,jsonParam);
                break;

        }
        return lineScript;
    }


    private void addCollection(Template template, DartComponent component, String typeText, String paramName, String jsonParamName) {
        int genericBracketIndex = typeText.indexOf("<");
        String collectionType = genericBracketIndex == -1 ? typeText : typeText.substring(0, genericBracketIndex);
        String genericType = genericBracketIndex == -1 ? "" : typeText.substring(genericBracketIndex + 1, typeText.lastIndexOf(">"));
//        template.addTextSegment(collectionType);
//        template.addTextSegment(".of(");
//        addJsonRetrieval(template, component);
//        template.addTextSegment(").map((i) => ");

        String scriptTemplate="%s = PotatoDataParser.parse%sValue(%s,(i){return %s;});";

        String returnValue=null;
        switch (genericType) {
            case "int":
                returnValue= "PotatoDataParser.parseIntValue(i)";
                break;
            case "double":
                returnValue= "PotatoDataParser.parseDoubleValue(i)";
                break;
            case "DateTime": {
                returnValue= "PotatoDataParser.parseDateTime(i)";
                break;
            }
            case "bool": {
                returnValue= "PotatoDataParser.parseBool(i)";
                break;
            }
            case "":
            case "String": {
                returnValue= "i";
                break;
            }
            default:
                returnValue= String.format("%s.fromJson(i)",genericType);
                break;
        }
        String line=String.format(scriptTemplate,paramName,collectionType,jsonParamName,returnValue);
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
