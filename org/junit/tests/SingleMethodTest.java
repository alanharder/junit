package org.junit.tests;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.notify.RunNotifier;
import org.junit.plan.CompositePlan;
import org.junit.plan.LeafPlan;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.extensions.Parameterized;
import org.junit.runner.extensions.Suite;
import org.junit.runner.extensions.Parameterized.Parameters;
import org.junit.runner.extensions.Suite.SuiteClasses;

public class SingleMethodTest {
	public static int count;

	static public class OneTimeSetup {
		@BeforeClass
		public static void once() {
			count++;
		}

		@Test
		public void one() {
		}

		@Test
		public void two() {
		}
	}

	@Test
	public void oneTimeSetup() throws Exception {
		count = 0;
		Runner runner = Request.aMethod(OneTimeSetup.class, "one").getRunner();
		Result result = run(runner);
		
		assertEquals(1, count);
		assertEquals(1, result.getRunCount());
	}

	@RunWith(Parameterized.class)
	static public class ParameterizedOneTimeSetup {
		@Parameters
		public static Collection<Object[]> params() {
			return Parameterized.eachOne(1, 2);
		}

		public ParameterizedOneTimeSetup(int x) {
			
		}
		
		@BeforeClass
		public static void once() {
			count++;
		}

		@Test
		public void one() {
		}
	}

	@Test
	public void parameterizedOneTimeSetup() throws Exception {
		count = 0;
		Runner runner = Request.aMethod(ParameterizedOneTimeSetup.class, "one[0]").getRunner();
		Result result = run(runner);

		assertEquals(1, count);
		assertEquals(1, result.getRunCount());
	}

	@Test
	public void filteringAffectsPlan() throws Exception {
		Runner runner = Request.aMethod(OneTimeSetup.class, "one").getRunner();
		assertEquals(1, runner.testCount());
	}

	public static class TestOne {
		@Test public void a() {}
		@Test public void b() {}
	}

	public static class TestTwo {
		@Test public void a() {}
		@Test public void b() {}
	}
	
	@RunWith(Suite.class)
	@SuiteClasses({TestOne.class, TestTwo.class})
	public static class OneTwoSuite {} 

	@Test
	public void eliminateUnnecessaryTreeBranches() throws Exception {
		Runner runner = Request.aClass(OneTwoSuite.class).filterWith(new LeafPlan(TestOne.class, "a")).getRunner();
		CompositePlan plan = (CompositePlan) runner.getPlan();
		assertEquals(1, plan.getChildren().size());
	}
	
	private Result run(Runner runner) {
		RunNotifier runNotifier = new RunNotifier();
		Result result = new Result();
		result.addListenerTo(runNotifier);

		runner.run(runNotifier);
		return result;
	}
}