import java.util.concurrent.Executor;

import org.apache.log4j.PropertyConfigurator;

import com.reader.AbstractReader;
import com.reader.ReaderParamParser;


public class ConcreteReader extends AbstractReader{
	/**
	 * Main will create a new SEA_city_IncCon, call run function
	 * 
	 * @param args
	 * @return None
	 * @throws Exception 
	 * @exception
	 * @see
	 */
	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch("log4j.properties", 60000);
		AbstractReader incCon = new ConcreteReader();
		incCon.setParser(new ReaderParamParser());
		try {
			incCon.run();
		} catch (Exception ex) {
			LOGGER.fatal("Unexpected problem, program will terminate now (" + ex.getMessage() + ")");
		}
	}

	//Choose parse method
	@Override
	public void parseDataSource() {
	    String name = Thread.currentThread().getName();
	    Runnable thread = new Runnable() {
			public void run() {
				
			}
		};
		
		if (name.contains("")){
			
		} else{
			
		}
		
	}
	
	public void parseInfo(){
		
	}
	
	
}
