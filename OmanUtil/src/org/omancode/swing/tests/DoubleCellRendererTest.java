package org.omancode.swing.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.omancode.swing.DoubleCellRenderer;

public class DoubleCellRendererTest {

	private static DoubleCellRenderer renderer;
	private static final double dbl20digits = 1.12345678911234567890;
	private static final double dbl5zeros = 0.0000012345;
	
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		renderer = new DoubleCellRenderer(10);
	}

	@Test
	public void testFormatter() {
		NumberFormat formatter = renderer.getFormatter();
		
		
		
		System.out.println("MaximumFractionDigits: " + formatter.getMaximumFractionDigits());
		System.out.println("MinimumFractionDigits: " + formatter.getMinimumFractionDigits());
		
		renderer.setValue(dbl20digits);
		System.out.println(renderer.getText());
		assertThat(renderer.getText().length(), equalTo(12));
		
		renderer.setValue(dbl5zeros);
		System.out.println(renderer.getText());
		assertThat(renderer.getText().length(), equalTo(12));
		
		//renderer = DecimalFormat.getInstance();
		
		renderer.setValue(1.12E-19);
		System.out.println(renderer.getText());
		
	}

}
