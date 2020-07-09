package com.vjtech.gtfsAlertProducer.services.session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.vjtech.gtfsAlertProducer.MessageProducer;



@Component
public class ApplicationBean {
	
	@Autowired
	MessageProducer messageProducer;

	@Value("${app.persist_md5_path}")
	String persist_md5_path;	

	String accessToken  = "";
	String refreshToken = "";
	String md5Checksum  = "";
	
	ScheduledFuture<?> taskScheduler;
	long scheduler_interval=30;
	

	public long getScheduler_interval() {
		return scheduler_interval;
	}

	public ScheduledFuture<?> getTaskScheduler() {
		return taskScheduler;
	}

	public void setTaskScheduler(ScheduledFuture<?> taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public String getMd5Checksum() {
		return md5Checksum;
	}

	public void setMd5Checksum(String md5Checksum) {
		this.md5Checksum = md5Checksum;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	void clear() {
		accessToken  = "";
		refreshToken = "";
	}
	
	public void persistMd5Checksum() throws IOException {
		Files.write(Paths.get(persist_md5_path), md5Checksum.getBytes());
	}

	public void loadMd5ChecksumFromDisk() throws IOException {
		if (!(new File(persist_md5_path).exists()))
			md5Checksum="";
		else 		
			md5Checksum = new String(Files.readAllBytes(Paths.get(persist_md5_path)));
	}


	public void startScheduler(long interval) throws InterruptedException {
		scheduler_interval=interval;
		taskScheduler=	startBackGroundThreadScheduler();
	}

	public void restartScheduler() throws InterruptedException {
		restartScheduler(scheduler_interval);
	}
	
	public void restartScheduler(long newInterval) throws InterruptedException {
		if (taskScheduler!=null) taskScheduler.cancel(false);
		if (scheduler_interval!=newInterval) scheduler_interval=newInterval;
		taskScheduler=	startBackGroundThreadScheduler();
	}
	
	private ScheduledFuture<?> startBackGroundThreadScheduler() throws InterruptedException {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		ScheduledFuture<?> task = scheduledExecutorService.scheduleAtFixedRate(() -> messageProducer.checkRemoteFileUpdates(), 0, scheduler_interval,
				TimeUnit.SECONDS);
		return task;
	}

}
