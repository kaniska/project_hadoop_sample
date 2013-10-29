Hadoop setup on Ubuntu :

The steps are mentioned in my blog post :
http://phisymmetry.wordpress.com/2013/03/24/programming-with-cdh4-hadoop-in-springsource-on-ubuntu/#more-5891

Remember to point to mysql as hive metastore.

# Building and running

    $ hadoop dfs -copyFromLocal /tmp/gutenberg/download /user/gutenberg/input
    $ hadoop dfs -rmr /user/gutenberg/qa/output
    $ cd hadoop/wordcount-spring-intermediate
    $ mvn clean package appassembler:assemble
    $ export ENV=qa
    $ sh ./target/appassembler/bin/wordcount
    

=========================================

Loading the Application Context When the Application Starts

We can execute the created Hadoop job by loading the application context when our application is started. 
We can do this by creating a new ClasspathXmlApplicationContext object and 

============================================



Remember to start hive thrift server before running the application :
hive --service hiveserver
