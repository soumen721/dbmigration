package com.sms800.quickwin.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QueryGeneratorUtil {
	private static final Logger logger = LoggerFactory.getLogger(QueryGeneratorUtil.class);
	
	static String excelMappingAttr[] = {Constant.ATTR_XLS_COL, Constant.ATTR_TABLE_COL, Constant.ATTR_PATTRN,
										Constant.ATTR_IS_PRIMERY_KEY, Constant.ATTR_DATATYPE};
	
	
	static StringBuilder insertQuery = new StringBuilder(Constant.INSERT_QUERY_1);
	static String finalInsertQuery = null;
	static String finalUpdateQuery = null;
	static StringBuilder updateQuery=null;
	static StringBuilder whereQuery = null;
	static StringBuilder valQuery = null;

	@SuppressWarnings("unchecked")
	public static Map<String, List<String>> generateQueryFrmExcel(Map<String, String> confMap, Sheet sheet) throws Exception {
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
			//String[] colsMapArray = columnMapping.split(Constant.TILD_DELEMETER);
			Map<String, List<String>> mapingDtlsMap = new HashMap<String, List<String>>();
			/*if (colsMapArray != null && colsMapArray.length > 0) {
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
			}*/

			//**************New **********/
			//New Implementation
			NodeList nodeList = loadMappingFile(confMap.get(Constant.EXCEL_MAPPING_FILE_NAME));			
			Map<String, Map<String,Object>> fullConfMap= loadMappingDetails(nodeList);
			List<Map<String,String>> mapingList=null;
			
			if(fullConfMap!=null && !fullConfMap.isEmpty()){
				Map<String,Object> map = fullConfMap.get(confMap.get(Constant.EXCEL_ALIAS_TO_READ));
				if(map!=null && !map.isEmpty()){
					mapingList=(List<Map<String, String>>) map.get(Constant.TABLE_META_DATA);
				} else{
					throw new Exception("XML Mapping not proper");
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
	
	//Load Mapping File
	private static NodeList loadMappingFile(String mappingConfFile) throws Exception{
		NodeList mappings = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(mappingConfFile);
			if (document != null) {
				Element root = document.getDocumentElement();
				mappings = root.getElementsByTagName(Constant.TAG_JOB_TYPE);
			} else{
				throw new Exception("XML Mapping Document cannot be null");
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
		return mappings;
	}
	
	//Load Property from mapping File
	private static Map<String, Map<String,Object>> loadMappingDetails(NodeList mappings ){
		Map<String, Map<String,Object>> xlTableMap = new HashMap<>();
		try {
			for(int mapingCnt=0; mapingCnt < mappings.getLength(); mapingCnt++){
				Node mapping = mappings.item(mapingCnt);
				NamedNodeMap mappingAttrs = mapping.getAttributes();
				Map<String,Object> outerMap = new HashMap<>();
				outerMap.put(Constant.ATTR_JOB_TABLE, mappingAttrs.getNamedItem(Constant.ATTR_JOB_TABLE).getTextContent());
				NodeList columns = ((Element)mapping).getElementsByTagName(Constant.ATTR_TABLE_NODE);
				List<Map<String,String>> innerMapLst = new ArrayList<>();
				for(int colCnt=0; colCnt<columns.getLength(); colCnt++){
					Map<String,String> innerMap = new HashMap<>();
					Node column = columns.item(colCnt);
					NamedNodeMap columnMap = column.getAttributes();
					/*innerMap.put(Constant.ATTR_XLS_COL, columnMap.getNamedItem(Constant.ATTR_XLS_COL).getTextContent());
					innerMap.put(Constant.ATTR_TABLE_COL, columnMap.getNamedItem(Constant.ATTR_TABLE_COL).getTextContent());
					innerMap.put(Constant.ATTR_PATTRN, columnMap.getNamedItem(Constant.ATTR_PATTRN).getTextContent());
					innerMap.put(Constant.ATTR_IS_PRIMERY_KEY, columnMap.getNamedItem(Constant.ATTR_IS_PRIMERY_KEY).getTextContent());
					innerMap.put(Constant.ATTR_DATATYPE, columnMap.getNamedItem(Constant.ATTR_DATATYPE).getTextContent());*/
					
					for (String attr: excelMappingAttr) {
						if(columnMap.getNamedItem(attr)!=null){
							innerMap.put(attr, columnMap.getNamedItem(attr).getTextContent());
						}else{
							throw new Exception("Error: Attribute "+attr +" doesn't exist in XML mapping ");
						}
					}
					if(Constant.ATTR_DATATYPE.equalsIgnoreCase(innerMap.get(Constant.ATTR_DATATYPE))){
						innerMap.put(Constant.ATTR_DATAFORMAT, columnMap.getNamedItem(Constant.ATTR_DATAFORMAT).getTextContent());
					}
					innerMapLst.add(innerMap);
				}
				outerMap.put(Constant.TABLE_META_DATA, innerMapLst);
				xlTableMap.put(mappingAttrs.getNamedItem(Constant.ATTR_JOB_ALIAS).getTextContent(), outerMap);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return xlTableMap;
	}
}
