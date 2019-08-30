package com.quoai.challenge.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PushEventDto{
	
	@JsonProperty("push_id")
	private Long pushId;
	
	private int size;
	
	@JsonProperty("distinct_size")
	private int distinctSize;
	
	private String ref;
	
	private String head;
	
	private String before;
	
	private List<CommitDto> commits;
	
	
}
