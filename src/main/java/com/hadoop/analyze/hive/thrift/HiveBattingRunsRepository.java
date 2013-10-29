package com.hadoop.analyze.hive.thrift;


public interface HiveBattingRunsRepository {
	
	String count();

	void processInputFile(String inputFile);
	
}
