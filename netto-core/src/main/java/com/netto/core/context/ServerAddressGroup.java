package com.netto.core.context;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.netto.core.util.Constants;
import com.netto.service.desc.ServerDesc;

public class ServerAddressGroup{
	private List<ServerAddress> servers;
	private String serverApp;
	private String serverGroup = Constants.DEFAULT_SERVER_GROUP;
	private String registry;
	
	
	private boolean compareServers(List<ServerAddress> otherServers){
	   if(this.servers.size()==otherServers.size()){
    	   Map<String,ServerAddress> myServers = this.servers.stream().collect(Collectors.toConcurrentMap(i -> i.toString(), j -> j,(a,b)-> a));
    	   
//    	   Map<String,ServerAddress> theirServers = otherServers.stream().collect(Collectors.toMap (i -> i.toString(), j -> j));
    	   
    	   if(this.servers.size()!=otherServers.size()){
    	       return false;
    	   }
    	   else{
    	       for(ServerAddress serverAddress:otherServers){
    	           if(!myServers.containsKey(serverAddress.toString())){
    	               return false;
    	           }
    	       }
    	       
    	       return true;
    	   }
	   }
	   else
	       return false;
	   
	}
	

	public String getRegistry() {
		return registry;
	}

	public void setRegistry(String registry) {
		this.registry = registry;
	}

	public String getServerApp() {
		return serverApp;
	}

	public void setServerApp(String serverApp) {
		this.serverApp = serverApp;
	}

	public String getServerGroup() {
		return serverGroup;
	}

	public void setServerGroup(String serverGroup) {
		this.serverGroup = serverGroup;
	}

	public List<ServerAddress> getServers() {
		return servers;
	}
	
    public void setServers(List<ServerAddress> servers) {
        this.servers = servers;
    }


	public ServerDesc getServerDesc() {
		ServerDesc serverDesc = new ServerDesc();
		serverDesc.setRegistry(registry);
		serverDesc.setServerApp(serverApp);
		serverDesc.setServerGroup(serverGroup);
		return serverDesc;
	}

    public void setServersFromString(String servers) {
		List<String> serverList = Arrays.asList(servers.split(";"));
		List<ServerAddress> addresses = serverList.stream().map(server -> {
			String[] s = server.split(":");
			String host = s[0];
			int port = 1234;
			if (s.length > 1) {
				port = Integer.parseInt(s[1]);
			}

			ServerAddress address = new ServerAddress();
			address.setIp(host);
			address.setPort(port);
			return address;

		}).collect(Collectors.toList());
		this.servers = addresses;

	}

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  ServerAddressGroup){
            ServerAddressGroup another = (ServerAddressGroup)obj;
            return serverApp.equals(another.getServerApp())&&serverGroup.equals(another.getServerGroup())&&this.compareServers(another.servers);
        }
        else{
            return false;
        }
    }

   
    
}
