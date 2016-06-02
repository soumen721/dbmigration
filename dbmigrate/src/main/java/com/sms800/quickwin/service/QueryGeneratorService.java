package com.sms800.quickwin.service;

import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;

public interface QueryGeneratorService {

	void generateQuery(Environment env, Map<String, List<String>> queryMap);

}
