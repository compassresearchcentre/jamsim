package org.jamsim.ascape.ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TableBuilderConfigTest {

	@Test
	public void test() throws IOException {
		Gson gson = new Gson();
		Reader reader = new BufferedReader(new FileReader("resource/TableBuilderConfig.json"));
		
		//Object result = gson.fromJson(reader, Object.class);
		
		Type mapType = new TypeToken<Map<String, Map<String, List<String>>>>(){}.getType();
		Map<String, Map<String, List<String>>> result = gson.fromJson(reader, mapType);
	}

}
