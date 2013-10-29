package com.hadoop.analyze.hbase;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;



public class UserPut
{

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception
    {
        Configuration config = HBaseConfiguration.create();

        // create table
        HTableDescriptor tableDescriptor = new HTableDescriptor("users");
        HBaseAdmin admin = new HBaseAdmin(config);
        
        if (!admin.tableExists("users"))
        {
            tableDescriptor.addFamily(new HColumnDescriptor("info"));
            admin.createTable(tableDescriptor);
            System.out.println ("created table");
        }
        else
        {
            System.out.println ("table exists");
        }
        

        HTable htable = new HTable(config, "users");
        long t1 = System.currentTimeMillis();
        int total = 10;
        byte [] family = Bytes.toBytes("info");
        for (int i=1; i <= total ; i++)
        {
            String userName = "user" + i;
            String email = "user" + i + "@foo.com";
            String phone = "555-1234";
            
            byte [] key = Bytes.toBytes(userName);
            
            Put put = new Put (key);
            
            // add email
            byte [] qualifier = Bytes.toBytes("email");
            byte [] value = Bytes.toBytes(email);
            put.add(family, qualifier, value);
     
            // and phone number
            put.add(family, Bytes.toBytes("phone"), Bytes.toBytes(phone));
            
            htable.put(put);
            System.out.println ("added user : " + userName);
        }
        long t2 = System.currentTimeMillis();
        System.out.println ("inserted " + total + " users  in " + (t2-t1) + " ms");
        htable.close();

    }

}
