package edu.sjsu.cmpe.cache.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * POJO class for CRDT
 */
public class CRDT {

    private final String cacheServerUrl;
    private final Integer port;
    private final Integer serviceCount;
    private final int writeCount;
    private final int readCount;
    private AtomicInteger successCount;
    private AtomicInteger attemptsCount;
    private String[] values;
    private AtomicInteger index;
    private CountDownLatch responseWaiter;


    public CRDT(String serverUrl, Integer port, Integer serviceCount) {
        this.cacheServerUrl = serverUrl;
        this.port = port;
        this.serviceCount = serviceCount;
        this.writeCount = 2;
        this.readCount = 2;
        this.successCount = new AtomicInteger(0);
        this.attemptsCount = new AtomicInteger(0);
        this.values = new String[this.serviceCount];
        this.index = new AtomicInteger(0);
        this.responseWaiter = new CountDownLatch(this.serviceCount);
    }

    public String getCacheServerUrl() {
        return cacheServerUrl;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getServiceCount() {
        return serviceCount;
    }

    public int getWriteCount() {
        return writeCount;
    }

    public int getReadCount() {
        return readCount;
    }

    public AtomicInteger getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(AtomicInteger successCount) {
        this.successCount = successCount;
    }

    public AtomicInteger getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(AtomicInteger attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public String[] getValues() {
        return values;
    }

    public String getValues(int index) {
        return values[index];
    }

    public void setValues(String[] getValues) {
        this.values = getValues;
    }

    public void setValues(String values, int index) {
        this.values[index] = values;
    }

    public AtomicInteger getIndex() {
        return index;
    }

    public void setGetIndex(AtomicInteger getIndex) {
        this.index = getIndex;
    }

    public CountDownLatch getResponseWaiter() {
        return responseWaiter;
    }

    public void setResponseWaiter(CountDownLatch responseWaiter) {
        this.responseWaiter = responseWaiter;
    }


}
