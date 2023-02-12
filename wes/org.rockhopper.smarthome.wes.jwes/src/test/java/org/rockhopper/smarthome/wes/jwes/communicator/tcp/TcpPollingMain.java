package org.rockhopper.smarthome.wes.jwes.communicator.tcp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.rockhopper.smarthome.wes.jwes.WesConstants;
import org.rockhopper.smarthome.wes.jwes.communicator.xml.FieldConverter;
import org.rockhopper.smarthome.wes.jwes.communicator.xml.WesRelaysCardsConverter;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.WesSensor;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesDataNavigatorHelper;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesLabelsHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.WstxDriver;

public class TcpPollingMain {

	public static void main(String[] args) {
		System.out.println(String.format("Command-line Args[WesDeviceIp,WesTcpPort]: %s", Arrays.toString(args)));
		
		WesData wesData = null;

		XStream xstream = new XStream(new WstxDriver());
		Class<?>[] annotatedClasses= {WesData.class, WesSensor.class};
		xstream.processAnnotations(annotatedClasses);
		xstream.registerConverter(new WesRelaysCardsConverter(xstream.getMapper(), null, false));
		xstream.registerConverter(new FieldConverter(null, false));
		xstream.allowTypesByWildcard(new String[] { 
										        "org.rockhopper.smarthome.wes.jwes.**"
										        });

		try (FileReader fileReader = new FileReader(new File("OUT.XML"))) {
			wesData = (WesData) xstream.fromXML(fileReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WesLabelsHelper wesLabelsHelper = new WesLabelsHelper();
		// wesLabelsHelper.label(WesDataNavigatorHelper.LABEL_DATA_PREFIX, wesData);
		wesLabelsHelper.browse(WesDataNavigatorHelper.LABEL_DATA_PREFIX, new HashSet<Field<?, ?>>(), wesData);
		/*
		 * fields.forEach(field -> { eventListener.onEvent(new
		 * WesEvent(field.getLabel(), WesEvent.WesEventCode.UPDATE, null,
		 * field.getValue())); });
		 */
		wesData.truncate();

		String wesIpAddress= (String)args[0];
		Integer wesTcpPort= (args.length>1)?Integer.valueOf(args[1]):WesConstants.DEFAULT_TCP_PORT;
		
		new TcpPolling(wesIpAddress,wesTcpPort,wesData).run();
	}
}
