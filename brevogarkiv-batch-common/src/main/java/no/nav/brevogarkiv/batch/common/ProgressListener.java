package no.nav.brevogarkiv.batch.common;

/**
 * Copied from Stelvio.
 * 
 * @author Ole Hjalmar Herje, BEKK
 *
 */
public interface ProgressListener {

	void progressed(BatchCounter counter);

	void finished(BatchCounter counter);

	void started(BatchCounter counter);

}
