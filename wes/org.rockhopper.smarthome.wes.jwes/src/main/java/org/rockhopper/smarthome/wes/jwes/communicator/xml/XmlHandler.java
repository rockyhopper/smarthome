package org.rockhopper.smarthome.wes.jwes.communicator.xml;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.WesSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.WstxDriver;

public class XmlHandler {
	protected Logger log= LoggerFactory.getLogger(getClass());
	
	private XmlProtocol xmlProtocol;
	
	private XStream xStreamWithFormat;
	private XStream xStreamWithoutFormat;
	
	private WesData wesData;
	
	public XmlHandler(WesData wesData) {
		this.wesData= wesData;
		xmlProtocol = new XmlProtocol(wesData);
	}
	
	protected XStream getXStreamWithFormat(){
		if (xStreamWithFormat==null) {
			xStreamWithFormat= new XStream(new WstxDriver());
			Class<?>[] annotatedClasses= {WesData.class, WesSensor.class};
			xStreamWithFormat.processAnnotations(annotatedClasses);
			xStreamWithFormat.registerConverter(new WesRelaysCardsConverter(xStreamWithFormat.getMapper(), xmlProtocol, true));
			xStreamWithFormat.registerConverter(new FieldConverter(xmlProtocol, true));
			xStreamWithFormat.allowTypesByWildcard(new String[] { 
											        "org.rockhopper.smarthome.wes.jwes.**"
											        });
		}
		return xStreamWithFormat;
	}
	
	protected XStream getXStreamWithoutFormat(){
		if (xStreamWithoutFormat==null) {
			xStreamWithoutFormat= new XStream(new WstxDriver());
			Class<?>[] annotatedClasses= {WesData.class, WesSensor.class};
			xStreamWithoutFormat.processAnnotations(annotatedClasses);
			xStreamWithoutFormat.registerConverter(new WesRelaysCardsConverter(xStreamWithoutFormat.getMapper(), xmlProtocol, false));
			xStreamWithoutFormat.registerConverter(new FieldConverter(xmlProtocol, false));
			xStreamWithoutFormat.allowTypesByWildcard(new String[] { 
											        "org.rockhopper.smarthome.wes.jwes.**"
											        });
		}
		return xStreamWithoutFormat;
	}
	
    public Object deserialize(String xml) {
    	log.debug("Deserialize: \n{}\n\n", xml);
    	return getXStreamWithoutFormat().fromXML(xml);
    }
    
    public String serialize(boolean withFormat){
    	StringWriter stringWriter = new StringWriter();
    	if (withFormat) {
    		getXStreamWithFormat().marshal(wesData, new PrettyPrintWriter(stringWriter));
    	}
    	else {
    		getXStreamWithoutFormat().marshal(wesData, new PrettyPrintWriter(stringWriter));
    	}
    	return stringWriter.toString();
    }
    
    public String buildCGX(){
    	return convertXMLToCGX(serialize(true));
    }
        
    private String convertXMLToCGX(String xml) {    	
    	StringWriter cgx = new StringWriter();
    	
        String pattern = " command=\\\"([^\\\"]*)\\\""; // The pattern is 'command=\"([^\"]*)\"'

        Pattern p = Pattern.compile(pattern);
        try (PrintWriter pw = new PrintWriter(cgx)) {
            String[] arr = xml.split("\\r?\\n");
            List<String> allLines = Arrays.asList(arr);
            for (String line : allLines) {
                Matcher matcher = p.matcher(line);
                String command = "";
                if (matcher.find()) {
                    line = matcher.replaceFirst("");
                    command = matcher.group(1);
                    if (StringUtils.contains(line, "%xml")) {
                        int idxStart = line.indexOf("<");
                        int idxStop = line.indexOf(">", idxStart);
                        String startTag = line.substring(idxStart + 1, idxStop);
                        int idx2Start = line.indexOf("</");
                        int idx2Stop = line.indexOf(">", idx2Start);
                        String endTag = line.substring(idx2Start + 1, idx2Stop);
                        pw.write("t <" + startTag + ">" + "\n");
                        pw.write("c " + command + "\n");
                        pw.write("t <" + endTag + ">" + "\n");
                    } else {
                        pw.write("c " + command + line + "\n");
                    }
                } else {
                    pw.write("t " + line + "\n");
                }

            }
        }
        return cgx.toString();
    }
}
