package org.renci.epi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

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

    private static Log logger = LogFactory.getLog (Executor.class); 

    private ThreadPoolExecutor _executorService = null;
    private List<Future> _futures = new ArrayList<Future> ();

    /**
     *
     * Create an executor 
     *
     */
    public Executor () {
	int maxThreads = Runtime.getRuntime().availableProcessors ();
	//BlockingQueue queue = new ArrayBlockingQueue<Runnable> (maxThreads, true);
	BlockingQueue queue = new LinkedBlockingQueue<Runnable> ();
	_executorService =
	    new ThreadPoolExecutor (maxThreads, // core thread pool size
				    maxThreads, // maximum thread pool size
				    1,          // time to wait before killing idle threads
				    TimeUnit.MINUTES, 
				    queue,
				    new ThreadPoolExecutor.CallerRunsPolicy ());
 
        _executorService.setRejectedExecutionHandler (new RejectedExecutionHandler () {
		public void rejectedExecution (Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {  
		    throw new RuntimeException ("Rejected queueing of job: " + runnable);
		}  
	    }); 
    }
    public BlockingQueue<Runnable> getQueue () {
	return _executorService.getQueue();
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
    public Future queue (Runnable runnable) {
	Future future = _executorService.submit (runnable);
	_futures.add (future);
	return future;
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
	waitFor (_futures);
    }
    public void waitFor (List<Future> futures) {
	for (Future future : futures) {
	    try {
		future.get ();
	    } catch (InterruptedException e) {
		throw new RuntimeException (e);
	    } catch (ExecutionException e) {
		throw new RuntimeException (e);
	    }
	}
    }
    private void logNeverRun (List<Runnable> neverRun) {
	for (Runnable runnable : neverRun) {
	    logger.info ("Runnable " + runnable + " was never run");
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
		logNeverRun (_executorService.shutdownNow ());

	    }

	    if ( ! _executorService.awaitTermination (60, TimeUnit.SECONDS)) {
		// pool didn't terminate after the second try
	    }

	} catch (InterruptedException ex) {
	    logNeverRun (_executorService.shutdownNow ());
	    Thread.currentThread().interrupt ();
	}
    }
    public static void main (String [] args) {
	Executor e = new Executor ();
	for (int c = 0; c < 1000; c++) {
	    final int j = c;
	    e.queue (new Runnable () {
		    @Override
		    public void run () {
			try {
			    Thread.sleep (400);
			} catch (InterruptedException e) {
			}
			System.out.println (j);
		    }
		});
	}
    }
}

