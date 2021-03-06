package com.netto.server.desc.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netto.server.bean.NettoServiceBean;
import com.netto.server.bean.ServiceBean;
import com.netto.service.desc.ClassDesc;
import com.netto.service.desc.FieldDesc;
import com.netto.service.desc.MethodDesc;
import com.netto.service.desc.ServerDesc;
import com.netto.service.desc.ServiceDesc;
import com.netto.service.desc.ServiceDescApi;

public class ServiceDescApiImpl implements ServiceDescApi {
	private ServerDesc serverDesc;
	private Map<String, NettoServiceBean> serviceBeans;

	public ServiceDescApiImpl(ServerDesc serverDesc, Map<String, NettoServiceBean> serviceBeans) {
		this.serverDesc = serverDesc;
		this.serviceBeans = serviceBeans;
	}

	@Override
	public ServerDesc getServerDesc() {
		return this.serverDesc;
	}

	public List<MethodDesc> findServiceMethods(String serviceName) {
		NettoServiceBean serviceObj = this.serviceBeans.get(serviceName);
		if (serviceObj == null)
			return null;
		List<MethodDesc> methodDescs = new ArrayList<MethodDesc>();
		Method[] methods = serviceObj.getObject().getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equals("getClass") || method.getName().equals("toString")
					|| method.getName().equals("equals") || method.getName().equals("clone")
					|| method.getName().equals("finalize") || method.getName().equals("hashCode")
					|| method.getName().equals("notify") || method.getName().equals("notifyAll")
					|| method.getName().equals("wait")) {
				continue;
			}
			MethodDesc methodDesc = new MethodDesc();
			methodDesc.setMethodName(method.getName());
			methodDesc.setReturnType(method.getGenericReturnType().getTypeName());
			dependons(methodDesc, method.getGenericReturnType());
			Type[] argTypes = method.getGenericParameterTypes();
			List<String> list1 = new ArrayList<String>();
			for (Type argType : argTypes) {
				list1.add(argType.getTypeName());
				dependons(methodDesc, argType);
			}
			methodDesc.setArgTypes(list1);
			methodDescs.add(methodDesc);

		}
		return methodDescs;
	}

	public Set<ServiceDesc> findServices() {
		Set<ServiceDesc> services = new HashSet<ServiceDesc>();
		for (String key : this.serviceBeans.keySet()) {
			ServiceBean serviceBean = this.serviceBeans.get(key).getServiceBean();
			ServiceDesc desc = new ServiceDesc();
			desc.setServiceName(key);
			desc.setServerApp(this.serverDesc.getServerApp());
			desc.setTimeout(serviceBean.getTimeout());
			Class<?>[] interfaces = this.serviceBeans.get(key).getObjectType().getInterfaces();
			if (interfaces != null && interfaces.length > 0) {
				desc.setInterfaceClazz(interfaces[0].getName());
			}
			desc.setGateway(serviceBean.getGateway());
			services.add(desc);
		}
		return services;
	}

	private void dependons(MethodDesc methodDesc, Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType type1 = (ParameterizedType) type;
			Type[] actTypes = type1.getActualTypeArguments();
			for (Type actType : actTypes) {
				if (actType.getTypeName().startsWith("java")) {
					continue;
				}
				Class<?> actClass = (Class<?>) actType;
				if (methodDesc.getDependClasses() != null) {
					if (methodDesc.getDependClasses().containsKey(actClass.getName())) {
						continue;
					}
				}
				ClassDesc classDesc = new ClassDesc();
				classDesc.setClassName(actClass.getName());
				List<FieldDesc> fields = new ArrayList<FieldDesc>();
				classDesc.setFields(fields);
				Field[] actFields = actClass.getDeclaredFields();
				for (Field actField : actFields) {
					FieldDesc fieldDesc = new FieldDesc();
					fieldDesc.setFieldName(actField.getName());
					fieldDesc.setFieldType(actField.getGenericType().getTypeName());
					this.dependons(methodDesc, actField.getGenericType());
					fields.add(fieldDesc);
				}
				if (methodDesc.getDependClasses() == null) {
					methodDesc.setDependClasses(new HashMap<String, ClassDesc>());
				}
				methodDesc.getDependClasses().put(classDesc.getClassName(), classDesc);
			}
		}
	}

	// private boolean checkToken(String token) {
	// DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	// String content = format.format(new Date());
	// String temp = DesUtil.encrypt(content.getBytes(), content);
	// return temp.equals(token);
	// }

	@Override
	public int queryServiceTimeout(String serviceName) {
		NettoServiceBean serviceObj = this.serviceBeans.get(serviceName);
		if (serviceObj == null)
			throw new RuntimeException(serviceName + " don't exists!");
		return serviceObj.getServiceBean().getTimeout();
	}

	@Override
	public int pingService(String data) {
		return data.length();
	}

	@Override
	public Set<String> findServicesByInterface(String interfaceClazzStr) {
		Set<String> services = new HashSet<String>();
		Class<?> interfaceClazz;
		try {
			interfaceClazz = Class.forName(interfaceClazzStr);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		for (String key : this.serviceBeans.keySet()) {
			Class<?>[] interfaces = this.serviceBeans.get(key).getObjectType().getInterfaces();
			if (Arrays.asList(interfaces).contains(interfaceClazz)) {
				services.add(key);
			}
		}
		return services;
	}

}