package com.netto.client;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import com.netto.client.context.RpcContext;
import com.netto.client.pool.HttpConnectPool;
import com.netto.client.provider.ServiceProvider;
import com.netto.context.ServiceRequest;
import com.netto.context.ServiceResponse;
import com.netto.filter.InvokeMethodFilter;
import com.netto.util.Constants;

public class RpcHttpClient extends AbstactRpcClient {
	private HttpConnectPool pool;

	public RpcHttpClient(ServiceProvider provider, List<InvokeMethodFilter> filters, String serviceName, int timeout) {
		super(provider, filters, serviceName, timeout);
		this.pool = (HttpConnectPool) provider.getPool("http");
	}

	@Override
	protected Object invokeMethod(Method method, Object[] args) throws Throwable {
		ServiceRequest req = new ServiceRequest();
		req.setMethodName(method.getName());
		req.setServiceName(this.getServiceName());
		if (args != null) {
			for (Object arg : args) {
				if (arg != null) {
					req.getArgs().add(gson.toJson(arg));
				} else {
					req.getArgs().add(null);
				}
			}
		}
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(this.getTimeout())
				.setConnectionRequestTimeout(this.getTimeout()).setSocketTimeout(this.getTimeout()).build();

		HttpClient httpClient = this.pool.getResource();
		try {
			// 创建httppost
			HttpPost post = new HttpPost(this.getInvokeUrl(method.getName()));
			post.setConfig(requestConfig);
			post.addHeader("$app", this.getProvider().getServiceApp());
			if (RpcContext.getRouterContext() != null) {
				post.addHeader("$router", RpcContext.getRouterContext());
			}
			// body
		    StringWriter writer = new StringWriter();
		    gson.toJson(req,writer);
		    writer.append(Constants.PROTOCOL_REQUEST_DELIMITER);
			StringEntity se = new StringEntity(writer.toString(), ContentType.APPLICATION_JSON);
			post.setEntity(se);

			HttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity, "UTF-8");            
			if(response.getStatusLine().getStatusCode()!=200){


    			ServiceResponse res = gson.fromJson(body, ServiceResponse.class);
    			if (res.getSuccess()) {
    				return gson.fromJson(res.getBody(), method.getGenericReturnType());
    			} else {
    				throw new Exception(res.getBody());
    			}
			}
			else{
			    throw new Exception(body);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			this.pool.release(httpClient);
		}
	}

	private String getInvokeUrl(String methodName) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(this.getProvider().getRegistry());
		if (!this.getProvider().getRegistry().endsWith("/")) {
			sb.append("/");
		}
		sb.append(this.getProvider().getServiceApp()).append("/").append(this.getServiceName()).append("/")
				.append(methodName);
		return sb.toString();
	}
}
