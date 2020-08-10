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
package org.talend.dq.analysis;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.metadata.builder.connection.ConnectionPackage;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.AnalysisContext;
import org.talend.dataquality.analysis.AnalysisPackage;
import org.talend.dataquality.analysis.AnalysisParameters;
import org.talend.dataquality.analysis.AnalysisResult;
import org.talend.dataquality.indicators.columnset.BlockKeyIndicator;
import org.talend.dataquality.indicators.columnset.ColumnsetPackage;
import org.talend.dataquality.indicators.columnset.RecordMatchingIndicator;
import org.talend.dataquality.record.linkage.constant.AttributeMatcherType;
import org.talend.dataquality.record.linkage.utils.BlockingKeyPreAlgorithmEnum;
import org.talend.dataquality.rules.AlgorithmDefinition;
import org.talend.dataquality.rules.BlockKeyDefinition;
import org.talend.dataquality.rules.MatchKeyDefinition;
import org.talend.dataquality.rules.MatchRule;
import org.talend.dataquality.rules.MatchRuleDefinition;
import org.talend.dataquality.rules.RulesFactory;
import org.talend.dataquality.rules.RulesPackage;
import org.talend.dataquality.rules.SurvivorshipKeyDefinition;
import org.talend.dq.helper.UnitTestBuildHelper;

import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * created by zhao on Aug 28, 2013 Detailled comment
 *
 */
public class MatchAnalysisExecutorTest {

    private DelimitedFileConnection delimitedFileconnection = null;

    private MetadataTable metadataTable = null;

    private MetadataColumn name = null;

    @Before
    public void setUp() throws Exception {
        delimitedFileconnection = ConnectionPackage.eINSTANCE.getConnectionFactory().createDelimitedFileConnection();

    }

    /**
     * Test method for
     * {@link org.talend.dq.analysis.MatchAnalysisExecutor#execute(org.talend.dataquality.analysis.Analysis)}.
     */
    @SuppressWarnings("nls")
    @Test
    public void testExecute() {
        MatchAnalysisExecutor matchAnalysisExecutor = new MatchAnalysisExecutor();
        Analysis analysis = AnalysisPackage.eINSTANCE.getAnalysisFactory().createAnalysis();
        AnalysisContext context = AnalysisPackage.eINSTANCE.getAnalysisFactory().createAnalysisContext();
        analysis.setContext(context);

        AnalysisParameters params = AnalysisPackage.eINSTANCE.getAnalysisFactory().createAnalysisParameters();
        analysis.setParameters(params);
        TaggedValueHelper.setTaggedValue(analysis, TaggedValueHelper.PREVIEW_ROW_NUMBER, String.valueOf(100));

        // analysisResult.setAnalysis(analysis);

        context.setConnection(delimitedFileconnection);
        URL fileUrl = this.getClass().getResource("match_test_data"); //$NON-NLS-1$
        metadataTable = UnitTestBuildHelper.getDefault().initFileConnection(fileUrl, delimitedFileconnection);

        this.name = UnitTestBuildHelper.getDefault().initColumns(context, this.metadataTable);

        // Scenario 1
        // - Match key: name, no block key, levenshtein attribute algorithm. groupQualityThreshold = 0.9d, matchInterval
        // = 0.95d .
        double groupQualityThreshold = 0.9d;
        double matchInterval = 0.95d;
        assertScenario1(matchAnalysisExecutor, analysis, name, "name", groupQualityThreshold, matchInterval);
        // Scenario 2
        // - Same to scenario 1, EXCEPT matchInterval = 0.8d .
        matchInterval = 0.8d;
        assertScenario2(matchAnalysisExecutor, analysis, name, "name", groupQualityThreshold, matchInterval);
        // Scenario 3
        // - Same to scenario 2, EXCEPT groupQualityThreshold = 0.95d.
        groupQualityThreshold = 0.95d;
        assertScenario3(matchAnalysisExecutor, analysis, name, "name", groupQualityThreshold, matchInterval);
        // Scenario 4
        // - Same to scenario 3, EXCEPT a new blocking key = country.

        assertScenario4(matchAnalysisExecutor, analysis, name, "name", groupQualityThreshold, matchInterval);

    }

    /**
     * DOC zhao Comment method "assertScenario1".
     *
     * @param matchAnalysisExecutor
     * @param analysis
     * @param name
     * @param nameVar
     */
    private void assertScenario1(MatchAnalysisExecutor matchAnalysisExecutor, Analysis analysis, MetadataColumn name,
            String nameVar, double groupQualityThreshold, double matchInterval) {
        // Set indicators into analysis result.
        RecordMatchingIndicator matchIndicator =
                ColumnsetPackage.eINSTANCE.getColumnsetFactory().createRecordMatchingIndicator();
        // Match key: name, no block key, levenshtein attribute algorithm.
        matchIndicator.setAnalyzedElement(name);

        createMatchIndicatorWithOneMathRule(nameVar, matchIndicator, groupQualityThreshold, matchInterval);

        executeAnalysis(matchAnalysisExecutor, analysis, matchIndicator);

        // Assert group size and frequency.
        Map<Object, Long> size2Frequency = matchIndicator.getGroupSize2groupFrequency();
        assertTrue(size2Frequency.get(String.valueOf(4)) == 1l);// For 4 -> "seb"
        assertTrue(size2Frequency.get(String.valueOf(1)) == 4l);// For 1 -> "Sebastião","babass","nico","nicola"
        assertTrue(size2Frequency.get(String.valueOf(2)) == 3l);// For 2 -> "sebas","nicolas","nigula"

        // Assert row count, unique records, matched records and suspect records.
        assertTrue(matchIndicator.getCount() == 14);
        assertTrue(matchIndicator.getMatchedRecordCount() == 10);
        assertTrue(matchIndicator.getSuspectRecordCount() == 0);
    }

    /**
     * DOC zhao Comment method "assertScenario2".
     *
     * @param matchAnalysisExecutor
     * @param analysis
     * @param name
     * @param nameVar
     */
    private void assertScenario2(MatchAnalysisExecutor matchAnalysisExecutor, Analysis analysis, MetadataColumn name,
            String nameVar, double groupQualityThreshold, double matchInterval) {
        // Set indicators into analysis result.
        RecordMatchingIndicator matchIndicator =
                ColumnsetPackage.eINSTANCE.getColumnsetFactory().createRecordMatchingIndicator();
        // Match key: name, no block key, levenshtein attribute algorithm.
        matchIndicator.setAnalyzedElement(name);

        createMatchIndicatorWithOneMathRule(nameVar, matchIndicator, groupQualityThreshold, matchInterval);

        executeAnalysis(matchAnalysisExecutor, analysis, matchIndicator);

        // Assert group size and frequency.
        Map<Object, Long> size2Frequency = matchIndicator.getGroupSize2groupFrequency();
        assertTrue(size2Frequency.get(String.valueOf(4)) == 1l);// For 4 -> "seb"
        assertTrue(size2Frequency.get(String.valueOf(1)) == 3l);// For 1 -> "Sebastião","babass","nico"
        assertTrue(size2Frequency.get(String.valueOf(3)) == 1l);// For 3 -> "nicolas"("nicola")
        assertTrue(size2Frequency.get(String.valueOf(2)) == 2l);// For 2 -> "sebas","nigula"

        // Assert row count, unique records, matched records and suspect records.
        assertTrue(matchIndicator.getCount() == 14);
        assertTrue(matchIndicator.getMatchedRecordCount() == 11);
        assertTrue(matchIndicator.getSuspectRecordCount() == 0);
    }

    /**
     * DOC zhao Comment method "assertScenario3".
     *
     * @param matchAnalysisExecutor
     * @param analysis
     * @param name
     * @param nameVar
     */
    private void assertScenario3(MatchAnalysisExecutor matchAnalysisExecutor, Analysis analysis, MetadataColumn name,
            String nameVar, double groupQualityThreshold, double matchInterval) {
        // Set indicators into analysis result.
        RecordMatchingIndicator matchIndicator =
                ColumnsetPackage.eINSTANCE.getColumnsetFactory().createRecordMatchingIndicator();
        // Match key: name, no block key, levenshtein attribute algorithm.
        matchIndicator.setAnalyzedElement(name);

        createMatchIndicatorWithOneMathRule(nameVar, matchIndicator, groupQualityThreshold, matchInterval);

        executeAnalysis(matchAnalysisExecutor, analysis, matchIndicator);

        // Assert group size and frequency.
        Map<Object, Long> size2Frequency = matchIndicator.getGroupSize2groupFrequency();
        assertTrue(size2Frequency.get(String.valueOf(4)) == 1l);// For 4 -> "seb"
        assertTrue(size2Frequency.get(String.valueOf(1)) == 3l);// For 1 -> "Sebastião","babass","nico"
        assertTrue(size2Frequency.get(String.valueOf(3)) == 1l);// For 3 -> "nicolas"("nicola")
        assertTrue(size2Frequency.get(String.valueOf(2)) == 2l);// For 2 -> "sebas","nigula"

        // Assert row count, unique records, matched records and suspect records.
        assertTrue(matchIndicator.getCount() == 14);
        assertTrue(matchIndicator.getMatchedRecordCount() == 8);
        assertTrue(matchIndicator.getSuspectRecordCount() == 3); // For 3 -> "nicolas"("nicola"), group score: 0.9 <
                                                                 // 0.95
    }

    /**
     * DOC zhao Comment method "assertScenario3".
     *
     * @param matchAnalysisExecutor
     * @param analysis
     * @param name
     * @param nameVar
     */
    @SuppressWarnings("nls")
    private void assertScenario4(MatchAnalysisExecutor matchAnalysisExecutor, Analysis analysis, MetadataColumn name,
            String nameVar, double groupQualityThreshold, double matchInterval) {
        // Set indicators into analysis result.
        RecordMatchingIndicator matchIndicator =
                ColumnsetPackage.eINSTANCE.getColumnsetFactory().createRecordMatchingIndicator();
        // Match key: name, no block key, levenshtein attribute algorithm.
        matchIndicator.setAnalyzedElement(name);

        createMatchIndicatorWithOneMathRule(nameVar, matchIndicator, groupQualityThreshold, matchInterval);
        // Add a blocking key: country
        BlockKeyDefinition blockKeyDef = RulesPackage.eINSTANCE.getRulesFactory().createBlockKeyDefinition();
        AlgorithmDefinition algoDef = RulesPackage.eINSTANCE.getRulesFactory().createAlgorithmDefinition();
        algoDef.setAlgorithmType(AttributeMatcherType.EXACT.name());
        blockKeyDef.setAlgorithm(algoDef);
        blockKeyDef.setColumn("country");
        blockKeyDef.setName("country");

        AlgorithmDefinition dummyAlgoPre = RulesPackage.eINSTANCE.getRulesFactory().createAlgorithmDefinition();
        dummyAlgoPre.setAlgorithmType(BlockingKeyPreAlgorithmEnum.NON_ALGO.getComponentValueName());
        blockKeyDef.setPreAlgorithm(dummyAlgoPre);
        AlgorithmDefinition dummyAlgoPost = RulesPackage.eINSTANCE.getRulesFactory().createAlgorithmDefinition();
        dummyAlgoPost.setAlgorithmType(BlockingKeyPreAlgorithmEnum.NON_ALGO.getComponentValueName());
        blockKeyDef.setPostAlgorithm(dummyAlgoPost);

        matchIndicator.getBuiltInMatchRuleDefinition().getBlockKeys().add(blockKeyDef);

        executeAnalysis(matchAnalysisExecutor, analysis, matchIndicator);

        // Assert group size and frequency.
        Map<Object, Long> size2Frequency = matchIndicator.getGroupSize2groupFrequency();
        assertTrue(size2Frequency.get(String.valueOf(1)) == 6l);// For 1 -> FR(4)"babass","Sebastião","nicolas","nigula"
                                                                // CN(2)"nigula","nico"
        assertTrue(size2Frequency.get(String.valueOf(2)) == 2l);// For 2 -> FR(1)"sebas", CN(1)"nicolas"("nicola")

        assertTrue(size2Frequency.get(String.valueOf(4)) == 1l);// For 4 -> FR(4)"seb"

        // Assert row count, unique records, matched records and suspect records.
        assertTrue(matchIndicator.getCount() == 14);
        assertTrue(matchIndicator.getMatchedRecordCount() == 6); // For 6 -> FR 4*"seb", FR 2 *"sebas"
        assertTrue(matchIndicator.getSuspectRecordCount() == 2); // For 2 -> CN "nicolas"("nicola"), group score: 0.9 <
                                                                 // 0.95
    }

    /**
     * DOC zhao Comment method "executeAnalysis".
     *
     * @param matchAnalysisExecutor
     * @param analysis
     * @param matchIndicator
     */
    private void executeAnalysis(MatchAnalysisExecutor matchAnalysisExecutor, Analysis analysis,
            RecordMatchingIndicator matchIndicator) {
        BlockKeyIndicator blockKeyIndicator =
                ColumnsetPackage.eINSTANCE.getColumnsetFactory().createBlockKeyIndicator();

        AnalysisResult anaResult = AnalysisPackage.eINSTANCE.getAnalysisFactory().createAnalysisResult();
        anaResult.setResultMetadata(AnalysisPackage.eINSTANCE.getAnalysisFactory().createExecutionInformations());

        analysis.setResults(anaResult);
        analysis.getResults().getIndicators().add(matchIndicator);
        analysis.getResults().getIndicators().add(blockKeyIndicator);

        matchAnalysisExecutor.setMonitor(new NullProgressMonitor());
        matchAnalysisExecutor.execute(analysis);
    }

    /**
     * DOC zhao Comment method "createMatchIndicatorWithOneMathRule".
     *
     * @param nameVar
     * @param matchIndicator
     * @param groupQualityThreshold
     * @param matchInterval
     */
    private void createMatchIndicatorWithOneMathRule(String nameVar, RecordMatchingIndicator matchIndicator,
            double groupQualityThreshold, double matchInterval) {
        MatchRuleDefinition matchRuleDefinition = RulesPackage.eINSTANCE.getRulesFactory().createMatchRuleDefinition();
        matchRuleDefinition.setMatchGroupQualityThreshold(groupQualityThreshold);
        MatchRule matchRule = RulesPackage.eINSTANCE.getRulesFactory().createMatchRule();
        matchRule.setMatchInterval(matchInterval);
        matchRule.setName("match rule 1");
        MatchKeyDefinition matchkeyDef = RulesPackage.eINSTANCE.getRulesFactory().createMatchKeyDefinition();
        matchkeyDef.setName(nameVar);
        matchkeyDef.setColumn(nameVar);

        AlgorithmDefinition algoDef = RulesPackage.eINSTANCE.getRulesFactory().createAlgorithmDefinition();
        algoDef.setAlgorithmType(AttributeMatcherType.LEVENSHTEIN.name());
        matchkeyDef.setAlgorithm(algoDef);
        matchkeyDef.setConfidenceWeight(1);
        matchRule.getMatchKeys().add(matchkeyDef);

        matchRuleDefinition.getMatchRules().add(matchRule);
        matchIndicator.setBuiltInMatchRuleDefinition(matchRuleDefinition);
    }

    /**
     * the Record Linkage Algorithm is Simple VSR Matcher
     * https://jira.talendforge.org/browse/TDQ-18542
     * 
     * Test method for
     * {@link org.talend.dq.analysis.MatchAnalysisExecutor#execute(org.talend.dataquality.analysis.Analysis)}.
     */
    @Test
    public void testExecuteWithMultiMatchRuleVSR() {
        testExecuteWithMultiMatchRule("simpleVSRMatcher");
    }

    /**
     * the Record Linkage Algorithm is T-Swoosh
     * https://jira.talendforge.org/browse/TDQ-18542
     * 
     * Test method for
     * {@link org.talend.dq.analysis.MatchAnalysisExecutor#execute(org.talend.dataquality.analysis.Analysis)}.
     */
    @Test
    public void testExecuteWithMultiMatchRuleTSwoosh() {
        testExecuteWithMultiMatchRule("T_SwooshAlgorithm");
    }

    /**
     * @param recordLinkageAlgorithm the label of Record Linkage Algorithm
     * {@link org.talend.dataquality.record.linkage.constant.RecordMatcherType}
     */
    private void testExecuteWithMultiMatchRule(String recordLinkageAlgorithm) {
        MatchAnalysisExecutor matchAnalysisExecutor = new MatchAnalysisExecutor();
        Analysis analysis = AnalysisPackage.eINSTANCE.getAnalysisFactory().createAnalysis();
        AnalysisContext context = AnalysisPackage.eINSTANCE.getAnalysisFactory().createAnalysisContext();
        analysis.setContext(context);

        AnalysisParameters params = AnalysisPackage.eINSTANCE.getAnalysisFactory().createAnalysisParameters();
        analysis.setParameters(params);
        TaggedValueHelper.setTaggedValue(analysis, TaggedValueHelper.PREVIEW_ROW_NUMBER, String.valueOf(100));

        context.setConnection(delimitedFileconnection);
        URL fileUrl = this.getClass().getResource("multi_match_rule_test_data"); //$NON-NLS-1$
        metadataTable = UnitTestBuildHelper.getDefault().initFileConnection(fileUrl, delimitedFileconnection);

        // this list contain 2 column list
        List<List<MetadataColumn>> columnListList = initColumns4MutilMatchRule(context, this.metadataTable);

        double groupQualityThreshold = 0.9d;
        double matchInterval = 0.85d;

        // Scenario 1 only 1 Rule tab
        List<List<MetadataColumn>> columnListList1 = new ArrayList<List<MetadataColumn>>();
        columnListList1.add(columnListList.get(0));
        RecordMatchingIndicator matchIndicator = createMatchIndicatorWithMathRules(recordLinkageAlgorithm,
                columnListList1, groupQualityThreshold, matchInterval);
        executeAnalysis(matchAnalysisExecutor, analysis, matchIndicator);

        // Assert group size and frequency.
        Map<Object, Long> size2Frequency = matchIndicator.getGroupSize2groupFrequency();
        assertTrue(size2Frequency.get(String.valueOf(1)) == 1l);
        assertTrue(size2Frequency.get(String.valueOf(2)) == 1l);

        // Assert row count, unique records, matched records and suspect records.
        assertTrue(matchIndicator.getCount() == 3);
        assertTrue(matchIndicator.getMatchedRecordCount() == 2);
        assertTrue(matchIndicator.getSuspectRecordCount() == 0);

        // Scenario 2 with 2 Rule tab
        matchIndicator = createMatchIndicatorWithMathRules(recordLinkageAlgorithm, columnListList,
                groupQualityThreshold, matchInterval);
        executeAnalysis(matchAnalysisExecutor, analysis, matchIndicator);

        // Assert group size and frequency.
        size2Frequency = matchIndicator.getGroupSize2groupFrequency();
        assertTrue(size2Frequency.get(String.valueOf(1)) == 1l);
        assertTrue(size2Frequency.get(String.valueOf(2)) == 1l);

        // Assert row count, unique records, matched records and suspect records.
        assertTrue(matchIndicator.getCount() == 3);
        assertTrue(matchIndicator.getMatchedRecordCount() == 2);
        assertTrue(matchIndicator.getSuspectRecordCount() == 0);
    }

    private RecordMatchingIndicator createMatchIndicatorWithMathRules(String recordLinkageAlgorithm,
            List<List<MetadataColumn>> columnListList, double groupQualityThreshold, double matchInterval) {
        // Set indicators into analysis result.
        RecordMatchingIndicator matchIndicator =
                ColumnsetPackage.eINSTANCE.getColumnsetFactory().createRecordMatchingIndicator();
        // Match key: name, no block key, Exact attribute algorithm.
        matchIndicator.setAnalyzedElement(name);

        int i = 1;
        for (List<MetadataColumn> columnList : columnListList) {
            MatchRuleDefinition matchRuleDefinition =
                    RulesPackage.eINSTANCE.getRulesFactory().createMatchRuleDefinition();
            matchRuleDefinition.setRecordLinkageAlgorithm(recordLinkageAlgorithm);
            matchRuleDefinition.setMatchGroupQualityThreshold(groupQualityThreshold);

            MatchRule matchRule = RulesPackage.eINSTANCE.getRulesFactory().createMatchRule();
            matchRule.setMatchInterval(matchInterval);
            matchRule.setName("match rule " + i);
            List<SurvivorshipKeyDefinition> survivorDefs = new ArrayList<SurvivorshipKeyDefinition>();

            String survivorAlgorithmType = "Concatenate";
            if (i == 1) {
                survivorAlgorithmType = "Concatenate";
            } else if (i == 2) {
                survivorAlgorithmType = "MostCommon";
            }
            for (MetadataColumn column : columnList) {
                MatchKeyDefinition matchkeyDef =
                        createMatchKeyDefinition(column, AttributeMatcherType.EXACT.name(), 1, i);
                matchRule.getMatchKeys().add(matchkeyDef);
                survivorDefs.add(createConcatenateKeyDefinition(matchkeyDef.getName(), survivorAlgorithmType, ""));
            }

            i++;

            matchRuleDefinition.getMatchRules().add(matchRule);
            matchRuleDefinition.getSurvivorshipKeys().addAll(survivorDefs);
            matchIndicator.setBuiltInMatchRuleDefinition(matchRuleDefinition);
        }

        return matchIndicator;
    }

    /**
     * create a MatchKeyDefinition
     * 
     * @param column the MetadataColumn
     * @param algorithmType the AlgorithmType
     * @param confidenceWeight the ConfidenceWeight
     * @param suffix the suffix of the name of the MatchKeyDefinition, if the column name is id, the suffix is 1, then
     * the name of the MatchKeyDefinition is id1
     */
    private MatchKeyDefinition createMatchKeyDefinition(MetadataColumn column, String algorithmType,
            int confidenceWeight, int suffix) {
        MatchKeyDefinition matchkeyDef = RulesPackage.eINSTANCE.getRulesFactory().createMatchKeyDefinition();
        matchkeyDef.setName(column.getName() + suffix);
        matchkeyDef.setColumn(column.getName());
        AlgorithmDefinition algoDef = RulesPackage.eINSTANCE.getRulesFactory().createAlgorithmDefinition();
        algoDef.setAlgorithmType(algorithmType);
        matchkeyDef.setAlgorithm(algoDef);
        matchkeyDef.setConfidenceWeight(confidenceWeight);
        return matchkeyDef;
    }

    /**
     * create a SurvivorshipKeyDefinition
     * 
     * @param name
     * @param algorithmType
     * @param algorithmParameters
     */
    private SurvivorshipKeyDefinition createConcatenateKeyDefinition(String name, String algorithmType,
            String algorithmParameters) {
        AlgorithmDefinition concatenateAlgoDef = RulesFactory.eINSTANCE.createAlgorithmDefinition();
        concatenateAlgoDef.setAlgorithmParameters(algorithmParameters);
        concatenateAlgoDef.setAlgorithmType(algorithmType);
        SurvivorshipKeyDefinition createSurvivorshipKeyDefinition =
                RulesFactory.eINSTANCE.createSurvivorshipKeyDefinition();
        createSurvivorshipKeyDefinition.setName(name);
        createSurvivorshipKeyDefinition.setFunction(concatenateAlgoDef);
        return createSurvivorshipKeyDefinition;
    }

    private List<List<MetadataColumn>> initColumns4MutilMatchRule(AnalysisContext context,
            MetadataTable metadataTable2) {
        List<List<MetadataColumn>> result = new ArrayList<List<MetadataColumn>>();
        List<MetadataColumn> columns1 = new ArrayList<MetadataColumn>();
        List<MetadataColumn> columns2 = new ArrayList<MetadataColumn>();

        List<ModelElement> anaElements = context.getAnalysedElements();

        // id
        MetadataColumn id = ConnectionPackage.eINSTANCE.getConnectionFactory().createMetadataColumn();
        id.setName("id"); //$NON-NLS-1$
        id.setLabel("id"); //$NON-NLS-1$
        anaElements.add(id);
        metadataTable2.getColumns().add(id);

        // name
        MetadataColumn name = ConnectionPackage.eINSTANCE.getConnectionFactory().createMetadataColumn();
        name.setName("name"); //$NON-NLS-1$
        name.setLabel("name"); //$NON-NLS-1$
        anaElements.add(name);
        metadataTable.getColumns().add(name);

        // address
        MetadataColumn address = ConnectionPackage.eINSTANCE.getConnectionFactory().createMetadataColumn();
        address.setName("address"); //$NON-NLS-1$
        address.setLabel("address"); //$NON-NLS-1$
        anaElements.add(address);
        metadataTable.getColumns().add(address);

        // provinceID
        MetadataColumn provinceID = ConnectionPackage.eINSTANCE.getConnectionFactory().createMetadataColumn();
        provinceID.setName("provinceID"); //$NON-NLS-1$
        provinceID.setLabel("provinceID"); //$NON-NLS-1$
        anaElements.add(provinceID);
        metadataTable.getColumns().add(provinceID);

        columns1.add(name);
        columns1.add(address);

        columns2.add(address);
        columns2.add(provinceID);

        result.add(columns1);
        result.add(columns2);

        return result;
    }
}
