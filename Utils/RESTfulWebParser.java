import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RESTfulWebParser {	
	
	public abstract <T> T parseInput(Class<T> outputClass, InputStream stream) throws Exception;
	public abstract void formatOutput(Object response, OutputStream stream) throws IOException;
	public abstract <T> String formatToString(T response) throws IOException;	
	
}
