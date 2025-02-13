package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.trackery.trackerybackapiserver.test.ExampleClass;

public class ExampleClassTest {

	ExampleClass exampleClass;

	@BeforeEach
	public void setUp() {
		exampleClass = new ExampleClass();
	}

	@Test
	void method1_성공() {
		boolean result = exampleClass.method1();
		Assertions.assertTrue(result);
	}

	@Test
	void method2_성공() {
		boolean result = exampleClass.method2();
		Assertions.assertTrue(result);
	}
}
