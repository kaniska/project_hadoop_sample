package com.hadoop.analyze.pig;

import java.util.Collection;

public interface BattingRunsRepository {

	void processPasswordFile(String inputFile);

	void processPasswordFiles(Collection<String> inputFiles);
	
}
