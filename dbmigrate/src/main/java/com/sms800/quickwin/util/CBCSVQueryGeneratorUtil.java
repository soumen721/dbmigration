package com.sms800.quickwin.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

public class CBCSVQueryGeneratorUtil {
	private static final Logger logger = LoggerFactory.getLogger(CBCSVQueryGeneratorUtil.class);
	static String[] cvsMappingAttr = { Constant.ATTR_CSV_COL, Constant.ATTR_TABLE_COL, Constant.ATTR_PATTRN,
										Constant.ATTR_IS_PRIMERY_KEY, Constant.ATTR_DATATYPE };

	static String[] cvsaliasMappingAttr = { Constant.CSV_FILE_NAME, Constant.CSV_FILE_DELM,
											Constant.CSV_FILE_REJ_PTRN };
	static StringBuilder insertQuery = new StringBuilder(Constant.INSERT_QUERY_1);
	static String finalInsertQuery = null;
	static String finalUpdateQuery = null;
	static StringBuilder updateQuery = null;
	static StringBuilder whereQuery = null;
	static StringBuilder valQuery = null;

	@SuppressWarnings({ "unused", "unchecked" })
	public static Map<String, List<String>> generateQueryFrmCSV(Map<String, String> confMap) throws Exception {
		logger.debug("Enter into Method :: CBCSVQueryGeneratorUtil.generateQueryFrmCSV()");
		long startTime = System.currentTimeMillis();
		Map<String, List<String>> queryMap = new HashMap<String, List<String>>();
		List<String> insertQueryList = new ArrayList<String>();
		List<String> updateQueryList = new ArrayList<String>();

		int startRow = Integer.parseInt(confMap.get(Constant.START_ROW).trim());
		int endRow = Integer.parseInt(confMap.get(Constant.END_ROW).trim());

		Scanner scnr = null;
		String cvsFileName = "";
		String rejPtrn = "";
		boolean isDelmSepFile = false;
		String colmnDelm = "";

		try {
			NodeList nodes = CommonUtil.loadMappingFile(confMap.get(Constant.ALIAS_TO_CSV_MAPPING_FILE_NAME));
			Map<String, Map<String, Object>> aliasMappingConfMap = CommonUtil.loadAliasMappingDetails(nodes, cvsaliasMappingAttr);
			if (aliasMappingConfMap != null && !aliasMappingConfMap.isEmpty()) {
				Map<String, Object> aliasDtlsMap = aliasMappingConfMap.get(confMap.get(Constant.ALIAS_TO_READ));
				if (aliasDtlsMap != null && !aliasDtlsMap.isEmpty()) {
					List<Map<String, String>> csvConf = (List<Map<String, String>>) aliasDtlsMap.get(Constant.CSV_CONF_DATA);
					if (csvConf != null && !csvConf.isEmpty()) {
						cvsFileName = csvConf.get(0).get(Constant.CSV_FILE_NAME).toString();
						rejPtrn = csvConf.get(0).get(Constant.CSV_FILE_REJ_PTRN).toString();
						colmnDelm = csvConf.get(0).get(Constant.CSV_FILE_DELM);
						isDelmSepFile = (colmnDelm != null && !"".equalsIgnoreCase(colmnDelm)) ? true : false;
					} else {
						throw new Exception("Excetion in AliastoFileMap can not be null");
					}
				} else {
					throw new Exception("Excetion in AliastoFileMap can not be null");
				}
			} else {
				throw new Exception("Excetion in AliastoFileMap can not be null");
			}

			// Load Text to Table Mapping File
			NodeList nodeList = CommonUtil.loadMappingFile(confMap.get(Constant.CSV_MAPPING_FILE_NAME));
			Map<String, Map<String, Object>> mappingConfMap = CommonUtil.loadMappingDetails(nodeList, cvsMappingAttr);

			Path filePath = Paths.get(confMap.get(Constant.CSV_FILE_PATH) + "//" + cvsFileName);
			Map<String, Object> fileMetaDataTable = mappingConfMap.get(confMap.get(Constant.ALIAS_TO_READ));

			String tableName = fileMetaDataTable.get(Constant.ATTR_JOB_TABLE).toString();
			confMap.put(Constant.DB_TABLE_NAME, tableName);

			List<Map<String, String>> fileMetaData = (List<Map<String, String>>) fileMetaDataTable.get(Constant.TABLE_META_DATA);
			scnr = new Scanner(filePath);
			int lineNumber = 0;

			while (scnr.hasNextLine()) {
				lineNumber++;
				String line = scnr.nextLine();

				if (lineNumber < startRow) {
					continue;
				} else if (lineNumber > endRow) {
					break;
				}

				if (CommonUtil.rejectLinePattrn(line, rejPtrn))
					continue;
				
				String[] vals = line.split(colmnDelm);
				for (String str : vals) {
					updateQuery = new StringBuilder(Constant.UPDATE_QUERY_1);
					whereQuery = new StringBuilder(Constant.UPDATE_QUERY_WHERE_1);
					updateQuery.append(tableName + " set ");
					String tableCol = ""; String tableVal = ""; String delim = ""; String andDelim = " "; String val = "";

					for (Map<String, String> individual : fileMetaData) {

						boolean isPrimaryKey = Constant.YES_CONSTANT.equalsIgnoreCase(individual.get(Constant.ATTR_IS_PRIMERY_KEY)) ? true : false;
						if (Constant.DFLT_COLMN_NAME.equalsIgnoreCase(individual.get(Constant.ATTR_CSV_COL))) {
							val = individual.get(Constant.ATTR_DFLT_VAL);
						} else {
							val = str; 
						}
						/*System.out.println("Line No:" + lineNumber + " Details : " + line + "  Line length : "
								+ line.length() + ":::::::::::: Column Name :: "
								+ individual.get(Constant.ATTR_TABLE_COL) + "::::::::::::::::" + val);*/

						// Prepare Query
						if ("INT".equalsIgnoreCase(individual.get(Constant.ATTR_DATATYPE))) {
							tableVal += delim + Integer.parseInt(val);
							updateQuery.append(delim + individual.get(Constant.ATTR_TABLE_COL) + "=" + val);
						} else if ("NUMBER".equalsIgnoreCase(individual.get(Constant.ATTR_DATATYPE))) {
							tableVal += delim + Double.parseDouble(val);
							updateQuery.append(delim + individual.get(Constant.ATTR_TABLE_COL) + "=" + val);
						} else if ("DATE".equalsIgnoreCase(individual.get(Constant.ATTR_DATATYPE))) {
							tableVal += delim + val;
							updateQuery.append(delim + individual.get(Constant.ATTR_TABLE_COL) + "=" + val);
						} else {
							tableVal += delim + "'" + val + "'";
							updateQuery.append(delim + individual.get(Constant.ATTR_TABLE_COL) + "='" + val + "'");
						}
						tableCol += delim + individual.get(Constant.ATTR_TABLE_COL);

						if (isPrimaryKey) {
							if ("VARCHAR".equalsIgnoreCase(individual.get(Constant.ATTR_DATATYPE))) {
								whereQuery.append(
										andDelim + individual.get(Constant.ATTR_TABLE_COL) + "='" + val + "'");
							} else {
								whereQuery.append(andDelim + individual.get(Constant.ATTR_TABLE_COL) + "=" + val);
							}
							andDelim = " and ";
						}
						// Update delimiter
						delim = ", ";
					}

					finalInsertQuery = Constant.INSERT_QUERY_1 + tableName + "(" + tableCol.toString()+ ") values (" + tableVal.toString() + ");";
					finalUpdateQuery = updateQuery.toString() + whereQuery.toString() + ";";

					logger.info("*********Line NO: **********************"+ lineNumber+ " Query ==> "+ finalInsertQuery);
					logger.info("*********Line NO: **********************"+ lineNumber+ " Query ==> "+  finalUpdateQuery);
					
					insertQueryList.add(lineNumber + Constant.TILD_DELEMETER + finalInsertQuery);
					updateQueryList.add(lineNumber + Constant.TILD_DELEMETER + finalUpdateQuery);
				}
			}

			queryMap.put(Constant.MAP_INSERT_KEY, insertQueryList);
			queryMap.put(Constant.MAP_UPDATE_KEY, updateQueryList);
		} catch (Exception ex) {
			logger.debug("[[Exception -CBCSVQueryGeneratorUtil-->generateQueryFrmCSV Detils : " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			scnr.close();
		}
		logger.info("**********MAP : " + queryMap.get(Constant.MAP_INSERT_KEY));

		long endTime = System.currentTimeMillis();
		logger.debug("Totale Query Preparion Time  :: " + (endTime - startTime) + " sec");
		logger.debug("Leaving from Method :: CBCSVQueryGeneratorUtil.generateQueryFrmCSV()");
		return queryMap;
	}

}