package com.hadoop.playground;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Mapper1 receives >> url 1 : product 1, product 2, product 3
 *                 >> url 2 : product 1, product 4
 * Mapper 1 :: extracts url 1 and sets in the VALUE , extracts product and sets in KEY SET                
 * Mapper1 emits :: output.collect -> {product 1, url 1}, output.collect -> {product 2, url 1}
 * Mapper2 emits :: output.collect -> {product 1, url 2}, output.collect -> {product 4, url 2}
 * 
 * Shuffler shuffles and merges :: {product 1, {url1, url2, url 5} } , {product 4, {url 2}}
 * 
 * Reducer 1 receives :: {product 1 , {url1, url2, url 5} }
 * Reducer - simply concatenates url names... 
 * Reducer 1 emits :: {product 1, url1 url2 url5 }
 * 
 * @author root
 *
 */
public class ProductSearchIndexer extends Configured implements Tool {

  static class Mapper extends MapReduceBase 
  implements org.apache.hadoop.mapred.Mapper<LongWritable, Text, Text, Text> {
    private Text url = new Text();
    private Text product = new Text();
    
    @Override
    public void map(LongWritable key, Text value,
        OutputCollector<Text, Text> output, Reporter reporter)
        throws IOException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      url.set(itr.nextToken());
      while (itr.hasMoreTokens()) {
        product.set(itr.nextToken());
        output.collect(product, url);
      }
    }
  }
  
  static class Reducer extends MapReduceBase
  implements org.apache.hadoop.mapred.Reducer<Text, Text, Text, Text> {

    private Text urls = new Text();
    
    @Override
    public void reduce(Text key, Iterator<Text> values,
        OutputCollector<Text, Text> output, Reporter reporter)
        throws IOException {
      StringBuilder sb = new StringBuilder();
      while (values.hasNext()) {
        sb.append(" ");
        sb.append(values.next().toString());
      }
      urls.set(sb.toString());
      output.collect(key, urls);
    }
    
  }
  
  @Override
  public int run(String[] args) throws Exception {
    if (args.length != 2) {
      System.out.println("Usage: ProductSearchIndexer <in-dir> <out-dir>");
      ToolRunner.printGenericCommandUsage(System.out);
      return -1;
    }
    
    JobConf job = new JobConf(getConf(), ProductSearchIndexer.class);
    job.setJobName("product-search-indexer");
    job.setMapperClass(Mapper.class);
    job.setCombinerClass(Reducer.class);
    job.setReducerClass(Reducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    Date startTime = new Date();
    System.out.println("Job started: " + startTime);
    JobClient.runJob(job);
    Date endTime = new Date();
    System.out.println("Job ended: " + endTime);
    System.out.println("The job took " + 
                       ((endTime.getTime() - startTime.getTime()) / 1000) + 
                       " seconds.");
    
    return 0;
  }

  public static void main(String[] args) throws Exception {
	String[] params = new String[]{ "hdfs://localhost:8020/user/train"  
		  ,"hdfs://localhost:8020/user/train/output1"};
    int res = 
        ToolRunner.run(new Configuration(), new ProductSearchIndexer(), params);
    System.exit(res);
  }

}
