package org.jamsim.swing.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.text.NumberFormat;

import org.jamsim.swing.DoubleCellRenderer;
import org.junit.BeforeClass;
import org.junit.Test;

public class DoubleCellRendererTest {

	private static DoubleCellRenderer renderer;
	private static final double dbl20digits = 1.12345678911234567890;
	
	
	
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
	}

}
