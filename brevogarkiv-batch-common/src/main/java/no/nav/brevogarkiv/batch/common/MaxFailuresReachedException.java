package no.nav.brevogarkiv.batch.common;

/**
 * Exception thrown if max failures has been reached in a batch
 * Used by MaxFailuresSupport
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class MaxFailuresReachedException extends RuntimeException {

	/** Serialization UID */
	private static final long serialVersionUID = 4792201935597878008L;

	public MaxFailuresReachedException(String message) {
		super(message);
	}
}
