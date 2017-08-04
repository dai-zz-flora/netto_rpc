package com.netto.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.netto.client.pool.TcpConnectPool;
import com.netto.client.provider.ServiceProvider;
import com.netto.context.ServiceRequest;
import com.netto.context.ServiceResponse;

public class RpcTcpClient implements InvocationHandler {
	private static Logger logger = Logger.getLogger(RpcTcpClient.class);
	private final String serviceName;
	private TcpConnectPool pool;
	private int timeout = 10 * 1000;
	private static Gson gson = new Gson();

	public RpcTcpClient(ServiceProvider provider, String serviceName, int timeout) {
		this.pool = (TcpConnectPool) provider.getPool("tcp");
		this.serviceName = serviceName;
		this.timeout = timeout;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		ServiceRequest req = new ServiceRequest();
		req.setMethodName(method.getName());
		req.setServiceName(serviceName);
		for (Object arg : args) {
			if (arg != null) {
				req.getArgs().add(gson.toJson(arg));
			} else {
				req.getArgs().add(null);
			}
		}
		Socket socket = null;
		try {
			socket = this.pool.getResource();
			socket.setSoTimeout(this.timeout);
			OutputStream os = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			osw.write(gson.toJson(req));
			osw.flush();

			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String body = br.readLine();

			ServiceResponse res = gson.fromJson(body, ServiceResponse.class);
			if (res.getSuccess()) {
				return gson.fromJson(res.getBody(), method.getGenericReturnType());
			} else {
				throw new Exception(res.getBody());
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			this.pool.release(socket);
		}
	}
}