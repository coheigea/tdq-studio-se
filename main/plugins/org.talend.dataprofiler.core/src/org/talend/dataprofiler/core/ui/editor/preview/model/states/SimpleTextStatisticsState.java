// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.editor.preview.model.states;

import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.TopChartFactory;
import org.talend.dataprofiler.core.ui.editor.preview.model.ICustomerDataset;
import org.talend.dataprofiler.core.ui.editor.preview.model.dataset.CustomerDefaultCategoryDataset;
import org.talend.dataprofiler.core.ui.utils.ComparatorsFactory;
import org.talend.dq.indicators.preview.table.ChartDataEntity;

/**
 * DOC yyi class global comment. Detailled comment
 */
public class SimpleTextStatisticsState extends TextStatisticsState {

    /**
     * DOC yyi SimpleTextStatisticsState constructor comment.
     * 
     * @param units
     */
    public SimpleTextStatisticsState(List<IndicatorUnit> units) {
        super(units);
    }

    public JFreeChart getChart() {
        JFreeChart chart = TopChartFactory.createBarChart(DefaultMessagesImpl
.getString("SimpleTextStatisticsState.SimpleTextStatistics"), getDataset(), false);//$NON-NLS-1$
        chart.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        return chart;
    }

    public ICustomerDataset getCustomerDataset() {

        // sort these indicators.
        ComparatorsFactory.sort(units, ComparatorsFactory.TEXT_STATISTICS_COMPARATOR_ID);

        CustomerDefaultCategoryDataset customerdataset = new CustomerDefaultCategoryDataset();
        for (IndicatorUnit unit : units) {
            double value = Double.parseDouble(unit.getValue().toString());
            String label = unit.getIndicatorName();
            String type = "";//$NON-NLS-1$
            switch (unit.getType()) {
            case MinLengthWithNullIndicatorEnum:
            case MinLengthWithBlankIndicatorEnum:
            case MinLengthWithBlankNullIndicatorEnum:
                type = "Minimal";//$NON-NLS-1$
                break;
            case MaxLengthWithBlankIndicatorEnum:
            case MaxLengthWithBlankNullIndicatorEnum:
            case MaxLengthWithNullIndicatorEnum:
                type = "Maximal";//$NON-NLS-1$
                break;
            case AverageLengthWithNullIndicatorEnum:
            case AverageLengthWithBlankIndicatorEnum:
            case AverageLengthWithNullBlankIndicatorEnum:
                type = "Average";//$NON-NLS-1$
                break;
            default:
                type = label;
                break;
            }

            customerdataset.addValue(value, type, label); //$NON-NLS-1$

            ChartDataEntity entity = new ChartDataEntity();
            entity.setIndicator(unit.getIndicator());
            entity.setLabel(label);
            entity.setValue(String.valueOf(value));
            entity.setPercent(value / unit.getIndicator().getCount());

            customerdataset.addDataEntity(entity);
        }
        return customerdataset;
    }
}