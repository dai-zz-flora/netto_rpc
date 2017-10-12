package com.netto.core.message;

import io.netty.buffer.ByteBuf;

public class NettoFrame {
    public static String NETTO_HEADER_START = "NETTO:";
    public static String SIGNATURE_HEADER = "signature";
    public static String ERROR_HEADER = "error";
    public static String SERVICE_HEADER = "service";
    public static String METHOD_HEADER = "method";
    public static String ARGSLEN_HEADER = "argsLen";
    
    public static String NETTO_SUCESSS = "200";
    public static String NETTO_FAILED = "500";

    public static int HEADER_LENGTH = 64;
    public static String HEADER_DELIMITER = "\r\n";

    private int bodySize = 0;

    private int headerContentSize = 0;

    private ByteBuf headerContent;

    private ByteBuf body;

    public int getBodySize() {
        return bodySize;
    }

    public void setBodySize(int bodySize) {
        this.bodySize = bodySize;
    }

    public int getHeaderContentSize() {
        return headerContentSize;
    }

    public void setHeaderContentSize(int headerContentSize) {
        this.headerContentSize = headerContentSize;
    }

    public ByteBuf getHeaderContent() {
        return headerContent;
    }

    public void setHeaderContent(ByteBuf headerContent) {
        this.headerContent = headerContent;
    }

    public ByteBuf getBody() {
        return body;
    }

    public void setBody(ByteBuf body) {
        this.body = body;
    }

}
