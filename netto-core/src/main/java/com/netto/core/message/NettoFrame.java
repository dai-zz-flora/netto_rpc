package com.netto.core.message;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;


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
    public static String ENCODING = "UTF-8";

    private int bodySize = 0;

    private int headerContentSize = 0;

    private byte[] headerContent;

    private byte[] body;
    
    private byte[] headerLine;
    
    private String serviceName;
    private String methodName;
    private int argsLen = -1;
    private String signature;
 

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getArgsLen() {
        return argsLen;
    }

    public void setArgsLen(int argsLen) {
        this.argsLen = argsLen;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public byte[] getHeaderLine() {
        return headerLine;
    }

    public void setHeaderLine(byte[] headerLine) {
        this.headerLine = headerLine;
    }

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

    public byte[] getHeaderContent() {
        return headerContent;
    }

    public void setHeaderContent(byte[] headerContent) {
        this.headerContent = headerContent;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
    
    
    public Map<String,String> decodeHeader() throws UnsupportedEncodingException{
        String headerContent = new String(this.headerContent,ENCODING);
        String[] headers = StringUtils.split(headerContent, NettoFrame.HEADER_DELIMITER);
        Map<String,String> headersMap = Arrays.asList(headers).stream().map(str->{
            String[] pair = str.split(":");
            if(pair.length<2){
                pair = new String[]{str,""};
                return pair;
            }            
            else{
                return pair;
            }
        }).collect(
                Collectors.toMap(pair -> pair[0], pair -> pair[1]));
        
        return headersMap;
    }

}
