package com.quoai.challenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class OrgDto {
	
	private Long id;
	
	@JsonProperty("login")
	private String orgName;
	
	private String url;
}
