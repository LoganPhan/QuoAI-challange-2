package com.quoai.challenge.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.quoai.challenge.dto.MetricDto;
import com.quoai.challenge.dto.OrgDto;
import com.quoai.challenge.dto.PushEventDto;
import com.quoai.challenge.dto.ReleaseEventDto;
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
	public String downloadGitHubDataSource() throws IOException {
		long startTime = System.currentTimeMillis();
		LocalDateTime cal = LocalDateTime.now().minusDays(1).minusDays(DAYS);
		DateTimeFormatter formmat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
		executorService = Executors.newFixedThreadPool(DAYS * HOURS);

		try {
			for (int i = 0; i < DAYS; i++) {
				cal = cal.plusDays(1);
				String date = formmat.format(cal);
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
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (Exception e ) {
			System.out.println(e.getMessage() + "");
		}
	
		long total = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("Time send: " + total);
		System.out.println("Total Respositories: " + repositories.size());
		List<MetricDto> repoDtos = repositories.entrySet().stream()
				.map(p -> {
					RepoDto dto = p.getValue();
					Double numberCommit = Double.valueOf(dto.getTotalCommits() / DAYS);
					Double numberRelease = Double.valueOf(dto.getReleaseNumber() / DAYS);
					Double commitMetric = dto.getTotalCommits() > 0 ? Double.valueOf(numberCommit / dto.getTotalCommits()) : Double.valueOf(0);
					Double releaseMetric = dto.getReleaseNumber() > 0 ? Double.valueOf(numberRelease / dto.getReleaseNumber()) : Double.valueOf(0);
					Double healthScore = calculateHealthScore(commitMetric, releaseMetric);
					return MetricDto.builder()
						.orgName(p.getValue().getOrgName())
						.repoName(p.getValue().getRepoName())
						.numberCommit(numberCommit)
						.numberRelease(numberRelease)
						.healthScore(healthScore).build();	
				})
				.sorted(Comparator.comparing(MetricDto::getHealthScore).reversed())
				.limit(100)
				.collect(Collectors.toList());
		return toCSV(repoDtos);
		
	}
	
	private void download(String suffix) throws IOException {
		GZIPInputStream gzip = null;
		BufferedReader br = null;
		HttpURLConnection con = null;
		try {
			// This user agent is for if the server wants real humans to visit https://data.gharchive.org/2015-01-01-15.json.gz");
			URL url = new URL(DATA_GH_ARCHIVE_DOMAIN + suffix);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setAllowUserInteraction(true);
			con.setRequestProperty("Content-type", "application/gzip");
			gzip = new GZIPInputStream(con.getInputStream());
			br = new BufferedReader(new InputStreamReader(gzip));
			String content;
			
			while ((content = br.readLine()) != null) {
				if(content.contains("\"type\":\"PushEvent\"")) {
					Response<PushEventDto> pushDto = objectMapper.readValue(content, new TypeReference<Response<PushEventDto>>() {});
					RepoDto repoDto = pushDto.getRepo();
					if(!ObjectUtils.isEmpty(pushDto.getOrg())) {
						setOrg(repoDto, pushDto.getOrg());
					}
					setTotalPushes(repoDto, pushDto.getPayLoad().getSize());
				}
				else if(content.contains("\"type\":\"ReleaseEvent\"")) {
					Response<ReleaseEventDto> eventDto = objectMapper.readValue(content, new TypeReference<Response<ReleaseEventDto>>() {});
					RepoDto repoDto = eventDto.getRepo();
					if(!ObjectUtils.isEmpty(eventDto.getOrg())) {
						setOrg(repoDto, eventDto.getOrg());
					}
					setRelease(repoDto, eventDto.getPayLoad());
				}
				
			}
			gzip.close();
			br.close();
			con.disconnect();
			System.out.println("Count: " + DATA_GH_ARCHIVE_DOMAIN + suffix);
		} catch (IOException e) {
			System.out.println(e.getMessage() + "  ---  " + suffix);
		}
		finally {
			if(gzip!=null) {
				gzip.close();
			}
			if(br!=null) {
				br.close();
			}
			if(con!=null) {
				con.disconnect();
			}
		}
	}
	
	private void setOrg(RepoDto repoDto, OrgDto orgDto) {
		repoDto.setOrgId(orgDto.getId());
		repoDto.setOrgName(orgDto.getOrgName());
	}
	
	
	private void setTotalPushes(RepoDto repoDto, int commits) {
		repositories.compute(repoDto.getId(), (key, val) -> {
			if (val == null) {
				repoDto.setTotalCommits(Long.valueOf(commits));
				return repoDto;
			} else {
				val.setTotalCommits(val.getTotalCommits() + commits);
				return val;
			}
		});
	}
	
	private void setRelease(RepoDto repoDto, ReleaseEventDto releaseDto) {	
		repositories.compute(repoDto.getId(), (key, val) -> {
			if (val == null) {
				repoDto.setReleaseNumber(Long.valueOf(0L));
				return repoDto;
			} else {
				if(!ObjectUtils.isEmpty(releaseDto.getRelease())) {
					val.setReleaseNumber(val.getReleaseNumber() + 1);
				}
				return val;
			}
		});
	}
	
	private String toCSV(List<MetricDto> data) throws IOException{
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema schema = csvMapper.schemaFor(MetricDto.class).withHeader();
	    return csvMapper.writer(schema).writeValueAsString(data);
	}
	
	private Double calculateHealthScore(Double commitMetric, Double releaseMetric) {
		return commitMetric * releaseMetric;
	}
}
