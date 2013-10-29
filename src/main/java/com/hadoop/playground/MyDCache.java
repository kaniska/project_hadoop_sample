package com.hadoop.playground;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.apache.hadoop.filecache.DistributedCache;
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
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

/**
 * An example MapReduce program that uses the distributed cache.  
 * It uses the NYSE_daily dataset, which has a schem of:
 * exchange,stock_symbol,date,stock_price_open,stock_price_high,stock_price_low,stock_price_close,stock_volume,stock_price_adj_close
 * and the NYSE_dividends data set, which has a schema of:
 * exchange,stock_symbol,date,dividends
 * It finds the adjusted closing price for each day that a stock reported a dividend.  
 * The dividends data is placed in the distributed
 * cache and then loaded into a lookup table so that the join can be done on the map side.
 */
public class MyDCache {

    public static class Pair<T, U> {

        public T first;
        public U second;

        public Pair(T f, U s) {
            first = f;
            second = s;
        }
    
        @Override
        public int hashCode() {
            return (((this.first == null ? 1 : this.first.hashCode()) * 17)
                    + (this.second == null ? 1 : this.second.hashCode()) * 19);
        }

        @Override
        public boolean equals(Object other) {
            if(other == null) {
                return false;
            }
            if(! (other instanceof Pair)) {
                return false;
            }
            Pair otherPair = (Pair) other;
            boolean examinedFirst = false;
            boolean examinedSecond = false;
            if (this.first == null) {
                if (otherPair.first != null) {
                    return false;
                }
                examinedFirst = true;
            }

            if (this.second == null) {
                if (otherPair.second != null) {
                    return false;
                }
                examinedSecond = true;
            }

            if (!examinedFirst && !this.first.equals(otherPair.first)) {
                return false;
            }
            if (!examinedSecond && !this.second.equals(otherPair.second)) {
                return false;
            }
            return true;
        }
    }

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, NullWritable, Text> {
        private HashSet<Pair<String, String>> lookup = null;
        private Path[] localFiles;
       
        public void configure(JobConf job) {
            // Get the cached archives/files
            try {

		/* TODO 1: invoke the DistributedCache method to get local cache files
		* and store the returned value in the localFiles instance variable */

localFiles = DistributedCache.getLocalCacheFiles(job);




                lookup = new HashSet<Pair<String, String>>();



		// Open the file as a local file
		/* TODO 2: Instantiate a FileReader and using the constructor argument,
		* wrap it around the localFiles at index zero as a String */
FileReader  fr = new FileReader(localFiles[0].toString());






                BufferedReader d = new BufferedReader(fr);
                String line;
                while ((line = d.readLine()) != null) {
                    String[] toks = new String[4];
                    toks = line.split(",", 4);
                    // put the stock symbol
                    lookup.add(new Pair<String, String>(toks[1], toks[2]));
                }
                fr.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        public void map(LongWritable key, Text value, OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {

            // Convert the value from Text to a String so we can use the StringTokenizer on it.
            String line = value.toString();
            // Split the line into fields, using comma as the delimiter
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            // We only care about the 2nd, 3rd, and 9th fields (stock_symbol, date, and stock_price_adj_close)
            String stock_symbol = null;
            String date = null;
            String stock_price_adj_close = null;
            for (int i = 0; i < 9 && tokenizer.hasMoreTokens(); i++) {
                switch (i) {
                case 1:
                    stock_symbol = tokenizer.nextToken();
                    break;

                case 2:
                    date = tokenizer.nextToken();
                    break;

                case 8:
                    stock_price_adj_close = tokenizer.nextToken();
                    break;

                default:
                    tokenizer.nextToken();
                    break;
                }
            }

            if (stock_symbol == null || date == null || stock_price_adj_close == null) {
                // This is a bad record, throw it out
                System.err.println("Warning, bad record!");
                return;
            } 
            
            if (stock_symbol.equals("stock_symbol")) {
                // NOP, throw out the schema line at the head of each file
                return;
            } 

            // Lookup the stock symbol and date in the lookup table
            if (lookup.contains(new Pair<String, String>(stock_symbol, date))) {
                StringBuilder buf = new StringBuilder(stock_symbol);
                buf.append(',').append(date).append(',').append(stock_price_adj_close);
                output.collect(NullWritable.get(), new Text(buf.toString()));
            }
        }
    }

    public static void main(String[] args) throws Exception {
      JobConf conf = new JobConf(MyDCache.class);
      conf.setJobName("DistributedCache_Example");

      conf.setOutputKeyClass(NullWritable.class);
      conf.setOutputValueClass(Text.class);

      conf.setMapperClass(Map.class);
      conf.setNumReduceTasks(0);

      conf.setInputFormat(TextInputFormat.class);
      conf.setOutputFormat(TextOutputFormat.class);

      FileInputFormat.setInputPaths(conf, new Path(args[0]));
      FileOutputFormat.setOutputPath(conf, new Path(args[1]));

	/* TODO 3: invoke the public static addCacheFile(...) method on DistributedCache,
	* passing a new URI wrapped around args[2], and a second argument the JobConf reference. */

      // COPY THE DIVIDEND FILE IN EVERY NODE of TASK TRACKER
      DistributedCache.addCacheFile(new URI(args[2]), conf);


      JobClient.runJob(conf);
    }
}