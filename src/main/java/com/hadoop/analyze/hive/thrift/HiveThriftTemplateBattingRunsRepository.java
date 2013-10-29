package com.hadoop.analyze.hive.thrift;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.hadoop.hive.HiveOperations;
import org.springframework.stereotype.Repository;

@Repository
public class HiveThriftTemplateBattingRunsRepository implements
		HiveBattingRunsRepository {

	private @Value("${hive.table}")
	String tableName;

	private HiveOperations hiveOperations;

	@Autowired
	public HiveThriftTemplateBattingRunsRepository(HiveOperations hiveOperations) {
		this.hiveOperations = hiveOperations;
	}

	@Override
	public String count() {
		List<String> resultSet = hiveOperations.query(
				"SELECT" + " a.year , a.player_id, a.runs"
						+ " FROM batting a JOIN (SELECT year, max(runs) runs"
						+ " FROM batting GROUP BY year) b"
						+ " ON (a.year = b.year AND a.runs = b.runs);");
		
		StringBuilder sb = new StringBuilder();
		
		for (String string : resultSet) {
			sb.append(string).append("\n");
		}

		return sb.toString();
	}

	@Override
	public void processInputFile(String inputFile) {
		Map parameters = new HashMap();
		parameters.put("inputFile", inputFile);
		hiveOperations.query("classpath:baseball-analysis.hql", parameters);
	}

}