package com.sms800.quickwin.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;


public class QueryGeneratorUtil {
	private static final Logger logger = LoggerFactory.getLogger(QueryGeneratorUtil.class);
	
	static String[] excelMappingAttr = {Constant.ATTR_XLS_COL, Constant.ATTR_TABLE_COL, Constant.ATTR_PATTRN,
										Constant.ATTR_IS_PRIMERY_KEY, Constant.ATTR_DATATYPE};
	static String[] cvsMappingAttr = {Constant.ATTR_CSV_COL, Constant.ATTR_TABLE_COL, Constant.ATTR_PATTRN,
			Constant.ATTR_IS_PRIMERY_KEY, Constant.ATTR_DATATYPE};
	
	static StringBuilder insertQuery = new StringBuilder(Constant.INSERT_QUERY_1);
	static String finalInsertQuery = null;
	static String finalUpdateQuery = null;
	static StringBuilder updateQuery=null;
	static StringBuilder whereQuery = null;
	static StringBuilder valQuery = null;

	@SuppressWarnings("unchecked")
	public static Map<String, List<String>> generateQueryFrmExcel(Map<String, String> confMap, Sheet sheet) throws Exception {
		logger.debug("Enter into Method :: QueryGeneratorUtil.generateQueryFrmExcel()");
		long startTime=System.currentTimeMillis();
		
		Map<String, List<String>> queryMap = new HashMap<String, List<String>>();
		List<String> insertQueryList = new ArrayList<String>();
		List<String> updateQueryList = new ArrayList<String>();
		try {
			int startRow = Integer.parseInt(confMap.get(Constant.START_ROW).trim());
			int endRow = Integer.parseInt(confMap.get(Constant.END_ROW).trim());			
			//String columnMapping = confMap.get(Constant.COLUMN_MAPPING).trim();
			
			// Populate Column Mapping Details
			logger.debug("\n");
			//String[] colsMapArray = columnMapping.split(Constant.TILD_DELEMETER);
			Map<String, List<String>> mapingDtlsMap = new HashMap<String, List<String>>();
			//**************New **********/
			//New Implementation
			NodeList nodeList = CommonUtil.loadMappingFile(confMap.get(Constant.EXCEL_MAPPING_FILE_NAME));			
			Map<String, Map<String,Object>> fullConfMap= CommonUtil.loadMappingDetails(nodeList, excelMappingAttr);
			Map<String,Object> map =null;
			List<Map<String,String>> mapingList=null;
			
			if(fullConfMap!=null && !fullConfMap.isEmpty()){
				map= fullConfMap.get(confMap.get(Constant.ALIAS_TO_READ));
				if(map!=null && !map.isEmpty()){
					mapingList=(List<Map<String, String>>) map.get(Constant.TABLE_META_DATA);
				} else{
					throw new Exception("ALIAS to be read ...not found in Mapping Config File");
				}
			} else{
				throw new Exception("XML Mapping not proper");
			}
			
			for (Map<String,String> dtls : mapingList) {
				String colVal = null;
				String fullCol=dtls.get(Constant.ATTR_XLS_COL)+"["+dtls.get(Constant.ATTR_PATTRN)+"]";
				if (fullCol.startsWith(Constant.DFLT_VAL_CONSTANT)) {
					colVal = "DefaultValue-" + fullCol.substring(1);
				} else {
					if (fullCol.split(Constant.HYPHEN_DELEMETER).length > 1) {
						colVal = "PartialColumnValue";
					} else {
						colVal = "FullColumnValue";
					}
				}
				logger.debug("Coulmn Details ==>> ColumnName = " + dtls.get(Constant.ATTR_TABLE_COL) + " & ColumnValue = " + colVal
						+ " & isPrimaryKey = " + ("Y".equalsIgnoreCase(dtls.get(Constant.ATTR_IS_PRIMERY_KEY)) ? true : false) + " & DataType = " + dtls.get(Constant.ATTR_DATATYPE));

				String[] array = {dtls.get(Constant.ATTR_TABLE_COL), fullCol,
						"Y".equalsIgnoreCase(dtls.get(Constant.ATTR_IS_PRIMERY_KEY))? "P": "N", dtls.get(Constant.ATTR_DATATYPE)};
				
				mapingDtlsMap.put(dtls.get(Constant.ATTR_TABLE_COL), Arrays.asList(array));
			}
			String tableName = map.get(Constant.ATTR_JOB_TABLE).toString();
			confMap.put(Constant.DB_TABLE_NAME, tableName);
			logger.info("Table Name :"+ tableName);
			
			if (mapingDtlsMap == null || mapingDtlsMap.isEmpty()) {
				throw new Exception("Conf Map can not be null");
			}
			
			logger.debug("\n");
			
			String columnQuery = mapingDtlsMap.keySet().toString().replaceAll("^\\[", "(").replaceAll("\\]", ")");
			insertQuery.append(" "+tableName+" ");
			insertQuery.append(columnQuery);
			insertQuery.append(Constant.INSERT_QUERY_2);
			
			//logger.debug("Insert Query Section : " + insertQuery.toString());
			//logger.debug("Update Query Section : " + updateQuery.toString());

			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (row.getRowNum() > endRow - Constant.EXCEL_ROW_INCREMENTER) {
					break;
				}
				// Start Generating Script Data
				if (row.getRowNum() >= startRow - Constant.EXCEL_ROW_INCREMENTER) {
					
					valQuery = new StringBuilder("(");
					updateQuery = new StringBuilder(Constant.UPDATE_QUERY_1);
					whereQuery = new StringBuilder(Constant.UPDATE_QUERY_WHERE_1);
					updateQuery.append(tableName + " set ");
					
					for (String key : mapingDtlsMap.keySet()) {
						List<String> list = mapingDtlsMap.get(key);
						String columnName=list.get(0);
						String columnDtls = list.get(1);
						String columnVal="";
						boolean isdfltVal = list.get(1).startsWith(Constant.DFLT_VAL_CONSTANT);
						String dataType = mapingDtlsMap.get(key).get(3).trim();
						boolean isPrimaryKey=Constant.PRIMARY_KEY.equalsIgnoreCase(mapingDtlsMap.get(key).get(2))?true:false;
						
						if (isdfltVal) {
							columnVal = list.get(1).substring(2, list.get(1).lastIndexOf("]"));
						} else{
							int excelColumn = Integer.parseInt(columnDtls.substring(0, columnDtls.lastIndexOf("[")));
							//logger.debug("Excel Row : "+(row.getRowNum()+Constant.EXCEL_ROW_INCREMENTER) +" & Column : "+ excelColumn);
							if(row.getCell(excelColumn)==null){
								throw new Exception("Excel Cell Type null for Row : "+ (row.getRowNum()+ Constant.EXCEL_ROW_INCREMENTER) +" & Cell : "+excelColumn);
							}
							switch (row.getCell(excelColumn).getCellType()) {
							case Cell.CELL_TYPE_STRING:
								columnVal=row.getCell(excelColumn).getStringCellValue().trim();
								break;

							case Cell.CELL_TYPE_NUMERIC:
								double numVal = row.getCell(excelColumn).getNumericCellValue();
								columnVal=new DecimalFormat("0").format(numVal);
							}
							
							columnVal=CommonUtil.getExactVal(columnVal, columnDtls, row.getRowNum()+ Constant.EXCEL_ROW_INCREMENTER);
						}
						
						if (Constant.DATA_TYP_VARCHAR.equalsIgnoreCase(dataType)) {
							valQuery.append("'" + columnVal + "', ");
							updateQuery.append(" "+ columnName +"='" + columnVal + "', ");
						} else {
							valQuery.append(columnVal + ", ");
							updateQuery.append(" "+ columnName +"=" + columnVal + ", ");
						}
												
						if (isPrimaryKey) {
							if (Constant.DATA_TYP_VARCHAR.equalsIgnoreCase(dataType)) {
								whereQuery.append(" " + columnName+ "='" + columnVal + "' and ");
							} else {
								whereQuery.append(" " + columnName + "=" + columnVal + " and ");
							}
						}
					}
					
					String finalValQuery = valQuery.toString().substring(0, valQuery.toString().lastIndexOf(",")) + ");";
					finalInsertQuery = insertQuery.toString() + finalValQuery;
					
					if(whereQuery.lastIndexOf("and")!=-1){
						finalUpdateQuery = updateQuery.toString().substring(0, updateQuery.toString().lastIndexOf(","))  
										+ whereQuery.substring(0, whereQuery.lastIndexOf("and"))+";";
					} else{
						finalUpdateQuery = updateQuery.toString().substring(0, updateQuery.toString().lastIndexOf(","))+";"  ;
					}
					//logger.debug("\n Insert Query ::" + finalInsertQuery);
					//logger.debug("\n Update Query ::" + finalUpdateQuery);

					// Generate Query
					insertQueryList.add(row.getRowNum()+Constant.EXCEL_ROW_INCREMENTER +Constant.TILD_DELEMETER+ finalInsertQuery);
					updateQueryList.add(row.getRowNum()+Constant.EXCEL_ROW_INCREMENTER +Constant.TILD_DELEMETER+ finalUpdateQuery);
				}
			}
			
			queryMap.put(Constant.MAP_INSERT_KEY, insertQueryList);
			queryMap.put(Constant.MAP_UPDATE_KEY, updateQueryList);
		} catch (Exception exc) {
			exc.printStackTrace();
			logger.debug("[[ Error :: QueryGeneratorUtil.generateInsertQuery() , Exception : " + exc.getMessage()+" ]]");
			//exc.printStackTrace();
			throw exc;
		}

		long endTime=System.currentTimeMillis();		
		logger.debug("Totale Query Preparion Time  :: "+ (endTime-startTime) +" sec");
		logger.debug("Leaving from Method :: QueryGeneratorUtil.generateInsertQuery()");
		return queryMap;
	}

	@SuppressWarnings("unused")
	public static Map<String, List<String>> generateQueryFrmCSV(Map<String, String> confMap) throws Exception {
		logger.debug("Enter into Method :: QueryGeneratorUtil.generateQueryFrmCSV()");
		long startTime=System.currentTimeMillis();
		Map<String, List<String>> queryMap = new HashMap<String, List<String>>();
		List<String> insertQueryList = new ArrayList<String>();
		List<String> updateQueryList = new ArrayList<String>();
		
		Scanner scnr=null;
		try{
				//Load Mapping File 
				NodeList nodeList = CommonUtil.loadMappingFile(confMap.get(Constant.CSV_MAPPING_FILE_NAME));		
				Map<String, Map<String,Object>> mappingConfMap= CommonUtil.loadMappingDetails(nodeList, cvsMappingAttr);
				
				Path filePath = Paths.get(confMap.get(Constant.CSV_FILE_PATH));
				Map<String,Object> fileMetaDataTable = mappingConfMap.get(confMap.get(Constant.ALIAS_TO_READ));
				
				String tableName = fileMetaDataTable.get(Constant.ATTR_JOB_TABLE).toString();
				confMap.put(Constant.DB_TABLE_NAME, tableName);
				//String insertSQL = "INSERT IGNORE INTO "+tableName+" ";
				String tableCol = "";String tableVal=""; String delim=""; 
									
				@SuppressWarnings("unchecked")
				List<Map<String,String>> fileMetaData = (List<Map<String,String>>)fileMetaDataTable.get(Constant.TABLE_META_DATA);
				scnr= new Scanner(filePath);
				int lineNumber = 0;
				String colPattern = ""; 
				//String colRedPattern="";
				List<String> patterns=new ArrayList<String>();
				
				Map<String,String> cols = new HashMap<>();
				for(Map<String,String> innerMap:fileMetaData){
					/*tableCol+=delim+innerMap.get(Constant.ATTR_TABLE_COL);
					tableVal+=delim+"?";
					delim=",";
					cols.put(innerMap.get(Constant.ATTR_CSV_COL), "1");
					colPattern+="(.*)("+innerMap.get(Constant.ATTR_CSV_COL)+")";*/
					if(innerMap!=null && !innerMap.isEmpty() && !Constant.DFLT_COLMN_NAME.equalsIgnoreCase(innerMap.get("cvsColumn"))){
						patterns.add("(.*)("+innerMap.get(Constant.ATTR_CSV_COL)+")");
					}
				}
				//insertSQL+="("+tableCol+") values("+tableVal+")";
				colPattern+="(.*)";
				Map<String,Integer> mapPositon = new HashMap<>();
				boolean isCaptionIdentified = false;
				Map<String,String> prevVal = new HashMap<>();
				
				while(scnr.hasNextLine()){
					updateQuery = new StringBuilder(Constant.UPDATE_QUERY_1);
					whereQuery = new StringBuilder(Constant.UPDATE_QUERY_WHERE_1);
					updateQuery.append(tableName + " set ");
					
					lineNumber++;
					String line = scnr.nextLine();
					if(CommonUtil.rejectLine(line, confMap.get(Constant.CSV_LINE_REJ_PATTRN)))
						continue;
					//line = line.replaceAll("(\\t)+", "\t");
					//Pattern r = Pattern.compile(colPattern);
					//Matcher m = r.matcher(line);
					
					if(CommonUtil.validateExpession(patterns, line)){
					//if (line.contains(header)) {
						isCaptionIdentified = true;
						String[] attrs = line.split(confMap.get(Constant.CSV_COLMN_DEL));
						for(int attrCnt=0;attrCnt<attrs.length;attrCnt++){
							if(cols.get(attrs[attrCnt])!=null){
								mapPositon.put( attrs[attrCnt],attrCnt);
							}
						}
						lineNumber++;
						scnr.nextLine();
					}else{
						if(isCaptionIdentified){
							String[] vals = line.split(confMap.get(Constant.CSV_COLMN_DEL));
							//int queryPos = 1;
							tableVal="";
							tableCol="";String val ="";
							for(Map<String,String> individual:fileMetaData){
								boolean isPrimaryKey=Constant.YES_CONSTANT.equalsIgnoreCase(individual.get(Constant.ATTR_IS_PRIMERY_KEY))? true:false;
								String cvsCol = individual.get(Constant.ATTR_CSV_COL);
								if(Constant.DFLT_COLMN_NAME.equalsIgnoreCase(cvsCol)){
									val = individual.get(Constant.ATTR_DFLT_VAL);
								} else{
									int pos = mapPositon.get(cvsCol);
									val = vals[pos];
								}
								if(val.trim().length()==0){
									val = prevVal.get(cvsCol);
								}
								val = CommonUtil.getExactVal(val, "["+individual.get(Constant.ATTR_PATTRN)+"]", lineNumber);
								prevVal.put(cvsCol,val);
																
								if("INT".equalsIgnoreCase(individual.get(Constant.ATTR_DATATYPE))){
									tableVal += Integer.parseInt(val) +", ";
									updateQuery.append(" "+ individual.get(Constant.ATTR_TABLE_COL) +"=" + val + ", ");
								}else if("NUMBER".equalsIgnoreCase(individual.get(Constant.ATTR_DATATYPE))){
									tableVal += Double.parseDouble(val) +", ";
									updateQuery.append(" "+ individual.get(Constant.ATTR_TABLE_COL) +"=" + val + ", ");
								}else if("DATE".equalsIgnoreCase(individual.get(Constant.ATTR_DATATYPE))){
									//SimpleDateFormat sdf = new SimpleDateFormat(individual.get(Constant.ATTR_DATAFORMAT));
									//tableVal += new Date((sdf.parse(val)).getTime())+ ", ";
									tableVal += val +", ";
									updateQuery.append(" "+ individual.get(Constant.ATTR_TABLE_COL) +"=" + val + ", ");
								}else{
									tableVal += "'"+val +"', ";  
									updateQuery.append(" "+ individual.get(Constant.ATTR_TABLE_COL) +"='" + val + "', ");
								}
								tableCol+= individual.get(Constant.ATTR_TABLE_COL) +", ";
								
								if (isPrimaryKey) {
									if ("VARCHAR".equalsIgnoreCase(individual.get(Constant.ATTR_DATATYPE))) {
										whereQuery.append(" " + individual.get(Constant.ATTR_TABLE_COL) +"='" + val + "' and ");
									} else {
										whereQuery.append(" " + individual.get(Constant.ATTR_TABLE_COL) + "=" + val + " and ");
									}
								}
							}
							
							finalInsertQuery=Constant.INSERT_QUERY_1 +tableName+"("+tableCol.toString().substring(0, tableCol.toString().lastIndexOf(","))+") values "
									+ "("+ tableVal.toString().substring(0, tableVal.toString().lastIndexOf(",")) + ")";
							finalUpdateQuery = updateQuery.toString().substring(0, updateQuery.toString().lastIndexOf(","))  
									+ whereQuery.substring(0, whereQuery.lastIndexOf("and"))+";";
							
							//logger.info("*********Line NO: **********************"+ lineNumber+ "==> "+ finalInsertQuery);
							//logger.info("*********Line NO: **********************"+ lineNumber+ "==> "+ finalUpdateQuery);
							
							insertQueryList.add(lineNumber+ Constant.TILD_DELEMETER+ finalInsertQuery);
							updateQueryList.add(lineNumber+ Constant.TILD_DELEMETER+ finalUpdateQuery);
						}
					}
				}
				
				queryMap.put(Constant.MAP_INSERT_KEY, insertQueryList);
				queryMap.put(Constant.MAP_UPDATE_KEY, updateQueryList);
		}catch(Exception ex){
			logger.debug("[[Exception -QueryGeneratorUtil-->generateQueryFrmCSV Detils : "+ex.getMessage());
			ex.printStackTrace();
		}finally{
			scnr.close();
		}
		logger.info("**********MAP : "+queryMap.get(Constant.MAP_INSERT_KEY));
		
		long endTime=System.currentTimeMillis();		
		logger.debug("Totale Query Preparion Time  :: "+ (endTime-startTime) +" sec");
		logger.debug("Leaving from Method :: QueryGeneratorUtil.generateQueryFrmCSV()");
		return queryMap;
	}
}
