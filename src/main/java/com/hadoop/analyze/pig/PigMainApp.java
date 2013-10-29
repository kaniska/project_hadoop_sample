package com.hadoop.analyze.pig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.hadoop.fs.FsShell;

public class PigMainApp {

	private static final Log log = LogFactory.getLog(PigMainApp.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"/META-INF/spring/pig-context-repository.xml", PigMainApp.class);
		log.info("Pig Application Running");
		context.registerShutdownHook();	

		String outputDir = "hdfs://localhost:8020/user/train/data/baseball/output";		
		FsShell fsShell = context.getBean(FsShell.class);
		if (fsShell.test(outputDir)) {
			fsShell.rmr(outputDir);
		}
		
		PigBattingRunsRepository repo = context.getBean(PigBattingRunsRepository.class);
		repo.processPasswordFile("hdfs://localhost:8020/user/train/data/baseball/");
		
		/*
		Collection<String> files = new ArrayList<String>();
		files.add("/data/passwd/input");
		files.add("/data/passwd/input2");
		repo.processPasswordFiles(files);
		*/

	}

}
