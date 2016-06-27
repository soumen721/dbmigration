package com.sms800.quickwin.dao;

import java.util.List;
import java.util.Map;

public interface QueryExecutorDao {

	int executeQuery(Map<String, String> confMap, Map<String, List<String>> map);
	//void generateQuery(Map<String, List<String>> map);

}
