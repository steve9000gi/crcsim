package org.renci.epi.util;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;

public class Executor {

    private ExecutorService executorService = null;

    public Executor () {
	int maxThreads = 64;

	executorService = 
	    new ThreadPoolExecutor (maxThreads, // core thread pool size
				    maxThreads, // maximum thread pool size
				    1, // time to wait before resizing pool
				    TimeUnit.MINUTES, 
				    new ArrayBlockingQueue<Runnable> (maxThreads, true),
				    new ThreadPoolExecutor.CallerRunsPolicy ());
    }

    public void run (Runnable runnable) {
	executorService.submit (runnable);
    }

    public void stop () {
	// wait for all of the executor threads to finish
	executorService.shutdown ();
	try {
	    if ( ! executorService.awaitTermination(60, TimeUnit.SECONDS)) {
		// pool didn't terminate after the first try
		executorService.shutdownNow();
	    }

	    if ( ! executorService.awaitTermination (60, TimeUnit.SECONDS)) {
		// pool didn't terminate after the second try
	    }

	} catch (InterruptedException ex) {
	    executorService.shutdownNow ();
	    Thread.currentThread().interrupt ();
	}
    }

}

