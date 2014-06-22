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
package org.talend.dataprofiler.core.ui.editor.analysis;

import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.database.JavaSqlFactory;
import org.talend.core.model.metadata.builder.database.dburl.SupportDBUrlStore;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.cwm.helper.CatalogHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.TableHelper;
import org.talend.cwm.relational.TdTable;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.model.OverviewIndUIElement;
import org.talend.dataprofiler.core.model.SqlExplorerBridge;
import org.talend.dataprofiler.core.ui.ColumnSortListener;
import org.talend.dataprofiler.core.ui.action.actions.AnalyzeColumnSetAction;
import org.talend.dataprofiler.core.ui.action.actions.OverviewAnalysisAction;
import org.talend.dataquality.analysis.AnalysisType;
import org.talend.dataquality.analysis.ExecutionInformations;
import org.talend.dataquality.domain.Domain;
import org.talend.dataquality.exception.DataprofilerCoreException;
import org.talend.dataquality.helpers.AnalysisHelper;
import org.talend.dataquality.helpers.DomainHelper;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.schema.CatalogIndicator;
import org.talend.dataquality.indicators.schema.SchemaIndicator;
import org.talend.dataquality.indicators.schema.TableIndicator;
import org.talend.dataquality.indicators.schema.ViewIndicator;
import org.talend.dataquality.properties.TDQAnalysisItem;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.nodes.DBTableFolderRepNode;
import org.talend.dq.writer.impl.ElementWriterFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.Schema;

/**
 * DOC rli class global comment. Detailled comment
 */
public abstract class AbstractFilterMetadataPage extends AbstractAnalysisMetadataPage {

    private static Logger log = Logger.getLogger(ConnectionMasterDetailsPage.class);

    private static final String SCHEMA = DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.schema"); //$NON-NLS-1$

    private static final String CATALOG = DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.catalog"); //$NON-NLS-1$

    private SchemaTableSorter[][] tableSorters = {
            { new SchemaTableSorter(SchemaTableSorter.TABLE), new SchemaTableSorter(-SchemaTableSorter.TABLE) },
            { new SchemaTableSorter(SchemaTableSorter.ROWS), new SchemaTableSorter(-SchemaTableSorter.ROWS) },
            { new SchemaTableSorter(SchemaTableSorter.KEYS), new SchemaTableSorter(-SchemaTableSorter.KEYS) },
            { new SchemaTableSorter(SchemaTableSorter.INDEXES), new SchemaTableSorter(-SchemaTableSorter.INDEXES) } };

    private SchemaViewSorter[][] viewSorters = {
            { new SchemaViewSorter(SchemaViewSorter.VIEW), new SchemaViewSorter(-SchemaViewSorter.VIEW) },
            { new SchemaViewSorter(SchemaViewSorter.ROWS), new SchemaViewSorter(-SchemaViewSorter.ROWS) } };

    private SchemaSorter[][] schemaSorters = { { new SchemaSorter(SchemaSorter.SCHEMA), new SchemaSorter(-SchemaSorter.SCHEMA) },
            { new SchemaSorter(SchemaSorter.ROWS), new SchemaSorter(-SchemaSorter.ROWS) },
            { new SchemaSorter(SchemaSorter.TABLES), new SchemaSorter(-SchemaSorter.TABLES) },
            { new SchemaSorter(SchemaSorter.ROWS_TABLES), new SchemaSorter(-SchemaSorter.ROWS_TABLES) },
            { new SchemaSorter(SchemaSorter.VIEWS), new SchemaSorter(-SchemaSorter.VIEWS) },
            { new SchemaSorter(SchemaSorter.ROWS_VIEWS), new SchemaSorter(-SchemaSorter.ROWS_VIEWS) },
            { new SchemaSorter(SchemaSorter.KEYS), new SchemaSorter(-SchemaSorter.KEYS) },
            { new SchemaSorter(SchemaSorter.INDEXES), new SchemaSorter(-SchemaSorter.INDEXES) } };

    private CatalogWithSchemaSorter[][] catalogWithSchemaSorters = {
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.CATALOG),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.CATALOG) },
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.ROWS),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.ROWS) },
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.SCHEMAS),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.SCHEMAS) },
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.ROWS_SCHEMAS),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.ROWS_SCHEMAS) },
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.TABLES),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.TABLES) },
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.ROWS_TABLES),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.ROWS_TABLES) },
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.VIEWS),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.VIEWS) },
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.ROWS_VIEWS),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.ROWS_VIEWS) },
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.KEYS),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.KEYS) },
            { new CatalogWithSchemaSorter(CatalogWithSchemaSorter.INDEXES),
                    new CatalogWithSchemaSorter(-CatalogWithSchemaSorter.INDEXES) } };

    private static final int TABLE_COLUMN_INDEX = 0;

    private static final int VIEW_COLUMN_INDEX = 2;

    private static final int VIEW_COLUMN_INDEXES = 3;

    private static final int COLUMN_TABLE_WIDTH = 100;

    private static final int COLUMN_VIEW_WIDTH = 150;

    /**
     * Width of the first column.
     */
    private static final int COL1_WIDTH = 200;

    /**
     * Width of columns.
     */
    private static final int COL_WIDTH = 100;

    private Text tableFilterText;

    private Text viewFilterText;

    protected Connection tdDataProvider;

    private String latestTableFilterValue;

    private String latestViewFilterValue;

    private TableViewer catalogTableViewer;

    private Composite sumSectionClient;

    private Composite tableAndViewComposite;

    private ScrolledForm form;

    private TableViewer tableOfCatalogOrSchemaViewer;

    private TableViewer viewOfCatalogOrSchemaViewer;

    private SchemaIndicator currentSelectionSchemaIndicator;

    private Section analysisParamSection = null;

    private Section summarySection = null;

    private Section statisticalSection = null;

    private Button reloadDatabasesBtn = null;

    private SchemaIndicator currentCatalogIndicator = null; // used in sqlserver

    class DisplayTableAndViewListener implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            StructuredSelection selection = (StructuredSelection) event.getSelection();
            OverviewIndUIElement firstElement = (OverviewIndUIElement) selection.getFirstElement();
            if (firstElement != null) {
                // TableViewer viewer = (TableViewer) event.getSource();

                IRepositoryNode node = firstElement.getNode();
                currentSelectionSchemaIndicator = (SchemaIndicator) firstElement.getOverviewIndicator();
                if (currentSelectionSchemaIndicator == null) {
                    return;
                }
                displayTableAndViewComp(currentSelectionSchemaIndicator, node);
            }
        }
    }

    /**
     * The provider for display the data of catalog table viewer.
     * 
     * FIXME this inner class should be static. Confirm and fix the error.
     */
    class CatalogViewerProvier extends AbstractStatisticalViewerProvider {

        @Override
        protected String getOtherColumnText(int columnIndex, SchemaIndicator schemaIndicator) {
            String text = PluginConstant.EMPTY_STRING;
            switch (columnIndex) {
            case 1:
                return text + schemaIndicator.getTableRowCount();
            case 2:
                return text + schemaIndicator.getTableCount();
            case 3:
                return text + getRowCountPerTable(schemaIndicator);
            case 4:
                return text + schemaIndicator.getViewCount();
            case 5:
                return text + getRowCountPerView(schemaIndicator);
            case 6:
                return text + schemaIndicator.getKeyCount();
            case 7:
                return text + schemaIndicator.getIndexCount();
            default:
                break;
            }
            return text;
        }
    }

    /**
     * The provider for display the data of schema table viewer.
     * 
     * FIXME this inner class should be static. Confirm and fix the error.
     */
    class SchemaViewerProvier extends AbstractStatisticalViewerProvider {

        @Override
        protected String getOtherColumnText(int columnIndex, SchemaIndicator schemaIndicator) {
            String text = PluginConstant.EMPTY_STRING;
            switch (columnIndex) {
            case 2:
                return text + schemaIndicator.getTableCount();
            case 3:
                return text + getRowCountPerTable(schemaIndicator);
            case 4:
                return text + schemaIndicator.getViewCount();
            case 5:
                return text + getRowCountPerView(schemaIndicator);
            case 6:
                return text + schemaIndicator.getKeyCount();
            case 7:
                return text + schemaIndicator.getIndexCount();
            default:
                break;
            }
            return text;
        }
    }

    /**
     * The provider for display the data of catalog(contain schemas) table viewer.
     * 
     * FIXME this inner class should be static. Confirm and fix the error.
     */
    class CatalogSchemaViewerProvier extends AbstractStatisticalViewerProvider {

        @Override
        protected String getOtherColumnText(int columnIndex, SchemaIndicator schemaIndicator) {
            String text = PluginConstant.EMPTY_STRING;
            CatalogIndicator catalogIndicator = null;
            if (schemaIndicator instanceof CatalogIndicator) {
                catalogIndicator = (CatalogIndicator) schemaIndicator;
            } else {
                return PluginConstant.EMPTY_STRING;
            }
            switch (columnIndex) {
            case 2:
                return text + catalogIndicator.getSchemaCount();
            case 3:
                return text + getRowCountPerSchema(catalogIndicator);
            case 4:
                return text + catalogIndicator.getTableCount();
            case 5:
                return text + getRowCountPerTable(catalogIndicator);
            case 6:
                return text + schemaIndicator.getViewCount();
            case 7:
                return text + getRowCountPerView(schemaIndicator);
            case 8:
                return text + catalogIndicator.getKeyCount();
            case 9:
                return text + catalogIndicator.getIndexCount();
            default:
                break;
            }
            return text;
        }
    }

    protected static String getRowCountPerTable(SchemaIndicator schemaIndicator) {
        Double frac = Double.valueOf(schemaIndicator.getTableRowCount()) / schemaIndicator.getTableCount();
        return formatDouble(frac);
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        form = managedForm.getForm();
        form.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.connectionAnalysis")); //$NON-NLS-1$
        this.metadataSection.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.analysisMeta")); //$NON-NLS-1$
        this.metadataSection.setDescription(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.setAnalysisProp")); //$NON-NLS-1$
        createAnalysisParamSection(topComp);
        createAnalysisSummarySection(topComp);
        createStatisticalSection(topComp);
    }

    private void createAnalysisParamSection(Composite topComp) {
        analysisParamSection = createSection(form, topComp,
                DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.analysisParameter")); //$NON-NLS-1$
        Composite sectionClient = toolkit.createComposite(analysisParamSection);
        sectionClient.setLayout(new GridLayout(1, false));

        Composite comp1 = new Composite(sectionClient, SWT.NONE);
        comp1.setLayout(new GridLayout(1, false));
        this.createAnalysisLimitComposite(comp1);

        Composite comp2 = new Composite(sectionClient, SWT.NONE);
        comp2.setLayout(new GridLayout(2, false));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(comp2);
        Label tableFilterLabel = new Label(comp2, SWT.None);
        tableFilterLabel.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.filterOnTable")); //$NON-NLS-1$
        tableFilterLabel.setLayoutData(new GridData());
        tableFilterText = new Text(comp2, SWT.BORDER);
        EList<Domain> dataFilters = analysis.getParameters().getDataFilter();
        String tablePattern = DomainHelper.getTablePattern(dataFilters);
        latestTableFilterValue = tablePattern == null ? PluginConstant.EMPTY_STRING : tablePattern;
        tableFilterText.setText(latestTableFilterValue);
        tableFilterText.setToolTipText(DefaultMessagesImpl.getString("AbstractFilterMetadataPage.FilterTables")); //$NON-NLS-1$
        GridDataFactory.fillDefaults().grab(true, false).applyTo(tableFilterText);
        tableFilterText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                setDirty(true);
            }

        });
        Label viewFilterLabel = new Label(comp2, SWT.None);
        viewFilterLabel.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.filterOnView")); //$NON-NLS-1$
        viewFilterLabel.setLayoutData(new GridData());
        viewFilterText = new Text(comp2, SWT.BORDER);
        String viewPattern = DomainHelper.getViewPattern(dataFilters);
        latestViewFilterValue = viewPattern == null ? PluginConstant.EMPTY_STRING : viewPattern;
        viewFilterText.setText(latestViewFilterValue);
        viewFilterText.setToolTipText(DefaultMessagesImpl.getString("AbstractFilterMetadataPage.FilterViews")); //$NON-NLS-1$
        GridDataFactory.fillDefaults().grab(true, false).applyTo(viewFilterText);
        viewFilterText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                setDirty(true);
            }

        });

        // ADD yyi 2011-05-31 16158:add whitespace check for text fields.
        addWhitespaceValidate(tableFilterText, viewFilterText);
        // ADD xqliu 2010-01-04 bug 10190
        createReloadDatabasesButton(comp2);
        // ~
        analysisParamSection.setClient(sectionClient);
    }

    /**
     * DOC xqliu Comment method "createReloadDatabasesButton".
     * 
     * @param sectionClient
     */
    private void createReloadDatabasesButton(Composite sectionClient) {
        if (isConnectionAnalysis()) {
            reloadDatabasesBtn = new Button(sectionClient, SWT.CHECK);
            reloadDatabasesBtn.setText(DefaultMessagesImpl.getString("AbstractFilterMetadataPage.ReloadDatabases"));//$NON-NLS-1$

            reloadDatabasesBtn.setSelection(AnalysisHelper.getReloadDatabases(analysis));
            reloadDatabasesBtn.addMouseListener(new MouseListener() {

                public void mouseDoubleClick(MouseEvent e) {
                }

                public void mouseDown(MouseEvent e) {
                    setDirty(true);
                }

                public void mouseUp(MouseEvent e) {
                }
            });
        }
    }

    /**
     * DOC xqliu Comment method "isConnectionAnalysis".
     * 
     * @return
     */
    private boolean isConnectionAnalysis() {
        if (analysis != null) {
            return AnalysisType.CONNECTION.equals(AnalysisHelper.getAnalysisType(analysis));
        }
        return false;
    }

    private void createAnalysisSummarySection(Composite topComp) {
        summarySection = this.createSection(form, topComp,
                DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.analysisSummary")); //$NON-NLS-1$
        sumSectionClient = toolkit.createComposite(summarySection);
        sumSectionClient.setLayout(new GridLayout(2, false));
        refreshSumSection();
        summarySection.setClient(sumSectionClient);
    }

    /**
     * DOC qzhang Comment method "refreshSumSection".
     * 
     * @param summarySection
     */
    private void refreshSumSection() {
        fillDataProvider();

        if (tdDataProvider == null) {
            return;
        }

        if (sumSectionClient != null && !sumSectionClient.isDisposed()) {
            Control[] children = sumSectionClient.getChildren();
            for (Control control : children) {
                control.dispose();
            }
        }
        Composite leftComp = new Composite(sumSectionClient, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(leftComp);
        leftComp.setLayout(new GridLayout());
        Composite rightComp = new Composite(sumSectionClient, SWT.NONE);
        rightComp.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(rightComp);
        String connectionStr = JavaSqlFactory.getURL(tdDataProvider);
        Properties pameterProperties = SupportDBUrlStore.getInstance().getDBPameterProperties(connectionStr);
        String labelContent = pameterProperties
                .getProperty(org.talend.core.model.metadata.builder.database.PluginConstant.DBTYPE_PROPERTY);
        Label leftLabel = new Label(leftComp, SWT.NONE);
        leftLabel
                .setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.DBMS") + (labelContent == null ? PluginConstant.EMPTY_STRING : labelContent)); //$NON-NLS-1$
        leftLabel.setLayoutData(new GridData());
        leftLabel = new Label(leftComp, SWT.NONE);
        labelContent = pameterProperties
                .getProperty(org.talend.core.model.metadata.builder.database.PluginConstant.HOSTNAME_PROPERTY);
        leftLabel
                .setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.server") + (labelContent == null ? PluginConstant.EMPTY_STRING : labelContent)); //$NON-NLS-1$
        leftLabel.setLayoutData(new GridData());
        leftLabel = new Label(leftComp, SWT.NONE);
        labelContent = pameterProperties
                .getProperty(org.talend.core.model.metadata.builder.database.PluginConstant.PORT_PROPERTY);
        leftLabel
                .setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.port") + (labelContent == null ? PluginConstant.EMPTY_STRING : labelContent)); //$NON-NLS-1$
        leftLabel.setLayoutData(new GridData());
        leftLabel = new Label(leftComp, SWT.NONE);
        labelContent = JavaSqlFactory.getUsername(tdDataProvider);
        leftLabel
                .setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.connectAs") + (labelContent == null ? PluginConstant.EMPTY_STRING : labelContent)); //$NON-NLS-1$
        leftLabel.setLayoutData(new GridData());

        List<Catalog> tdCatalogs = getCatalogs();
        List<Schema> tdSchema = ConnectionHelper.getSchema(tdDataProvider);
        leftLabel = new Label(leftComp, SWT.NONE);
        leftLabel.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.catalogs", tdCatalogs.size())); //$NON-NLS-1$
        leftLabel.setLayoutData(new GridData());
        leftLabel = new Label(leftComp, SWT.NONE);
        leftLabel.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.schemata", tdSchema.size())); //$NON-NLS-1$
        leftLabel.setLayoutData(new GridData());
        // MOD qiongli 2011-5-16
        DataManager connection = this.analysis.getContext().getConnection();
        if (connection != null) {
            RepositoryNode connNode = RepositoryNodeHelper.recursiveFind(connection);
            if (connNode != null && connNode.getObject().isDeleted()) {
                leftLabel = new Label(leftComp, SWT.NONE);
                leftLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
                leftLabel.setText(DefaultMessagesImpl.getString("AbstractPagePart.LogicalDeleteWarn", connNode.getLabel())); //$NON-NLS-1$
                leftLabel.setLayoutData(new GridData());
            }
        }

        ExecutionInformations resultMetadata = analysis.getResults().getResultMetadata();

        Label rightLabel = new Label(rightComp, SWT.NONE);
        rightLabel.setText(DefaultMessagesImpl.getString(
                "ConnectionMasterDetailsPage.createionDate", getFormatDateStr(analysis.getCreationDate()))); //$NON-NLS-1$
        rightLabel.setLayoutData(new GridData());
        rightLabel = new Label(rightComp, SWT.NONE);
        rightLabel.setText(DefaultMessagesImpl.getString(
                "ConnectionMasterDetailsPage.executionDate", getFormatDateStr(resultMetadata.getExecutionDate()))); //$NON-NLS-1$
        rightLabel.setLayoutData(new GridData());
        rightLabel = new Label(rightComp, SWT.NONE);
        rightLabel.setText(DefaultMessagesImpl.getString(
                "ConnectionMasterDetailsPage.executionDuration", resultMetadata.getExecutionDuration() / 1000.0d)); //$NON-NLS-1$ //$NON-NLS-2$
        rightLabel.setLayoutData(new GridData());

        rightLabel = new Label(rightComp, SWT.NONE);
        String executeStatus = (resultMetadata.isLastRunOk() ? DefaultMessagesImpl
                .getString("ConnectionMasterDetailsPage.success") : DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.failure", resultMetadata.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
        rightLabel.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.executionStatus") //$NON-NLS-1$
                + (resultMetadata.getExecutionNumber() == 0 ? PluginConstant.EMPTY_STRING : executeStatus));
        if (!resultMetadata.isLastRunOk()) {
            rightLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        }
        rightLabel.setLayoutData(new GridData());
        rightLabel = new Label(rightComp, SWT.NONE);
        rightLabel.setText(DefaultMessagesImpl.getString(
                "ConnectionMasterDetailsPage.number", resultMetadata.getExecutionNumber())); //$NON-NLS-1$
        rightLabel.setLayoutData(new GridData());
        rightLabel = new Label(rightComp, SWT.NONE);
        rightLabel.setText(DefaultMessagesImpl.getString(
                "ConnectionMasterDetailsPage.successExecution", getFormatDateStr(resultMetadata.getExecutionDate()))); //$NON-NLS-1$
        rightLabel.setLayoutData(new GridData());
        sumSectionClient.layout();
    }

    /**
     * DOC rli Comment method "fillDataProvider".
     */
    protected abstract void fillDataProvider();

    private String getFormatDateStr(Date date) {
        if (date == null) {
            return PluginConstant.EMPTY_STRING;
        }
        String format = SimpleDateFormat.getDateTimeInstance().format(date);
        return format;
    }

    private AbstractStatisticalViewerProvider provider;

    private TableViewer schemaTableViewer;

    private void createStatisticalSection(Composite topComp) {
        statisticalSection = this.createSection(form, topComp,
                DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.statisticalinformations"), null); //$NON-NLS-1$
        Composite sectionClient = toolkit.createComposite(statisticalSection);
        sectionClient.setLayout(new GridLayout());

        catalogTableViewer = new TableViewer(sectionClient, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        Table table = catalogTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setBackgroundMode(SWT.INHERIT_FORCE);
        table.setLinesVisible(true);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(table);
        List<Catalog> catalogs = getCatalogs();
        boolean containSubSchema = false;
        for (Catalog catalog : catalogs) {
            List<Schema> schemas = CatalogHelper.getSchemas(catalog);
            if (schemas.size() > 0) {
                containSubSchema = true;
                break;
            }
        }

        if (catalogs.size() > 0 && containSubSchema) {
            createCatalogSchemaColumns(table);
            provider = new CatalogSchemaViewerProvier();
            addColumnSorters(catalogTableViewer, catalogTableViewer.getTable().getColumns(), catalogWithSchemaSorters);
            schemaTableViewer = createSecondStatisticalTable(sectionClient);
            schemaTableViewer.addSelectionChangedListener(new DisplayTableAndViewListener());
            catalogTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    StructuredSelection selection = (StructuredSelection) event.getSelection();
                    OverviewIndUIElement firstElement = (OverviewIndUIElement) selection.getFirstElement();
                    List<OverviewIndUIElement> cataUIEleList = new ArrayList<OverviewIndUIElement>();
                    if (firstElement != null) {
                        Indicator overviewIndicator = firstElement.getOverviewIndicator();
                        CatalogIndicator catalogIndicator = (CatalogIndicator) overviewIndicator;// selection.getFirstElement();
                        // MOD qiongli bug 13093,2010-7-2,
                        currentCatalogIndicator = (SchemaIndicator) overviewIndicator; // selection.getFirstElement();
                        // MOD xqliu 2009-11-30 bug 9114
                        if (catalogIndicator != null) {
                            EList<SchemaIndicator> schemaIndicators = catalogIndicator.getSchemaIndicators();
                            for (SchemaIndicator schemaIndicator : schemaIndicators) {
                                RepositoryNode schemaNode = RepositoryNodeHelper.recursiveFind(schemaIndicator
                                        .getAnalyzedElement());
                                OverviewIndUIElement cataUIEle = new OverviewIndUIElement();
                                cataUIEle.setNode(schemaNode);
                                cataUIEle.setOverviewIndicator(schemaIndicator);
                                cataUIEleList.add(cataUIEle);
                            }
                            // schemaTableViewer.setInput(catalogIndicator.getSchemaIndicators());
                            schemaTableViewer.setInput(cataUIEleList);
                            schemaTableViewer.getTable().setVisible(true);
                            addColumnSorters(schemaTableViewer, schemaTableViewer.getTable().getColumns(), schemaSorters);
                        }
                    }
                    // ~
                }

            });
            createContextMenuFor(schemaTableViewer);
        } else {
            if (catalogs.size() > 0) {
                createCatalogTableColumns(table);
                provider = new CatalogViewerProvier();
            } else {
                createSchemaTableColumns(table);
                provider = new SchemaViewerProvier();
            }
            addColumnSorters(catalogTableViewer, catalogTableViewer.getTable().getColumns(), schemaSorters);
            catalogTableViewer.addSelectionChangedListener(new DisplayTableAndViewListener());
        }
        catalogTableViewer.setLabelProvider(provider);
        catalogTableViewer.setContentProvider(provider);
        doSetInput();

        tableAndViewComposite = new Composite(sectionClient, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tableAndViewComposite);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.horizontalSpacing = 50;
        tableAndViewComposite.setLayout(layout);
        tableAndViewComposite.setVisible(false);

        sectionClient.layout();
        statisticalSection.setClient(sectionClient);

        createContextMenuFor(catalogTableViewer);
    }

    /**
     * DOC xqliu Comment method "addColumnSorters".
     * 
     * @param tableViewer
     * @param tableColumns
     * @param sorters
     */
    protected void addColumnSorters(TableViewer tableViewer, TableColumn[] tableColumns, ViewerSorter[][] sorters) {
        for (int i = 0; i < tableColumns.length; ++i) {
            tableColumns[i].addSelectionListener(new ColumnSortListener(tableColumns, i, tableViewer, sorters));
        }
    }

    /**
     * DOC rli Comment method "getCatalogs".
     * 
     * @return
     */
    protected abstract List<Catalog> getCatalogs();

    /**
     * DOC qzhang Comment method "doSetInput".
     */
    public void doSetInput() {
        List<OverviewIndUIElement> indicatorList = null;
        if (this.analysis.getResults().getIndicators().size() > 0) {
            indicatorList = getCatalogIndicators();
            if (indicatorList.size() == 0) {
                catalogTableViewer.setInput(getSchemaIndicators());
            } else {
                // MOD xqliu 2009-11-30 bug 9114
                // List<SchemaIndicator> schemaIndicators = new ArrayList<SchemaIndicator>();
                // schemaIndicators.addAll(getSchemaIndicators());
                // schemaIndicators.addAll(indicatorList);
                // catalogTableViewer.setInput(schemaIndicators);
                catalogTableViewer.setInput(indicatorList);
                // ~
            }
        }
    }

    /**
     * DOC rli Comment method "getCatalogIndicators".
     * 
     * @return
     */
    protected abstract List<OverviewIndUIElement> getCatalogIndicators();

    protected abstract List<OverviewIndUIElement> getSchemaIndicators();

    private TableViewer createSecondStatisticalTable(Composite parent) {
        TableViewer secondTableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        Table table = secondTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setBackgroundMode(SWT.INHERIT_FORCE);
        table.setLinesVisible(true);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(table);
        ((GridData) table.getLayoutData()).heightHint = 60;
        createSchemaTableColumns(table);
        SchemaViewerProvier svProvider = new SchemaViewerProvier();
        secondTableViewer.setLabelProvider(svProvider);
        secondTableViewer.setContentProvider(svProvider);
        table.setVisible(false);
        return secondTableViewer;
    }

    private void createCatalogTableColumns(Table table) {
        TableColumn tableColumn = new TableColumn(table, SWT.LEFT | SWT.FILL);
        tableColumn.setText(CATALOG);
        tableColumn.setWidth(COL1_WIDTH);
        createNbRowsCol(table, CATALOG);
        createCommonStatisticalColumns(table);
    }

    private void createSchemaTableColumns(Table table) {
        TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(SCHEMA);
        tableColumn.setWidth(COL1_WIDTH);
        createNbRowsCol(table, SCHEMA);
        createCommonStatisticalColumns(table);
    }

    /**
     * DOC scorreia Comment method "createNbRowsCol".
     * 
     * @param table
     * @param container
     */
    private void createNbRowsCol(Table table, String container) {
        TableColumn tableColumn;
        tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.rows")); //$NON-NLS-1$
        tableColumn.setToolTipText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.numberOfRows", container)); //$NON-NLS-1$
        tableColumn.setWidth(COL_WIDTH);
    }

    private void createCatalogSchemaColumns(Table table) {
        TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(CATALOG);
        tableColumn.setWidth(COL1_WIDTH);
        createNbRowsCol(table, CATALOG);
        tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.otherSchemata")); //$NON-NLS-1$
        tableColumn.setWidth(COL_WIDTH);
        tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.rows_schema")); //$NON-NLS-1$
        tableColumn.setToolTipText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.numberofRow")); //$NON-NLS-1$
        tableColumn.setWidth(COL_WIDTH);
        createCommonStatisticalColumns(table);
    }

    /**
     * DOC scorreia Comment method "createCommonStatisticalColumns".
     * 
     * @param table
     */
    private void createCommonStatisticalColumns(Table table) {
        TableColumn tableColumn;
        tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.table")); //$NON-NLS-1$
        tableColumn.setToolTipText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.numberOfTable")); //$NON-NLS-1$
        tableColumn.setWidth(COL_WIDTH);
        tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.rows_table")); //$NON-NLS-1$
        tableColumn.setToolTipText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.numberOfRowPerTable")); //$NON-NLS-1$
        tableColumn.setWidth(COL_WIDTH);
        tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.view")); //$NON-NLS-1$
        tableColumn.setToolTipText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.numberOfViews")); //$NON-NLS-1$
        tableColumn.setWidth(COL_WIDTH);
        tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.rows_view")); //$NON-NLS-1$
        tableColumn.setToolTipText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.nubmerOfRowsPerView")); //$NON-NLS-1$
        tableColumn.setWidth(COL_WIDTH);
        tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.key")); //$NON-NLS-1$
        tableColumn.setToolTipText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.numberOfPrimaryKeys")); //$NON-NLS-1$
        tableColumn.setWidth(COL_WIDTH);
        tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.indexes")); //$NON-NLS-1$
        tableColumn.setToolTipText(DefaultMessagesImpl.getString("ConnectionMasterDetailsPage.numberOfIndexes")); //$NON-NLS-1$
        tableColumn.setWidth(COL_WIDTH);
    }

    public void saveAnalysis() throws DataprofilerCoreException {
        // ADD gdbu 2011-3-3 bug 19179
        // remove the space from analysis name
        //        this.analysis.setName(this.analysis.getName().replace(" ", ""));//$NON-NLS-1$ //$NON-NLS-2$
        // for (Domain domain : this.analysis.getParameters().getDataFilter()) {
        // domain.setName(this.analysis.getName());
        // }
        // ~

        // ADD xqliu 2010-01-04 bug 10190
        if (isConnectionAnalysis()) { // MOD zshen 2010-03-19 bug 12041
            AnalysisHelper.setReloadDatabases(analysis, reloadDatabasesBtn.getSelection());
        }
        // ~

        EList<Domain> dataFilters = analysis.getParameters().getDataFilter();
        if (!this.tableFilterText.getText().equals(DomainHelper.getTablePattern(dataFilters))) {
            DomainHelper.setDataFilterTablePattern(dataFilters, tableFilterText.getText());
            latestTableFilterValue = this.tableFilterText.getText();
        }
        if (!this.viewFilterText.getText().equals(DomainHelper.getViewPattern(dataFilters))) {
            DomainHelper.setDataFilterViewPattern(dataFilters, viewFilterText.getText());
            latestViewFilterValue = this.viewFilterText.getText();
        }
        // ADD xqliu 2010-07-19 bug 14014
        this.updateAnalysisClientDependency();
        // ~ 14014

        // save the number of connections per analysis
        this.saveNumberOfConnectionsPerAnalysis();

        // 2011.1.12 MOD by zhsne to unify anlysis and connection id when saving.
        ReturnCode saved = new ReturnCode(false);
        IEditorInput editorInput = this.getEditorInput();
        if (editorInput instanceof AnalysisItemEditorInput) {
            AnalysisItemEditorInput analysisInput = (AnalysisItemEditorInput) editorInput;
            TDQAnalysisItem tdqAnalysisItem = analysisInput.getTDQAnalysisItem();

            // ADD gdbu 2011-3-3 bug 19179
            tdqAnalysisItem.getProperty().setDisplayName(analysis.getName());
            tdqAnalysisItem.getProperty().setLabel(analysis.getName());
            this.nameText.setText(analysis.getName());
            // ~
            // MOD yyi 2012-02-08 TDQ-4621:Explicitly set true for updating dependencies.
            saved = ElementWriterFactory.getInstance().createAnalysisWrite().save(tdqAnalysisItem, true);
        }
        // MOD yyi 2012-02-03 TDQ-3602:Avoid to rewriting all analyzes after saving, no reason to update all analyzes
        // which is depended in the referred connection.
        // Extract saving log function.
        // @see org.talend.dataprofiler.core.ui.editor.analysis.AbstractAnalysisMetadataPage#logSaved(ReturnCode)
        logSaved(saved);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (PluginConstant.ISDIRTY_PROPERTY.equals(evt.getPropertyName())) {
            ((AnalysisEditor) this.getEditor()).firePropertyChange(IEditorPart.PROP_DIRTY);
        }

    }

    /**
     * 
     * DOC klliu Comment method "displayTableAndViewComp".
     * 
     * @param schemaIndicator
     * @param parentNode
     */
    protected void displayTableAndViewComp(final SchemaIndicator schemaIndicator, final IRepositoryNode parentNode) {
        tableAndViewComposite.setVisible(true);
        // DOC wapperInput retrun OverViewUIElement

        EList<TableIndicator> indicatorTableList = (EList<TableIndicator>) schemaIndicator.getTableIndicators();
        List<OverviewIndUIElement> tableElements = wapperInput(indicatorTableList, parentNode);
        if (tableOfCatalogOrSchemaViewer == null) {
            tableOfCatalogOrSchemaViewer = new TableViewer(tableAndViewComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER
                    | SWT.SINGLE);
            final Table catalogOrSchemaTable = tableOfCatalogOrSchemaViewer.getTable();
            catalogOrSchemaTable.setHeaderVisible(true);
            catalogOrSchemaTable.setLinesVisible(true);
            GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
            layoutData.heightHint = 150;
            catalogOrSchemaTable.setLayoutData(layoutData);
            String[] columnTexts = new String[] {
                    DefaultMessagesImpl.getString("AbstractFilterMetadataPage.Table"), DefaultMessagesImpl.getString("AbstractFilterMetadataPage.rows"), DefaultMessagesImpl.getString("AbstractFilterMetadataPage.keys"), DefaultMessagesImpl.getString("AbstractFilterMetadataPage.indexes") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            createSorterColumns(tableOfCatalogOrSchemaViewer, columnTexts, tableSorters, COLUMN_TABLE_WIDTH);
            TableOfCatalogOrSchemaProvider providerTable = new TableOfCatalogOrSchemaProvider();
            tableOfCatalogOrSchemaViewer.setLabelProvider(providerTable);
            tableOfCatalogOrSchemaViewer.setContentProvider(providerTable);

            final TableCursor cursor = new TableCursor(catalogOrSchemaTable, SWT.NONE);
            cursor.setBackground(catalogOrSchemaTable.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
            cursor.setForeground(catalogOrSchemaTable.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
            cursor.setLayout(new FillLayout());
            // cursor.setVisible(true);
            final Menu menu = new Menu(catalogOrSchemaTable);
            MenuItem keyitem = new MenuItem(menu, SWT.PUSH);
            keyitem.setText(DefaultMessagesImpl.getString("AbstractFilterMetadataPage.ViewKeys")); //$NON-NLS-1$
            keyitem.setImage(ImageLib.getImage(ImageLib.PK_DECORATE));

            final Menu menu1 = new Menu(catalogOrSchemaTable);
            MenuItem indexitem = new MenuItem(menu1, SWT.PUSH);
            indexitem.setText(DefaultMessagesImpl.getString("AbstractFilterMetadataPage.ViewIndexes")); //$NON-NLS-1$
            indexitem.setImage(ImageLib.getImage(ImageLib.INDEX_VIEW));

            final Menu menu2 = new Menu(catalogOrSchemaTable);
            MenuItem tableAnalysisitem = new MenuItem(menu2, SWT.PUSH);
            tableAnalysisitem.setText(DefaultMessagesImpl.getString("CreateTableAnalysisAction.tableAnalysis")); //$NON-NLS-1$
            tableAnalysisitem.setImage(ImageLib.getImage(ImageLib.ACTION_NEW_ANALYSIS));

            // catalogOrSchemaTable.setMenu(menu);
            keyitem.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    TableItem tableItem = cursor.getRow();
                    String tableName = tableItem.getText(0);
                    Package parentPack = (Package) currentSelectionSchemaIndicator.getAnalyzedElement();
                    // MOD qiongli bug 13093,2010-7-2
                    if (currentCatalogIndicator != null)
                        parentPack = (Package) currentCatalogIndicator.getAnalyzedElement();

                    TypedReturnCode<TableNode> findSqlExplorerTableNode = SqlExplorerBridge.findSqlExplorerTableNode(
                            tdDataProvider, parentPack, tableName, Messages.getString("DatabaseDetailView.Tab.PrimaryKeys")); //$NON-NLS-1$

                    if (!findSqlExplorerTableNode.isOk()) {
                        // log.error(findSqlExplorerTableNode.getMessage());
                    }
                }

            });

            indexitem.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    TableItem tableItem = cursor.getRow();
                    String tableName = tableItem.getText(0);
                    Package parentPack = (Package) currentSelectionSchemaIndicator.getAnalyzedElement();
                    // MOD qiongli bug 13093,2010-7-2
                    if (currentCatalogIndicator != null)
                        parentPack = (Package) currentCatalogIndicator.getAnalyzedElement();

                    TypedReturnCode<TableNode> findSqlExplorerTableNode = SqlExplorerBridge.findSqlExplorerTableNode(
                            tdDataProvider, parentPack, tableName, Messages.getString("DatabaseDetailView.Tab.Indexes")); //$NON-NLS-1$

                    if (!findSqlExplorerTableNode.isOk()) {
                        // log.error(findSqlExplorerTableNode.getMessage());
                    }
                }

            });

            tableAnalysisitem.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    TableItem tableItem = cursor.getRow();
                    OverviewIndUIElement data = (OverviewIndUIElement) tableItem.getData();
                    runTableAnalysis(data);
                }

            });

            cursor.addMenuDetectListener(new MenuDetectListener() {

                public void menuDetected(MenuDetectEvent e) {
                    int column = cursor.getColumn();
                    if (column == TABLE_COLUMN_INDEX) {
                        cursor.setMenu(menu2);
                        menu2.setVisible(true);
                    } else if (column == VIEW_COLUMN_INDEXES) {
                        cursor.setMenu(menu1);
                        menu1.setVisible(true);
                    } else if (column == VIEW_COLUMN_INDEX) {
                        cursor.setMenu(menu);
                        menu.setVisible(true);
                    } else {
                        cursor.setMenu(null);
                    }
                }
            });
            viewOfCatalogOrSchemaViewer = new TableViewer(tableAndViewComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER
                    | SWT.FULL_SELECTION);
            Table tableCatalogOrSchemaView = viewOfCatalogOrSchemaViewer.getTable();
            tableCatalogOrSchemaView.setHeaderVisible(true);
            tableCatalogOrSchemaView.setLinesVisible(true);
            layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
            layoutData.heightHint = 150;
            tableCatalogOrSchemaView.setLayoutData(layoutData);
            columnTexts = new String[] {
                    DefaultMessagesImpl.getString("AbstractFilterMetadataPage.view"), DefaultMessagesImpl.getString("AbstractFilterMetadataPage.rows") }; //$NON-NLS-1$ //$NON-NLS-2$
            createSorterColumns(viewOfCatalogOrSchemaViewer, columnTexts, viewSorters, COLUMN_VIEW_WIDTH);
            ViewOfCatalogOrSchemaProvider viewProvider = new ViewOfCatalogOrSchemaProvider();
            viewOfCatalogOrSchemaViewer.setLabelProvider(viewProvider);
            viewOfCatalogOrSchemaViewer.setContentProvider(viewProvider);
        }
        tableOfCatalogOrSchemaViewer.getTable().setMenu(null);
        tableOfCatalogOrSchemaViewer.setInput(tableElements);
        List<ViewIndicator> indicatorViewList = (List<ViewIndicator>) schemaIndicator.getViewIndicators();

        viewOfCatalogOrSchemaViewer.setInput(indicatorViewList);
        // MOD xqliu 2009-11-05 bug 9521
        tableAndViewComposite.pack();
        statisticalSection.pack();
        statisticalSection.layout();
        // ~
        form.reflow(true);

    }

    /**
     * DOC klliu Comment method "wapperInput". relations
     * 
     * @param indicatorTableList
     * @param parentNode
     * @return
     */
    private List<OverviewIndUIElement> wapperInput(EList<TableIndicator> indicatorTableList, IRepositoryNode parentNode) {
        List<OverviewIndUIElement> cataUIEleList = new ArrayList<OverviewIndUIElement>();
        List<IRepositoryNode> children = parentNode.getChildren();
        for (IRepositoryNode folderNode : children) {
            if (folderNode instanceof DBTableFolderRepNode) {
                List<IRepositoryNode> tableNodes = folderNode.getChildren();
                // MOD 20120315 klliu&yyin TDQ-2391, avoid getting many times for table nodes.
                for (TableIndicator indicator : indicatorTableList) {
                    for (IRepositoryNode tableNode : tableNodes) {
                        MetadataTable table = ((MetadataTableRepositoryObject) tableNode.getObject()).getTable();
                        String name = table.getName();
                        String tableName = indicator.getTableName();
                        // String connUuid = ResourceHelper.getUUID(table);
                        // String anaUuid = ResourceHelper.getUUID(indicator.getAnalyzedElement());

                        boolean equals = name.equals(tableName);
                        if (equals) {
                            OverviewIndUIElement tableUIEle = new OverviewIndUIElement();
                            tableUIEle.setNode(tableNode);
                            tableUIEle.setOverviewIndicator(indicator);
                            cataUIEleList.add(tableUIEle);
                            break;
                        }
                    }
                }
            }
        }

        return cataUIEleList;
    }

    private void createSorterColumns(final TableViewer tableViewer, String[] columnTexts, ViewerSorter[][] sorters,
            int columnWidth) {
        Table table = tableViewer.getTable();
        TableColumn[] columns = new TableColumn[columnTexts.length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new TableColumn(table, SWT.LEFT | SWT.FILL);
            columns[i].setText(columnTexts[i]);
            columns[i].setWidth(columnWidth);
            columns[i].addSelectionListener(new ColumnSortListener(columns, i, tableViewer, sorters));
        }
    }

    protected static String getRowCountPerView(SchemaIndicator schemaIndicator) {
        Double frac = Double.valueOf(schemaIndicator.getViewRowCount()) / schemaIndicator.getViewCount();
        return formatDouble(frac);
    }

    protected static String getRowCountPerSchema(CatalogIndicator catalogIndicator) {
        Double frac = Double.valueOf(catalogIndicator.getTableRowCount()) / catalogIndicator.getSchemaCount();
        return formatDouble(frac);
    }

    private static String formatDouble(Double frac) {
        if (frac.isNaN()) {
            return String.valueOf(Double.NaN);
        }
        String formatValue = new DecimalFormat("0.00").format(frac); //$NON-NLS-1$
        return formatValue;
    }

    public AbstractFilterMetadataPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
    }

    public void fireRuningItemChanged(boolean status) {

        currentEditor.setRunActionButtonState(status);

        if (status) {
            refresh();
        }

        statisticalSection.setExpanded(status);
    }

    @Override
    protected ReturnCode canRun() {

        return new ReturnCode(true);
    }

    @Override
    public void refresh() {
        doSetInput();
        refreshSumSection();
        // MOD klliu 2011-05-09 bug 20930
        List<OverviewIndUIElement> catalogIndicators = getCatalogIndicators();
        List<OverviewIndUIElement> schemaIndicators = getSchemaIndicators();
        if (catalogIndicators != null && catalogIndicators.size() == 1) {
            Object cataUIEle = catalogTableViewer.getTable().getItem(0).getData();
            OverviewIndUIElement overviewIndUIElement = catalogIndicators.get(0);
            CatalogIndicator overviewIndicator = (CatalogIndicator) overviewIndUIElement.getOverviewIndicator();
            EList<SchemaIndicator> cataAndSchemaIndicators = overviewIndicator.getSchemaIndicators();
            if (cataAndSchemaIndicators != null) {
                // the count of schema
                int size = cataAndSchemaIndicators.size();
                if (size == 1) {
                    // Case like MS SQL Server
                    // Show schema TableViewer
                    catalogTableViewer.setSelection(new StructuredSelection(catalogIndicators.get(0)));
                    Object catalogAndSchemaUIEle = schemaTableViewer.getTable().getItem(0).getData();
                    schemaTableViewer.setSelection(new StructuredSelection(catalogAndSchemaUIEle));

                } else if (size == 0) {
                    // Case like Mysql
                    catalogTableViewer.setSelection(new StructuredSelection(cataUIEle));
                }
            }

        } else if (schemaIndicators != null && schemaIndicators.size() == 1) {
            // Case like Oracle
            Object schemaUIEle = catalogTableViewer.getTable().getItem(0).getData();
            catalogTableViewer.setSelection(new StructuredSelection(schemaUIEle));
        }
        // ~
    }

    @Override
    public void dispose() {
        if (provider != null) {
            provider.getZeroRowColor().dispose();
        }
        super.dispose();
    }

    private void runTableAnalysis(OverviewIndUIElement data) {

        new AnalyzeColumnSetAction(data.getNode()).run();
    }

    /**
     * DOC yyi Comment method "runTableAnalysis".
     * 
     * @param tableName
     */
    // protected void runTableAnalysis(String tableName) {
    //
    // Package parentPack = (Package) currentSelectionSchemaIndicator.getAnalyzedElement();
    // TdTable tdTable = getTable(parentPack, tableName);
    // if (null == tdTable) {
    // FolderNodeHelper.getTableFolderNode(parentPack).loadChildren();
    // tdTable = getTable(parentPack, tableName);
    // }
    // try {
    // List<TdColumn> columns = 0 == ColumnSetHelper.getColumns(tdTable).size() ? DqRepositoryViewService.getColumns(
    // tdDataProvider, tdTable, null, true) : ColumnSetHelper.getColumns(tdTable);
    //
    // new AnalyzeColumnSetAction(columns.toArray(new TdColumn[columns.size()])).run();
    // } catch (Exception e) {
    // log.error(e.getMessage());
    // e.printStackTrace();
    // }
    // }

    protected void createContextMenuFor(final StructuredViewer viewer) {
        final MenuManager contextMenu = new MenuManager("#PopUp");//$NON-NLS-1$
        contextMenu.add(new Separator("additions"));//$NON-NLS-1$
        contextMenu.setRemoveAllWhenShown(true);
        contextMenu.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager mgr) {
                Object overviewObject = ((StructuredSelection) viewer.getSelection()).getFirstElement();
                if (overviewObject instanceof OverviewIndUIElement) {
                    OverviewIndUIElement overview = (OverviewIndUIElement) overviewObject;
                    IRepositoryNode node = overview.getNode();
                    List<IRepositoryNode> nodes = new ArrayList<IRepositoryNode>();
                    nodes.add(node);
                    contextMenu.add(new OverviewAnalysisAction(nodes));
                }
                // if (obj instanceof SchemaIndicator) {
                // SchemaIndicator schemaIndicator = (SchemaIndicator) obj;
                // contextMenu.add(new OverviewAnalysisAction(new Package[] { (Package)
                // schemaIndicator.getAnalyzedElement() }));
                // }
            }
        });
        Menu menu = contextMenu.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
    }

    /**
     * DOC yyi get table form package by its name.
     * 
     * @param pkg
     * @param tableName
     * @return
     */
    private TdTable getTable(Package pkg, String tableName) {
        for (TdTable tabel : TableHelper.getTables(pkg.getOwnedElement())) {
            if (tabel.getName().equals(tableName)) {
                return tabel;
            }
        }
        return null;
    }
}