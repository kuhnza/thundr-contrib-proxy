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
package com.threewks.thundr.proxy.intercept;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.proxy.http.Request;
import com.threewks.thundr.proxy.http.Response;

public class ProxyInterceptorRegistryImplTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ProxyInterceptorRegistryImpl interceptorRegistry;
	private ProxyInterceptor interceptor1;
	private ProxyInterceptor interceptor2;

	@Before
	public void before() {
		interceptor1 = mock(ProxyInterceptor.class);
		interceptor2 = mock(ProxyInterceptor.class);

		interceptorRegistry = new ProxyInterceptorRegistryImpl();
		interceptorRegistry.register(interceptor1);
		interceptorRegistry.register(interceptor2);
	}

	@Test
	public void shouldContainOnlyRegisteredInterceptors() {
		assertThat(interceptorRegistry.size(), is(2));
		assertThat(interceptorRegistry.contains(interceptor1), is(true));
		assertThat(interceptorRegistry.contains(interceptor2), is(true));

		ProxyInterceptor interceptor3 = mock(ProxyInterceptor.class);
		assertThat(interceptorRegistry.contains(interceptor3), is(false));
	}

	@Test
	public void shouldReturnListOfRegisteredInterceptors() {
		List<ProxyInterceptor> interceptors = interceptorRegistry.list();

		assertThat(interceptors.size(), is(2));
		assertThat(interceptors.contains(interceptor1), is(true));
		assertThat(interceptors.contains(interceptor2), is(true));
	}

	@Test
	public void shouldFireBeforeEventForAllInterceptors() {
		interceptorRegistry.before(mock(Request.class), mock(Request.class));
		verify(interceptor1, times(1)).before(any(Request.class), any(Request.class));
		verify(interceptor2, times(1)).before(any(Request.class), any(Request.class));
	}

	@Test
	public void shouldHaltBeforeEventWhenInterceptorReturnsResponse() {
		doReturn(mock(Response.class)).when(interceptor1).before(any(Request.class), any(Request.class));

		interceptorRegistry.before(mock(Request.class), mock(Request.class));
		verify(interceptor1, times(1)).before(any(Request.class), any(Request.class));
		verify(interceptor2, times(0)).before(any(Request.class), any(Request.class));
	}

	@Test
	public void shouldFireAfterEventForAllInterceptors() {
		interceptorRegistry.after(mock(Request.class), mock(Request.class), mock(Response.class));
		verify(interceptor1, times(1)).after(any(Request.class), any(Request.class), any(Response.class));
		verify(interceptor2, times(1)).after(any(Request.class), any(Request.class), any(Response.class));
	}

	@Test
	public void shouldHaltAfterEventWhenInterceptorReturnsResponse() {
		doReturn(mock(Response.class)).when(interceptor1).after(any(Request.class), any(Request.class), any(Response.class));

		interceptorRegistry.after(mock(Request.class), mock(Request.class), mock(Response.class));
		verify(interceptor1, times(1)).after(any(Request.class), any(Request.class), any(Response.class));
		verify(interceptor2, times(0)).after(any(Request.class), any(Request.class), any(Response.class));
	}

	@Test
	public void shouldFireExceptionEventForAllInterceptors() {
		interceptorRegistry.exception(mock(Throwable.class), mock(HttpServletRequest.class), mock(HttpServletResponse.class));
		verify(interceptor1, times(1)).exception(any(Throwable.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
		verify(interceptor2, times(1)).exception(any(Throwable.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
	}

	@Test
	public void shouldHaltExceptionEventWhenInterceptorReturnsTrue() {
		doReturn(true).when(interceptor1).exception(any(Throwable.class), any(HttpServletRequest.class), any(HttpServletResponse.class));

		interceptorRegistry.exception(mock(Throwable.class), mock(HttpServletRequest.class), mock(HttpServletResponse.class));
		verify(interceptor1, times(1)).exception(any(Throwable.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
		verify(interceptor2, times(0)).exception(any(Throwable.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
	}
}
