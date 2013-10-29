package com.hadoop.analyze.bigdata;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
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

public class RunsPerYear {
	
	public static class MyMap extends MapReduceBase implements Mapper<LongWritable, 
						Text, LongWritable, LongWritable> {

		LongWritable outKey = new LongWritable();
		LongWritable outVal = new LongWritable();
		
		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<LongWritable, LongWritable> output,
				Reporter reporter) throws IOException {
			
			String[] values = value.toString().split(",");
			
			if(StringUtils.isNotEmpty(values[0]) && !values[0].equals("playerID")
					&& StringUtils.isNotEmpty(values[1]) && StringUtils.isNotEmpty(values[8])) {
				
				outKey.set(Long.valueOf(values[1]));
				outVal.set(Long.valueOf(values[8]));		
				
				output.collect(outKey, outVal);
			}
		}		
	}
	
    public static class MyReduce extends MapReduceBase implements 
        Reducer<LongWritable, LongWritable, LongWritable, LongWritable> {

    	LongWritable outTotal = new LongWritable();
    	
		@Override
		public void reduce(LongWritable key, Iterator<LongWritable> values,
				OutputCollector<LongWritable, LongWritable> output,
				Reporter reporter) throws IOException {
			
			long total = 0;			
			while(values.hasNext()) {
				total += values.next().get();
			}			
			outTotal.set(total);
			
			output.collect(key, outTotal);
		}
    	
    }
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// playerID,yearID,stint,teamID,lgID,G,G_batting,AB,R,H,2B,3B,HR,RBI,SB,CS,BB,SO,IBB,HBP,SH,SF,GIDP,G_old		
		// values[1] => YearId , values[8] => Runs
        
		String[] params = new String[]{ "hdfs://localhost:8020/user/train/data/baseball/"  
	  			  ,"hdfs://localhost:8020/user/train/baseball_output5"};	
	    	
	      JobConf conf = new JobConf(RunsPerYear.class);
	      conf.setJobName("Baseball Runs Analyzer");
	      conf.setInputFormat(TextInputFormat.class);
	      conf.setOutputFormat(TextOutputFormat.class);

	      conf.setMapperClass(MyMap.class);
	      conf.setReducerClass(MyReduce.class);
	      conf.setMapOutputKeyClass(LongWritable.class);
	      conf.setMapOutputValueClass(LongWritable.class);

	      conf.setOutputKeyClass(LongWritable.class);
	      conf.setOutputValueClass(LongWritable.class);
	      
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
