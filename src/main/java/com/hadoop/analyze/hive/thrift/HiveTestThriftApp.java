package com.hadoop.analyze.hive.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.hadoop.hive.HiveTemplate;


public class HiveTestThriftApp {

private static final Log log = LogFactory.getLog(HiveTestThriftApp.class);

public static void main(String[] args) throws Exception {
	AbstractApplicationContext context = new ClassPathXmlApplicationContext(
			"/META-INF/spring/hive-context.xml", HiveTestThriftApp.class);
	log.info("Hive Application Running");
	context.registerShutdownHook();	
				
	HiveTemplate template = context.getBean(HiveTemplate.class);
	log.info(template.query("show tables"));	
	
	HiveBattingRunsRepository repository = context.getBean(HiveThriftTemplateBattingRunsRepository.class);
	repository.processInputFile("/tmp/Batting.csv");
	log.info("Max Scoring Players per Year : \n\n" + repository.count());		    
	

}

}
