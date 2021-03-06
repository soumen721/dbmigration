package com.sms800.quickwin.dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.sms800.quickwin.util.Constant;

@Repository
public class QueryExecutorDaoImpl implements QueryExecutorDao {
	private static final Logger logger = LoggerFactory.getLogger(QueryExecutorDaoImpl.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public int executeQuery(Map<String, String> confMap, Map<String, List<String>> map) {
		logger.debug("Enter into Method :: QueryExecutorDaoImpl.executeQuery()");
		long startTime = System.currentTimeMillis();
		List<String> errorInsertQueryList = new ArrayList<String>();
		List<String> errorUpdateQueryList = new ArrayList<String>();

		int failCont = 0;
		List<String> insertQueryList = map.get(Constant.MAP_INSERT_KEY);
		List<String> updateQueryList = map.get(Constant.MAP_UPDATE_KEY);
		boolean isExistingRecordUpdate = "Y".equalsIgnoreCase(confMap.get(Constant.SQL_UPDATE_STATUS).trim()) ? true : false;

		logger.debug("\n");
		for (int i = 0; i < insertQueryList.size(); i++) {
			try {
				int updateCnt = 0;
				if(isExistingRecordUpdate && updateQueryList!=null &&  !updateQueryList.isEmpty() && updateQueryList.size()==insertQueryList.size()){
					try {
						updateCnt = jdbcTemplate.update(updateQueryList.get(i).split(Constant.TILD_DELEMETER)[1]);
					} catch (Exception exc) {
						failCont++;
						errorUpdateQueryList.add(updateQueryList.get(i));
						logger.debug("Exception in Date for Line no ::"+updateQueryList.get(i).split(Constant.TILD_DELEMETER)[0]+
									" For Query ==>> " + updateQueryList.get(i).split(Constant.TILD_DELEMETER)[1] + " :: " + exc.getMessage());
					}
				}
				if (updateCnt == 0) {
					jdbcTemplate.execute(insertQueryList.get(i).split(Constant.TILD_DELEMETER)[1]);
				}
			} catch (Exception exc) {
				failCont++;
				errorInsertQueryList.add(insertQueryList.get(i).split(Constant.TILD_DELEMETER)[1]);
				logger.debug("Exception in Date for Line no ::"+insertQueryList.get(i).split(Constant.TILD_DELEMETER)[0]+
						" For Query ==>> " + insertQueryList.get(i).split(Constant.TILD_DELEMETER)[1] + " :: " + exc.getMessage());
			}
		}
		logger.debug("\n");

		BufferedWriter bufferWritter = null;
		try {
			String filePath = confMap.get(Constant.ERROR_FILE_PATH).trim();
			String tableName = confMap.get(Constant.DB_TABLE_NAME.trim());
			String compFileName = filePath + "\\" + tableName + "_error.sql";
			
			logger.debug("Error File Path: "+ compFileName);
			File file = new File(compFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			bufferWritter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(compFileName), "utf-8"));

			if (errorUpdateQueryList != null && !errorUpdateQueryList.isEmpty()) {
				for (int i = 0; i < errorUpdateQueryList.size(); i++) {
					bufferWritter.write(errorUpdateQueryList.get(i) + "\n");
				}
			}
			bufferWritter.write("---------------------------------------------------------------------------------- \n");
			if (errorInsertQueryList != null && !errorInsertQueryList.isEmpty()) {
				for (int i = 0; i < errorInsertQueryList.size(); i++) {
					bufferWritter.write(errorInsertQueryList.get(i) + "\n");
				}
			}
			
			bufferWritter.flush();
		} catch (Exception exc) {
			logger.debug("[[Exception in QueryExecutorDaoImpl-->executeQuery -- Detils : "+exc.getMessage());
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

		long endTime = System.currentTimeMillis();
		logger.debug("Totale Query Preparion Time  :: " + (endTime - startTime) + " sec");
		logger.debug("Leaving from Method :: QueryExecutorDaoImpl.executeQuery()");
		return failCont;
	}

}