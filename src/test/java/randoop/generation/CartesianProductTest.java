package randoop.generation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class CartesianProductTest {

	@Test
	public void testEmpty() {
		CartesianProduct<Integer> c = new CartesianProduct<>(3);
		ArrayList<Integer> l0 = new ArrayList<Integer>();
		l0.add(0);
		ArrayList<Integer> l1 = new ArrayList<Integer>();
		ArrayList<Integer> l2 = new ArrayList<Integer>();
		l2.add(2);
		l2.add(2);
		
		c.setIthComponent(0, l0);
		c.setIthComponent(1, l1);
		c.setIthComponent(2, l2);
		assertFalse(c.hasNext());
	}
	
	@Test
	public void testOnlyOneComponent() {
		CartesianProduct<Integer> c = new CartesianProduct<Integer>(3);
		ArrayList<Integer> l0 = new ArrayList<Integer>();
		l0.add(0);
		ArrayList<Integer> l1 = new ArrayList<Integer>();
		l1.add(1);
		ArrayList<Integer> l2 = new ArrayList<Integer>();
		l2.add(2);
		
		c.setIthComponent(0, l0);
		c.setIthComponent(1, l1);
		c.setIthComponent(2, l2);
		assertTrue(c.hasNext());
		List<Integer> res = c.next();
		assertEquals((int)res.get(0), 0);
		assertEquals((int)res.get(1), 1);
		assertEquals((int)res.get(2), 2);
		
		assertFalse(c.hasNext());
	}
	
	
	@Test
	public void testManyComponents1() {
		CartesianProduct<Integer> c = new CartesianProduct<Integer>(3);
		ArrayList<Integer> l0 = new ArrayList<Integer>();
		l0.add(11);
		l0.add(12);
		ArrayList<Integer> l1 = new ArrayList<Integer>();
		l1.add(21);
		l1.add(22);
		ArrayList<Integer> l2 = new ArrayList<Integer>();
		l2.add(31);
		l2.add(32);
		c.setIthComponent(0, l0);
		c.setIthComponent(1, l1);
		c.setIthComponent(2, l2);

		int steps = l0.size()*l1.size()*l2.size();
		while (steps > 0) {
			assertTrue(c.hasNext());
			System.out.println(c.next());
			steps--;
		}

		assertFalse(c.hasNext());
	}
	
	
	@Test
	public void testManyComponents2() {
		CartesianProduct<Integer> c = new CartesianProduct<Integer>(3);
		ArrayList<Integer> l0 = new ArrayList<Integer>();
		l0.add(11);
		l0.add(12);
		ArrayList<Integer> l1 = new ArrayList<Integer>();
		l1.add(21);
		l1.add(22);
		ArrayList<Integer> l2 = new ArrayList<Integer>();
		l2.add(31);
		l2.add(32);
		l2.add(33);
		c.setIthComponent(0, l0);
		c.setIthComponent(1, l1);
		c.setIthComponent(2, l2);

		int steps = l0.size()*l1.size()*l2.size();
		while (steps > 0) {
			assertTrue(c.hasNext());
			System.out.println(c.next());
			steps--;
		}

		assertFalse(c.hasNext());
	}
	
	

}
