package com.hadoop.playground;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Partitioner;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
//import org.apache.hadoop.util.*;

/**
 * An example MapReduce program that uses a custom partitioner.  It uses the NYSE_daily dataset, which has a schem of:
 * exchange,stock_symbol,date,stock_price_open,stock_price_high,stock_price_low,stock_price_close,stock_volume,stock_price_adj_close
 * It will guarantee that every record whose stock symbol begins with a given letter goes to the same reducer.  Obviously this algorithm
 * does not scale beyond 26 reducers.
 */
public class MRPartitioner {

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
        private Text stock = new Text();

        public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            // Convert the value from Text to a String so we can use the StringTokenizer on it.
            String line = value.toString();
            // Split the line into fields, using comma as the delimiter
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            // We only care about the 2nd and 5th fields (stock_symbol and stock_price_high)
            String stock_symbol = null;
            for (int i = 0; i < 2 && tokenizer.hasMoreTokens(); i++) {
                switch (i) {
                case 1:
                    stock_symbol = tokenizer.nextToken();
                    break;

                default:
                    tokenizer.nextToken();
                    break;
                }
            }

            if (stock_symbol == null) {
                // This is a bad record, throw it out
                System.err.println("Warning, bad record!");
                return;
            } 
            
            if (stock_symbol.equals("stock_symbol")) {
                // NOP, throw out the schema line at the head of each file
            } else {
                stock.set(stock_symbol);
                output.collect(stock, value);
            }
        }
    }

    public static class Reduce extends MapReduceBase implements Reducer<Text, Text, NullWritable, Text> {
      public void reduce(Text key, Iterator<Text> values, OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
        while (values.hasNext()) {
            output.collect(NullWritable.get(), values.next());
        }
      }
    }

    public static class AlphabeticPartitioner implements Partitioner<Text, Text> {
        public int getPartition(Text key, Text value, int numPartitions) 

	{
        
		return (key.toString().charAt(0) - 'A') % numPartitions;

        }

        public void configure(JobConf job) {
        }
    }
        
    public static void main(String[] args) throws Exception {
    	
    	String[] params = new String[]{ "hdfs://localhost:8020/user/train/data/nyse"  
    			  ,"hdfs://localhost:8020/user/train/part_output"};	
    	
    	
      JobConf conf = new JobConf(MRPartitioner.class);
      conf.setJobName("Partitioner_Example");

      conf.setOutputKeyClass(Text.class);
      conf.setOutputValueClass(Text.class);

      conf.setMapperClass(Map.class);
      conf.setReducerClass(Reduce.class);

/* TODO: set the partitioner class to the alphabetic partitioner we provided */
conf.setPartitionerClass(AlphabeticPartitioner.class);
	/* TODO: set the number of reduce tasks to twenty six */
conf.setNumReduceTasks(26);

      conf.setInputFormat(TextInputFormat.class);
      conf.setOutputFormat(TextOutputFormat.class);

      FileInputFormat.setInputPaths(conf, new Path(params[0]));
      FileOutputFormat.setOutputPath(conf, new Path(params[1]));

      JobClient.runJob(conf);
    }
}
