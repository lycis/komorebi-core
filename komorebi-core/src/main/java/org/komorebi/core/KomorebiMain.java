package org.komorebi.core;

public class KomorebiMain {

	public static void main(String[] args) {
		ServerRunner sr = new ServerRunner();
		
		if(args.length < 1){
			sr.run();
		}else{
			if("--console".equals(args[0])){
				sr.run();
			}else if("--demon".equals(args[0]) || "-d".equals(args[0])){
				System.out.println("Starting Komorebi in daemon mode.");
				Thread dt = new Thread(sr);
				//dt.setDaemon(true);
				dt.start();
			}
		}
	}

}
