package wc;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Transmogrify extends Configured implements Tool {

	public static class WordMapper extends Mapper<Object, Text, Text, IntWritable> {
		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(String.valueOf(value));
			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				context.write(word, new IntWritable(1));
			}
		}
	}

	public static class CountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values)
				sum += val.get();

			result.set(sum);
			context.write(key, result);
		}
	}

	public static class KeyComparator extends WritableComparator {
		protected KeyComparator() {
			super(Text.class, true);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public int compare(WritableComparable w1, WritableComparable w2) {
			Text word1 = (Text) w1;
			Text word2 = (Text) w2;
			return String.valueOf(word1).compareToIgnoreCase(String.valueOf(word2));
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = new Job();
		job.setJarByClass(Transmogrify.class);
		job.setMapperClass(WordMapper.class);
		job.setCombinerClass(CountReducer.class);
		job.setReducerClass(CountReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setSortComparatorClass(KeyComparator.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		Transmogrify driver = new Transmogrify();
		int exitCode = ToolRunner.run(driver, args);
		System.exit(exitCode);
	}

}
