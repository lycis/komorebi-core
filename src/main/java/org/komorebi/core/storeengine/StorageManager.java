package org.komorebi.core.storeengine;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.komorebi.core.configuration.KomorebiCoreConfig;
import org.komorebi.core.security.User;

/**
 * The storage manager is responsible for splitting up files and saving them according
 * to the configured algorithm. It also provides read access to files and resembles them
 * on request from the various sources.
 * 
 * The storage manager follows an asynchronous job approach for saving files. This means 
 * that each storage request will be accepted and processed but this won't happen instantly. 
 * The status of each storage request can be polled by using the returned request number.
 * Files will be queued for saving when there are not enough resources available and then be
 * processed as soon as resources are available again.
 * 
 * Read access to files will be ranted synchronously as this needs to be done faster.
 * TODO flash this out
 * 
 * This part of the code will be highly multi-threaded.
 * 
 * @author lycis
 *
 */
public class StorageManager {
	// constants
	private static final String LOGGER_NAME = "storagemanager";
	
	// singleton (again)
	private static StorageManager instance = new StorageManager();
	
	// private members
	private int threadCounter = 0;
	private ExecutorService executor = null;
	private int jobRetentionTime = 0;
	private Map<Long, StorageJob> jobRegister = null;
	
	/**
	 * Creates and initialises the store manager. This constructor is called
	 * on first access.
	 */
	private StorageManager(){
		KomorebiCoreConfig config = new KomorebiCoreConfig();
		threadCounter = config.getInt("storage.threads");
		try{
			executor = Executors.newFixedThreadPool(threadCounter);
			Logger.getLogger(LOGGER_NAME).warning("Storage Manager initialised with "+threadCounter+" threads.");
		}catch(IllegalArgumentException e){
			Logger.getLogger(LOGGER_NAME).warning("Invalid thread count for storage jobs were given. Assuming 1 as default.");
			threadCounter = 1;
			executor = Executors.newFixedThreadPool(threadCounter);
		}
		
		jobRetentionTime = config.getInt("storage.retentionTime");
		String retentionUnit = config.getString("storage.retentionTime[@unit]");
		if(retentionUnit == null || "minutes".equals(retentionUnit)){
			jobRetentionTime *= 60;
		}else if("seconds".equals(retentionUnit)){
			// do nothing
		}else if("hours".equals(retentionUnit)){
			jobRetentionTime *= 3600;
		}
		
		if(jobRetentionTime <= 0){
			Logger.getLogger(LOGGER_NAME).warning("Invalid retention time for storage jobs were given. Assuming 5 minutes as default.");
			jobRetentionTime = 300;
		}else{
			Logger.getLogger(LOGGER_NAME).warning("Retention time for storage jobs set to "+jobRetentionTime+" seconds.");
		}
	}
	
	/**
	 * Schedules a file for saving. As soon as a job slot is free the Storage Manager starts saving.
	 * This method works asynchronously and will thus return immediately. The status of the job
	 * has to be polled to see if it has finished.
	 *  
	 * @param user the user who wishes to store a file
	 * @param filename name of the file (absolute path within the storage)
	 * @param data this array contains the whole data of the file
	 * @return ID of the storage job (can be used for polling the status) 
	 */
	public static long storeFile(User user, String filename, byte[] data){
		// TODO implement
		return instance.nextJobId();
	}
	
	/**
	 * Gives the ID for the next job.
	 * @return next free job id or -1 in case all ids are taken
	 */
	synchronized private long nextJobId(){
		Set<Long> takenIds = jobRegister.keySet();
		for(long l=0; l<Long.MAX_VALUE; ++l){
			if(!takenIds.contains(l)){
				return l;
			}
		}
		return -1;
	}
	
	
}
