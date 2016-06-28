package com.sms800.quickwin.conf;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.sms800.quickwin.service.ReadExcelFileService;
import com.sms800.quickwin.util.Constant;

@SpringBootApplication
@ComponentScan(basePackages="com.sms800.quickwin")
//@PropertySource(value="file:${app.home}/config.properties", ignoreResourceNotFound=true)
public class DBMigrateApplication implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(DBMigrateApplication.class);
	
	@Autowired
	private Environment environment;
	@Autowired
	private ReadExcelFileService readExcelFileService;
	
	public static void main(String[] args) {
		SpringApplication.run(DBMigrateApplication.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		logger.debug("Start Execution ..............");
		Map<String, String> confMap=validateConfigParameter(environment); 
		if(confMap!=null && !confMap.isEmpty() && confMap.get("isConfigValid")!=null && "true".equalsIgnoreCase(confMap.get("isConfigValid")) ){
			readExcelFileService.readExcelData(confMap);
		}
		logger.debug("End of Execute ..............");
	}

	@Bean
	public DataSource getDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");	//"com.mysql.jdbc.Driver";
		
		if(environment.getProperty(Constant.DB_URL)!=null && !"".equals(environment.getProperty(Constant.DB_URL))
				&& environment.getProperty(Constant.DB_SCHEMA_NAME)!=null && !"".equals(environment.getProperty(Constant.DB_SCHEMA_NAME)) ){	
			dataSource.setUrl(environment.getProperty(Constant.DB_URL)+"/"+ environment.getProperty(Constant.DB_SCHEMA_NAME));
		} else{
			logger.debug(Constant.DB_URL+" or "+Constant.DB_SCHEMA_NAME+ " property value can not be null or blank");
		}
		if(environment.getProperty(Constant.DB_USR_NAME)!=null && !"".equals(environment.getProperty(Constant.DB_USR_NAME))){	
			dataSource.setUsername(environment.getProperty(Constant.DB_USR_NAME));
		} else{
			logger.debug(Constant.DB_USR_NAME+ " property value can not be null or blank");
		}
		if(environment.getProperty(Constant.DB_USER_PASSWORD)!=null && !"".equals(environment.getProperty(Constant.DB_USER_PASSWORD))){	
			dataSource.setPassword(environment.getProperty(Constant.DB_USER_PASSWORD));
		} else{
			logger.debug(Constant.DB_USER_PASSWORD+ " property value can not be null or blank");
		}
				
		return dataSource;
	}

	@Bean
	public JdbcTemplate getJdbcTemplate() {	
		return new JdbcTemplate(getDataSource()) ;
	}
	private Map<String, String> validateConfigParameter(Environment environment) {
		Map<String, String> configMap=new HashMap<String, String>();
		boolean isConfigValid=true;
		if(environment.getProperty(Constant.DB_TABLE_NAME)!=null && !"".equals(environment.getProperty(Constant.DB_TABLE_NAME))){	
			configMap.put(Constant.DB_TABLE_NAME, environment.getProperty(Constant.DB_TABLE_NAME));
		} else{
			isConfigValid=false;
			logger.debug(Constant.DB_TABLE_NAME+ " property value can not be null or blank");
		}	
		if(environment.getProperty(Constant.EXCEL_FILE_PATH)!=null && !"".equals(environment.getProperty(Constant.EXCEL_FILE_PATH))){	
			configMap.put(Constant.EXCEL_FILE_PATH, environment.getProperty(Constant.EXCEL_FILE_PATH));
		} else{
			isConfigValid=false;
			logger.debug(Constant.EXCEL_FILE_PATH+ " property value can not be null or blank");
		}
		if(environment.getProperty(Constant.EXCEL_SHEET_NAME)!=null && !"".equals(environment.getProperty(Constant.EXCEL_SHEET_NAME))){	
			configMap.put(Constant.EXCEL_SHEET_NAME, environment.getProperty(Constant.EXCEL_SHEET_NAME));
		} else{
			isConfigValid=false;
			logger.debug(Constant.EXCEL_SHEET_NAME+ " property value can not be null or blank");
		}
		if(environment.getProperty(Constant.SQL_GEN_FLAG)!=null && !"".equals(environment.getProperty(Constant.SQL_GEN_FLAG))){	
			configMap.put(Constant.SQL_GEN_FLAG, environment.getProperty(Constant.SQL_GEN_FLAG));
		} else{
			isConfigValid=false;
			logger.debug(Constant.SQL_GEN_FLAG+ " property value can not be null or blank");
		}
		if(environment.getProperty(Constant.SQL_EXEC_FLAG)!=null && !"".equals(environment.getProperty(Constant.SQL_EXEC_FLAG))){	
			configMap.put(Constant.SQL_EXEC_FLAG, environment.getProperty(Constant.SQL_EXEC_FLAG));
		} else{
			isConfigValid=false;
			logger.debug(Constant.SQL_EXEC_FLAG+ " property value can not be null or blank");
		}
		if(environment.getProperty(Constant.SQL_UPDATE_STATUS)!=null && !"".equals(environment.getProperty(Constant.SQL_UPDATE_STATUS))){	
			configMap.put(Constant.SQL_UPDATE_STATUS, environment.getProperty(Constant.SQL_UPDATE_STATUS));
		} else{
			isConfigValid=false;
			logger.debug(Constant.SQL_UPDATE_STATUS+ " property value can not be null or blank");
		}
		if(environment.getProperty(Constant.START_ROW)!=null && !"".equals(environment.getProperty(Constant.START_ROW))){	
			configMap.put(Constant.START_ROW, environment.getProperty(Constant.START_ROW));
		} else{
			configMap.put(Constant.START_ROW, "2");
		}
		if(environment.getProperty(Constant.END_ROW)!=null && !"".equals(environment.getProperty(Constant.END_ROW))){	
			configMap.put(Constant.END_ROW, environment.getProperty(Constant.END_ROW));
		} else{
			configMap.put(Constant.END_ROW, "1000");
		}
		
		String columnMappingNmae=Constant.COLUMN_MAPPING+Constant.DOT_DELEMETER+ environment.getProperty(Constant.DB_TABLE_NAME);
		if(environment.getProperty(columnMappingNmae)!=null && !"".equals(environment.getProperty(columnMappingNmae))){	
			configMap.put(Constant.COLUMN_MAPPING, environment.getProperty(columnMappingNmae));
		} else if(environment.getProperty(Constant.COLUMN_MAPPING)!=null && !"".equals(environment.getProperty(Constant.COLUMN_MAPPING))){
			configMap.put(Constant.COLUMN_MAPPING, environment.getProperty(Constant.COLUMN_MAPPING));
		} else{
			isConfigValid=false;
			logger.debug(columnMappingNmae +" or " +Constant.COLUMN_MAPPING+ " property value can not be null or blank");
		}
		
		
		if(environment.getProperty(Constant.SQL_FILE_PATH)!=null && !"".equals(environment.getProperty(Constant.SQL_FILE_PATH))){	
			configMap.put(Constant.SQL_FILE_PATH, environment.getProperty(Constant.SQL_FILE_PATH));
		} else{
			isConfigValid=false;
			logger.debug(Constant.SQL_FILE_PATH+ " property value can not be null or blank");
		}
		if(environment.getProperty(Constant.ERROR_FILE_PATH)!=null && !"".equals(environment.getProperty(Constant.ERROR_FILE_PATH))){	
			configMap.put(Constant.ERROR_FILE_PATH, environment.getProperty(Constant.ERROR_FILE_PATH));
		} else{
			isConfigValid=false;
			logger.debug(Constant.ERROR_FILE_PATH+ " property value can not be null or blank");
		}
		configMap.put("isConfigValid", isConfigValid+"");
		return configMap;
	}
}