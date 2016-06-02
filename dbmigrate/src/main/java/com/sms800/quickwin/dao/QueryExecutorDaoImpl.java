package com.sms800.quickwin.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.sms800.quickwin.util.Constant;

@Repository
public class QueryExecutorDaoImpl implements QueryExecutorDao {
	private static final Logger logger = LoggerFactory.getLogger(QueryExecutorDaoImpl.class);
	
	@Autowired
	JdbcTemplate jdbcTemplate;

	@SuppressWarnings("unused")
	@Override
	public int executeQuery(Map<String, List<String>> map) {
		logger.debug("Enter into Method :: QueryExecutorDaoImpl.executeQuery()");
		long startTime=System.currentTimeMillis();
		
		List<String> insertQueryList=map.get(Constant.MAP_INSERT_KEY);
		List<String> updateQueryList=map.get(Constant.MAP_UPDATE_KEY);
		
		logger.debug("Map :: "+ map);
		
		jdbcTemplate.batchUpdate(insertQueryList.toArray(new String[insertQueryList.size()]));
		
		long endTime=System.currentTimeMillis();		
		logger.debug("Totale Query Preparion Time  :: "+ (endTime-startTime) +" sec");
		logger.debug("Leaving from Method :: QueryExecutorDaoImpl.executeQuery()");
		return 0;
	}

	

}
