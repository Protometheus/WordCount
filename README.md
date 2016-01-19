* Download hadoop-2.7.1
* Unpack it, set `HADOOP_HOME`, and add `$HADOOP_HOME/bin` to your `PATH`
* Build w/ `mvn clean install`
* Run `hadoop jar target/wc-0.0.1-SNAPSHOT.jar wc.Transmogrify input output`
  (make sure output directory doesn't already exist)


