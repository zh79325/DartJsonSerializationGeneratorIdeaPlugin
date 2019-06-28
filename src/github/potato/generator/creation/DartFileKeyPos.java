package github.potato.generator.creation;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author zh_zhou
 * created at 2019/06/28 12:57
 * Copyright [2019] [zh_zhou]
 */
public class DartFileKeyPos {

    private DartImportStatement firstImport;
    private DartImportStatement lastImport;
    private DartVarDeclarationList lastField;
    private DartMethodDeclaration firstMethod;
    private DartMethodDeclaration lastMethod;
    private DartClass dartClass;

    public static DartFileKeyPos getKeyPos(Editor editor, PsiFile file) {
        DartClass[] dartClasses = PsiTreeUtil.getChildrenOfType(file, DartClassDefinition.class);
        if (dartClasses == null) {
            HintManager.getInstance().showErrorHint(editor, "No Model Class Found");
            return null;
        }
        if (dartClasses.length > 1) {
            HintManager.getInstance().showErrorHint(editor, "Find Multi Model Class,Only Support One Model");
            return null;
        }
        DartClass dartClass = dartClasses[0];
        DartImportStatement[] importStatements = PsiTreeUtil.getChildrenOfType(file, DartImportStatement.class);
        DartImportStatement firstImport = null;
        DartImportStatement lastImport = null;
        if (importStatements != null && importStatements.length > 0) {
            firstImport = importStatements[0];
            lastImport = importStatements[importStatements.length - 1];
        }
        Collection<DartVarDeclarationList> fileds = PsiTreeUtil.findChildrenOfType(dartClass, DartVarDeclarationList.class);
        Collection<DartMethodDeclaration> functions = PsiTreeUtil.findChildrenOfType(dartClass, DartMethodDeclaration.class);
        DartVarDeclarationList lastField = null;
        if (fileds != null) {
            List<DartVarDeclarationList> fList = new ArrayList<>(fileds);
            lastField = fList.get(fList.size() - 1);
        }
        DartMethodDeclaration firstMethod = null;
        DartMethodDeclaration lastMethod = null;
        if (functions != null && functions.size() > 0) {
            List<DartMethodDeclaration> fList = new ArrayList<>(functions);
            firstMethod = fList.get(0);
            lastMethod = fList.get(fList.size() - 1);
        }

        DartFileKeyPos keyPos = new DartFileKeyPos();
        keyPos.setFirstImport(firstImport);
        keyPos.setLastImport(lastImport);
        keyPos.setLastField(lastField);
        keyPos.setFirstMethod(firstMethod);
        keyPos.setLastMethod(lastMethod);
        keyPos.setDartClass(dartClass);
        return keyPos;
    }

    public  int getOtherFunctionPos(){
        int constructPos=0;
        if(lastField!=null){
            constructPos=lastField.getTextRange().getEndOffset()+2;
        }
        if(lastMethod!=null){
            constructPos=lastMethod.getTextRange().getEndOffset();
        }
        return constructPos;
    }
    public  int getConstructPos(){
        int constructPos=0;
        if(lastField!=null){
            constructPos=lastField.getTextRange().getEndOffset()+2;
        }
        if(firstMethod!=null){
            constructPos=firstMethod.getTextRange().getStartOffset();
        }
        return constructPos;
    }
    public int getImportInsertPos() {
        if (firstImport == null) {
            return 0;
        }
        return firstImport.getTextRange().getStartOffset();
    }

    public int getPartPos() {
        if (lastImport == null) {
            return 0;
        }
        return lastImport.getTextRange().getEndOffset();
    }

    public void setFirstImport(DartImportStatement firstImport) {
        this.firstImport = firstImport;
    }

    public DartImportStatement getFirstImport() {
        return firstImport;
    }

    public void setLastImport(DartImportStatement lastImport) {
        this.lastImport = lastImport;
    }

    public DartImportStatement getLastImport() {
        return lastImport;
    }

    public void setLastField(DartVarDeclarationList lastField) {
        this.lastField = lastField;
    }

    public DartVarDeclarationList getLastField() {
        return lastField;
    }

    public void setFirstMethod(DartMethodDeclaration firstMethod) {
        this.firstMethod = firstMethod;
    }

    public DartMethodDeclaration getFirstMethod() {
        return firstMethod;
    }

    public void setLastMethod(DartMethodDeclaration lastMethod) {
        this.lastMethod = lastMethod;
    }

    public DartMethodDeclaration getLastMethod() {
        return lastMethod;
    }

    public void setDartClass(DartClass dartClass) {
        this.dartClass = dartClass;
    }

    public DartClass getDartClass() {
        return dartClass;
    }

    public int getMetaPos() {
        return dartClass.getTextRange().getStartOffset();
    }
}
