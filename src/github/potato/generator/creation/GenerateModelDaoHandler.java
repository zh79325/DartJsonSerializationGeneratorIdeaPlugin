package github.potato.generator.creation;

import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author zh_zhou
 * created at 2019/06/24 22:43
 * Copyright [2019] [zh_zhou]
 */
public class GenerateModelDaoHandler extends BaseDartCreateHandler {

    @NotNull
    @Override
    protected BaseCreateFileFix createFix(@NotNull DartClass dartClass) {
        return new GenerateModelDaoFix(dartClass);
    }

    @NotNull
    @Override
    protected String getTitle() {
        return "generateDao";
    }

    @Override
    protected void collectCandidates(@NotNull DartClass dartClass, @NotNull List<DartComponent> candidates) {
        candidates.addAll(ContainerUtil.findAll(computeClassMembersMap(dartClass, false).values(),
                component -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
    }

    @Override
    protected boolean doAllowEmptySelection() {
        return true;
    }
}
