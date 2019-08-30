package com.quoai.challenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class RepoDto {
	
	private Long id;
	
	@JsonProperty("name")
	private String repoName;
	
	private String url;
	
	private Long orgId;
	
	private String orgName;
	
	private int totalCommits;
}
