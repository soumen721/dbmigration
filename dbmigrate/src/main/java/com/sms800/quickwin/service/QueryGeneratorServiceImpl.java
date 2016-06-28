package com.sms800.quickwin.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sms800.quickwin.util.Constant;

@Service
public class QueryGeneratorServiceImpl implements QueryGeneratorService {
	private static final Logger logger = LoggerFactory.getLogger(QueryGeneratorServiceImpl.class);

	@Override
	public void generateQuery(Map<String, String> confMap, Map<String, List<String>> queryMap) {
		logger.debug("Enter into Method :: QueryGeneratorServiceImpl.generateQuery()");

		BufferedWriter bufferWritter = null;
		try {
			List<String> insertQueryList = queryMap.get("insertQueryList");
			List<String> updateQueryList = queryMap.get("updateQueryList");
			logger.debug("Number of Query :: " + queryMap.get("insertQueryList").size() + "||" + queryMap.get("insertQueryList").size());
			String filePath = confMap.get(Constant.SQL_FILE_PATH).trim();
			String tableName = confMap.get(Constant.DB_TABLE_NAME.trim());
			String sheetName = confMap.get(Constant.EXCEL_SHEET_NAME).trim();
			String compFileName = filePath + "\\" + sheetName+"_"+tableName + ".sql";
			
			logger.debug("Generated SQL File Path : "+ compFileName);
			File file = new File(compFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			bufferWritter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(compFileName), "utf-8"));
			
			if(updateQueryList!=null && insertQueryList!=null && !updateQueryList.isEmpty() && !insertQueryList.isEmpty() 
						&& updateQueryList.size()==insertQueryList.size()){
				
				for (int i = 0; i < updateQueryList.size(); i++) {
					bufferWritter.write(updateQueryList.get(i).split(Constant.TILD_DELEMETER)[1] + "\n");
					bufferWritter.write(insertQueryList.get(i).split(Constant.TILD_DELEMETER)[1] + "\n");
				}
			}
			
			bufferWritter.flush();
		} catch (Exception exc) {
			try {
				if (bufferWritter != null) {
					bufferWritter.close();
				} else {
					logger.debug("Buffer has not been initialized!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			try {
				if (bufferWritter != null) {
					bufferWritter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		logger.debug("Leaving from Method :: QueryGeneratorServiceImpl.generateQuery()");
	}

}
