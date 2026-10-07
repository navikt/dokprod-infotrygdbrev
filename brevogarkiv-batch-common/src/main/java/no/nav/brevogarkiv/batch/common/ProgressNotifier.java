package no.nav.brevogarkiv.batch.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

/**
 * Copied from Stelvio
 *
 * Logs batch progress by use of a {@link BatchCounter} and a optionally configured {@link #progressInterval}, default value is 1.
 * ProgressInterval can be set using {@link #setProgressInterval(Integer)} or by starting batch with a job parameter with
 * name {@link CommonBatchInputParameters#PROGRESS_INTERVAL_KEY} and a long value.
 */
public class ProgressNotifier implements ChunkListener<Object, Object>, JobExecutionListener {
    private BatchCounter counter;
    private List<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
    private Integer progressInterval = 1;
    private int chunkCounter;

    public ProgressNotifier(BatchCounter counter) {
        this.counter = counter;
    }

    /** {@inheritDoc} */
    @Override
    public void beforeJob(JobExecution jobExecution) {
    	setProgressIntervalFromJobParameters(jobExecution);
    	counter.resetCounter();
    	counter.start(CommonBatchEvents.JOB_EVENT);
    	notifyListenersAboutStart();
    }

    /** {@inheritDoc} */
    @Override
    public void afterJob(JobExecution jobExecution) {
        counter.stop(CommonBatchEvents.JOB_EVENT);
        notifyListenersAboutFinish();
    }

    /** {@inheritDoc} */
    @Override
    public void afterChunk(ChunkContext context) {
    	chunkCounter++;
    	if (chunkCounter % progressInterval == 0) {
    		notifyListenersAboutProgress();
    		chunkCounter = 0;
    	}
    }

    /**
     * Stops job event and logs summary.
     */
    public void afterJob() {
        afterJob(null);
    }

    /**
     * Starts job event.
     */
    public void beforeJob() {
        beforeJob(null);
    }


    private void setProgressIntervalFromJobParameters(JobExecution jobExecution) {
        if (jobExecution != null) {
            Long progressInterval = (Long) jobExecution.getExecutionContext().get(
                    CommonBatchInputParameters.PROGRESS_INTERVAL_KEY);
            if (progressInterval != null) {
                setProgressInterval(progressInterval.intValue());
            }
        }
    }

    private void notifyListenersAboutProgress() {
        for (ProgressListener listener : progressListeners) {
            listener.progressed(counter);
        }
    }

    private void notifyListenersAboutFinish() {
        for (ProgressListener listener : progressListeners) {
            listener.finished(counter);
        }
    }

    private void notifyListenersAboutStart() {
        for (ProgressListener listener : progressListeners) {
            listener.started(counter);
        }
    }

    /**
     * Sets implementation to use for counting.
     * @param counter A batch counter.
     */
    public void setCounter(BatchCounter counter) {
        this.counter = counter;
    }

    /**
     * Number of chunks between each progress logging.
     * @param progressInterval sets the interval to use
     */
    public void setProgressInterval(Integer progressInterval) {
        this.progressInterval = progressInterval;
    }

    public void setProgressListeners(List<ProgressListener> listeners) {
        this.progressListeners = listeners;

    }

    public void addProgressListener(ProgressLoggerListener listener) {
        progressListeners.add(listener);
    }


}
