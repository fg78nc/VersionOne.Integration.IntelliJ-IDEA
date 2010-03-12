package com.versionone.common.oldsdk;

import com.versionone.Oid;
import com.versionone.common.oldsdk.IDataLayer;
import com.versionone.apiclient.APIException;
import com.versionone.apiclient.Asset;
import com.versionone.apiclient.IAttributeDefinition;
import com.versionone.apiclient.MetaException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents one Task in the VersionOne system
 */
class Task {

    private static final String TASK_PREFIX = "Task.";

    public final Asset asset;
    private BigDecimal effort = BigDecimal.ZERO.setScale(2);
    private Map<String, IAttributeDefinition> definitions = new HashMap<String, IAttributeDefinition>(TasksProperties.values().length);
    private static final BigDecimal MAX_NUMBER_VALUE = BigDecimal.valueOf(1000000); 

    /**
     * Create
     *
     * @param asset - Task asset
     */
    public Task(Asset asset) throws MetaException {
        this.asset = asset;
    }

    public String getToken() throws Exception {
        return asset.getOid().getToken();
    }

    /**
     * Get the value of an attribute
     *
     * @param key - name of attribute
     */
    private Object getValue(String key) {
        Object value = null;
        try {
            value = asset.getAttributes().get(TASK_PREFIX + key).getValue();
        } catch (Exception e) {
            //do nothing
        }
        return value;
    }

    public void setProperty(TasksProperties property, String value, IDataLayer dataLayer) {
        try {
            switch (property.type) {
                case LIST: {
                    final Oid oid = dataLayer.getPropertyValueOid(value, property);
                    if (oid != null && !TasksProperties.isEqual(getProperty(property), oid)) {
                        asset.setAttributeValue(getDefinition(property.propertyName), oid);
                    }
                    break;
                }
                case NUMBER: {
                    BigDecimal newValue = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
                    if (newValue.signum() >= 0 && newValue.compareTo(MAX_NUMBER_VALUE) <=0) {
                        if (property == TasksProperties.EFFORT) {
                            effort = newValue;
                        } else if (!TasksProperties.isEqual(getProperty(property), value)) {
                            asset.setAttributeValue(getDefinition(property.propertyName), value);
                        }
                    }
                    break;
                }
                default: {
                    if (!TasksProperties.isEqual(getProperty(property), value)) {
                        asset.setAttributeValue(getDefinition(property.propertyName), value);
                    }
                }
            }
        } catch (APIException e) {
            e.printStackTrace(); //do nothing
        } catch (MetaException e) {
            e.printStackTrace(); //do nothing
        } catch (NumberFormatException e) {
            e.printStackTrace(); //do nothing
        }
    }

    private IAttributeDefinition getDefinition(String property) {
        IAttributeDefinition res = definitions.get(property);
        if (res == null) {
            res = asset.getAssetType().getAttributeDefinition(property);
            definitions.put(property, res);
        }
        return res;
    }

    public Object getProperty(TasksProperties property) {
        if (property.equals(TasksProperties.EFFORT)) {
            return effort;
        }
        return getValue(property.propertyName);
    }

    public boolean isPropertyChanged(TasksProperties property) {
        boolean value = false;
        if (property.equals(TasksProperties.EFFORT)) {
            value = effort.compareTo(BigDecimal.ZERO) != 0;
        } else try {
            value = asset.getAttributes().get(TASK_PREFIX + property.propertyName).hasChanged();
        } catch (Exception e) {
            //do nothing
        }
        return value;
    }

    public boolean isChanged() {
        return effort.compareTo(BigDecimal.ZERO) != 0 || asset.hasChanged();
    }
}