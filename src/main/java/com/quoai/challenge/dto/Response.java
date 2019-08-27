package com.quoai.challenge.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Response<PayLoad> {
	
	private String id;
	private String type;
	private RepoDto repo;
	private OrgDto org;
	
	private PayLoad payload;
	
	@JsonProperty("created_at")
	private ZonedDateTime createAt;
}
