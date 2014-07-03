package org.talend.cwm.compare;

import static org.junit.Assert.*;


import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.IMetadata;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.MetadataFillFactory;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.metadata.builder.util.MetadataConnectionUtils;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.cwm.helper.CatalogHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.utils.sql.metadata.constants.MetaDataConstants;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.Schema;


@PrepareForTest({ MetadataFillFactory.class,ExtractMetaDataUtils.class,ConnectionHelper.class,CoreRuntimePlugin.class})
public class DQStructureComparerTest {
	@Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

	@Test
	@Ignore
	public void testCopyedToDestinationFile() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetTempRefreshFile() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetNeedReloadElementsFile() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetFirstComparisonLocalFile() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetSecondComparisonLocalFile() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testDeleteFirstResourceFile() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testDeleteSecondResourceFile() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetDiffResourceFile() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetLocalDiffResourceFile() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRefreshedDataProvider() throws SQLException {
		DatabaseConnection dbProvider=ConnectionFactory.eINSTANCE.createDatabaseConnection();
		setJDBCMysqlConnection(dbProvider);
		List<Catalog> catalogPackageFilter=new ArrayList<Catalog>();
		List<orgomg.cwm.objectmodel.core.Package> schemaPackageFilter=new ArrayList<orgomg.cwm.objectmodel.core.Package>();
		//mock ReturnCode sql.Connection
		TypedReturnCode<java.sql.Connection> returnCode = new TypedReturnCode<java.sql.Connection>(true);
		java.sql.Connection mockSqlConn = Mockito.mock(java.sql.Connection.class);
		returnCode.setObject(mockSqlConn);
		//~mock
		
		//mock DatabaseMetaData
		DatabaseMetaData mockDatabaseMetaData = Mockito.mock(DatabaseMetaData.class);
		Mockito.when(mockDatabaseMetaData.supportsCatalogsInIndexDefinitions()).thenReturn(true);
		
		
		//initial the data of catalogs
		List<String> catalogNames=new ArrayList<String>();
		List<String> packageFilter = MetadataConnectionUtils.getPackageFilter(dbProvider, mockDatabaseMetaData, true);
		boolean haveFilter=false;
		if(packageFilter.size()>0){
			catalogNames.addAll(packageFilter);
			haveFilter=true;
		}else{
			catalogNames.add("tbi");
			catalogNames.add("test");
			catalogNames.add("testtable");
		}
		//~
		//mock ResultSet
		ResultSet mockCatalogResults=Mockito.mock(ResultSet.class);
		if(haveFilter){
			Mockito.when(mockCatalogResults.next()).thenReturn(true,false);
		}else{
			Mockito.when(mockCatalogResults.next()).thenReturn(true,true,true,false);
		}
		Mockito.when(mockCatalogResults.getString(MetaDataConstants.TABLE_CAT.name())).thenReturn("tbi","test","testtable");
		//~mock
		//mock ResultSet
		ResultSet mockSchemaResults=Mockito.mock(ResultSet.class);
		Mockito.when(mockSchemaResults.next()).thenReturn(false);
		//~mock
		
		
//		getDatabaseProductName
		Mockito.when(mockDatabaseMetaData.getDatabaseProductName()).thenReturn(EDatabaseTypeName.MYSQL.getProduct());
		//getCatalogs
		
		Mockito.when(mockDatabaseMetaData.getCatalogs()).thenReturn(mockCatalogResults);
		Mockito.when(mockDatabaseMetaData.getDriverName()).thenReturn("don't match");
		Mockito.when(mockDatabaseMetaData.getSchemas()).thenReturn(mockSchemaResults);
		//~mock
		
		//mock CoreRuntimePlugin
		CoreRuntimePlugin instanceMock=Mockito.mock(CoreRuntimePlugin.class);
		PowerMockito.mockStatic(CoreRuntimePlugin.class);
		Mockito.when(CoreRuntimePlugin.getInstance()).thenReturn(instanceMock);
		Mockito.when(instanceMock.getRepositoryService()).thenReturn(null);
		//~CoreRuntimePlugin
		
		//mock ExtractMetaDataUtils
		PowerMockito.mock(ExtractMetaDataUtils.class);
		Mockito.when(ExtractMetaDataUtils.getConnectionMetadata(mockSqlConn)).thenReturn(mockDatabaseMetaData);
		//~mock
		
		//mock ConnectionHelper
		PowerMockito.mockStatic(ConnectionHelper.class);
		Set<MetadataTable> result = new HashSet<MetadataTable>();
		Mockito.when(ConnectionHelper.getTables(dbProvider)).thenReturn(result);
		Mockito.when(ConnectionHelper.addCatalogs((Collection<Catalog>)Mockito.any(), (Connection)Mockito.any())).thenCallRealMethod();
		Mockito.when(ConnectionHelper.addPackages((Collection<Catalog>)Mockito.any(), (Connection)Mockito.any())).thenCallRealMethod();
		//~mock
		
		//mock MetadataFillFactory
		MetadataFillFactory mockMetadataFillFactory=Mockito.mock(MetadataFillFactory.class);
		PowerMockito.mockStatic(MetadataFillFactory.class);
		Mockito.when(MetadataFillFactory.getDBInstance()).thenReturn(mockMetadataFillFactory);
		Mockito.when(MetadataFillFactory.getDBInstance()).thenReturn(mockMetadataFillFactory);
		Mockito.when(MetadataFillFactory.getDBInstance()).thenCallRealMethod();
		Mockito.when(MetadataFillFactory.getDBInstance()).thenReturn(mockMetadataFillFactory);
		Mockito.when(mockMetadataFillFactory.checkConnection((IMetadataConnection)Mockito.any())).thenReturn(returnCode);
		Mockito.when(mockMetadataFillFactory.fillUIConnParams((IMetadataConnection)Mockito.any(), (Connection)Mockito.isNull())).thenReturn(dbProvider);
		Mockito.when(mockMetadataFillFactory.fillCatalogs((Connection)Mockito.any(), (DatabaseMetaData)Mockito.any(), Mockito.anyList())).thenCallRealMethod();
		Mockito.when(mockMetadataFillFactory.fillSchemas((Connection)Mockito.any(), (DatabaseMetaData)Mockito.any(), Mockito.anyList())).thenReturn(schemaPackageFilter);
		List<Schema> schemaList=new ArrayList<Schema>();
		Mockito.when(mockMetadataFillFactory.fillSchemaToCatalog((Connection)Mockito.any(), (DatabaseMetaData)Mockito.any(), (Catalog)Mockito.any(), (List<String>)Mockito.any())).thenReturn(schemaList);
		//~mock
		
		DQStructureComparer.getRefreshedDataProvider(dbProvider);
		
		List<Catalog> catalogs = CatalogHelper.getCatalogs(dbProvider.getDataPackage());
		
		assertTrue(catalogs.size()==catalogNames.size());
		for(int index=0;index<catalogNames.size();index++){
			assertTrue(catalogNames.get(index).equalsIgnoreCase(catalogs.get(index).getName()));
		}
		
		//TODO decide the num
		
	}
	private boolean setJDBCMysqlConnection(DatabaseConnection dbProvider){
		//General JDBC case
		dbProvider.setComment("");
		dbProvider.setSID("tbi");
		dbProvider.setDatasourceName("");
		dbProvider.setDatabaseType("MySQL");
		dbProvider.setDbVersionString("MYSQL_5");
		dbProvider.setDriverClass("org.gjt.mm.mysql.Driver");
		dbProvider.setFileFieldName("");
		dbProvider.setId("_9bw28cccEeGQNaw_qcyMFw");
		dbProvider.setLabel("jdbcmysql1");
		dbProvider.setNullChar("");
		dbProvider.setPassword("shenze");
		dbProvider.setPort("3306");
		dbProvider.setServerName("");
		dbProvider.setSqlSynthax("SQL Syntax");
		dbProvider.setUiSchema("");
		dbProvider.setStringQuote("");
		dbProvider.setURL("jdbc:mysql://192.168.30.151:3306/tbi?noDatetimeStringSync=true");
		dbProvider.setAdditionalParams("");
		dbProvider.setUsername("shenze");
		dbProvider.setDbmsId("mysql_id");
		dbProvider.setProductId("MYSQL");
		dbProvider.setDBRootPath("");
		dbProvider.setSQLMode(false);
		dbProvider.setContextMode(false);
		dbProvider.setContextId("");
		dbProvider.setContextName("");
		return true;
	}

	@Test
	@Ignore
	public void testFindMatchedPackage() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testFindMatchedColumnSet() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testFindMatchedColumn() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testClearSubNode() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testOpenDiffCompareEditor() {
		fail("Not yet implemented");
	}

}