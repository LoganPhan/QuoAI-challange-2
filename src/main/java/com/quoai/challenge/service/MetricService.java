package com.quoai.challenge.service;

import java.util.List;

import com.quoai.challenge.dto.RepoDto;

public interface MetricService {

	List<RepoDto> downloadGitHubDataSource();
}
