package randoop.fieldextensions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ObjectSerializer {

	private String file;
	private ObjectOutputStream oos;

	public ObjectSerializer(String file) { 
		this.file = file;
	}
	
	public void open() {
        try {
			oos = new ObjectOutputStream(new FileOutputStream(file));
		} catch (IOException e) {
			throw new Error("Can't create serial file: " + file);
		}
	}
	
	public void serialize(Object o) {
		try {
			oos.writeObject(o);
		} catch (Exception e) {
			throw new Error("Can't serialize object: " + o.toString());
		}
	}
	
	public void close() {
		try { 
			oos.close();
		} catch (IOException e) {
			throw new Error("Can't close serial file: " + file);
		}
	}
	
}
