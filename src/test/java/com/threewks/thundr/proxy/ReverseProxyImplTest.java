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
package com.threewks.thundr.proxy;



import com.threewks.thundr.proxy.http.Request;
import com.threewks.thundr.proxy.http.Response;
import com.threewks.thundr.proxy.intercept.ProxyInterceptor;
import com.threewks.thundr.proxy.rule.BuildProxyRequest;
import com.threewks.thundr.proxy.rule.ProxyRule;
import com.threewks.thundr.proxy.rule.SimpleProxyRule;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ReverseProxyImplTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static final Charset Utf8 = Charset.forName("utf-8");
	private static final String OriginPath = "/commerce/v1";
	private static final String TargetBaseUrl = "http://example.org";

	private ProxyRule rule;
	private ProxyImpl reverseProxy;

	@Before
	public void before() {
		rule = new SimpleProxyRule(OriginPath, TargetBaseUrl);
		reverseProxy = spy(new ProxyImpl());
	}

	@Test
	public void shouldFaithfullyReplicateGetRequestToTarget() {
		String uri = "/customer.json";
		String method = "GET";
		String content = null;

		Map<String, String[]> headers = new HashMap<String, String[]>();
		headers.put("Accept", new String[]{"application/json"});

		doRequest(uri, method, headers, content);
	}

	@Test
	public void shouldFaithfullyReplicatePostRequestToTarget() {
		String uri = "/customer.json";
		String method = "POST";
		String content = "{" +
				"    \"customer\": {" +
				"        \"first_name\": \"Test\"," +
				"        \"last_name\": \"Customer\"," +
				"        \"email\": \"tets@example.org\"" +
				"    }" +
				"}";

		Map<String, String[]> headers = new HashMap<String, String[]>();
		headers.put("Accept", new String[]{"application/json"});
		headers.put("Content-Type", new String[]{"application/json"});

		doRequest(uri, method, headers, content);
	}

	@Test
	public void shouldFaithfullyReplicatePutRequestToTarget() {
		String uri = "/customer/1234.json";
		String method = "PUT";
		String content = "{\"customer\":{\"city\":\"Sydney\"}";

		Map<String, String[]> headers = new HashMap<String, String[]>();
		headers.put("Accept", new String[]{"application/json"});
		headers.put("Content-Type", new String[]{"application/json"});

		doRequest(uri, method, headers, content);
	}

	@Test
	public void shouldFaithfullyReplicateDeleteRequestToTarget() {
		String uri = "/customer/1234.json";
		String method = "DELETE";
		String content = null;
		Map<String, String[]> headers = new HashMap<String, String[]>();

		doRequest(uri, method, headers, content);
	}

	@Test
	public void shouldFilterOutIgnoredHeaders() {
		String uri = "/customer.json";
		String method = "GET";
		String content = null;

		Map<String, String[]> headers = new HashMap<String, String[]>();
		headers.put("Host", new String[]{"http://localhost:8080"});

		doRequest(uri, method, headers, content);
	}

	@Test
	 public void shouldRunInterceptors() {
		ProxyInterceptor interceptor = mock(ProxyInterceptor.class);
		rule.addInterceptor(interceptor);

		doRequest("/customer.json", "GET", new HashMap<String, String[]>(), null);

		verify(interceptor, times(1)).before(any(Request.class), any(Request.class));
		verify(interceptor, times(1)).after(any(Request.class), any(Request.class), any(Response.class));

		// Force an exception for this next request
		RuntimeException fabricated = new RuntimeException("Expected");
		doThrow(fabricated).when(reverseProxy).sendRequest(any(Request.class));

		MockHttpServletRequest request = TestUtil.newMockHttpServletRequest()
				.method("GET")
				.url("http://localhost.local" + OriginPath + "/customer.json")
				.header(new HashMap<String, String[]>());
		reverseProxy.proxy(rule, request, mock(HttpServletResponse.class));

		verify(interceptor, times(1)).exception(eq(fabricated), any(MockHttpServletRequest.class), any(MockHttpServletResponse.class));
	}

	@Test
	public void shouldReturnAlternateResponseFromBeforeInterceptor() {
		int status = 418;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/plain");
		byte[] body = "I'm a teapot!".getBytes(Utf8);
		Response alternateResponse = new Response()
				.status(status)
				.headers(headers)
				.body(body);

		ProxyInterceptor interceptor = mock(ProxyInterceptor.class);
		doReturn(alternateResponse).when(interceptor).before(any(Request.class), any(Request.class));
		rule.addInterceptor(interceptor);

		doRequest(alternateResponse);
	}

	@Test
	public void shouldReturnAlternateResponseFromAfterInterceptor() {
		int status = 401;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/plain");
		byte[] body = "No soup for you!".getBytes(Utf8);
		Response alternateResponse = new Response()
				.status(status)
				.headers(headers)
				.body(body);

		ProxyInterceptor interceptor = mock(ProxyInterceptor.class);
		doReturn(alternateResponse).when(interceptor).after(any(Request.class), any(Request.class), any(Response.class));
		rule.addInterceptor(interceptor);

		doRequest(alternateResponse);
	}

	private MockHttpServletResponse doRequest(String path, String method, Map<String, String[]> headers, String content) {
		MockHttpServletRequest request = TestUtil.newMockHttpServletRequest()
				.method(method)
				.url("http://localhost.local" + OriginPath + path)
				.header(headers)
				.content(content);
		MockHttpServletResponse response = new MockHttpServletResponse();

		doReturn(mock(Response.class)).when(reverseProxy).sendRequest(any(Request.class));
		reverseProxy.proxy(rule, request, response);

		ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);
		verify(reverseProxy).sendRequest(argument.capture());

		Request capturedRequest = argument.getValue();
		assertThat(capturedRequest.url(), is(TargetBaseUrl + path));
		assertThat(capturedRequest.method(), is(request.getMethod()));

		Map<String, String> capturedHeaders = capturedRequest.headers();
		assertThat(capturedHeaders, is(notNullValue()));
		for (String name : headers.keySet()) {
			if (BuildProxyRequest.IgnoredRequestHeaders.contains(name.toLowerCase())) {
				assertThat(capturedHeaders.containsKey(name), is(false));
			} else {
				assertThat(capturedHeaders, hasKey(name));
				String value = StringUtils.join(headers.get(name), ", ");
				assertThat(capturedHeaders.get(name), equalTo(value));
			}
		}

		byte[] body = capturedRequest.body();
		if (content == null) {
			assertThat(body, is(nullValue()));
		} else {
			assertThat(body, is(notNullValue()));
			assertThat(body, equalTo(content.getBytes()));
		}

		return response;
	}

	private void doRequest(Response alternateResponse) {
		MockHttpServletRequest request = TestUtil.newMockHttpServletRequest()
				.method("GET")
				.url("http://localhost" + OriginPath + "/foo/bar")
				.header(new HashMap<String, String[]>());
		MockHttpServletResponse response = new MockHttpServletResponse();

		doReturn(mock(Response.class)).when(reverseProxy).sendRequest(any(Request.class));
		reverseProxy.proxy(rule, request, response);

		assertThat(response.status(), is(alternateResponse.status()));

		Map<String, String> headers = alternateResponse.headers();
		for (String name : headers.keySet()) {
			assertThat(response.containsHeader(name), is(true));
			assertThat((String) response.header(name), equalTo(headers.get(name)));
		}

		byte[] body = alternateResponse.body();
		if (response.content() == null) {
			assertThat(body, is(nullValue()));
		} else {
			assertThat(body, is(notNullValue()));
			assertThat(response.content().getBytes(), equalTo(body));
		}
	}
}
