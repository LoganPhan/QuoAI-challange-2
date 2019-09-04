package com.quoai.challenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(value = {"org", "repo_name", "health_score", "num_commits", "num_release"})
public class MetricDto{

	@JsonProperty("org")
	private String orgName;
	@JsonProperty("repo_name")
	private String repoName;
	@JsonProperty("health_score")
	private Double healthScore;
	@JsonProperty("num_commits")
	private Double numberCommit;
	@JsonProperty("num_release")
	private Double numberRelease;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getOrgName());
		builder.append(",");
		builder.append(getRepoName());
		builder.append(",");
		builder.append(getHealthScore());
		builder.append(",");
		builder.append(getNumberCommit());
		return builder.toString();
	}
}
