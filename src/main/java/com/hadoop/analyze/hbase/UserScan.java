package com.hadoop.analyze.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class UserScan
{

    /**
     */
    public static void main(String[] args) throws Exception
    {

        Configuration config = HBaseConfiguration.create();
        HTable htable = new HTable(config, "users");

        byte [] family = Bytes.toBytes("info");
        byte [] emailColumn = Bytes.toBytes("email");
        
        
        Scan scan = new Scan();
        ResultScanner scanner = htable.getScanner(scan);
        try {
            for (Result rr : scanner) {
                KeyValue kv = rr.getColumnLatest(family, emailColumn);
                if (kv != null)
                {
                    String user = new String(kv.getRow());
                    String email = new String(kv.getValue());
                    System.out.println (user + "=" + email);
                }
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            scanner.close();
        }
        htable.close();
        
    }

}