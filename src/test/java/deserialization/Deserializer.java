package deserialization;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.junit.Test;

public class Deserializer {

	@Test
	public void deserializeAndPrint() {
		try {
			ObjectInputStream ois = new ObjectInputStream(
			        new FileInputStream("input_objects.ser"));
			Object o;
			try {
				while (true) {
					o = ois.readObject();
					System.out.println(o.toString());
				}
			}
			catch (EOFException e) { /* The loop ends here */ }
			ois.close();
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
