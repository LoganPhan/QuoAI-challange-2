package com.quoai.challenge.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quoai.challenge.dto.OrgDto;
import com.quoai.challenge.dto.PushEventDto;
import com.quoai.challenge.dto.RepoDto;
import com.quoai.challenge.dto.Response;
import com.quoai.challenge.service.MetricService;

@Service
public class MetricServiceImpl implements MetricService {

	static final String DATA_GH_ARCHIVE_DOMAIN = "https://data.gharchive.org/";
	static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
	static final String JSON_GZ_EXTENSION = ".json.gz";
	private Map<Long, OrgDto> organizations = new ConcurrentHashMap<>();
	private Map<Long, RepoDto> repositories = new ConcurrentHashMap<>();
	private Map<Long, Set<Long>> orgIndexInRepos = new ConcurrentHashMap<>();
	private Map<Long, Long> totalPush = new ConcurrentHashMap<>();
	
	static final Integer DAYS = 30;
	static final Integer HOURS = 24;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private ExecutorService executorService;
	
	@Override
	public Map<Long, Long> downloadGitHubDataSource() {
		long startTime = System.currentTimeMillis();
		LocalDateTime cal = LocalDateTime.now().minusDays(DAYS);
		DateTimeFormatter formmat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
		executorService = Executors.newFixedThreadPool(DAYS * HOURS);
		try {
			for (int i = 0; i < DAYS; i++) {
				cal = cal.plusDays(1);
				String date = formmat1.format(cal);
				for (int j = 0; j < HOURS; j++) {
					String suffix = date + "-" + j + JSON_GZ_EXTENSION;
					executorService.submit(() -> {
						try {
							download(suffix);
						} catch (IOException e) {
						System.out.println(e.getMessage());
						}
					});
					;
				}
			}
			;
			executorService.shutdown();
			executorService.awaitTermination(1000L, TimeUnit.SECONDS);
		} catch (Exception e ) {
			System.out.println(e.getMessage());
		}
		long total = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("Time send: " + total);
		System.out.println("Total Respositories: " + repositories.size());
		System.out.println("Total Repo in Push: " + totalPush.size());
		return totalPush;
	}
	
	private void download(String suffix) throws IOException {
		GZIPInputStream gzip = null;
		BufferedReader br = null;
		try {
			// This user agent is for if the server wants real humans to visit https://data.gharchive.org/2015-01-01-15.json.gz");
			URL url = new URL(DATA_GH_ARCHIVE_DOMAIN + suffix);
			URLConnection con = url.openConnection();
			con.setRequestProperty("User-Agent", USER_AGENT);

			gzip = new GZIPInputStream(con.getInputStream());
			br = new BufferedReader(new InputStreamReader(gzip));
			String content;
			
			while ((content = br.readLine()) != null) {
				if(content.contains("\"type\":\"PushEvent\"")) {
					Response<PushEventDto> pushDto = objectMapper.readValue(content, new TypeReference<Response<PushEventDto>>() {});
					setOrganizations(pushDto.getOrg(), pushDto.getRepo());
					setRepositories(pushDto.getRepo());
					setTotalPushes(pushDto.getRepo().getId(), pushDto.getPayLoad());
					pushDto = null;
				}
				
			}
			System.out.println("Count: " + DATA_GH_ARCHIVE_DOMAIN + suffix);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if(gzip!=null) {
				gzip.close();
			}
			if(br!=null) {
				br.close();
			}
		}
	}
	
	private void setOrganizations(OrgDto orgDto, RepoDto repoDto) {
		if(!ObjectUtils.isEmpty(orgDto)) {
			organizations.putIfAbsent(orgDto.getId(), orgDto);
			setOrgIndexInRepos(orgDto.getId(), repoDto);
		}
	}
	
	private void setOrgIndexInRepos(Long orgId, RepoDto repoDto) {
		Set<Long> repoIds = orgIndexInRepos.computeIfAbsent(orgId, k -> new HashSet<Long>());
		synchronized (repoIds) {
			repoIds.add(repoDto.getId());
			orgIndexInRepos.putIfAbsent(orgId, repoIds);
		}
	}
	
	private void setRepositories(RepoDto repoDto) {
		repositories.putIfAbsent(repoDto.getId(), repoDto);
	}
	
	private void setTotalPushes(Long repoId, PushEventDto pushDto) {
		totalPush.compute(repoId, (key, val) -> {
			if (val == null) {
				return 0l;
			} else {
				return val + pushDto.getSize();
			}
		});
	}
}
