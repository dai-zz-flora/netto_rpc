package com.netto.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netto.client.pool.TcpConnectPool;
import com.netto.client.provider.ServiceProvider;
import com.netto.core.exception.RemoteAccessException;
import com.netto.core.filter.InvokeMethodFilter;
import com.netto.core.message.NettoFrame;
import com.netto.core.util.JsonMapperUtil;



public class RpcTcpClient extends AbstactRpcClient {
	private static Logger logger = Logger.getLogger(RpcTcpClient.class);
	private TcpConnectPool pool;

	public RpcTcpClient(ServiceProvider provider, List<InvokeMethodFilter> filters, String serviceName, int timeout) {
		super(provider, filters, serviceName, timeout, provider.needSignature());
		this.pool = (TcpConnectPool) provider.getPool("tcp");
	}
	
	
    private Map<String,String> decoderHeader(String headerContent){
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

	@Override
	protected Object invokeMethod(Method method, Object[] args) throws Throwable {


		Socket socket = null;
		boolean invalidatedSocket = false;
		try {
			ObjectMapper mapper = JsonMapperUtil.getJsonMapper();
			socket = this.pool.getResource();
			socket.setSoTimeout(this.getTimeout());
			OutputStream os = socket.getOutputStream();
			String requestBody = mapper.writeValueAsString(args);
			byte[] byteBody = requestBody.getBytes("UTF-8");
			StringWriter headerWriter = new StringWriter(128);
			headerWriter.append(NettoFrame.SERVICE_HEADER).append(":").append(this.getServiceName()).append("\r\n");
			headerWriter.append(NettoFrame.METHOD_HEADER).append(":").append(method.getName()).append("\r\n");
			if (args != null) {
				headerWriter.append(NettoFrame.ARGSLEN_HEADER).append(":").append(args.length + "");
			} else {
				headerWriter.append(NettoFrame.ARGSLEN_HEADER).append(":0");
			}

			if (this.doSignature) {
				headerWriter.append("\r\n");
				String signature = this.createSignature(requestBody);
				headerWriter.append(NettoFrame.SIGNATURE_HEADER).append(":").append(signature);
			}

			byte[] headerContentBytes = headerWriter.toString().getBytes("UTF-8");

			String header = String.format("%s%d/%d/%d", NettoFrame.NETTO_HEADER_START, 2, headerContentBytes.length,
					byteBody.length);
			byte[] headerBytes = new byte[NettoFrame.HEADER_LENGTH];

			Arrays.fill(headerBytes, (byte) ' ');
			System.arraycopy(header.getBytes("utf-8"), 0, headerBytes, 0, header.length());
			os.write(headerBytes);
			os.write(headerContentBytes);
			os.flush();
			os.write(byteBody);

			os.flush();

			InputStream is = socket.getInputStream();
			Arrays.fill(headerBytes, (byte) ' ');
			is.read(headerBytes);
			String responseHeader = new String(headerBytes,"utf-8");
			
			if(!responseHeader.startsWith(NettoFrame.NETTO_HEADER_START)){
			    throw new RemoteAccessException("error header start:"+responseHeader);
			}			    
			else{
			    String[] headerSections = responseHeader.substring(NettoFrame.NETTO_HEADER_START.length()).split("/");
                if (headerSections.length == 3) {
                    String flag = headerSections[0];
                    String headerContentSizeAsString = headerSections[1].trim();
                    String bodySizeAsString = headerSections[2].trim();
                    int headerContentSize = Integer.parseInt(headerContentSizeAsString);
                    int bodySize = Integer.parseInt(bodySizeAsString);
                    
                    byte[] headerContent = new byte[headerContentSize];
                    is.read(headerContent);
                    
                    String responseHeaderContent = new String(headerContent,"utf-8");
                    Map<String,String> headers = this.decoderHeader(responseHeaderContent);
                    if(flag.equals(NettoFrame.NETTO_FAILED)){
                        String errorMessage = headers.get(NettoFrame.ERROR_HEADER);
                        throw new RemoteAccessException(errorMessage);
                    }
                    else{
                        byte[] body = new byte[bodySize];
                        is.read(body);
                        Object retObject = mapper.readValue(body, mapper.getTypeFactory().constructType(method.getGenericReturnType()));
                        return retObject;
                    }
                    
                } else {
                    throw new RemoteAccessException("error header start:"+responseHeader);
                }
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (e instanceof SocketException || e instanceof SocketTimeoutException) {
				socket.close();
				this.pool.invalidate(socket);
				invalidatedSocket = true;
			}
			throw e;
		} finally {
		    if(!invalidatedSocket)
		        this.pool.release(socket);
		}
	}
}
