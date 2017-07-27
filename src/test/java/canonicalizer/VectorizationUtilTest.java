package canonicalizer;

import java.io.IOException;

import org.junit.Test;


public class VectorizationUtilTest {

	@Test
	public void test1() throws IOException {	
		VectorizationUtils.deserializeAndVectorize("/Users/pponzio/git/randoop/src/test/java/canonicalizer/sll3.obj", "", 3);
	}
	
}
