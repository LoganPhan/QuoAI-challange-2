package com.quoai.challenge.ws.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quoai.challenge.dto.RepoDto;
import com.quoai.challenge.service.MetricService;

@RestController
@RequestMapping("/api")
public class MetricResource {
	
	@Autowired
	private MetricService metricService;
	
	@GetMapping("/metrics")
	public List<RepoDto> getGitHubMetrics() throws FileNotFoundException, IOException {
		return metricService.downloadGitHubDataSource();
	}

}
