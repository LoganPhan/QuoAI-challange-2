package com.quoai.challenge.ws.rest;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.quoai.challenge.service.MetricService;

@RestController
@RequestMapping("/api")
public class MetricResource {
	
	@Autowired
	private MetricService metricService;
	
	@RequestMapping(method = RequestMethod.GET, value="/metrics",produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public ResponseEntity<?> getGitHubMetrics(HttpServletResponse response) throws FileNotFoundException, IOException {
		String data = metricService.downloadGitHubDataSource();
	
		return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=metrics.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(data);
	}

	
}
