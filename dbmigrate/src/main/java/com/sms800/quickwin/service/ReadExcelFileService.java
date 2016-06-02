package com.sms800.quickwin.service;

import java.io.IOException;
import java.util.List;

import org.springframework.core.env.Environment;

import com.sms800.quickwin.domain.CustomerTemplateDTO;

public interface ReadExcelFileService {
	
	List<CustomerTemplateDTO> readExcelData(Environment env) throws IOException;

}
