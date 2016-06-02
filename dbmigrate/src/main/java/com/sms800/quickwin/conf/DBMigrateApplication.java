package com.sms800.quickwin.conf;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.sms800.quickwin.service.ReadExcelFileService;
import com.sms800.quickwin.util.Constant;

@SpringBootApplication
@ComponentScan(basePackages="com.sms800.quickwin")
@PropertySource(value="file:${app.home}/config.properties", ignoreResourceNotFound=true)
public class DBMigrateApplication implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(DBMigrateApplication.class);
	
	@Autowired
	private Environment env;
	@Autowired
	private ReadExcelFileService readExcelFileService;
	
	public static void main(String[] args) {
		SpringApplication.run(DBMigrateApplication.class, args);

	}

	@Override
	public void run(String... arg0) throws Exception {
		logger.debug("Start Execution ..............");
		
		readExcelFileService.readExcelData(env);
		
		logger.debug("End of Execute ..............");
	}

	@Bean
	public DataSource getDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl(env.getProperty(Constant.DB_URL));
		dataSource.setUsername(env.getProperty(Constant.DB_USR_NAME));
		dataSource.setPassword(env.getProperty(Constant.DB_USER_PASSWORD));		
		return dataSource;
	}

	@Bean
	public JdbcTemplate getJdbcTemplate() {	
		return new JdbcTemplate(getDataSource()) ;
	}
	  
}
