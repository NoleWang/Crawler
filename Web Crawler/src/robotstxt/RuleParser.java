package robotstxt;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
*
* @author Namhost.com from stackoverflow
* Source: http://stackoverflow.com/questions/19332982/parsing-robot-txt-using-java-and-identify-whether-an-url-is-allowed/26825017#26825017
*/
public class RuleParser {

	private Map<String, List<String>> ruleSet;

	public Map<String, List<String>> getRuleSet() {
		return ruleSet;
	}

	public RuleParser(String path){
		
		URL urlRobot = null;
		String strRobot = null;
		URL url = null;
		try { 
			if(path.equals("http://lyle.smu.edu/~fmoore/")){
				strRobot = "http://lyle.smu.edu/~fmoore/robots.txt";
			}else{
				url = new URL(path);
				strRobot = "http://" + url.getHost() + "/robots.txt";
			}
			
				ruleSet = new HashMap<>();
		    	urlRobot = new URL(strRobot);
			}catch (MalformedURLException e) {
		        // something weird is happening, so don't trust it
			}

		String strCommands = null;
		try{
		        InputStream urlRobotStream = urlRobot.openStream();
		        byte b[] = new byte[1000];
		        int numRead = urlRobotStream.read(b);
		        strCommands = new String(b, 0, numRead);
		        while (numRead != -1) {
		            numRead = urlRobotStream.read(b);
		            if (numRead != -1) 
		            {
		                    String newCommands = new String(b, 0, numRead);
		                    strCommands += newCommands;
		            }
		        }
		       urlRobotStream.close();
		    } 
		    catch (IOException e) 
		    {
		         // if there is no robots.txt file, it is OK to search
		    }

		    if (strCommands.toLowerCase().contains("disallow")) // if there are no "disallow" values, then they are not blocking anything.
		    {
		        String[] split = strCommands.split("\n");
		        String mostRecentUserAgent = null;
		        for (int i = 0; i < split.length; i++) 
		        {
		            String line = split[i].trim();
		            if (line.toLowerCase().startsWith("user-agent")){
		                int start = line.indexOf(":") + 1;
		                int end   = line.length();
		                mostRecentUserAgent = line.substring(start, end).trim();
		                ruleSet.putIfAbsent(mostRecentUserAgent, new ArrayList<String>());
		            }
		            else if (line.toLowerCase().startsWith("disallow")) {
		                if (mostRecentUserAgent != null) {
		                    int start = line.indexOf(":") + 1;
		                    int end   = line.length();
		                    ruleSet.get(mostRecentUserAgent).add(line.substring(start, end).trim());
		                }
		            }
		        }
		    }
	}
	
}
