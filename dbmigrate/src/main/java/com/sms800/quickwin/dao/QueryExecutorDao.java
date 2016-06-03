package com.sms800.quickwin.dao;

import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;

public interface QueryExecutorDao {

	int executeQuery(Environment env, Map<String, List<String>> map);
	//void generateQuery(Map<String, List<String>> map);

}
