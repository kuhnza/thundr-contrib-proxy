/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.proxy.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.proxy.Proxy;
import com.threewks.thundr.proxy.TestUtil;
import com.threewks.thundr.proxy.exception.ProxyResolverException;
import com.threewks.thundr.proxy.rule.ProxyRule;
import com.threewks.thundr.proxy.rule.SimpleProxyRule;
import com.threewks.thundr.route.RouteType;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;

public class ProxyActionResolverTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ProxyActionResolver resolver;
	private Proxy proxy;

	@Before
	public void before() {
		proxy = mock(Proxy.class);
		resolver = new ProxyActionResolver(proxy);
	}

	@Test
	public void shouldCreateAction() {
		ProxyAction action = resolver.createActionIfPossible(ProxyActionResolver.ActionName);
		assertThat(action, is(notNullValue()));
	}

	@Test
	public void shouldNotCreateAction() {
		ProxyAction action = resolver.createActionIfPossible("static");
		assertThat(action, is(nullValue()));
	}

	@Test
	public void shouldResolveActionWhereRuleExistsForPath() {
		MockHttpServletRequest request = TestUtil.newMockHttpServletRequest()
				.url("http://localhost:8080/foo/bar/baz?qux=true");
		MockHttpServletResponse response = new MockHttpServletResponse();

		ProxyRule rule1 = new SimpleProxyRule("/foo/bar", "http://example.org");
		resolver.registerProxyRule(rule1);

		ProxyRule rule2 = new SimpleProxyRule("/baz", "http://fake.org");
		resolver.registerProxyRule(rule2);

		resolver.resolve(new ProxyAction(), RouteType.GET, request, response, null);

		ArgumentCaptor<ProxyRule> ruleCaptor = ArgumentCaptor.forClass(ProxyRule.class);
		verify(proxy, times(1)).proxy(ruleCaptor.capture(), any(HttpServletRequest.class), any(HttpServletResponse.class));

		assertThat(ruleCaptor.getValue(), is(rule1));
	}

	@Test
	public void shouldNotResolveActionWhereNoRuleExistsForPath() {
		thrown.expect(BaseException.class);
		thrown.expectCause(Matchers.any(ProxyResolverException.class));

		MockHttpServletRequest request = TestUtil.newMockHttpServletRequest()
				.url("http://localhost:8080/foo/bar");
		MockHttpServletResponse response = new MockHttpServletResponse();

		resolver.resolve(new ProxyAction(), RouteType.GET, request, response, null);
		verify(proxy, times(0)).proxy(any(ProxyRule.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
	}

}
