package ch.so.agi.ilivalidator.endpoints;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.stereotype.Component;

@Component
public class ClasspathEndpoint implements Endpoint<List<String>> {
	
    public String getId() {
        return "classpath";
    }
 
    public boolean isEnabled() {
        return true;
    }
 
    public boolean isSensitive() {
        return false;
    }
 
    public List<String> invoke() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs();

        List<String> messages = new ArrayList<String>();
        for(URL url: urls) {
        	messages.add(url.getFile().toString());
        }		

        return messages;
    }
}
