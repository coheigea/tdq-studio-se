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
package org.talend.dq.helper;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.talend.core.model.properties.Property;
import org.talend.dataquality.properties.TDQReportItem;
import org.talend.utils.sugars.ReturnCode;

/**
 * DOC qiongli class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 * 
 */
// @RunWith(PowerMockRunner.class)
@PrepareForTest({ DQDeleteHelper.class, PropertyHelper.class, ReportUtils.class, EObjectHelper.class })
public class DQDeleteHelperTest {

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    /**
     * Test method for
     * {@link org.talend.dq.helper.DQDeleteHelper#deleteRelations(org.talend.core.model.properties.Item)}.
     */
    @Test
    public void testDeleteRelations() {
        TDQReportItem item = mock(TDQReportItem.class);
        IFile file = mock(IFile.class);
        when(file.exists()).thenReturn(false);
        IFolder folder = mock(IFolder.class);
        when(folder.exists()).thenReturn(true);
        Property prop = mock(Property.class);
        when(item.getProperty()).thenReturn(prop);
        PowerMockito.mockStatic(PropertyHelper.class);
        when(PropertyHelper.getItemFile(prop)).thenReturn(file);
        PowerMockito.mockStatic(ReportUtils.class);
        when(ReportUtils.getOutputFolder(file)).thenReturn(folder);
        ReturnCode rc = DQDeleteHelper.deleteRelations(item);
        assertTrue(rc.isOk());

    }

}