package com.sms800.quickwin.service;

import java.util.List;
import java.util.Map;

public interface QueryGeneratorService {

	void generateQuery(Map<String, String> confMap, Map<String, List<String>> queryMap);

}
