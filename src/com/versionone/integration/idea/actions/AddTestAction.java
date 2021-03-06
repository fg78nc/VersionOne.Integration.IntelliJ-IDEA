/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.idea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.versionone.integration.idea.TasksComponent;
import com.versionone.common.sdk.Workitem;
import com.versionone.common.sdk.SecondaryWorkitem;
import com.versionone.common.sdk.PrimaryWorkitem;
import com.versionone.common.sdk.EntityType;
import com.versionone.common.sdk.DataLayerException;
import org.apache.log4j.Logger;

/**
 * Create in-memory Test. New entity will be persisted when user triggers Save action.
 */
public class AddTestAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(AddTestAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project ideaProject = resolveProject(e);
        if (ideaProject != null) {
            TasksComponent tc = resolveTasksComponent(ideaProject);

            Workitem currentItem = (Workitem)tc.getCurrentItem();
            if (currentItem != null) {
                SecondaryWorkitem newItem = createTest(currentItem);
                tc.update();
                tc.selectNode(newItem);
            }
        }
    }

    private SecondaryWorkitem createTest(Workitem currentItem) {
        PrimaryWorkitem parent;
        if (currentItem.getType().isPrimary()) {
            parent = (PrimaryWorkitem)currentItem;
        } else {
            parent = ((SecondaryWorkitem)currentItem).parent;
        }
        SecondaryWorkitem newItem = null;
        try {
            newItem = dataLayer.createNewSecondaryWorkitem(EntityType.Test, parent);
        } catch (DataLayerException ignored) {
            LOG.warn("Failed to create new " + EntityType.Test.name(), ignored);
        }

        return newItem;
    }

    @Override
    public void update(AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        presentation.setEnabled(dataLayer.isConnected() && getSettings().isEnabled && !isActionDisabled(event));
    }

    private boolean isActionDisabled(AnActionEvent event) {
        Project ideaProject = resolveProject(event);
        if (ideaProject != null) {
            TasksComponent tc = resolveTasksComponent(event);
            return tc.getCurrentItem() == null;
        }

        return true;
    }
}
