package com.hadoop.playground;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
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
//import org.apache.hadoop.util.*;

/**
 * An example MapReduce program that uses the combiner.  It uses the NYSE_daily dataset, which has a schem of:
 * exchange,stock_symbol,date,stock_price_open,stock_price_high,stock_price_low,stock_price_close,stock_volume,stock_price_adj_close
 * It finds the maximum high price for each stock over the course of all the data.
 */
public class MyCombiner {

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, FloatWritable> {
        private Text stock = new Text();

        public void map(LongWritable key, Text value, OutputCollector<Text, FloatWritable> output, Reporter reporter) throws IOException {
            // Convert the value from Text to a String so we can use the StringTokenizer on it.
            String line = value.toString();
            // Split the line into fields, using comma as the delimiter
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            // We only care about the 2nd and 5th fields (stock_symbol and stock_price_high)
            String stock_symbol = null;
            String stock_price_high = null;
            for (int i = 0; i < 5 && tokenizer.hasMoreTokens(); i++) {
                switch (i) {
                case 1:
                    stock_symbol = tokenizer.nextToken();
                    break;
                case 4:
                    stock_price_high = tokenizer.nextToken();
                    break;
                default:
                    tokenizer.nextToken();
                    break;
                }
            }

            if (stock_symbol == null || stock_price_high == null) {
                // This is a bad record, throw it out
                System.err.println("Warning, bad record!");
                return;
            } 
            
            if (stock_symbol.equals("stock_symbol")) {
                // NOP, throw out the schema line at the head of each file
            } else {
                stock.set(stock_symbol);
                FloatWritable high = new FloatWritable(Float.valueOf(stock_price_high));
                output.collect(stock, high);
            }
        }
    }

    public static class Reduce extends MapReduceBase implements Reducer<Text, FloatWritable, Text, FloatWritable> {
      public void reduce(Text key, Iterator<FloatWritable> values, OutputCollector<Text, FloatWritable> output, Reporter reporter) throws IOException {
        float max = 0.0f; // preassumably prices are never negative
        while (values.hasNext()) {
            float current = values.next().get();
            if (max < current) max = current;
        }
        output.collect(key, new FloatWritable(max));
      }
    }

    public static void main(String[] args) throws Exception {
    	
    	String[] params = new String[]{ "hdfs://localhost:8020/user/train/data/nyse"  
  			  ,"hdfs://localhost:8020/user/train/combiner_output"};	
    	
      JobConf conf = new JobConf(MyCombiner.class);
      conf.setJobName("Combiner_Example");

      conf.setOutputKeyClass(Text.class);
      conf.setOutputValueClass(FloatWritable.class);

      conf.setMapperClass(Map.class);

      /* TODO:- comment below line and add a new Combiner class as instructed */
      conf.setReducerClass(Reduce.class);

      conf.setInputFormat(TextInputFormat.class);
      conf.setOutputFormat(TextOutputFormat.class);

      FileInputFormat.setInputPaths(conf, new Path(params[0]));
      FileOutputFormat.setOutputPath(conf, new Path(params[1]));

      JobClient.runJob(conf);
    }
}