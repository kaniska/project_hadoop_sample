package com.hadoop.playground;

//Standard Java imports 
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
//Standard Hadoop imports

public class MyWritable {

	// A Hadoop Writable datatype for use by my MapReduce code.
	public static class PairWritable implements Writable {	
	// TODO :- Private Variables for PairWritable datatype
		private Long myLong;
		private String myString;
	// TODO :-  Override the Hadoop serialization/Writable interface methods
		@Override
		public void write(DataOutput out) throws IOException {
			out.writeLong(myLong);
			out.writeUTF(myString);
		}
		@Override
		public void readFields(DataInput in) throws IOException {
			myLong = in.readLong();
			myString = in.readUTF();
		}
	//End of Implementation 

	  //Getter and Setter methods for myLong and mySring variables
  		public void set(Long l, String s) {
          		myLong = l;
          		myString = s;
  		}
  		public Long getLong() {
          		return myLong;
  		}
  		public String getString() {
          	return myString;
  		}


	}

	//Mapper class here
	public static class MyMapper extends MapReduceBase implements
		Mapper<LongWritable, Text, NullWritable, PairWritable>{

		public void map(LongWritable inputKey, Text inputValue,
				OutputCollector<NullWritable, PairWritable> output, Reporter reporter)
			throws IOException {
			PairWritable myPair = new PairWritable();
			myPair.set(inputKey.get(), inputValue.toString());
			output.collect(NullWritable.get(), myPair);
		}
	}		

	//Reducer class here
  	public static class MyReducer extends MapReduceBase implements
       		Reducer<NullWritable, PairWritable, LongWritable, Text> {

		public void reduce(NullWritable key, Iterator<PairWritable> myPair, OutputCollector<LongWritable, Text> output, Reporter reporter)
			throws IOException {
			while(myPair.hasNext()) {
			
				PairWritable myPairWritable = myPair.next();
				Long longValue = myPairWritable.getLong();
				String strValue = myPairWritable.getString();
				output.collect(new LongWritable(longValue), new Text(strValue));
			}
		}		
	}

  	/**
  	 * S1 : Given input data -> extract key and list of values
  	 *      PairWritable => { input.key LONG, input.val TEXT }
  	 *      Output " NullKey, List of PairWritable "
  	 * S2 : Analyze the Input i.e. " NullKey, List of PairWritable "       
  	 *      Output : List {key LONG : value TEXT}   
  	 *  
  	 * @param args
  	 * @throws IOException
  	 */

        public static void main(String[] args) throws IOException {
        	
        	String[] params = new String[]{ "hdfs://localhost:8020/user/train/input.txt"  
        			  ,"hdfs://localhost:8020/user/train/output2"};
        	
                //Code to create a new Job specifying the MapReduce class
                final JobConf conf = new JobConf(MyWritable.class);
                // MR related classes
                conf.setMapOutputKeyClass(NullWritable.class);
                conf.setMapOutputValueClass(PairWritable.class);

                // HDFS related classes
                conf.setOutputKeyClass(LongWritable.class);
                conf.setOutputValueClass(Text.class);

                //Set your mapper and reducer classes
                conf.setMapperClass(MyMapper.class);
                conf.setReducerClass(MyReducer.class);

                //File Input/Output argument passed as a command line argument
                FileInputFormat.setInputPaths(conf, new Path(params[0]));
                FileOutputFormat.setOutputPath(conf, new Path(params[1]));

                //statement to execute the job
                JobClient.runJob(conf);
        }
}

