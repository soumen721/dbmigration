package com.sms800.quickwin.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.springframework.stereotype.Service;

import com.sms800.quickwin.dao.QueryExecutorDao;
import com.sms800.quickwin.domain.CustomerTemplateDTO;
import com.sms800.quickwin.util.CSVQueryGeneratorUtil;
import com.sms800.quickwin.util.Constant;
import com.sms800.quickwin.util.QueryGeneratorUtil;

@Service
public class ReadDataFileServiceImpl implements ReadFileService {
	private static final Logger logger = LoggerFactory.getLogger(ReadDataFileServiceImpl.class);

	@Autowired
	QueryExecutorDao queryExecutorDao;
	@Autowired
	QueryGeneratorService queryGeneratorService;
	
	@Override
	public List<CustomerTemplateDTO> readExcelData(Map<String, String> confMap) throws IOException {
		logger.debug("Enter into Method :: ReadDataFileServiceImpl.readExcelData()");

		Map<String, List<String>> queryMap = new HashMap<String, List<String>>();
		String fileName = confMap.get(Constant.EXCEL_FILE_PATH).trim();
		String sheetName = confMap.get(Constant.EXCEL_SHEET_NAME).trim();
		logger.debug("EXCEL File Name ::" + fileName + " &  SheetName : " + sheetName);
		boolean isSqlGenerate = "Y".equalsIgnoreCase(confMap.get(Constant.SQL_GEN_FLAG).trim()) ? true : false;
		boolean isSqlExecute = "Y".equalsIgnoreCase(confMap.get(Constant.SQL_EXEC_FLAG).trim()) ? true : false;

		try {
			FileInputStream fis = new FileInputStream(fileName);
			Workbook workbook = null;
			if (fileName.toLowerCase().endsWith("xlsx")) {
				workbook = new XSSFWorkbook(fis);
			} else if (fileName.toLowerCase().endsWith("xls")) {
				workbook = new HSSFWorkbook(fis);
			}

			Sheet sheet = workbook.getSheet(sheetName);
			if(sheet!=null){
				queryMap=QueryGeneratorUtil.generateQueryFrmExcel(confMap, sheet);
				//logger.debug("\n Final Insert Query to Execute :: \n " + queryMap.get(Constant.MAP_INSERT_KEY) +"\n");
				//logger.debug("\n Final Update Query to Execute :: \n " + queryMap.get(Constant.MAP_UPDATE_KEY) +"\n");
	
				// Generate List Query DAO Call
				if (isSqlGenerate) {
					queryGeneratorService.generateQuery(confMap, queryMap);
				}
				// Execute List Query DAO Call
				if (isSqlExecute) {
					int noOfFailQuery=queryExecutorDao.executeQuery(confMap, queryMap);
					logger.info("NO of Fail Query : "+ noOfFailQuery);
				}
			} else{
				throw new Exception("Sheet Can not be null");
			}
			fis.close();
		} catch (FileNotFoundException exc) {
			//e.printStackTrace();
			logger.debug("[[ Error occureed File Not Found:: ReadDataFileServiceImpl.readExcelData() , Exception : "+ exc.getMessage()+ " ]]");
		} catch (Exception exc) {
			//e.printStackTrace();
			logger.debug("[[ Error occureed :: ReadDataFileServiceImpl.readExcelData() , Error : "+ exc.getMessage() +" ]]");
		}
		logger.debug("Exit from Method :: ReadDataFileServiceImpl.readExcelData()");
		return null;
	}

	@Override
	public List<CustomerTemplateDTO> readCsvData(Map<String, String> confMap) throws IOException {
		logger.debug("Enter into Method :: ReadDataFileServiceImpl.readCvsData()");

		Map<String, List<String>> queryMap = new HashMap<String, List<String>>();
		String fileName = confMap.get(Constant.CSV_FILE_PATH).trim();
		logger.debug("CVS File Name ::" + fileName );
		boolean isSqlGenerate = "Y".equalsIgnoreCase(confMap.get(Constant.SQL_GEN_FLAG).trim()) ? true : false;
		boolean isSqlExecute = "Y".equalsIgnoreCase(confMap.get(Constant.SQL_EXEC_FLAG).trim()) ? true : false;

		try {
			//queryMap=QueryGeneratorUtil.generateQueryFrmCVS(confMap);
			queryMap = CSVQueryGeneratorUtil.generateQueryFrmCSV(confMap);
			
			if(queryMap!=null && !queryMap.isEmpty()){
				// Generate List Query DAO Call
				if (isSqlGenerate) {
					queryGeneratorService.generateQuery(confMap, queryMap);
				}
				// Execute List Query DAO Call
				if (isSqlExecute) {
					int noOfFailQuery=queryExecutorDao.executeQuery(confMap, queryMap);
					logger.info("NO of Fail Query : "+ noOfFailQuery);
				}
			} else{
				throw new Exception("No data found ");
			}
		} catch (Exception exc) {
			//e.printStackTrace();
			logger.debug("[[ Error occureed :: ReadDataFileServiceImpl.readCvsData() , Error : "+ exc.getMessage() +" ]]");
		}
		logger.debug("Exit from Method :: ReadDataFileServiceImpl.readCvsData()");
		return null;
	}

}