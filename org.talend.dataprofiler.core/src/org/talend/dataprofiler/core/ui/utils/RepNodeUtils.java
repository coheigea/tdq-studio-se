// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.general.Project;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.AbstractItemEditorInput;
import org.talend.dataprofiler.core.ui.events.EventEnum;
import org.talend.dataprofiler.core.ui.events.EventManager;
import org.talend.dataprofiler.core.ui.views.resources.IRepositoryObjectCRUD;
import org.talend.dataprofiler.core.ui.views.resources.LocalRepositoryObjectCRUD;
import org.talend.dataprofiler.core.ui.views.resources.RemoteRepositoryObjectCRUD;
import org.talend.dataquality.helpers.ReportHelper;
import org.talend.dataquality.helpers.ReportHelper.ReportType;
import org.talend.dataquality.reports.AnalysisMap;
import org.talend.dataquality.reports.TdReport;
import org.talend.dq.helper.ProxyRepositoryManager;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.nodes.ReportRepNode;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.resource.EResourceConstant;
import org.talend.resource.ResourceManager;
import org.talend.utils.sugars.ReturnCode;

/**
 * RepositoryNode's UI utils.
 */
public final class RepNodeUtils {

    private static Logger log = Logger.getLogger(RepNodeUtils.class);

    private RepNodeUtils() {
    }

    /**
     * close file node's editor.
     * 
     * @param files
     * @param save
     */
    public static void closeFileEditor(List<IFile> files, boolean save) {
        List<IEditorReference> need2CloseEditorRefs = new ArrayList<IEditorReference>();
        IEditorReference[] editorReferences = CorePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getEditorReferences();

        try {
            for (IEditorReference editorRef : editorReferences) {
                if (editorRef != null) {
                    IEditorInput editorInput = editorRef.getEditorInput();
                    if (editorInput != null) {
                        if (editorInput instanceof FileEditorInput) {
                            IFile file = ((FileEditorInput) editorInput).getFile();
                            if (file != null) {
                                for (IFile ifile : files) {
                                    if (ifile != null) {
                                        String osString = ifile.getRawLocation().toOSString();
                                        String osString2 = file.getRawLocation().toOSString();
                                        if (osString.equals(osString2)) {
                                            need2CloseEditorRefs.add(editorRef);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (PartInitException e) {
            log.warn(e, e);
        }

        if (need2CloseEditorRefs.size() > 0) {
            CorePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .closeEditors(need2CloseEditorRefs.toArray(new IEditorReference[need2CloseEditorRefs.size()]), save);

        }
    }

    /**
     * close ModelElement node's editor.
     * 
     * @param nodes
     */
    public static void closeModelElementEditor(List<? extends IRepositoryNode> nodes, boolean save) {
        List<String> uuids = RepositoryNodeHelper.getUuids(nodes);

        List<IEditorReference> need2CloseEditorRefs = new ArrayList<IEditorReference>();
        IEditorReference[] editorReferences = CorePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getEditorReferences();

        try {
            for (IEditorReference editorRef : editorReferences) {
                if (editorRef != null) {
                    IEditorInput editorInput = editorRef.getEditorInput();
                    if (editorInput != null) {
                        if (editorInput instanceof AbstractItemEditorInput) {
                            String modelElementUuid = ((AbstractItemEditorInput) editorInput).getModelElementUuid();
                            if (modelElementUuid != null && uuids.contains(modelElementUuid)) {
                                need2CloseEditorRefs.add(editorRef);
                            }
                        }
                    }
                }
            }
        } catch (PartInitException e) {
            log.warn(e, e);
        }

        if (need2CloseEditorRefs.size() > 0) {
            CorePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .closeEditors(need2CloseEditorRefs.toArray(new IEditorReference[need2CloseEditorRefs.size()]), save);

        }
    }

    /**
     * 
     * Get repostiroy object CRUD class according to project type.
     * 
     * @return
     */
    public static IRepositoryObjectCRUD getRepositoryObjectCRUD() {
        if (ProxyRepositoryManager.getInstance().isLocalProject()) {
            return new LocalRepositoryObjectCRUD();
        } else {
            return new RemoteRepositoryObjectCRUD();
        }
    }

    /**
     * when the report's user defined template(Jrxml file) changed its name, or be moved, the path info in the report's
     * anaMap: jrxml source should also be updated. This method is used to update the related reports when the jrxml
     * name or path is changed.
     * 
     * @param oldPath : the whole path with whole name of the jrxml, e.g./TDQ_Libraries/JRXML
     * Template/columnset/column_set_basic_0.1.jrxml
     * @param newPath
     * @throws PersistenceException
     */
    public static ReturnCode updateJrxmlRelatedReport(IPath oldPath, IPath newPath) {
        if (oldPath == null || newPath == null) {
            ReturnCode rc = new ReturnCode();
            rc.setOk(Boolean.FALSE);
            rc.setMessage(DefaultMessagesImpl.getString("RepNodeUtils.updateReport.empty"));//$NON-NLS-1$ //$NON-NLS-2$
            return rc;
        }
        IPath makeRelativeTo = newPath.makeRelativeTo(ResourceManager.getRootProject().getLocation()).removeFirstSegments(1);

        List<String> jrxmlFileNames = new ArrayList<String>();
        List<String> jrxmlFileNamesAfterMove = new ArrayList<String>();
        jrxmlFileNames.add(oldPath.toOSString());
        jrxmlFileNamesAfterMove.add(makeRelativeTo.toOSString());

        return updateJrxmlRelatedReport(jrxmlFileNames, jrxmlFileNamesAfterMove);
    }

    /**
     * check if the anaMap comtains the Jrxml or not, by compare the jrxml's path with anaMap's jrxml source(when user
     * mode)
     * 
     * @param path contain the file name like:/TDQ_Libraries/JRXMLTemplate/column/column_basic_0.1.jrxml
     * @param anaMap
     * @return true :if the anaMap contains the path.
     */
    private static boolean isUsedByJrxml(IPath path, AnalysisMap anaMap) {
        ReportType reportType = ReportHelper.ReportType.getReportType(anaMap.getAnalysis(), anaMap.getReportType());
        // compare the Jrxml path if the report has the user defined one.
        if (ReportHelper.ReportType.USER_MADE.equals(reportType)) {
            String jrxmlPath = anaMap.getJrxmlSource();
            String oldPath = path.removeFirstSegments(2).toString();
            if (jrxmlPath.contains(oldPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * when the report's user defined template(Jrxml file) changed its name, or be moved, the path info in the report's
     * anaMap: jrxml source should also be updated. This method is used to update the related reports when the jrxml
     * name or path is changed.
     * 
     * @param jrxmlFileNames : the whole path with whole name of the jrxml, e.g./TDQ_Libraries/JRXML
     * Template/columnset/column_set_basic_0.1.jrxml
     * @param jrxmlFileNamesAfterMove
     * @return  ReturnCode.ok if suceed; ko, if any exception.
     */
    public static ReturnCode updateJrxmlRelatedReport(List<String> jrxmlFileNames, List<String> jrxmlFileNamesAfterMove) {
        ReturnCode rc = new ReturnCode();

        if (jrxmlFileNames.size() == 0 || jrxmlFileNamesAfterMove.size() < jrxmlFileNames.size()) {
            rc.setOk(Boolean.FALSE);
            rc.setMessage(DefaultMessagesImpl.getString("RepNodeUtils.updateReport.empty"));//$NON-NLS-1$ //$NON-NLS-2$
            return rc;
        }

        Project project = ProjectManager.getInstance().getCurrentProject();
        // get all reports
        IRepositoryNode ReportRootFolderNode = RepositoryNodeHelper.getDataProfilingFolderNode(EResourceConstant.REPORTS);
        List<ReportRepNode> repNodes = RepositoryNodeHelper.getReportRepNodes(ReportRootFolderNode, true, true);
        // go through every report to :if any one used current jrxml-->modify its jrxml resource name
        for (ReportRepNode report : repNodes) {
            boolean isUpdated = false;
            EList<AnalysisMap> analysisMap = ((TdReport) report.getReport()).getAnalysisMap();
            for (AnalysisMap anaMap : analysisMap) {
                for (int i = 0; i < jrxmlFileNames.size(); i++) {
                    String oldPath = jrxmlFileNames.get(i);
                    if (isUsedByJrxml(new Path(oldPath), anaMap)) {
                        // IPath makeRelativeTo = new Path(jrxmlFileNamesAfterMove.get(i)).makeRelativeTo(
                        // ResourceManager.getRootProject().getLocation()).removeFirstSegments(1);

                        // Added 20130128, using event/listener to refresh the page if opening
                        EventManager.getInstance().publish(report, EventEnum.DQ_JRXML_RENAME, jrxmlFileNamesAfterMove.get(i));

                        anaMap.setJrxmlSource(jrxmlFileNamesAfterMove.get(i));
                        isUpdated = true;
                    }
                }
            }
            if (isUpdated) {
                try {
                    ProxyRepositoryFactory.getInstance().save(project, report.getObject().getProperty().getItem());
                } catch (PersistenceException e) {
                    rc.setOk(Boolean.FALSE);
                    rc.setMessage(DefaultMessagesImpl.getString("RepNodeUtils.updateReport.fail", report.getLabel()));//$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        return rc;
    }

    /**
     * get the edtor if it is opened.
     * 
     * @param node the node which need to check: if opened find its editor
     * @return the opened editor of the node, null: if the node is no editor opened.
     
    public static IRepositoryNode getOpenedEditor(IRepositoryNode node) {

    }*/

}
