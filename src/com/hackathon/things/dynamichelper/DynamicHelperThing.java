package com.hackathon.things.dynamichelper;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import com.thingworx.logging.LogUtilities;
import com.thingworx.metadata.annotations.ThingworxEventDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinitions;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.things.Thing;
import com.thingworx.types.primitives.NumberPrimitive;


@ThingworxPropertyDefinitions(properties = {
		@ThingworxPropertyDefinition(name="NumberProperty1", description="Sample number property", baseType="NUMBER", aspects={"isPersistent:false","isReadOnly:false"}),
})

@ThingworxEventDefinitions(events = {
		@ThingworxEventDefinition(name="SalutationSent", description="Salutation sent event", dataShape="SalutationSentEvent")
})


public class DynamicHelperThing extends Thing {
	
	protected static Logger _logger = LogUtilities.getInstance().getApplicationLogger(DynamicHelperThing.class);
	private static Pattern patternDomainName;
	  private Matcher matcher;
	  private static final String DOMAIN_NAME_PATTERN
		= "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}";
	  static {
		patternDomainName = Pattern.compile(DOMAIN_NAME_PATTERN);
	  }
	
	private Double _heartRate = 0.0;
	
	protected void initializeThing() throws Exception {
		_heartRate = ((NumberPrimitive)this.getPropertyValue("NumberProperty1")).getValue();
	}

	@ThingworxServiceDefinition( name="DynamicHelperService", description="Dynamic Helper" )
	@ThingworxServiceResult( name="Result", description="Result", baseType="STRING" )
	public String DynamicHelperService(
			@ThingworxServiceParameter( name="heartRate", description="The heart rate you want to analyze", baseType="NUMBER" ) Double heartRate) throws Exception {

		String results = generateResultdata(heartRate);
		
		return results ;
	}
	
	private String generateResultdata(Double heartRate) {
		 double minimumHeartRate = 50;
	     double maximumHeartRate = 120;
	     String searchWords = null;
	     
	     int minimumComparison = Double.compare(minimumHeartRate, heartRate);
	     
	     int maximumComparison = Double.compare(heartRate, maximumHeartRate);

	     if(minimumComparison > 0) {
	    	 searchWords= "Low%20heart%20rate";
	     } else if(maximumComparison > 0) {
		        searchWords= "Very%20high%20heart%20rate";
		     } else if(minimumComparison < 0 || maximumComparison < 0) {
		    	 searchWords = "Normal%20Heart%20Rate";
			     }
	     else {
	    	 searchWords = "Normal%20Heart%20Rate";
	     }
	     
	     return getDataFromGoogle(searchWords);
	}
	
	private String getDataFromGoogle(String query) {

		String request = "https://www.google.com/search?q=" + query + "&num=20";
		System.out.println("Sending request..." + request);

		try {

			// need http protocol, set this as a Google bot agent :)
			Document doc = Jsoup
				.connect(request)
				.userAgent(
				  "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
				.timeout(5000).get();

			Elements results = doc.select("span.st");
			
			System.out.println(doc.html());
			
			System.out.println("Total number of elements found : " + results.size());
			
			StringBuilder builder = new StringBuilder();
			
			for (Element result : results) {
				builder.append(result.text());
				builder.append("\n");
				builder.append("\n");
				builder.append("\n");
			}
			System.out.println("String representation :" + builder.toString());
			
			return builder.toString();
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "Something gone wrong. try again.";

	  }

}
