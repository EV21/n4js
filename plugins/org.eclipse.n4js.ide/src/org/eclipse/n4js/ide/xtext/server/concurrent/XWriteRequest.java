package org.eclipse.n4js.ide.xtext.server.concurrent;

import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function2;

/**
 * A write request.
 */
public class XWriteRequest<U, V> extends XAbstractRequest<V> {
	private static final Logger LOG = Logger.getLogger(XWriteRequest.class);

	private final Function0<? extends U> nonCancellable;

	private final Function2<? super CancelIndicator, ? super U, ? extends V> cancellable;

	private final CompletableFuture<Void> previous;

	/**
	 * Standard constructor.
	 */
	public XWriteRequest(XRequestManager requestManager, Function0<? extends U> nonCancellable,
			Function2<? super CancelIndicator, ? super U, ? extends V> cancellable,
			CompletableFuture<Void> previous) {
		super(requestManager);
		this.nonCancellable = nonCancellable;
		this.cancellable = cancellable;
		this.previous = previous;
	}

	@Override
	public void run() {
		try {
			previous.join();
		} catch (Throwable t) {
			// ignore
		}
		try {
			U intermediateResult = this.nonCancellable.apply();
			cancelIndicator.checkCanceled();
			result.complete(cancellable.apply(cancelIndicator, intermediateResult));
		} catch (Throwable t) {
			if (!requestManager.isCancelException(t)) {
				LOG.error("Error during request: ", t);
			}
			result.completeExceptionally(t);
		}
	}

}
