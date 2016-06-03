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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.sms800.quickwin.util.Constant;

@Service
public class QueryGeneratorServiceImpl implements QueryGeneratorService {
	private static final Logger logger = LoggerFactory.getLogger(QueryGeneratorServiceImpl.class);

	@Override
	public void generateQuery(Environment env, Map<String, List<String>> queryMap) {
		logger.debug("Enter into Method :: QueryGeneratorServiceImpl.generateQuery()");

		BufferedWriter bufferWritter = null;
		try {
			List<String> insertQueryList = queryMap.get("insertQueryList");
			List<String> updateQueryList = queryMap.get("updateQueryList");
			logger.debug("Number of Query :: " + queryMap.get("insertQueryList").size() + "||" + queryMap.get("insertQueryList").size());
			String filePath = env.getProperty(Constant.SQL_FILE_PATH).trim();
			String tableName = env.getProperty(Constant.DB_TABLE_NAME.trim());
			String compFileName = filePath + "\\" + tableName + ".sql";
			
			File file = new File(compFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			bufferWritter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(compFileName), "utf-8"));
			
			if(updateQueryList!=null && insertQueryList!=null && !updateQueryList.isEmpty() && !insertQueryList.isEmpty() 
						&& updateQueryList.size()==insertQueryList.size()){
				
				for (int i = 0; i < updateQueryList.size(); i++) {
					bufferWritter.write(updateQueryList.get(i) + "\n");
					bufferWritter.write(insertQueryList.get(i) + "\n");
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