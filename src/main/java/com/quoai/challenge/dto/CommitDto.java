package com.quoai.challenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class CommitDto {
	
	private String sha;
	
	@JsonProperty("author")
	private AuthorDto author;
	
	@JsonProperty("message")
	private String message;
	
	private Boolean distinct;
	
	@JsonProperty("pusher_type")
	private String url;

}
