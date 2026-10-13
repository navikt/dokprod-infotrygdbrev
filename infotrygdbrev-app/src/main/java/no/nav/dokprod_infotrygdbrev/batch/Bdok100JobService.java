package no.nav.dokprod_infotrygdbrev.batch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class Bdok100JobService {

	private final JobLauncher jobLauncher;
	private final Job bdok100Job;

	public Bdok100JobService(
			JobLauncher jobLauncher,
			@Qualifier("bdok100Job") Job bdok100Job
	) {
		this.jobLauncher = jobLauncher;
		this.bdok100Job = bdok100Job;
	}

	public JobExecution start(JobParameters jobParameters) throws JobExecutionException {
		return jobLauncher.run(bdok100Job, jobParameters);
	}
}
