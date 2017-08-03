package randoop.reloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleReloader extends URLClassLoader {

	private Set<String> classesToReload;
	private Map<String, Class<?>> loaded;

	public SimpleReloader(Set<String> classesToReload) {
		super(getClassPath());
		this.classesToReload = classesToReload;
		this.loaded = new HashMap<>();
	}
	
    @Override
    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
    	int pos;
    	//System.out.println(">> Loading class: " + name);
    	if (classesToReload.contains(name) ||
    			((pos = name.lastIndexOf('$')) > 1 && classesToReload.contains(name.substring(0, pos)))) {
    		Class<?> c = loaded.get(name);
    		if (c == null) {
    			c = findClass(name);
    			loaded.put(name, c);
    		}
    		return c;
    	}

    	return super.loadClass(name);
    }	

    private static URL [] getClassPath() {
        URL [] cp = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        return cp;
      }
    
    
}
