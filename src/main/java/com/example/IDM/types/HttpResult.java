package com.example.IDM.types;

public class HttpResult {
    public int responseCode;
    public long contentLength;
    public HttpResult(int r, long c) {
        responseCode = r;
        contentLength = c;
    }
}
