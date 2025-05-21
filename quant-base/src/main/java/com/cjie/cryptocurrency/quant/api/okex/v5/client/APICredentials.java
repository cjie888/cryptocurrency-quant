package com.cjie.cryptocurrency.quant.api.okex.v5.client;


import com.cjie.cryptocurrency.quant.api.okex.v5.config.APIConfiguration;

public class APICredentials {
    /**
     * The user's secret key provided by OKEx.
     */
    private String apiKey;
    /**
     * The private key used to sign your request data.
     */
    private String secretKey;
    /**
     * The Passphrase will be provided by you to further secure your API access.
     */
    private String passphrase;


    private boolean mock = false;


    public APICredentials(APIConfiguration config) {
        super();
        this.apiKey = config.getApiKey();
        this.secretKey = config.getSecretKey();
        this.passphrase = config.getPassphrase();
        this.mock = config.isMock();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }
}
