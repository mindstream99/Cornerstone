package com.paxxis.cornerstone.scripting;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResultVariableTest {

	@Test
	public void mergedResultCodesTest(){
		ResultVariable var = new ResultVariable();
		var.merge(new ResultVariable());
		
		assertEquals(var.getResultCodes().size(), 2);
	}
	
	@Test
	public void deepMergedResultCodesTest() {
		ResultVariable var = new ResultVariable();
		var.merge(new ResultVariable());

		ResultVariable var2 = new ResultVariable();
		var2.merge(new ResultVariable());
		var2.merge(new ResultVariable());
		var.merge(var2);

		assertEquals(var.getResultCodes().size(), 5);
	}

}
