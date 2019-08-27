package com.quoai.challenge.ws.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MetricResource {
	
	 @GetMapping("/metrics")
	    public String getGitHubMetrics() {
	        return "aaa";
	    }
	 
}
