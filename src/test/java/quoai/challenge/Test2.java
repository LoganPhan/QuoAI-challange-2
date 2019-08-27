package quoai.challenge;

import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class Test2 {
	static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

	public static void main(String[] args) {
	    try (RandomAccessFile reader = new RandomAccessFile("https://data.gharchive.org/2015-01-01-15.json.gz", "r");
	            FileChannel channel = reader.getChannel();
	            ByteArrayOutputStream out = new ByteArrayOutputStream()){
	    	 int bufferSize = 1024;
	         if (bufferSize > channel.size()) {
	            bufferSize = (int) channel.size();
	         }
	         ByteBuffer buff = ByteBuffer.allocate(bufferSize);
	  
	         while (channel.read(buff) > 0) {
	             out.write(buff.array(), 0, buff.position());
	             buff.clear();
	         }
	          
	      String fileContent = new String(out.toByteArray(), StandardCharsets.UTF_8);
	   
	      System.out.println(fileContent);
	    }catch (Exception e) {
	    	e.printStackTrace();
		}
		        
	}
}
