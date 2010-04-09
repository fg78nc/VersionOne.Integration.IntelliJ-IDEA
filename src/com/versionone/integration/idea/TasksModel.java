/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.idea;


import com.versionone.common.sdk.ApiDataLayer;
import com.versionone.common.sdk.PrimaryWorkitem;
import com.versionone.common.sdk.Workitem;

import com.intellij.util.ui.treetable.TreeTableModel;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class TasksModel extends AbstractTreeTableModel {
    protected final Configuration configuration;

    private List<PrimaryWorkitem> workitems;

    protected static final Map<String, Class> cTypes = new HashMap<String, Class>();

    public TasksModel(List<PrimaryWorkitem> data) {
        super("root");
        update(data);
        configuration = Configuration.getInstance();
        cTypes.put(Configuration.AssetDetailSettings.STRING_TYPE, String.class);
        cTypes.put(Configuration.AssetDetailSettings.LIST_TYPE, JComboBox.class);
        cTypes.put(Configuration.AssetDetailSettings.EFFORT_TYPE, String.class);
        cTypes.put(Configuration.AssetDetailSettings.MULTI_VALUE_TYPE, String.class);
        cTypes.put(Configuration.AssetDetailSettings.RICH_TEXT_TYPE, String.class);
    }

    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return TreeTableModel.class;
        }
	    return cTypes.get(getColumnSettings(columnIndex).type);
    }

    public int getColumnCount() {
        return getColumnSettings().length;
    }

    public String getColumnName(int columnIndex) {
        return ApiDataLayer.getInstance().localizerResolve(getColumnSettings(columnIndex).name);
    }

    public Object getValueAt(Object node, int columnIndex) {
        if (node instanceof Workitem) {
            return ((Workitem)node).getProperty(getColumnSettings(columnIndex).attribute);
        }
        
        return null;
    }

    public void update(List<PrimaryWorkitem> data) {
        workitems = data;
    }

    public void setValueAt(Object aValue, Object node, int column) {
        if(isPropertyEditable(node, column)) {
            Workitem item = (Workitem) node;
            String propertyName = getColumnSettings(column).attribute;
            item.setProperty(propertyName, aValue);

            fireTreeNodesChanged(node, getPathToRoot(new DefaultMutableTreeNode(node)), null, null);
        }
    }

    public boolean isChanged(Object workitem) {
        Workitem item = (Workitem) workitem;
        return item.hasChanges();
    }

    protected Configuration.ColumnSetting getColumnSettings(int columnIndex) {
        return getColumnSettings()[columnIndex];
    }


    protected Object[] getChildren(Object parent) {
        if (parent == null || parent.equals("root")) {
            return workitems.toArray();
        } else if (parent instanceof PrimaryWorkitem) {
            return ((PrimaryWorkitem)parent).children.toArray();
        }

        return new Object[]{};
    }

    public Object getChild(Object parent, int index) {
        if (parent instanceof Object[]) {
            return ((Object[])parent)[index];
        }

        Object[] data = getChildren(parent);
        if (index < data.length) {
            return data[index];
        }

        return new Object();
    }

    public int getChildCount(Object parent) {
        if (parent instanceof Object[]) {
            return ((Object[])parent).length;
        }
        return getChildren(parent).length;
    }

    private Configuration.ColumnSetting[] getColumnSettings() {
        Configuration.ColumnSetting[] columnSettings;
        //if (columnSettings == null) {
            final Configuration.ColumnSetting[] columns = configuration.getColumnsForMainTable();
            final List<Configuration.ColumnSetting> settingsData = new ArrayList<Configuration.ColumnSetting>(columns.length);
            for (Configuration.ColumnSetting column : columns) {
                if (!column.effortTracking || ApiDataLayer.getInstance().isTrackEffortEnabled()) {
                    settingsData.add(column);
                }
            }
            columnSettings = settingsData.toArray(new Configuration.ColumnSetting[settingsData.size()]);
        //}
        return columnSettings;
    }

    public TableCellEditor getCellEditor(int row, int col, Object workitem) {
        Workitem item = (Workitem) workitem;
        Configuration.ColumnSetting settings = getColumnSettings(col);

        if (settings.type.equals(Configuration.AssetDetailSettings.STRING_TYPE)  || settings.type.equals(Configuration.AssetDetailSettings.EFFORT_TYPE)) {
            boolean isReadOnly = settings.readOnly || item.isPropertyReadOnly(settings.attribute);
            return EditorFactory.createTextFieldEditor(!isReadOnly);
        } else if (getColumnSettings(col).type.equals(Configuration.AssetDetailSettings.LIST_TYPE)) {
            return EditorFactory.createComboBoxEditor(item, settings.attribute, getValueAt(row, col));
        }

        return EditorFactory.createTextFieldEditor(false);
    }

    @Override
    /**
     * All cells, except for the first column, are considered editable.
     * Read-only cells are handled on editor control level, thus we have copying ability everywhere.
     */
    public boolean isCellEditable(Object node, int columnIndex) {
        return getColumnClass(columnIndex) != TreeTableModel.class;
    }

    private boolean isPropertyEditable(Object node, int columnIndex) {
        Workitem item = (Workitem) node;
        Configuration.ColumnSetting settings = getColumnSettings(columnIndex);
        boolean propertyTypeSupported = settings.type.equals(Configuration.AssetDetailSettings.STRING_TYPE) ||
                                        settings.type.equals(Configuration.AssetDetailSettings.EFFORT_TYPE) ||
                                        settings.type.equals(Configuration.AssetDetailSettings.RICH_TEXT_TYPE) ||
                                        settings.type.equals(Configuration.AssetDetailSettings.LIST_TYPE);
        return !settings.readOnly && !item.isPropertyReadOnly(settings.attribute) && propertyTypeSupported;
    }
}