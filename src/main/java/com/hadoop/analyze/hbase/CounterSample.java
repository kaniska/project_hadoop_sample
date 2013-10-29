package com.hadoop.analyze.hbase;

import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;

public class CounterSample {

	/**
     */
    public static void main(String[] args) throws Exception
    {
        
        Configuration config = HBaseConfiguration.create();
        // create table
        HTableDescriptor tableDescriptor = new HTableDescriptor("hits");
        HBaseAdmin admin = new HBaseAdmin(config);
        
        if (!admin.tableExists("hits"))
        {
            tableDescriptor.addFamily(new HColumnDescriptor("info"));
            admin.createTable(tableDescriptor);
            System.out.println ("created table");
        }
        else
        {
            System.out.println ("table exists");
        }
        
        HTable htable = new HTable(config, "hits");
        String [] domains = {"amazon.com", "facebook.com", "yahoo.com", "google.com"};
        Random rand = new Random();
        
        byte [] rowkey = Bytes.toBytes("2011-12-01");
        byte [] family = Bytes.toBytes("info");
        
        for (int i=0; i < 10; i++)
        {
            String domain = domains[rand.nextInt(domains.length)];
            long updated = htable.incrementColumnValue(rowkey, family, Bytes.toBytes(domain), 1)  ;
            System.out.println (domain + " : "  + updated);
        }
    }

}
