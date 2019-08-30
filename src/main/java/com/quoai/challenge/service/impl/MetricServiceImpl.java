package com.quoai.challenge.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	static final String FILE_EXTENSION = ".json.gz";
	private Map<Long, RepoDto> repositories = new ConcurrentHashMap<>();
	
	static final Integer DAYS = 30;
	static final Integer HOURS = 24;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private ExecutorService executorService;
	
	@Override
	public List<RepoDto> downloadGitHubDataSource() {
		long startTime = System.currentTimeMillis();
		LocalDateTime cal = LocalDateTime.now().minusDays(1).minusDays(DAYS);
		DateTimeFormatter formmat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
		executorService = Executors.newFixedThreadPool(DAYS * HOURS);
		try {
			for (int i = 0; i < DAYS; i++) {
				cal = cal.plusDays(1);
				String date = formmat1.format(cal);
				for (int j = 0; j < HOURS; j++) {
					String filePath = date + "-" + j + FILE_EXTENSION;
					executorService.submit(() -> {
						try {
							download(filePath);
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
		List<RepoDto> list = new ArrayList<RepoDto>();
		int count = 0;
		for (Map.Entry<Long, RepoDto> ele : repositories.entrySet()) {
			if(count == 100) {
				break;
			}
			list.add(ele.getValue());
			count ++;
		}
		return list;
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
					RepoDto repoDto = pushDto.getRepo();
					if(!ObjectUtils.isEmpty(pushDto.getOrg())) {
						OrgDto orgDto = pushDto.getOrg();
						repoDto.setOrgId(orgDto.getId());
						repoDto.setOrgName(orgDto.getOrgName());
					}
					setRepositories(repoDto);
					setTotalPushes(repoDto.getId(), pushDto.getPayLoad().getSize());
				}
				
			}
			System.out.println("Count: " + DATA_GH_ARCHIVE_DOMAIN + suffix);
		} catch (IOException e) {
			System.out.println(e.getMessage());
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
	
	private void setRepositories(RepoDto repoDto) {
		repositories.putIfAbsent(repoDto.getId(), repoDto);
	}
	
	private void setTotalPushes(Long repoId, int commits) {
		repositories.compute(repoId, (key, val) -> {
			if (val == null) {
				return val;
			} else {
				val.setTotalCommits(val.getTotalCommits() + commits);
				return val;
			}
		});
	}
}
