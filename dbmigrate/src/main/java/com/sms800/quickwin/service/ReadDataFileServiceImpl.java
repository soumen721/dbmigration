package com.sms800.quickwin.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sms800.quickwin.dao.QueryExecutorDao;
import com.sms800.quickwin.domain.CustomerTemplateDTO;
import com.sms800.quickwin.util.Constant;
import com.sms800.quickwin.util.QueryGeneratorUtil;

@Service
public class ReadDataFileServiceImpl implements ReadFileService {
	private static final Logger logger = LoggerFactory.getLogger(ReadDataFileServiceImpl.class);

	static String[] excelAliasMappingAttr = {Constant.EXCEL_FILE_NAME, Constant.EXCEL_SHEET_TO_READ, Constant.FILE_SEQ, Constant.ATTR_JOB_ALIAS};
	
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
		//logger.debug("EXCEL File Name ::" + fileName + " &  SheetName : " + sheetName);
		boolean isSqlGenerate = "Y".equalsIgnoreCase(confMap.get(Constant.SQL_GEN_FLAG).trim()) ? true : false;
		boolean isSqlExecute = "Y".equalsIgnoreCase(confMap.get(Constant.SQL_EXEC_FLAG).trim()) ? true : false;
		FileInputStream fis=null;
		try {			
			NodeList nodeList = QueryGeneratorUtil.loadMappingFile(confMap.get(Constant.EXCEL_TO_ALIAS_MAP));		
			Map<String, Map<String, String> > mappingConfMap= loadAliastoFileMapping(nodeList, excelAliasMappingAttr);
			
			for (String key : mappingConfMap.keySet()) {
				fileName=confMap.get(Constant.EXCEL_FILE_PATH)+"/"+ mappingConfMap.get(key).get(Constant.EXCEL_FILE_NAME);
				sheetName=mappingConfMap.get(key).get(Constant.EXCEL_SHEET_TO_READ);
				logger.debug("-----------------------------------------------------------------------------------------------\n\n");
				logger.debug("Reading EXCEL File Name ::" + fileName + " &  SheetName : " + sheetName);
				
				fis = new FileInputStream(fileName);
				Workbook workbook = null;
				if (fileName.toLowerCase().endsWith("xlsx")) {
					workbook = new XSSFWorkbook(fis);
				} else if (fileName.toLowerCase().endsWith("xls")) {
					workbook = new HSSFWorkbook(fis);
				}
	
				Sheet sheet = workbook.getSheet(sheetName);
				if(sheet!=null){
					queryMap=QueryGeneratorUtil.generateQueryFrmExcel(confMap, sheet, mappingConfMap.get(key).get(Constant.ATTR_JOB_ALIAS));
	
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
					throw new Exception(confMap.get(Constant.EXCEL_SHEET_TO_READ) +" Sheet Not found");
				}
				
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
	public List<CustomerTemplateDTO> readCvsData(Map<String, String> confMap) throws IOException {
		logger.debug("Enter into Method :: ReadDataFileServiceImpl.readCvsData()");

		Map<String, List<String>> queryMap = new HashMap<String, List<String>>();
		String fileName = confMap.get(Constant.CVS_FILE_PATH).trim();
		logger.debug("CVS File Name ::" + fileName );
		boolean isSqlGenerate = "Y".equalsIgnoreCase(confMap.get(Constant.SQL_GEN_FLAG).trim()) ? true : false;
		boolean isSqlExecute = "Y".equalsIgnoreCase(confMap.get(Constant.SQL_EXEC_FLAG).trim()) ? true : false;

		try {
			queryMap=QueryGeneratorUtil.generateQueryFrmCVS(confMap);
			
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

	
	//Load Property from mapping File
	private static Map<String, Map<String, String> > loadAliastoFileMapping(NodeList mappings, String[] mappingAttr){
		Map<String, Map<String, String> > xlTableMap = new TreeMap<>();
		try {
			for(int mapingCnt=0; mapingCnt < mappings.getLength(); mapingCnt++){
				Map<String, String> innerMap=new HashMap<>(); 
				Node mapping = mappings.item(mapingCnt);
				NamedNodeMap mappingAttrs = mapping.getAttributes();
			
				for (String attr: mappingAttr) {
					if(mappingAttrs.getNamedItem(attr)!=null){
						innerMap.put(attr, mappingAttrs.getNamedItem(attr).getTextContent());
					}else{
						throw new Exception("Error: Attribute "+attr +" doesn't exist in XML mapping ");
					}
				}
				xlTableMap.put(innerMap.get(Constant.FILE_SEQ), innerMap);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return xlTableMap;
	}
		
}