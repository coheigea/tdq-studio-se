// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.emf.EMFSharedResources;
import org.talend.commons.emf.EMFUtil;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.manager.DQStructureManager;
import org.talend.dataquality.domain.pattern.Pattern;
import org.talend.dataquality.domain.pattern.util.PatternSwitch;

/**
 * DOC rli class global comment. Detailled comment
 */
public final class PatternResourceFileHelper extends ResourceFileMap {

    private static Logger log = Logger.getLogger(PatternResourceFileHelper.class);

    private static PatternResourceFileHelper instance;

    private Map<IFile, Pattern> patternsMap = new HashMap<IFile, Pattern>();

    private PatternResourceFileHelper() {
        super();
    }

    public static PatternResourceFileHelper getInstance() {
        if (instance == null) {
            instance = new PatternResourceFileHelper();
        }
        return instance;
    }

    public Collection<Pattern> getAllAnalysis() {
        if (resourceChanged) {
            patternsMap.clear();
            IFolder defaultAnalysFolder = ResourcesPlugin.getWorkspace().getRoot().getProject(
                    PluginConstant.DATA_PROFILING_PROJECTNAME).getFolder(DQStructureManager.ANALYSIS);
            try {
                searchAllAnalysis(defaultAnalysFolder);
            } catch (CoreException e) {
                e.printStackTrace();
            }
            resourceChanged = false;
        }
        return patternsMap.values();
    }

    private void searchAllAnalysis(IFolder folder) throws CoreException {
        for (IResource resource : folder.members()) {
            if (resource.getType() == IResource.FOLDER) {
                searchAllAnalysis(folder.getFolder(resource.getName()));
            }
            IFile file = (IFile) resource;
            findPattern(file);

        }
    }

    /**
     * DOC xy Comment method "findPathAnalysis".
     * 
     * @param file
     * @return
     */
    public Pattern findPattern(IFile file) {
        Pattern pattern = patternsMap.get(file);
        if (pattern != null) {
            return pattern;
        }
        Resource fileResource = getFileResource(file);
        pattern = retirePattern(fileResource);
        if (pattern != null) {
            patternsMap.put(file, pattern);
        }
        return pattern;
    }

    /**
     * DOC rli Comment method "retireAnalysis".
     * 
     * @param fileResource
     * @return
     */
    private Pattern retirePattern(Resource fileResource) {
        EList<EObject> contents = fileResource.getContents();
        if (contents.isEmpty()) {
            log.error("No content in " + fileResource);
        }
        if (log.isDebugEnabled()) {
            log.debug("Nb elements in contents " + contents.size());
        }
        PatternSwitch<Pattern> mySwitch = new PatternSwitch<Pattern>() {

            public Pattern casePattern(Pattern object) {
                return object;
            }
        };
        Pattern pattern = null;
        if (contents != null && contents.size() != 0) {
            pattern = mySwitch.doSwitch(contents.get(0));
        }
        return pattern;
    }

    public void remove(IFile file) {
        super.remove(file);
        this.patternsMap.remove(file);
    }

    public void clear() {
        super.clear();
        this.patternsMap.clear();
    }

    @SuppressWarnings("static-access")
    public boolean save(Pattern pattern) {
        EMFUtil sharedEmfUtil = EMFSharedResources.getSharedEmfUtil();
        boolean saved = sharedEmfUtil.saveSingleResource(pattern.eResource());
        if (saved) {
            setResourceChanged(true);
        }
        return saved;
    }
}
