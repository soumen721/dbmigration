package com.sms800.quickwin.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class QueryGeneratorUtil {
	private static final Logger logger = LoggerFactory.getLogger(QueryGeneratorUtil.class);

	static StringBuilder insertQuery = new StringBuilder(Constant.INSERT_QUERY_1);
	static String finalInsertQuery = null;
	static String finalUpdateQuery = null;
	static StringBuilder updateQuery=null;
	static StringBuilder whereQuery = null;
	static StringBuilder valQuery = null;

	public static Map<String, List<String>> generateQuery(Map<String, String> confMap, Sheet sheet) throws Exception {
		logger.debug("Enter into Method :: QueryGeneratorUtil.generateInsertQuery()");
		long startTime=System.currentTimeMillis();
		
		Map<String, List<String>> queryMap = new HashMap<String, List<String>>();
		List<String> insertQueryList = new ArrayList<String>();
		List<String> updateQueryList = new ArrayList<String>();
		try {
			int startRow = Integer.parseInt(confMap.get(Constant.START_ROW).trim());
			int endRow = Integer.parseInt(confMap.get(Constant.END_ROW).trim());
			String tableName = confMap.get(Constant.DB_TABLE_NAME.trim());
			String columnMapping = confMap.get(Constant.COLUMN_MAPPING).trim();
			
			// Populate Column Mapping Details
			logger.debug("\n");
			String[] colsMapArray = columnMapping.split(Constant.TILD_DELEMETER);
			Map<String, List<String>> mapingDtlsMap = new HashMap<String, List<String>>();
			if (colsMapArray != null && colsMapArray.length > 0) {
				for (String str : colsMapArray) {
					String[] colDtls = str.split(Constant.PIPE_DELEMETER);
					if (colDtls != null && colDtls.length == 4) {
						for (String strCol : colDtls) {
							if (strCol == null || "".equals(strCol)) {
								throw new Exception("Column Conf Not Valid for Entry :: " + str + "	4'|' separated value Mandatory");
							}
						}

						String colVal = null;
						if (colDtls[1].startsWith(Constant.DFLT_VAL_CONSTANT)) {
							colVal = "DefaultValue-" + colDtls[1].substring(1);
						} else {
							if (colDtls[1].split(Constant.HYPHEN_DELEMETER).length > 1) {
								colVal = "PartialColumnValue";
							} else {
								colVal = "FullColumnValue";
							}
						}
						logger.debug("Coulmn Details ==>> ColumnName = " + colDtls[0] + " & ColumnValue = " + colVal
								+ " & isPrimaryKey = " + ("P".equalsIgnoreCase(colDtls[2]) ? true : false) + " & DataType = " + colDtls[3]);

						String[] array = { colDtls[0], colDtls[1], colDtls[2], colDtls[3] };
						mapingDtlsMap.put(colDtls[0], Arrays.asList(array));
					} else {
						throw new Exception("Column Conf Not Valid for :: " + str);
					}
				}
			} else {
				throw new Exception("Column Mapping Value not Proper");
			}

			if (mapingDtlsMap == null || mapingDtlsMap.isEmpty()) {
				throw new Exception("Conf Map can not be null");
			}
			
			logger.debug("\n");
			//logger.debug("\n\n Column Mapping Details for '" + tableName + "' Table ::\n " + mapingDtlsMap + "\n\n");
			//logger.debug("Column name Details for '" + tableName + "' Table  :: " + mapingDtlsMap.keySet());

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
							
							columnVal=getExactVal(columnVal, columnDtls, row.getRowNum()+ Constant.EXCEL_ROW_INCREMENTER);
						}
						
						if (Constant.DATA_TYP_VARCHAR.equalsIgnoreCase(dataType)) {
							valQuery.append("'" + columnVal + "', ");
							updateQuery.append(" "+ columnName +"='" + columnVal + "', ");
						} else {
							valQuery.append(columnVal + ", ");
							updateQuery.append(" "+ columnName +"='" + columnVal + "', ");
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
					finalUpdateQuery = updateQuery.toString().substring(0, updateQuery.toString().lastIndexOf(","))  
										+ whereQuery.substring(0, whereQuery.lastIndexOf("and"))+";";

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
			logger.debug("[[ Error :: QueryGeneratorUtil.generateInsertQuery() , Exception : " + exc.getMessage()+" ]]");
			//exc.printStackTrace();
			throw exc;
		}

		long endTime=System.currentTimeMillis();		
		logger.debug("Totale Query Preparion Time  :: "+ (endTime-startTime) +" sec");
		logger.debug("Leaving from Method :: QueryGeneratorUtil.generateInsertQuery()");
		return queryMap;
	}

	public static List<String> generateUpdateQuery(Environment env, Sheet sheet) {
		logger.debug("Enter into Method :: QueryGeneratorUtil.generateUpdateQuery()");

		logger.debug("Leaving from Method :: QueryGeneratorUtil.generateUpdateQuery()");
		return null;
	}
	
	private static String getExactVal(String str, String pattren, int rowNumber) throws Exception{
		if(str!=null && !"".equals(str) && pattren!=null && !"".equals(pattren)){
			String colPat=pattren.substring(pattren.indexOf("[")+1,pattren.length()-1);
			if(colPat!=null && "0".equals(colPat)){
				return str;
			} else if(colPat!=null && !"".equals(colPat)){
				String[] indexs=colPat.split(Constant.HYPHEN_DELEMETER);
				if(indexs!=null && indexs.length==2 && str.length()>Integer.parseInt(indexs[1])){
					return str.substring(Integer.parseInt(indexs[0]), Integer.parseInt(indexs[1])+1);
				} else{
					throw new Exception("Error occureed in Retriving Column Value for Row ::"+rowNumber+"	& Value: "+str +"	& Pattern : "+ pattren);
				}
			}
		} else{
			throw new Exception("Error occureed in Retriving Column Value for Row ::"+rowNumber+"	& Value: "+str +"	& Pattern : "+ pattren);
		}
		return null;
	}
}
