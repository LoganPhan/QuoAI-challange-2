package com.quoai.challenge.ws.rest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MetricResource {
	static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
	@GetMapping("/metrics")
	public String getGitHubMetrics() throws FileNotFoundException, IOException {
		try {
			  // This user agent is for if the server wants real humans to visit
	        
			URL url = new URL("https://data.gharchive.org/2015-01-01-15.json.gz");
			URLConnection con = url.openConnection();
			con.setRequestProperty("User-Agent", USER_AGENT);

			GZIPInputStream gzip = new GZIPInputStream(
					con.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(gzip));

			String content;
			int size = 0;
			while ((content = br.readLine()) != null) {
				System.out.println(content);
				size++;
			}
			System.out.println("Count: " + size);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
