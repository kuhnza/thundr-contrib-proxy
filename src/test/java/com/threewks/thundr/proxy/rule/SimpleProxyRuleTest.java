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
package com.threewks.thundr.proxy.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.proxy.intercept.ProxyInterceptor;
import com.threewks.thundr.proxy.intercept.ProxyInterceptorRegistry;

public class SimpleProxyRuleTest {

	private String path = "/foo/bar";
	private String origin = "http://localhost:8080";
	private String target = "http://example.org";
	private SimpleProxyRule rule;

	@Before
	public void before() {
		rule = new SimpleProxyRule(path, target);
	}

	@Test
	public void shouldMatchUrlWhenStartsWithBasePath() {
		String url = origin + "/foo/bar/baz/qux";
		assertThat(rule.appliesTo(url), is(true));
	}

	@Test
	public void shouldNotMatchPathWhenDoesNotStartWithBasePath() {
		String url = origin + "/bar/baz/qux";
		assertThat(rule.appliesTo(url), is(false));
	}

	@Test
	public void shouldTransformPathToTarget() {
		String url = rule.transform(origin + "/foo/bar/baz/qux");
		assertThat(url, equalTo(target + "/baz/qux"));
	}

	@Test
	public void shouldTransformPathToTargetPreservingQueryString() {
		String url = rule.transform(origin + "/foo/bar/baz/qux?search=hello");
		assertThat(url, equalTo(target + "/baz/qux?search=hello"));
	}

	@Test
	public void shouldAddProxyInterceptor() {
		ProxyInterceptorRegistry registry = rule.getInterceptorRegistry();
		assertThat(registry.size(), is(0));

		ProxyInterceptor interceptor = mock(ProxyInterceptor.class);
		rule.addInterceptor(interceptor);
		assertThat(registry.size(), is(1));
		assertThat(registry.contains(interceptor), is(true));
	}

	@Test
	public void shouldDescribeRuleInToString() {
		String description = rule.toString();
		assertThat(description, equalTo("SimpleProxyRule{ '" + path + "' -> '" + target + "' }"));
	}
}
