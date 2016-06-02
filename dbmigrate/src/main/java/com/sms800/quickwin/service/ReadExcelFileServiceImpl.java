package com.sms800.quickwin.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.sms800.quickwin.dao.QueryExecutorDao;
import com.sms800.quickwin.domain.CustomerTemplateDTO;
import com.sms800.quickwin.util.Constant;
import com.sms800.quickwin.util.QueryGeneratorUtil;

@Service
public class ReadExcelFileServiceImpl implements ReadExcelFileService {
	private static final Logger logger = LoggerFactory.getLogger(ReadExcelFileServiceImpl.class);

	@Autowired
	QueryExecutorDao queryExecutorDao;
	@Autowired
	QueryGeneratorService queryGeneratorService;
	
	@Override
	public List<CustomerTemplateDTO> readExcelData(Environment env) throws IOException {
		logger.debug("Enter into Method :: ReadExcelFileServiceImpl.readExcelData()");

		Map<String, List<String>> queryMap = new HashMap<String, List<String>>();
		String fileName = env.getProperty(Constant.EXCEL_FILE_PATH).trim();
		String sheetName = env.getProperty(Constant.EXCEL_SHEET_NAME).trim();
		logger.debug("EXCEL File Name ::" + fileName + " &  SheetName : " + sheetName);
		boolean isSqlGenerate = "Y".equalsIgnoreCase(env.getProperty(Constant.SQL_GEN_FLAG).trim()) ? true : false;
		boolean isSqlExecute = "Y".equalsIgnoreCase(env.getProperty(Constant.SQL_EXEC_FLAG).trim()) ? true : false;

		try {
			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = null;
			if (fileName.toLowerCase().endsWith("xlsx")) {
				workbook = new XSSFWorkbook(fis);
			} else if (fileName.toLowerCase().endsWith("xls")) {
				workbook = new HSSFWorkbook(fis);
			}

			Sheet sheet = workbook.getSheet(sheetName);
			//insertQueryList = QueryGeneratorUtil.generateQuery(env, sheet);
			//updateQueryList = QueryGeneratorUtil.generateUpdateQuery(env, sheet);

			queryMap=QueryGeneratorUtil.generateQuery(env, sheet);

			logger.debug("\n Final Insert Query to Execute :: \n " + queryMap.get(Constant.MAP_INSERT_KEY) +"\n");
			logger.debug("\n Final Insert Query to Execute :: \n " + queryMap.get(Constant.MAP_UPDATE_KEY) +"\n");

			// Generate List Query DAO Call
			if (isSqlGenerate) {
				queryGeneratorService.generateQuery(env, queryMap);
			}
			// Execute List Query DAO Call
			if (isSqlExecute) {
				int noOfFailQuery=queryExecutorDao.executeQuery(queryMap);
				logger.info("NO of Fail Query : "+ noOfFailQuery);
			}

			fis.close();
		} catch (Exception exc) {
			//e.printStackTrace();
			logger.debug("In Exception :: ReadExcelFileServiceImpl.readExcelData() , Exception : "+ exc.getMessage());
		}
		logger.debug("Exit from Method :: ReadExcelFileServiceImpl.readExcelData()");
		return null;
	}

}