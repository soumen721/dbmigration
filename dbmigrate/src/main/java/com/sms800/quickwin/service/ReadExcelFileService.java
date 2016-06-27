package com.sms800.quickwin.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sms800.quickwin.domain.CustomerTemplateDTO;

public interface ReadExcelFileService {
	
	List<CustomerTemplateDTO> readExcelData(Map<String, String> confMap) throws IOException;

}
