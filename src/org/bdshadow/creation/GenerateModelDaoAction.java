package org.bdshadow.creation;

import org.jetbrains.annotations.NotNull;

/**
 * @author zh_zhou
 * created at 2019/06/24 22:43
 * Copyright [2019] [zh_zhou]
 */
public class GenerateModelDaoAction extends BaseDartCreateFileAction {
    @Override
    protected @NotNull BaseDartCreateHandler getGenerateHandler() {
        return new GenerateModelDaoHandler();
    }




}
