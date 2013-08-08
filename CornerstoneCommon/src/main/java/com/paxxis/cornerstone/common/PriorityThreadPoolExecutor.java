package com.paxxis.cornerstone.common;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.common.BlockingThreadPoolExecutor;
import com.paxxis.cornerstone.common.DefaultThreadFactory;
import com.paxxis.cornerstone.common.PriorityQueueThreadPoolExecutor;
import com.paxxis.cornerstone.common.ScheduledExecutionPool;
import com.paxxis.cornerstone.common.ScheduledExecutor;

public class PriorityThreadPoolExecutor extends BlockingThreadPoolExecutor {
    private static final Logger LOGGER = Logger.getLogger(PriorityThreadPoolExecutor.class);

    private static final int MAX_MATCHES_POOLSIZE = -1;
    private static final int MAX_POOLSIZE = 100;
    private static final int THREAD_TIMEOUT = 30; // in seconds

    private static final int MIN_CHECKSIZE_FREQ = 1;
    private static final int DEFAULT_CHECKSIZE_FREQ = MIN_CHECKSIZE_FREQ;
    private static final int MAX_CHECKSIZE_FREQ = 10;

    public interface CountListener {
        public void offer();
        public void take();
        public void needHelp(boolean helpMe);
    }

    private static Runnable emptyRunnable = new Runnable() {
        public void run() {
        }
    };

    /** the maximum number of threads this pool can have, where MAX_MATCHES_POOLSIZE 
     *  means the maximum should match the poolSize property from the BlockingThreadPoolExecutor.  */
    private int maxPoolSize = MAX_MATCHES_POOLSIZE;

    private ScheduledExecutionPool scheduledPool = null;
    private CountListener countListener = null;
    private PriorityQueueThreadPoolExecutor executor = null;

    private AtomicLong approxQueued = new AtomicLong(0);
    private long elasticUpperBound = -1;
    private long elasticLowerBound = -1;
    private int checkSizeFrequency = DEFAULT_CHECKSIZE_FREQ;

    private static class CountingPriorityBlockingQueue<M> extends PriorityBlockingQueue<M> {
        private static final long serialVersionUID = 1L;
        private final CountListener countListener;

        public CountingPriorityBlockingQueue(int capacity, CountListener listener) {
            super(capacity);
            countListener = listener;
        }

        public boolean offer(M element) {
            if (countListener != null) {
                countListener.offer();
            }
            return super.offer(element);
        }

        public M take() {
            M result = null;
            try {
                result = super.take();
                if (countListener != null) {
                    countListener.take();
                }
            } catch (InterruptedException e) {
            }

            return result;
        }
    }

    public PriorityThreadPoolExecutor() {
    }

    public void setElasticCheckResizeFrequency(int freq) {
        this.checkSizeFrequency = freq;
    }

    public void setElasticUpperBound(long bound) {
        this.elasticUpperBound = bound;
    }

    public void setScheduledExecutor(ScheduledExecutionPool scheduledExecutor) {
        this.scheduledPool = scheduledExecutor;
    }

    protected ScheduledExecutor getScheduledExecutor() {
        return this.scheduledPool;
    }

    public void setMaxPoolSize(int size) {
        this.maxPoolSize = size;
    }

    public void setCountListener(CountListener listener) {
        this.countListener = listener;
    }

    public void initialize() {
        setStartOnInitialize(false);
        super.initialize();

        if (scheduledPool == null) {
            throw new RuntimeException("scheduledExecutor property can't be null");
        }

        if (checkSizeFrequency < MIN_CHECKSIZE_FREQ || checkSizeFrequency > MAX_CHECKSIZE_FREQ) {
            throw new RuntimeException("elasticCheckSizeFrequency must be between " + MIN_CHECKSIZE_FREQ + " and " + MAX_CHECKSIZE_FREQ);
        }

        if (maxPoolSize == MAX_MATCHES_POOLSIZE) {
            maxPoolSize = getPoolSize();
        } else if (maxPoolSize < getPoolSize()) {
            throw new RuntimeException("maxPoolSize can't be less than poolSize");
        } else if (maxPoolSize > MAX_POOLSIZE) {
            throw new RuntimeException("maxPoolSize can't be greater than " + MAX_POOLSIZE);
        }

        if (elasticUpperBound == -1) {
            elasticUpperBound = getMaxRunnables();
        } 

        if (elasticUpperBound > getMaxRunnables()) {
            throw new RuntimeException("elasticUpperBound can't be greater than maxRunnables.");
        }

        // lower bound is 25% of upper bound
        elasticLowerBound = elasticUpperBound / 4;

        // there must be a minimum of 50 between upper and lower bound
        if ((elasticUpperBound - elasticLowerBound) < 50) {
            throw new RuntimeException("elasticUpperBound is too small.");
        }

        start();
        scheduleCheckSize();
    }

    private void scheduleCheckSize() {
        Runnable r = new Runnable() {
            public void run() {
                checkSize();
                scheduledPool.schedule(this, checkSizeFrequency, TimeUnit.MINUTES);
            }
        };

        scheduledPool.schedule(r, checkSizeFrequency, TimeUnit.MINUTES);
    }

    @Override
    protected ThreadPoolExecutor createThreadPoolExecutor() {
        executor = new PriorityQueueThreadPoolExecutor(
                getPoolSize(), 
                maxPoolSize, 
                THREAD_TIMEOUT, 
                TimeUnit.SECONDS, 
                new CountingPriorityBlockingQueue<Runnable>(getMaxRunnables(), countListener),
                new DefaultThreadFactory(getThreadPoolName()));

        return executor;
    }

    protected void checkSize() {
        int cnt = executor.getPoolSize();
        if (approxQueued.longValue() > elasticUpperBound) {
            countListener.needHelp(true);
            if (cnt < maxPoolSize) {
                int newSize = cnt + 1;
                LOGGER.info(getName() + " increasing thread pool size to " + newSize);
                executor.setCorePoolSize(newSize);
            }
        } else if (approxQueued.longValue() < elasticLowerBound) {
            countListener.needHelp(false);
            if (cnt > getPoolSize()) {
                int newSize = cnt - 1;
                LOGGER.info(getName() + " deccreasing thread pool size to " + newSize);
                executor.setCorePoolSize(newSize);

                // ThreadPoolExecutor will not decrease the thread count immediately.  It performs this
                // process when a runnable or callable is submitted.  In case things have gotten very quiet
                // we submit the empty runnable to force the pool to adjust its thread count if necessary.  
                executor.submit(emptyRunnable);
            }
        }
    }

    /**
     * Used to track an approximate number of queued up entries without having to ask the executor.
     */
    protected void trackSize(final long inQueue) {
        approxQueued.set(inQueue);
    }
}