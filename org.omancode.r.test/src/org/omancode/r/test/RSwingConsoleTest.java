package org.omancode.r.test;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JFrame;

import org.omancode.r.RFaceException;
import org.omancode.r.RFace;
import org.omancode.r.ui.RSwingConsole;

public class RSwingConsoleTest {

	private RFace rInterface;
	private final Collection<Person> people1 = new LinkedList<Person>();
	
	public RSwingConsoleTest() {
		Person p1 = new Person(1, "mike", 'm', 18.25, true);
		Person p2 = new Person(2, "michael", 'm', 28.25, true);
		Person p3 = new Person(3, "peter", 'm', 28.25 + (1.0 / 3.0), false);
		Person p4 = new Person(4, "bob", 'm', 17, true);
		Person p5 = new Person(5, "barbara", 'f', 18.7635120384, false);

		p1.setdArray(new double[] {1,2,3});
		
		people1.add(p1);
		people1.add(p2);
		people1.add(p3);
		people1.add(p4);
		people1.add(p5);
	}
	
	public static void main(String[] args) {
		new RSwingConsoleTest().testRSwingConsole();
	}
	
	public void testRSwingConsole() {
		loadConsole();
		try {
			//rInterface.assignDataFrame("p1", people1, Object.class);
			rInterface.printlnToConsole("Created p1");
			
			//char[] array = new char[] {'c','d'};
			double[] array = new double[] {1.2,2.2,3.3};
			//Double[] array = new Double[] {1.2,2.2,3.3};
			//RUtil.toVector((Object)array);
			
			//System.out.println(rInterface.evalMean(new double[] {1,2,3,4,5}));
			
		} catch (RFaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadConsole() {
		final RSwingConsole rConsole = new RSwingConsole();
		rConsole.setFont(new Font("Consolas", Font.PLAIN, 12));
		rConsole.setPreferredSize(new Dimension(500,500));

		try {
			// load R
			rInterface = RFace.getInstance(rConsole);

			// load
			rInterface.loadPackage("rJava");
			rInterface.loadPackage("JavaGD");

		} catch (RFaceException e) {

			// output stack trace to stderr
			e.printStackTrace();

			// output exception message to ascape log tab
			System.out.print(e.getMessage());
		}

		// Create and set up the window.
		JFrame frame = new JFrame("RSwingConsoleTest");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(rConsole);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static class Person {

		private int id;
		private String name;
		private char gender;
		private double age;
		private boolean updated;
		private double[] dArray;

		public Person(int id, String name, char gender, double age,
				boolean updated) {
			super();
			this.id = id;
			this.name = name;
			this.gender = gender;
			this.age = age;
			this.updated = updated;
		}

		public double[] getdArray() {
			return dArray;
		}

		public void setdArray(double[] dArray) {
			this.dArray = dArray;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public char getGender() {
			return gender;
		}

		public void setGender(char gender) {
			this.gender = gender;
		}

		public double getAge() {
			return age;
		}

		public void setAge(double age) {
			this.age = age;
		}

		public boolean isUpdated() {
			return updated;
		}

		public void setUpdated(boolean updated) {
			this.updated = updated;
		}
		
		@Override
		public String toString() {
			StringBuffer sbuf = new StringBuffer();
			
			sbuf.append(age).append(", ");
			sbuf.append(gender).append(", ");
			sbuf.append(id).append(", ");
			sbuf.append(name).append(", ");
			sbuf.append(updated).append("\n");
			
			return sbuf.toString();
		}
		
		public static String collToString(Collection<?> coll) {
			StringBuffer sbuf = new StringBuffer();
			
			for (Object obj : coll) {
				sbuf.append(obj.toString());
			}
			
			return sbuf.toString();
		}

	}

}

