/* Wordcount example with important map and reduce functions SimpleWordCount.java */



import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import java.util.*;
import java.io.*;

import org.apache.spark.api.java.JavaSparkContext;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

public class SampleJson {

  public static void main(String[] args) {

	SparkConf conf = new SparkConf().setAppName("SampleJson Application");
	JavaSparkContext sc = new JavaSparkContext(conf);
	SQLContext sqlContext = new SQLContext(sc);
	DataFrame df = sqlContext.jsonFile("hdfs://localhost:9000/weatherTweets.json");
	df.registerTempTable("weather");
	//df.printSchema();


	//   DataFrame df4 = sqlContext.sql("SELECT user.name FROM df");
	//   DataFrame df5 = sqlContext.sql("SELECT user.location, count(user.location) FROM df WHERE text LIKE '%snow%' AND user.location != 'null' GROUP BY user.location ORDER BY count(user.location)");//didnt like the count

	//DataFrame df5 = sqlContext.sql("SELECT user.location FROM df WHERE text LIKE '%snow%' AND user.location != 'null' GROUP BY user.location "); //worked 

	//DataFrame df5 = sqlContext.sql(" SELECT 'snow' AS 'condition', user.time_zone FROM df WHERE text LIKE '%snow%' UNION SELECT 'rain' AS 'condition', user.time_zone FROM df WHERE text LIKE '%rain%' UNION SELECT 'sunny' AS 'condition', user.time_zone FROM df WHERE text LIKE '%sunny%' "); //didnt like the AS Condition

	//DataFrame df5 = sqlContext.sql(" SELECT 'snow', user.time_zone FROM df WHERE text LIKE '%snow%' UNION SELECT 'rain', user.time_zone FROM df WHERE text LIKE '%rain%' UNION SELECT 'sunny' , user.time_zone FROM df WHERE text LIKE '%sunny%' "); //worked

	//DataFrame df5 = sqlContext.sql("SELECT DISTINCT lang FROM df ORDER BY lang");//worked

	try
	{
		FileWriter fstream = new FileWriter("/home/hdcuser/Desktop/test.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		//out.write("command line args[0]:  " + args[0]);
		//out.newLine();


		if (args[0].equals("1"))
		{
			DataFrame result = sqlContext.sql("SELECT lang, count(lang) FROM weather WHERE lang != 'null'GROUP BY lang");
			List<WeatherData> wds = result.javaRDD().map(new Function<Row, WeatherData>(){
			public WeatherData call(Row row){
				WeatherData wd = new WeatherData();
				wd.lang = row.getString(0);
				wd.count = row.getLong(1);
				return  wd;
			}}).collect();


			// file data format
			// english>100
			for(WeatherData wd : wds){
				out.write(wd.lang + ">" + wd.count.toString());
				out.newLine();
			}
		}
		else if (args[0].equals("2"))
		{
			//DataFrame result = sqlContext.sql("SELECT user.location, count(text) FROM weather WHERE text LIKE '%snow%' AND user.location != 'null' GROUP BY user.location");
			// winter, sunny, rain
			DataFrame result = sqlContext.sql("SELECT coordinates.coordinates, user.screen_name, text FROM weather WHERE coordinates IS NOT NULL AND (text LIKE '%winter%' OR text LIKE '%sunny%' OR text LIKE '%rain%') LIMIT 50");
			result.show();
			List<WeatherData> wds = result.javaRDD().map(new Function<Row, WeatherData>(){
			public WeatherData call(Row row){
				WeatherData wd = new WeatherData();
				wd.coordinates = row.getAs(0);
				wd.screenname = row.getString(1);
				wd.text = row.getString(2);
				return  wd;
			}}).collect();

			// file data format
			// kansas city>100
			for(WeatherData wd : wds){
				//ArrayBuffer(-3.58333, 50.55)
				String coord = wd.coordinates.toString();
				int start = coord.indexOf("(") + 1;
				int end = coord.indexOf(")");
				String condition = "Rain";
				if (wd.text.contains("winter"))
					condition = "Winter";
				else if (wd.text.contains("sunny"))
					condition = "Sunny";

				out.write(coord.substring(start, end) + ">" + wd.screenname + ">" + condition);
				out.newLine();
			}

		}
		else if (args[0].equals("3"))
		{
			DataFrame result = sqlContext.sql("SELECT 'Snow',user.time_zone,count(user.time_zone) FROM weather WHERE text LIKE '%snow%' AND user.time_zone != 'null' GROUP BY 'Snow', user.time_zone UNION SELECT 'Rain',user.time_zone,count(user.time_zone) FROM weather WHERE text LIKE '%rain%' AND user.time_zone != 'null' GROUP BY 'Rain',user.time_zone UNION SELECT 'Sunny',user.time_zone,count(user.time_zone) FROM weather WHERE text LIKE '%sunny%' AND user.time_zone != 'null' GROUP BY 'Sunny',user.time_zone ");

			List<WeatherData> wds = result.javaRDD().map(new Function<Row, WeatherData>(){
			public WeatherData call(Row row){
				WeatherData wd = new WeatherData();
				wd.condition = row.getString(0);
				wd.timezone = row.getString(1);
				wd.count = row.getLong(2);
				return  wd;
			}}).collect();

			// file data format
			// sunny>Central Time>3
			for(WeatherData wd : wds){
				out.write(wd.condition + ">" + wd.timezone + ">" + wd.count.toString());
				out.newLine();
			}

		}
		else if (args[0].equals("4"))
		{
			//DataFrame result = sqlContext.sql("SELECT text FROM weather WHERE retweet_count > 20 OR favorite_count > 50"); -- not working yet
			DataFrame result = sqlContext.sql("SELECT text, retweeted_status.retweet_count FROM weather WHERE retweeted_status IS NOT NULL AND retweeted_status.retweet_count > 50 LIMIT 50");
			result.show();
			List<WeatherData> wds = result.javaRDD().map(new Function<Row, WeatherData>(){
			public WeatherData call(Row row){
				WeatherData wd = new WeatherData();
				wd.text = row.getString(0);
				wd.count = row.getLong(1);
				return  wd;
			}}).collect();

			// file data format
			// text
			for(WeatherData wd : wds){
				out.write(wd.text + ">" + wd.count.toString());
				out.newLine();
			}
		}
		else if (args[0].equals("5"))
		{
			//Get the user names of all the tweets who have tweeted weather more than 1 times
			DataFrame result = sqlContext.sql("SELECT user.screen_name, count(user.screen_name) FROM weather WHERE text LIKE '%weather%' GROUP BY user.screen_name HAVING count(user.screen_name) > 1");
			result.show();
			List<WeatherData> wds = result.javaRDD().map(new Function<Row, WeatherData>(){
			public WeatherData call(Row row){
				WeatherData wd = new WeatherData();
				wd.screenname = row.getString(0);
				wd.count = row.getLong(1);
				return  wd;
			}}).collect();

			// file data format
			// screen_name>109
			for(WeatherData wd : wds){
				out.write(wd.screenname + ">" + wd.count.toString());
				out.newLine();
			}
		}
		else if (args[0].equals("6"))
		{
			//get different source(such as iphone, android or web) of tweet for each country 
			//DataFrame result = sqlContext.sql("SELECT place.country, source, count(source) AS TotalUsers FROM weather WHERE place.country != 'null' GROUP BY place.country, source ORDER BY TotalUsers DESC LIMIT 10");
			DataFrame result = sqlContext.sql("SELECT place.country, source, count(source) AS TotalUsers FROM weather WHERE (place.country = 'United States' OR place.country = 'United Kingdom' OR place.country = 'Canada' OR place.country = 'Australia') AND (source LIKE '%Twitter for iPhone%' OR source LIKE '%Twitter for Android%' OR source LIKE '%Twitter Web Client%') GROUP BY place.country, source");

			result.show();
			List<WeatherData> wds = result.javaRDD().map(new Function<Row, WeatherData>(){
			public WeatherData call(Row row){
				WeatherData wd = new WeatherData();
				wd.country = row.getString(0);
				wd.source = row.getString(1);
				wd.count = row.getLong(2);
				return  wd;
			}}).collect();

			// file data format
			// country>source>109
			for(WeatherData wd : wds){
				//Only get the source value from href tag
				String s = "iPhone";
				if (wd.source.contains("Twitter for Android"))
				{
					s = "Android";
				}
				else if (wd.source.contains("Twitter Web Client"))
				{
					s = "Web";
				}

				out.write(wd.country + ">" + s + ">" + wd.count.toString());
				out.newLine();
			}
		}
		else if (args[0].equals("7"))
		{
			//Get the top 5 country  that has tweeted about storm where users have tweeted about storm
			//DataFrame result = sqlContext.sql("SELECT place.country, count(place.country) FROM weather WHERE user.screen_name IN (SELECT user.screen_name FROM weather WHERE text LIKE '%storm%' OR text LIKE '%rain%') GROUP BY place.country");
			//DataFrame result = sqlContext.sql("SELECT place.country, count(place.country) FROM weather WHERE place.country != 'null' AND (text LIKE '%storm%' OR text LIKE '%rain%') GROUP BY place.country");
			//DataFrame result = sqlContext.sql("SELECT place.country, user.screen_name FROM weather WHERE text LIKE '%storm%'");

			DataFrame result = sqlContext.sql("SELECT DISTINCT u1.user.screen_name, u2.text FROM weather u1 JOIN weather u2 ON (u1.user.screen_name = u2.retweeted_status.user.screen_name) ORDER BY u1.user.screen_name"); 
			//DataFrame result = sqlContext.sql("SELECT u1.user.screen_name, count(u2.retweeted_status.user.screen_name) FROM weather u1 JOIN weather u2 ON (u1.user.screen_name = u2.retweeted_status.user.screen_name) GROUP BY u2.retweeted_status.user.screen_name");
			result.show();
			List<WeatherData> wds = result.javaRDD().map(new Function<Row, WeatherData>(){
			public WeatherData call(Row row){
				WeatherData wd = new WeatherData();
				wd.screenname = row.getString(0);
				//wd.text = row.getString(1);
				wd.count = row.getLong(1);
				return  wd;
			}}).collect();

			// file data format
			// country>109
			for(WeatherData wd : wds){
				//Only get the source value from href tag
				out.write(wd.screenname + ">" + wd.count.toString());
				out.newLine();
			}
		}
		else if (args[0].equals("8"))
		{
			DataFrame result = sqlContext.sql("SELECT count(lang), lang FROM df GROUP BY lang");
		}

		out.close();
	}
	catch (Exception e)
	{
		// do nothing
	}
	finally
	{
	}

  }
}

