package se.kth.ms;

/**
 * Created by babbarshaer on 2014-08-20.
 */
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;

/**
 * This class launches the web application in an embedded Jetty container.
 * This is the entry point to your application. The Java command that is used for
 * launching should fire this main method.
 */
public class AngularJSWebApp {

    public static void main(String[] args) throws Exception {

        String _defaultWebappLocation = "src/main/webapp";
        String _defaultPort = "9090";
        
        
        // The simple Jetty config here will serve static content from the webapp directory
        String webappDirLocation = null;

        if(args.length > 0){
            webappDirLocation = args[0];
        }
        else{
            webappDirLocation = _defaultWebappLocation;
        }
        
        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = _defaultPort;
        }
        Server server = new Server(Integer.valueOf(webPort));

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setDescriptor(webappDirLocation + "WEB-INF/web.xml");

        File file  = new File(webappDirLocation);
        
        if(!file.exists()){
            System.out.println("Unable to find file ... ");
            System.exit(1);
        }

        webapp.setResourceBase(webappDirLocation);

        server.setHandler(webapp);
        server.start();
        server.join();
    }
}