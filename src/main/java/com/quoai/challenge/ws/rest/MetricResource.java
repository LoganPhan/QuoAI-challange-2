package com.quoai.challenge.ws.rest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MetricResource {

	@GetMapping("/metrics")
	public String getGitHubMetrics() throws FileNotFoundException, IOException {
	try {
		
		GZIPInputStream gzip = new GZIPInputStream(
				new URL("https://data.gharchive.org/2015-01-01-15.json.gz").openStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
		String content;
		int size = 10;
		while (size < 10 && (content = br.readLine()) != null) {
			System.out.println(content);
			size++;
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
		return null;
	}

}
