package com.github.tvdtb.mediaresource.browser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class BrowserResourceTest {

	@Test
	public void testSkip() {

		BrowserResource br = new BrowserResource();
		assertThat(br.calculateDelta(10, 5), equalTo(1));
		assertThat(br.calculateDelta(10, 10), equalTo(0));
		assertThat(br.calculateDelta(15, 10), equalTo(0));
		assertThat(br.calculateDelta(19, 10), equalTo(0));
		assertThat(br.calculateDelta(20, 10), equalTo(1));
		assertThat(br.calculateDelta(30, 10), equalTo(2));
	}
}
