package com.quoai.challenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReleaseEventDto {
	
	private String action;
	
	@JsonProperty("release")
	private Release release;
	

	@Data
	class Release {
		private Long id;
		private String url;
		private String name;
	}
}
