package com.cjie.cryptocurrency.quant.api.huobi.misc;

public class HuobiWSClientOption {

    private int connectTimeout = 5;

    private int readTimeout = 5;

    private int writeTimeout = 5;

    /**
     * Set the maximum number of requests to execute concurrently. Above this requests queue in memory, waiting for the running calls to complete.
     * If more than maxRequests requests are in flight when this is invoked, those requests will remain in flight.
     */
    private int maxRequests = 32;
    /**
     * Set the maximum number of requests for each host to execute concurrently. This limits requests by the URL's host name. Note that concurrent requests to a single IP address may still exceed this limit: multiple hostnames may share an IP address or be routed through the same HTTP proxy.
     * If more than maxRequestsPerHost requests are in flight when this is invoked, those requests will remain in flight.
     */
    private int maxRequestsPerHost = 32;

    private boolean reconWhenFailure = false;

    private boolean reconWhenClosed = false;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    public void setMaxRequestsPerHost(int maxRequestsPerHost) {
        this.maxRequestsPerHost = maxRequestsPerHost;
    }

    public boolean isReconWhenFailure() {
        return reconWhenFailure;
    }

    public void setReconWhenFailure(boolean reconWhenFailure) {
        this.reconWhenFailure = reconWhenFailure;
    }

    public boolean isReconWhenClosed() {
        return reconWhenClosed;
    }

    public void setReconWhenClosed(boolean reconWhenClosed) {
        this.reconWhenClosed = reconWhenClosed;
    }

    public HuobiWSClientOption() {
    }
}
