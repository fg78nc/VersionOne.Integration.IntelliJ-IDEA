/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.common.oldsdk;

import com.versionone.Oid;

import java.util.Collection;
import java.util.LinkedList;

public enum TasksProperties {

    TITLE("Title", Type.TEXT, true, "Name"),
    ID("ID", Type.TEXT, false, "Number"),
    PARENT("Story", Type.TEXT, false, "Parent.Name"),
    DETAIL_ESTIMATE("Detailed Estimate", Type.NUMBER, true, "DetailEstimate"),
    DONE("Done", Type.NUMBER, false, "Actuals.Value.@Sum"),
    EFFORT("Effort", Type.NUMBER, true, "Actuals"),
    TO_DO("Todo", Type.NUMBER, true, "ToDo"),
    STATUS("Status", Type.LIST, true, "Status"),
    DESCRIPTION("Description", Type.RICH_TEXT, true, "Description"),
    OWNER("Owner", Type.TEXT, false, "Owners.Nickname"),
    PROJECT("Project", Type.TEXT, false, "Scope.Name"),
    REFERENCE("Reference", Type.TEXT, true, "Reference"),
    SOURCE("Source", Type.LIST, true, "Source"),
    SPRINT("Sprint", Type.TEXT, false, "Timebox.Name"),
    TYPE("Type", Type.LIST, true, "Category"),
    BUILD("Build", Type.TEXT, false, "LastVersion");

    public final String columnName;
    public final Type type;
    public final boolean isEditable;
    public final String propertyName;
    String assetType;

    TasksProperties(String name, Type type, boolean editable) {
        this.columnName = name;
        this.type = type;
        isEditable = editable;
        propertyName = null;
    }

    TasksProperties(String columnName, Type type, boolean editable, String propertyName) {
        this.columnName = columnName;
        this.type = type;
        isEditable = editable;
        this.propertyName = propertyName;
        if (type == Type.LIST) {
            assetType = "Task" + propertyName;
        }
    }

    static Collection<String> getAllAttributes() {
        final TasksProperties[] v = TasksProperties.values();
        final LinkedList<String> res = new LinkedList<String>();
        for (TasksProperties property : v) {
            final String pName = property.propertyName;
            if (pName != null) {
                res.add(pName);
            }
        }
        return res;
    }

    /**
     * Checks equality of old Task property and new one. If properties equal then new value will not set to Task. Accept
     * values of Oid, Double and all values comparable by the equals() method.
     */
    static boolean isEqual(Object oldProp, Object newProp) {
        if (oldProp == null || newProp == null) {
            return oldProp == newProp;
        }
        if (oldProp instanceof Double) {
            return Math.abs((Double) oldProp - Double.parseDouble(newProp.toString())) < 0.005;
        }
        if (oldProp instanceof Oid) {
            return oldProp.toString().equals(newProp.toString());
        }
        return oldProp.equals(newProp);
    }

    public static enum Type {
        NUMBER, RICH_TEXT, TEXT, LIST
    }
}