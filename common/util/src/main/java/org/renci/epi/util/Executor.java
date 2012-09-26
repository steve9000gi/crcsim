package org.renci.epi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

/**
 *
 * An executor modeling the host machine's concurrency capabilities 
 * and abstracting these to the caller.
 *
 * Jobs are queued.
 *
 * Clients can wait for completion of all jobs.
 * 
 */
public class Executor {

    private ThreadPoolExecutor _executorService = null;
    private List<Future> _futures = new ArrayList<Future> ();

    /**
     *
     * Create an executor 
     *
     */
    public Executor () {
	int maxThreads = Runtime.getRuntime().availableProcessors ();
	BlockingQueue queue = new ArrayBlockingQueue<Runnable> (maxThreads, true);
	_executorService = 
	    new ThreadPoolExecutor (maxThreads, // core thread pool size
				    maxThreads, // maximum thread pool size
				    1, // time to wait before resizing pool
				    TimeUnit.MINUTES, 
				    queue,
				    new ThreadPoolExecutor.CallerRunsPolicy ());
    }

    /**
     *
     * Queue a job for execution.
     * This will block if the maximum queue length is exceeded.
     * A Future representing the job is recorded for future use.
     *
     * @param runnable The runnable object to execute.
     *
     */
    public void queue (Runnable runnable) {
	_futures.add (_executorService.submit (runnable));
    }

    /**
     *
     * Wait for all submitted jobs to terminate.
     *  Waits for the queue to become empty
     *   Then waits for all existing futures to complete.
     * Does not account for jobs submitted after the queue becomes empty.
     *
     */
    public void waitFor () {
	while (_executorService.getQueue ().size () > 0) {
	    try {
		Thread.sleep (200);
	    } catch (InterruptedException e) {
		throw new RuntimeException (e);
	    }
	}
	for (Future future : _futures) {
	    try {
		future.get ();
	    } catch (InterruptedException e) {
		throw new RuntimeException (e);
	    } catch (ExecutionException e) {
		throw new RuntimeException (e);
	    }
	}
    }

    /**
     *
     * Stop the executor.
     *
     */
    public void stop () {
	// wait for all of the executor threads to finish
	_executorService.shutdown ();
	try {
	    if ( ! _executorService.awaitTermination(60, TimeUnit.SECONDS)) {
		// pool didn't terminate after the first try
		_executorService.shutdownNow();
	    }

	    if ( ! _executorService.awaitTermination (60, TimeUnit.SECONDS)) {
		// pool didn't terminate after the second try
	    }

	} catch (InterruptedException ex) {
	    _executorService.shutdownNow ();
	    Thread.currentThread().interrupt ();
	}
    }

}

