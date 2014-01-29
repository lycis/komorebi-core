package org.komorebi.core.plugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.komorebi.core.configuration.KomorebiCoreConfig;
import org.komorebi.plugin.IKomorebiConfigurationConsumer;
import org.komorebi.plugin.IKomorebiPlugin;
import org.komorebi.plugin.IKomorebiStorage;
import org.komorebi.plugin.annotations.KomorebiPluginStatus;

/**
 * This is a singleton manager class that provides a possibility to load and instantiate
 * PlugIns. Access is only granted by using the static accessor methods.
 * 
 * @author lycis
 *
 */
public class PluginManager {
	// constants
	private static final String LOGGER_NAME = "pluginmanager";
	
	// singleton instance
	private static PluginManager instance = new PluginManager();
	
	// plugin register
	private Map<PluginType, Map<String, Class<IKomorebiPlugin>>> pluginRegister = null;
	
	/**
	 * The available types on PlugIns.
	 * @author lycis
	 *
	 */
	public enum PluginType{
		STORAGE,
	};
	
	private PluginManager(){
		pluginRegister = new HashMap<PluginType, Map<String, Class<IKomorebiPlugin>>>();
		loadPlugins();
	}
	
	/**
	 * Loads all registered PlugIns based on the configuration.
	 */
	private void loadPlugins(){
		KomorebiCoreConfig config = new KomorebiCoreConfig();
		
		if(config.getRootNode().getChildrenCount("plugins") < 1){
			return;
		}
		
		List<ConfigurationNode> pluginNodes = config.getRootNode().getChildren("plugins").get(0).getChildren("plugin");
		for(int pos=0; pos<pluginNodes.size(); ++pos){
			String name = config.getString("plugins.plugin("+pos+")[@name]");
			String version = config.getString("plugins.plugin("+pos+")[@version]");
			if(version == null || name == null){
				continue;
			}
			
			loadPluginsJar(name+"-"+version); // check all classes in jar
		}
	}
	
	/**
	 * Scan a JAR for PlugIns and register them.
	 * 
	 * @param jarname name of the JAR file
	 */
	private void loadPluginsJar(String jarname){
		URL[] urlList = new URL[1];
		try{
			URL jarUrl = new URL("file:"+jarname+".jar");
			urlList[0] = jarUrl;
		}catch(MalformedURLException e){
			Logger.getLogger(LOGGER_NAME).warning("Plugin '"+jarname+"' could not be loaded (invalid path)");
		}
		
		URLClassLoader classLoader = new URLClassLoader(urlList);
		try{
			JarFile jfile = new JarFile(jarname+".jar");
			
			// walk through all files of the jar
			Enumeration<JarEntry> entries = jfile.entries();
			while(entries.hasMoreElements()){
				JarEntry entry = entries.nextElement();
				if(entry.isDirectory() || !entry.getName().endsWith(".class")){
					continue; // we only care for classes
				}
				
				String className = entry.getName().substring(0,entry.getName().length()-6).replace('/', '.');
				
				Class<IKomorebiPlugin> pluginClass = null;
				try {
					Class<?> cl = classLoader.loadClass(className);
					if(!IKomorebiPlugin.class.isAssignableFrom(cl)){
						continue; // only care about PlugIn classes
					}
					
					pluginClass = (Class<IKomorebiPlugin>) cl;
				} catch (ClassNotFoundException e) {
					Logger.getLogger(LOGGER_NAME).warning("Error while registering PlugIns of '"+jarname+"': "+
				                                          className+" could not be loaded");
					continue;
				}
				
				tryRegisterPlugin(jarname, pluginClass);
			}
			
			jfile.close();
		}catch(IOException e){
			Logger.getLogger(LOGGER_NAME).warning("Plugin '"+jarname+"' could not be loaded (does not exist)");
		}finally{
			if(classLoader != null){
				try {
					classLoader.close();
				} catch (IOException e) {
					Logger.getLogger(LOGGER_NAME).warning("Potential resource leak: class loader could not be closed (reason: "+e.getMessage()+")");
				}
			}
		}
	}
	
	/**
	 * This method tries to register a class as plugin. It will only be registered if it matches any of the
	 * supported PlugIn types else this method will do nothing.
	 * 
	 * @param pluginClass class of the plugin
	 */
	synchronized private void tryRegisterPlugin(String name, Class<IKomorebiPlugin> pluginClass){
		
		// check status
		KomorebiPluginStatus pStatus = pluginClass.getAnnotation(KomorebiPluginStatus.class);
		if(pStatus != null){
			if(pStatus.disabled()){
				Logger.getLogger(LOGGER_NAME).info("Plugin '"+pluginClass.getName()+"' is disabled.");
				return;
			}
		}
		
		// check if configuration consumer is correctly implemented
		try{
			Constructor<IKomorebiPlugin> c = pluginClass.getConstructor();
			IKomorebiPlugin pi = c.newInstance();
			if(pi.isConfigConsumer() && !IKomorebiConfigurationConsumer.class.isAssignableFrom(pluginClass)){
				Logger.getLogger(LOGGER_NAME).warning("Plugin '"+name+"' does not adhere to defined standards (configuration consumer) and will be omitted.");
				return;
			}
		}catch(NoSuchMethodException e){
			Logger.getLogger(LOGGER_NAME).warning("Plugin '"+name+"' does not adhere to defined standards (default constructor) and will be omitted.");
			return;
		}catch(InvocationTargetException e){
			Logger.getLogger(LOGGER_NAME).warning("Plugin '"+name+"' caused an error ("+e.getMessage()+") and will be omitted.");
			return;
		}catch(IllegalAccessException e){
			Logger.getLogger(LOGGER_NAME).warning("Plugin '"+name+"' caused an error ("+e.getMessage()+") and will be omitted.");
			return;
		}catch(InstantiationException e){
			Logger.getLogger(LOGGER_NAME).warning("Plugin '"+name+"' caused an error ("+e.getMessage()+") and will be omitted.");
			return;
		}
		
		if(IKomorebiStorage.class.isAssignableFrom(pluginClass)){
			Logger.getLogger(LOGGER_NAME).info("Registered storage PlugIn '"+pluginClass.getName()+"'");
			if(!pluginRegister.containsKey(PluginType.STORAGE)){
				// if submap does not already exist it has to be created
				pluginRegister.put(PluginType.STORAGE, new HashMap<String, Class<IKomorebiPlugin>>());
			}
			pluginRegister.get(PluginType.STORAGE).put(name, pluginClass);
		}
		
		
		return;
	}
	
	public static IKomorebiStorage getPlugin(PluginType t, String name){
		// TODO implement
		return null;
	}
	
	/**
	 * Returns a list of the names of all PlugIns that are registered to the according type.
	 * 
	 * @param t PluginType you wish to read
	 * @return list of names
	 */
	public static List<String> getPluginsByType(PluginType t){
		Map<String, Class<IKomorebiPlugin>> plugins = instance.pluginRegister.get(t);
		if(plugins == null){
			return new ArrayList<String>();
		}
		return new ArrayList<String>(plugins.keySet());
	}
	
	/**
	 * Writes status information to the log. 
	 */
	public static void logStatus(){
		Logger log = Logger.getLogger(LOGGER_NAME);
		StringBuilder logString = new StringBuilder("\n*** PlugIn Status ***\n");
		
		List<String> storagePlugins = getPluginsByType(PluginType.STORAGE);
		logString.append("Storage PlugIns: "+storagePlugins.size()+"\n");
		
		log.info(logString.toString());
		return;
	}
}
