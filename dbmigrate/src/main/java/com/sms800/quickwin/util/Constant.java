package com.sms800.quickwin.util;

public class Constant {
	
	public static String DB_URL="quickwin.db.url";
	public static String DB_SCHEMA_NAME="quickwin.db.schema";
	public static String DB_USR_NAME="quickwin.db.user.name";
	public static String DB_USER_PASSWORD="quickwin.db.user.password";
	public static String DB_TABLE_NAME="db.table.name";
	public static String EXCEL_MAPPING_FILE_NAME="excel.map.file.name";
	public static String CSV_MAPPING_FILE_NAME="csv.map.file.name";
	public static String ALIAS_TO_READ="file.alias.to.read";
	public static String FILE_TYPE="read.file.type";	
	public static String IS_CVS_COLUMN_BASE_ROW = "csv.colmnbase.row";
	
	public static String EXCEL_FILE_PATH="excel.file.path";
	public static String EXCEL_SHEET_NAME="excel.sheet.name";
	public static String CSV_FILE_PATH="csv.file.path";
	public static String SQL_GEN_FLAG="sql.query.generate";
	public static String SQL_EXEC_FLAG="sql.query.execute";
	public static String SQL_UPDATE_STATUS="existing.record.update";
	public static String FILE_TIPE_CSV="csv";
	public static String FILE_TIPE_EXCEL="excel";
	public static String CSV_LINE_REJ_PATTRN = "csv.reject.line.pattern";
	public static String CSV_COLMN_DEL = "csv.column.delemeter";
	
	public static String START_ROW="start.row.num";
	public static String END_ROW="end.row.num";
	public static String COLUMN_MAPPING="column.mapping"; 
	public static String SQL_FILE_PATH="generate.sql.file.path"; 
	public static String ERROR_FILE_PATH="error.file.path";
	
	public static int EXCEL_ROW_INCREMENTER=1; 
	public static String TILD_DELEMETER="~~"; 
	public static String PIPE_DELEMETER="\\|"; 
	public static String HYPHEN_DELEMETER="-";
	public static String DOT_DELEMETER=".";
	
	public static String DFLT_VAL_CONSTANT="D";
	public static String PRIMARY_KEY="P";
	public static String DATA_TYP_VARCHAR="VARCHAR";
	public static String DATA_TYP_NUMBER="INT";
	public static String DATA_TYP_DATE="DATE";
	public static String YES_CONSTANT="Y";
	
	public static String INSERT_QUERY_1="insert into ";
	public static String INSERT_QUERY_2=" values ";
	
	public static String UPDATE_QUERY_1="update ";
	public static String UPDATE_QUERY_WHERE_1=" where ";
	
	public static String MAP_INSERT_KEY="insertQueryList";
	public static String MAP_UPDATE_KEY="updateQueryList";
	
	//Mapping Config
	public static  String TABLE_META_DATA = "tablemetadata";
	public static  String TAG_JOB_TYPE = "mapping";
	public static  String ATTR_JOB_ALIAS = "alias";
	public static  String ATTR_JOB_TABLE = "table";
	public static  String ATTR_TABLE_NODE = "column";
	public static  String ATTR_XLS_COL = "excelColumn";
	public static  String ATTR_CSV_COL ="csvColumnIndex";
	public static  String ATTR_DFLT_VAL= "dfltValue";
	public static  String ATTR_TABLE_COL = "toTableCloumn";
	public static  String ATTR_DATATYPE = "datatype";
	public static  String ATTR_PATTRN ="pattern";
	public static  String ATTR_IS_PRIMERY_KEY ="isPrimaryKey";
	public static  String ATTR_DATAFORMAT = "dataformat";

	public static  String DFLT_COLMN_NAME= "DFLT_COL";
	
	//New mapping for Alias to File
	public static String CSV_CONF_DATA = "csvConfDtls";
	public static String ALIAS_TO_CSV_MAPPING_FILE_NAME="aliastocsv.map.file.name";
	public static String CSV_FILE_NAME= "csvFileName";
	public static String CSV_FILE_DELM= "colmnDelm" ;
	public static String CSV_FILE_REJ_PTRN= "rejPtrn";	
	
}
