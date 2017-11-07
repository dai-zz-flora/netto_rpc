package com.netto.core.context;

public class ServerAddress implements Comparable<ServerAddress> {
	private String ip;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	private int port;
	private int weight;

    @Override
    public int compareTo(ServerAddress o) {
        int ret = ip.compareTo(ip);
        if(ret==0){
            return this.port-o.port;
        }
        else{
            return ret;
        }
    }
    
    public String toString(){
        return this.ip+":"+this.port;
    }
	

}
