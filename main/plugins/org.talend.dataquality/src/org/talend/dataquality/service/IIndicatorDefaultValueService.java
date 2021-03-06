// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.service;

/**
 * DOC zshen class global comment. Detailled comment
 */
public interface IIndicatorDefaultValueService {

    int getLowFrequencyLimitResult();

    int getFrequencyLimitResult();

    void setLowFrequencyLimitResult(int limit);

    void setFrequencyLimitResult(int limit);
}
