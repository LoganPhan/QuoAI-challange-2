package com.quoai.challenge.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response<PayLoad> {

	private String id;
	private String type;
	private RepoDto repo;
	private OrgDto org;
	
	@JsonProperty("payload")
	private PayLoad payLoad;
	
	@JsonProperty("created_at")
	private ZonedDateTime createAt;
}
