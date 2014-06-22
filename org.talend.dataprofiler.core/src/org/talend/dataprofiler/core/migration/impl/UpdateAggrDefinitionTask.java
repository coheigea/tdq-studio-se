// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.migration.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.talend.dataprofiler.core.migration.AbstractWorksapceUpdateTask;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dq.indicators.definitions.DefinitionHandler;
import org.talend.dq.writer.EMFSharedResources;

/**
 * 
 * DOC qiongli class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 * 
 */
public class UpdateAggrDefinitionTask extends AbstractWorksapceUpdateTask {

    private static String[] needUpateKeys;

    private static HashMap<String, String[]> map = new HashMap<String, String[]>();

    private static Logger log = Logger.getLogger(UpdateAggrDefinitionTask.class);


    public Date getOrder() {
        return createDate(2012, 2, 29);
    }

    public MigrationTaskType getMigrationTaskType() {
        return MigrationTaskType.FILE;
    }

    private void initializtion() {
        needUpateKeys = new String[] { "Simple Statistics", "Text Statistics", "Phone Number Statistics", "Catalog Overview", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                "Connection Overview", "Schema Overview", "Range", "IQR", "Summary Statistics" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        String[] simpArray = new String[] { "Blank Count", "Distinct Count", "Duplicate Count", "Unique Count", "Null Count", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                "Row Count", "Default Value Count" }; //$NON-NLS-1$ //$NON-NLS-2$
        map.put("Simple Statistics", simpArray); //$NON-NLS-1$

        String[] textArray = new String[] { "Average Length", "Maximal Length", "Minimal Length", "Minimal Length With Null", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                "Minimal Length With Blank", "Minimal Length With Blank and Null", "Maximal Length With Null", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "Maximal Length With Blank", "Maximal Length With Blank and Null", "Average Length With Null", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "Average Length With Blank", "Average Length With Blank and Null" }; //$NON-NLS-1$ //$NON-NLS-2$
        map.put("Text Statistics", textArray); //$NON-NLS-1$
        String[] phoneNumArray = new String[] { "Invalid Region Code Count", "Possible Phone Number Count", //$NON-NLS-1$ //$NON-NLS-2$
                "Valid Phone Number Count", "Valid Region Code Count", "Well Formed E164 Phone Number Count", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "Well Formed International Phone Number Count", "Well Formed National Phone Number Count", "Format Frequency Pie" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        map.put("Phone Number Statistics", phoneNumArray); //$NON-NLS-1$
        String[] sumaryArray = new String[] { "IQR", "Mean", "Median", "Range" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        map.put("Summary Statistics", sumaryArray); //$NON-NLS-1$
        String[] connViewArray = new String[] { "Catalog Overview", "Schema Overview" }; //$NON-NLS-1$ //$NON-NLS-2$
        map.put("Connection Overview", connViewArray); //$NON-NLS-1$
        String[] cataViewArray = new String[] { "Schema Overview", "Table Overview" }; //$NON-NLS-1$ //$NON-NLS-2$
        map.put("Catalog Overview", cataViewArray); //$NON-NLS-1$
        String[] scheViewArray = new String[] { "Table Overview", "View Overview" }; //$NON-NLS-1$ //$NON-NLS-2$
        map.put("Schema Overview", scheViewArray); //$NON-NLS-1$
        String[] interRangeArray = new String[] { "Lower Quartile", "Upper Quartile" }; //$NON-NLS-1$ //$NON-NLS-2$
        map.put("IQR", interRangeArray); //$NON-NLS-1$
        String[] rangeArray = new String[] { "Maximum", "Minimum" }; //$NON-NLS-1$ //$NON-NLS-2$
        map.put("Range", rangeArray); //$NON-NLS-1$

    }

    @Override
    protected boolean doExecute() throws Exception {
        initializtion();
        DefinitionHandler defiHandInstance = DefinitionHandler.getInstance();
        List<IndicatorDefinition> indicatorsDefinitions = defiHandInstance.getIndicatorsDefinitions();

        try {
            for (IndicatorDefinition indiDefinition : indicatorsDefinitions) {
                if (indiDefinition.eIsProxy()) {
                    continue;
                }
                String name = indiDefinition.getLabel();
                if (findName(name)) {
                    List<IndicatorDefinition> newAgrrDefiLs = new ArrayList<IndicatorDefinition>();
                    String[] array = map.get(name);
                    if (array == null || array.length == 0) {
                        continue;
                    }
                    for (String label : array) {
                        IndicatorDefinition findDef = defiHandInstance.getIndicatorDefinition(label);
                        if (findDef != null) {
                            newAgrrDefiLs.add(findDef);
                        }
                    }
                    indiDefinition.getAggregatedDefinitions().clear();
                    indiDefinition.getAggregatedDefinitions().addAll(newAgrrDefiLs);
                    EMFSharedResources.getInstance().saveResource(indiDefinition.eResource());
                }

            }
        } catch (Exception exc) {
            log.error("do migration for UpdateAggrDefinitionTask failed:", exc); //$NON-NLS-1$
        }

        return true;
    }

    private boolean findName(String name) {
        if (name == null) {
            return false;
        }
        for (String na : needUpateKeys) {
            if (na.equals(name)) {
                return true;
            }
        }
        return false;
    }
    
}