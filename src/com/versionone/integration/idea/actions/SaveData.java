/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.idea.actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.versionone.common.sdk.IDataLayer;

/**
 *
 */
public class SaveData extends AnAction {
    private IDataLayer dataLayer;

    public void actionPerformed(AnActionEvent e) {
        System.out.println("Save.actionPerformed()");

        final DataContext dataContext = e.getDataContext();
//        final Project ideaProject = (Project) dataContext.getData(DataConstantsEx.PROJECT);
        final Project ideaProject = DataKeys.PROJECT.getData(dataContext);
        final ProgressManager progressManager = ProgressManager.getInstance();
        final IDataLayer data = dataLayer;
        final boolean[] isErrors = {false};

        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
            public void run() {
                progressManager.getProgressIndicator().setText("Save task's data");
                try {
                    data.commitChangedTaskData();
                } catch (Exception e1) {
                    isErrors[0] = true;
                }
            }
        },
                "Save task's data",
                false,
                ideaProject
        );

        if (isErrors[0]) {
            Messages.showErrorDialog(
                    "Error connection to the VesionOne server",
                    "Error");
        }

        ActionManager.getInstance().getAction("V1.toolRefresh").actionPerformed(e);
    }


    public void setDataLayer(IDataLayer data) {
        this.dataLayer = data;
    }
}
