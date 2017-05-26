
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;


public class XMLLoader {
public static void main(String[] args) {
	Properties prop = new Properties();
	FileInputStream reader = null;
	try {
		reader = new FileInputStream("prop/AR_idrive_Con.xml");
		prop.loadFromXML(reader);
		Set<String> keySet = prop.stringPropertyNames();
		for (String key : keySet) {
			System.out.println("key: " + key + ", value: "+ prop.getProperty(key));
		}
		
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (InvalidPropertiesFormatException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
}
}
