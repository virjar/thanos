package com.virjar.thanos.service.engine;

import com.google.common.collect.Maps;
import com.virjar.thanos.api.util.ReflectUtil;
import com.virjar.thanos.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by virjar on 2018/2/25.<br>
 * 主入口
 *
 * @author virjar
 * @since 1.0
 */
@Slf4j
public class J2Executor {
    private final ThreadPoolExecutor parentThreadPoolExecutor;
    private final BlockingQueue<Runnable> parentBlockingQueue;
    private final Map<String, ThreadPoolExecutor> subExecutors = Maps.newConcurrentMap();

    /**
     * 二级线程池增强器,需要传入一级线程池对象。 请注意,一般情况下,一级线程池的拒绝策略不会生效。
     * <ul>
     * <li>1. 如果该任务是二级线程池溢出提交过来的,那么如果该线程池拒绝,拒绝策略将会路由到原生的二级线程池。</li>
     * <li>2. 如果该任务本身就是提交到一级线程池,那么仍然以一级线程池的拒绝策略为主</li>
     * </ul>
     * <p>
     * 一级线程池,永远不会达到maxThreadSize状态,当二级线程池溢出提交任务的时候,发现一级线程池繁忙,仍然会将该任务交给一级线程池自己处理
     *
     * @param parentThreadPoolExecutor 对应的一级线程池,
     */
    J2Executor(ThreadPoolExecutor parentThreadPoolExecutor) {
        this.parentThreadPoolExecutor = parentThreadPoolExecutor;
        parentBlockingQueue = parentThreadPoolExecutor.getQueue();

        // 替代 reject handler,虽然这个代码几乎不会被执行
        RejectedExecutionHandler originRejectExecutionHandler = parentThreadPoolExecutor.getRejectedExecutionHandler();
        if (!(originRejectExecutionHandler instanceof RejectMarkExecutionHandler)) {
            parentThreadPoolExecutor
                    .setRejectedExecutionHandler(new RejectMarkExecutionHandler(originRejectExecutionHandler));
        }
    }


    public void shutdownAll() {
        for (Map.Entry<String, ThreadPoolExecutor> entry : subExecutors.entrySet()) {
            entry.getValue().shutdown();
            subExecutors.remove(entry.getKey());
        }
        parentThreadPoolExecutor.shutdown();
    }

    private void registrySubThreadPoolExecutor(String key, ThreadPoolExecutor threadPoolExecutor) {
        // 将该线程池的任务队列替换掉,
        BlockingQueue<Runnable> blockingQueue = threadPoolExecutor.getQueue();
        if (!(blockingQueue instanceof ConsumeImmediatelyBlockingQueue)) {
            // 对线程池任务队列功能增强
            ReflectUtil.setFieldValue(threadPoolExecutor, "workQueue", new ConsumeImmediatelyBlockingQueue<>(
                    blockingQueue, runnable -> {
                if (parentBlockingQueue.size() > 0 || parentThreadPoolExecutor
                        .getActiveCount() >= parentThreadPoolExecutor.getCorePoolSize() - 1) {
                    return false;
                }
                RejectedMonitorRunnable rejectedMonitorRunnable = new RejectedMonitorRunnable(runnable);
                parentThreadPoolExecutor.execute(rejectedMonitorRunnable);
                return !rejectedMonitorRunnable.isRejected();
            }));
        }
        subExecutors.put(key, threadPoolExecutor);
    }

    ThreadPoolExecutor getOrCreate(String key, int coreSize, int maxSize) {
        ThreadPoolExecutor threadPoolExecutor = subExecutors.get(key);
        if (threadPoolExecutor != null) {
            return threadPoolExecutor;
        }
        synchronized (this) {
            if (subExecutors.containsKey(key)) {
                return subExecutors.get(key);
            }
            threadPoolExecutor = new ThreadPoolExecutor(
                    coreSize, maxSize,
                    10, TimeUnit.MILLISECONDS,
                    new LinkedBlockingDeque<>(), new NamedThreadFactory("grab-worker-" + key)
            );
            registrySubThreadPoolExecutor(key, threadPoolExecutor);
        }
        return threadPoolExecutor;
    }

    private static class RejectMarkExecutionHandler implements RejectedExecutionHandler {
        private final RejectedExecutionHandler originRejectExecutionHandler;

        RejectMarkExecutionHandler(RejectedExecutionHandler originRejectExecutionHandler) {
            this.originRejectExecutionHandler = originRejectExecutionHandler;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof RejectedMonitorRunnable) {
                ((RejectedMonitorRunnable) r).setRejected();
                return;
            }
            originRejectExecutionHandler.rejectedExecution(r, executor);
        }
    }

    private static class RejectedMonitorRunnable implements Runnable {
        private final Runnable delegate;
        private boolean rejected = false;

        RejectedMonitorRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        void setRejected() {
            this.rejected = true;
        }

        boolean isRejected() {
            return rejected;
        }

        @Override
        public void run() {
            delegate.run();
        }
    }

    /**
     * Created by virjar on 2018/2/25.<br>
     * 对队列包装增强,当任务被投递的时候,先看一级线程池是否空闲,如果空闲直接往一级线程池投递,同时吞掉该任务
     */
    public static class ConsumeImmediatelyBlockingQueue<T> implements BlockingQueue<T> {
        private final BlockingQueue<T> delegate;
        private final ImmediatelyConsumer<T> immediatelyConsumer;

        public interface ImmediatelyConsumer<T> {
            boolean consume(T t);
        }

        ConsumeImmediatelyBlockingQueue(BlockingQueue<T> delegate, ImmediatelyConsumer<T> immediatelyConsumer) {
            this.delegate = delegate;
            this.immediatelyConsumer = immediatelyConsumer;
        }

        @Override
        public boolean add(T t) {
            return immediatelyConsumer.consume(t) || delegate.add(t);
        }

        @Override
        public boolean offer(T t) {
            return immediatelyConsumer.consume(t) || delegate.offer(t);
        }

        @Override
        public T remove() {
            return delegate.remove();
        }

        @Override
        public T poll() {
            return delegate.poll();
        }

        @Override
        public T element() {
            return delegate.element();
        }

        @Override
        public T peek() {
            return delegate.peek();
        }

        @Override
        public void put(T t) throws InterruptedException {
            if (immediatelyConsumer.consume(t)) {
                return;
            }
            delegate.put(t);
        }

        @Override
        public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
            return immediatelyConsumer.consume(t) || delegate.offer(t, timeout, unit);
        }


        @Override
        public T take() throws InterruptedException {
            return delegate.take();
        }

        @Override
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.poll(timeout, unit);
        }

        @Override
        public int remainingCapacity() {
            return delegate.remainingCapacity();
        }

        @Override
        public boolean remove(Object o) {
            return delegate.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            List<T> remain = new LinkedList<>();
            for (T t : c) {
                if (!immediatelyConsumer.consume(t)) {
                    remain.add(t);
                }
            }
            return delegate.addAll(remain);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return delegate.retainAll(c);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return delegate.iterator();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T1> T1[] toArray(T1[] a) {
            return delegate.toArray(a);
        }

        @Override
        public int drainTo(Collection<? super T> c) {
            return delegate.drainTo(c);
        }

        @Override
        public int drainTo(Collection<? super T> c, int maxElements) {
            return delegate.drainTo(c, maxElements);
        }
    }

}