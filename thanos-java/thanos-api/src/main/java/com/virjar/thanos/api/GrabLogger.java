package com.virjar.thanos.api;


public class GrabLogger {

    private static final ThreadLocal<ILogger> loggerThreadLocal = new InheritableThreadLocal<>();

    private static final ILogger defaultLogger = new ConsoleLogger();

    public static void info(String info) {
        get().info(
                info
        );
    }

    public static void waring(String info) {
        get().waring(info);
    }

    public static void error(String info) {
        get().error(info);
    }

    public static void info(String info, Throwable throwable) {
        get().info(info, throwable);
    }

    public static void waring(String info, Throwable throwable) {
        get().waring(info, throwable);
    }

    public static void error(String info, Throwable throwable) {
        get().error(info, throwable);
    }


    private static ILogger get() {
        ILogger iLogger = loggerThreadLocal.get();
        if (iLogger != null) {
            return iLogger;
        }
        return defaultLogger;
    }

    static {
        setup(defaultLogger);
    }


    public static void setup(ILogger iLogger) {
        loggerThreadLocal.set(iLogger);
    }

    public static void clear() {
        loggerThreadLocal.remove();
    }


    public interface ILogger {
        void info(String info);

        void waring(String info);

        void error(String info);

        void info(String info, Throwable throwable);

        void waring(String info, Throwable throwable);

        void error(String info, Throwable throwable);
    }

    private static class ConsoleLogger implements ILogger {

        @Override
        public void info(String info) {
            System.out.println(info);
        }

        @Override
        public void waring(String info) {
            System.err.println();
        }

        @Override
        public void error(String info) {
            System.err.println(info);
        }

        @Override
        public void info(String info, Throwable throwable) {
            System.out.println(info);
            throwable.printStackTrace();
        }

        @Override
        public void waring(String info, Throwable throwable) {
            System.err.println(info);
            throwable.printStackTrace(System.err);
        }

        @Override
        public void error(String info, Throwable throwable) {
            waring(info, throwable);
        }
    }


}