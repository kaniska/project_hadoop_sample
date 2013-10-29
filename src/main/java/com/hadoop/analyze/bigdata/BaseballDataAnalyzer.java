/**
 * 
 */
package com.hadoop.analyze.bigdata;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

/**
 * @author root
 *
 */
public class BaseballDataAnalyzer {
	
	 public static class Map extends MapReduceBase 
	 	implements Mapper<LongWritable, Text, Text, IntWritable> {
		 
	        private Text team = new Text();

	        public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
	            // Convert the value from Text to a String so we can use the StringTokenizer on it.
	            String line = value.toString();
	            if(line.startsWith("playerID")) {
	             return;	
	            }
	            
	            // Split the line into fields, using comma as the delimiter
	            StringTokenizer tokenizer = new StringTokenizer(line, ",");
	            // We only care about (Team Id, Year, Run Count)
	            String teamId = null;
	            String year = null;
	            String runCount = null;
	            
	            for (int i = 0; i < 10 && tokenizer.hasMoreTokens(); i++) {
	                switch (i) {
	                case 1:
	                	year = tokenizer.nextToken();
	                    break;
	                case 3:
	                    teamId = tokenizer.nextToken();
	                    break;
	                case 8:
	                    runCount = tokenizer.nextToken();
	                    break;
	                default:
	                    tokenizer.nextToken();
	                    break;
	                }
	            }
	            if (teamId == null || year == null || runCount == null) {
	                // This is a bad record, throw it out
	                System.err.println("Warning, bad record!");
	                return;
	            } 
	            
	            
	            if(!year.trim().equals("1956")){
	            	return;
	            }

	            
	            
	            {
	            	team.set(teamId);
	                IntWritable runs = new IntWritable(Integer.valueOf(runCount));
	                output.collect(team, runs);
	            }
	        }
	    }

	 public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
	      public void reduce(Text team, Iterator<IntWritable> values, 
	    		  OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
	        int totalRuns = 0; // 
	        while (values.hasNext()) {
	            int currentRuns = values.next().get();
	            totalRuns += currentRuns;
	        }
	        output.collect(team, new IntWritable(totalRuns));
	      }
	    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	   	String[] params = new String[]{ "hdfs://localhost:8020/user/train/data/baseball/"  
	  			  ,"hdfs://localhost:8020/user/train/baseball_output3"};	
	    	
	      JobConf conf = new JobConf(BaseballDataAnalyzer.class);
	      conf.setJobName("Baseball Runs Analyzer");

	      conf.setOutputKeyClass(Text.class);
	      conf.setOutputValueClass(IntWritable.class);

	      conf.setMapperClass(Map.class);
	      conf.setReducerClass(Reduce.class);

	      conf.setInputFormat(TextInputFormat.class);
	      conf.setOutputFormat(TextOutputFormat.class);

	      FileInputFormat.setInputPaths(conf, new Path(params[0]));
	      FileOutputFormat.setOutputPath(conf, new Path(params[1]));

	      try {
			JobClient.runJob(conf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
