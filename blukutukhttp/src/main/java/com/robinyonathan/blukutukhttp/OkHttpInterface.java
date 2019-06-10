package com.robinyonathan.blukutukhttp;

public interface OkHttpInterface {

    void before();

    void progress(int progress);

    void status(int code, String message);

    void after(Object o);
}
