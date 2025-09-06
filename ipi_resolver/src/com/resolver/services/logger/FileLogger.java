package com.resolver.services.logger;

public interface FileLogger {
    void logInformation(String info);
    void flush();
}
