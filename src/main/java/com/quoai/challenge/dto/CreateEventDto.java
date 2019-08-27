package com.quoai.challenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class CreateEventDto {
	
	private String ref;
	@JsonProperty("ref_type")
	private String refType;
	
	@JsonProperty("master_branch")
	private String masterBranch;
	
	private String description;
	
	@JsonProperty("pusher_type")
	private String pusherType;

}
