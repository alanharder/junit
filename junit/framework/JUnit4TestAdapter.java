package junit.framework; 

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.internal.runner.TestIntrospector;

public class JUnit4TestAdapter implements Test {

	private List<Method> fMethods;
	private final List<TestCase> fTests;
	private final Class<? extends Object> fNewTestClass;
	private TestIntrospector testIntrospector;

	public JUnit4TestAdapter(Class<? extends Object> newTestClass) {
		fNewTestClass= newTestClass;
		testIntrospector= new TestIntrospector(fNewTestClass);
		fMethods= testIntrospector.getTestMethods(org.junit.Test.class);
		fTests = wrapMethodsAsTests();
	}

	
	public int countTestCases() {
		return fMethods.size();
	}

	public void run(TestResult result) {
		try {
			oneTimeSetUp();
			try {
				runTests(result);
			} finally {
				oneTimeTearDown();
			}
		} catch (Exception e) {
			result.addError(this, e);
		}
	}

	public List<TestCase> getTests() {
		return fTests;
	}
	
	private void oneTimeSetUp() throws Exception {
		List<Method> beforeMethods= testIntrospector.getTestMethods(BeforeClass.class);
		for (Method method : beforeMethods)
			method.invoke(null, new Object[0]);
	}
	
	private void runTests(TestResult result) throws Exception {
		for (TestCase test : fTests) {
			result.run(test);
		}
	}
	
	private void oneTimeTearDown() throws Exception {
		List<Method> beforeMethods= testIntrospector.getTestMethods(AfterClass.class);
		for (Method method : beforeMethods)
			method.invoke(null, new Object[0]);
	}

	@Override
	public String toString() {
		return "Wrapped " + fNewTestClass.toString();
	}

	private List<TestCase> wrapMethodsAsTests() {
		List<TestCase> result = new ArrayList<TestCase>();
		for (Method method : fMethods) {
			try {
				Object test = fNewTestClass.newInstance();
				TestCase wrapper= new JUnit4TestCaseAdapter(test, method);
				result.add(wrapper);
			} catch (Exception e) {
				// skip it
			}
		}
		return result;
	}
}