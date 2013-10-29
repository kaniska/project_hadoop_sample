package com.hadoop.playground;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;

/**
pass parameters :
input-file.txt
 hdfs://localhost:8020/user/train
*/

public class ShowAPI {

	private static final Log log = LogFactory.getLog(ShowAPI.class);

	
	public static void main(String[] args) {
		if (args.length == 2) {
			String inputPath = args[0];
			String outputPath = args[1];
			try {
				ShowAPI showMe = new ShowAPI();
				showMe.showHDFS(new Path(inputPath), new Path(outputPath));

				/**/
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public void showHDFS(Path inPath, Path outPath) throws IOException {
		Configuration config = new Configuration();
		FileSystem hdfs = FileSystem.get(config);
		LocalFileSystem local = FileSystem.getLocal(config);
		FSDataInputStream inStream = local.open(inPath);
		FSDataOutputStream outStream = hdfs.create(outPath);
		byte[] fromFile = new byte[1000];
		int data = 0;
		while ((data = inStream.read(fromFile)) > 0)
			outStream.write(fromFile, 0, 1000);
		inStream.close();
		outStream.close();

	}

}
