package com.cjie.cryptocurrency.quant.service;

public interface MessageService {

    void sendMessage(String title, String content);


    void sendStrategyMessage(String title, String content);

    void sendMonitorMessage(String title, String content);
}
