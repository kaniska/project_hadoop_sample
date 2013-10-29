package com.hadoop.analyze.hive.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.test.jdbc.JdbcTestUtils;

import com.hadoop.analyze.hive.thrift.HiveBattingRunsRepository;

@Repository
public class HiveJDBCTemplateBattingRunsRepository implements HiveBattingRunsRepository, ResourceLoaderAware  {

	private ResourceLoader resourceLoader;
	
	private @Value("${hive.table}") String tableName;
	
	//private HiveOperations hiveOperations;
	private @Autowired JdbcOperations hiveOperations;
	
	private @Autowired JdbcTemplate jdbcTemplate;
	
	/*@Autowired
	public HiveTemplateBattingRunsRepository(JdbcOperations hiveOperations) {
		this.hiveOperations = hiveOperations;
	}*/
	
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	public String execute() {
		SqlRowSet rowSet = hiveOperations.queryForRowSet("SELECT" +
				" a.year , a.player_id, a.runs" +
				" FROM batting a JOIN (SELECT year, max(runs) runs" +
				" FROM batting GROUP BY year) b" +
				" ON (a.year = b.year AND a.runs = b.runs)", null);
		
		StringBuilder sb = new StringBuilder();
		
		while(rowSet.next()) {
			sb.append(rowSet.getString(0));
		}
		return sb.toString();
	}

	public void processInputFile(String inputFile) {
		JdbcTestUtils.executeSqlScript(jdbcTemplate,
				resourceLoader.getResource(inputFile),
				true);
		
		
		/*Map parameters = new HashMap();
		parameters.put("inputFile", inputFile);
		hiveOperations.query("classpath:baseball-analysis.hql", parameters);	*/
	}

	@Override
	public String count() {
		// TODO Auto-generated method stub
		return null;
	}

}