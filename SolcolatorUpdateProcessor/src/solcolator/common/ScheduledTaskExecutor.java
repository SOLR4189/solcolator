package solcolator.common;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScheduledTaskExecutor {
	private final static Logger log = LoggerFactory.getLogger(ScheduledTaskExecutor.class);
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private final IScheduledTask task;
	private final int targetHour;
	private final int targetMin;
	private final int targetSec;
	
	public ScheduledTaskExecutor(IScheduledTask task, int targetHour, int targetMin, int targetSec) {
		this.task = task;
		this.targetHour = targetHour;
		this.targetMin = targetMin;
		this.targetSec = targetSec;
	}

	public void startExecutionAt() {
		Runnable taskWrapper = new Runnable() {
			
			@Override
			public void run() {
				task.scheduledMethod();
			}
		};
		
		long delay = computeNextDelay(targetHour, targetMin, targetSec);
		executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
	}

	private long computeNextDelay(int targetHour, int targetMin, int targetSec) {
		LocalDateTime localNow = LocalDateTime.now(ZoneId.of("Israel"));
		LocalDateTime localNextTarget = localNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
		
		if (localNow.compareTo(localNextTarget) > 0) {
			localNextTarget = localNextTarget.plusDays(1);
		}
		
		Duration duration = Duration.between(localNow, localNextTarget);
		
		return duration.getSeconds();
	}
	
	public void stop() {
		try {
			executorService.shutdown();
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			log.error("Stopping of executors service is interrupted");
		} finally {
			if (!executorService.isTerminated()) {
				log.error("Stopping of executors service is interrupted: cancel non-finished tasks");
			}

			executorService.shutdownNow();
		}	
	}
}
