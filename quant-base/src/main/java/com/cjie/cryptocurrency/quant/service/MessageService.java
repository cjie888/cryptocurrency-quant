package com.cjie.cryptocurrency.quant.service;

import java.io.IOException;

public interface MessageService {

    void sendMessage(String title, String content);


    void sendStrategyMessage(String title, String content);

    void sendMonitorMessage(String title, String content);

    void sendNoticeMessage(String title, String content);


    void sendPhoto(String photoPath, String caption) throws IOException;
}
