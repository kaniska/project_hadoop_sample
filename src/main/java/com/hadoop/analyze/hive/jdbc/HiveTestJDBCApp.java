package com.hadoop.analyze.hive.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.hadoop.analyze.hive.thrift.HiveBattingRunsRepository;


public class HiveTestJDBCApp {

private static final Log log = LogFactory.getLog(HiveTestJDBCApp.class);

public static void main(String[] args) throws Exception {
	AbstractApplicationContext context = new ClassPathXmlApplicationContext(
			"/META-INF/spring/hive-context.xml", HiveTestJDBCApp.class);
	log.info("Hive Application Running");
	context.registerShutdownHook();	
				
	JdbcTemplate template = context.getBean(org.springframework.jdbc.core.JdbcTemplate.class);
	log.info(template.queryForRowSet("show tables"));	
	
	HiveBattingRunsRepository repository = context.getBean(HiveJDBCTemplateBattingRunsRepository.class);
	repository.processInputFile("baseball-analysis.hql");
	log.info("Max Scoring Players per Year : \n\n" + repository.count());		    
	

}

}
