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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.proxy.http.Request;
import com.threewks.thundr.proxy.http.Response;

public class ProxyInterceptorRegistryImpl implements ProxyInterceptorRegistry {
	private final List<ProxyInterceptor> interceptors = new ArrayList<ProxyInterceptor>();

	@Override
	public void register(ProxyInterceptor interceptor) {
		interceptors.add(interceptor);
	}

	@Override
	public boolean contains(ProxyInterceptor interceptor) {
		return interceptors.contains(interceptor);
	}

	@Override
	public List<ProxyInterceptor> list() {
		return Collections.unmodifiableList(interceptors);
	}

	@Override
	public int size() {
		return interceptors.size();
	}

	@Override
	public Response before(Request inboundRequest, Request proxyRequest) {
		for (ProxyInterceptor interceptor : interceptors) {
			Response response = interceptor.before(inboundRequest, proxyRequest);
			if (response != null) {
				return response;
			}
		}
		return null;
	}

	@Override
	public Response after(Request inboundRequest, Request proxyRequest, Response proxiedResponse) {
		for (ProxyInterceptor interceptor : interceptors) {
			Response response = interceptor.after(inboundRequest, proxyRequest, proxiedResponse);
			if (response != null) {
				return response;
			}
		}
		return null;
	}

	@Override
	public boolean exception(Throwable t, HttpServletRequest request, HttpServletResponse response) {
		for (ProxyInterceptor interceptor : interceptors) {
			if (interceptor.exception(t, request, response)) {
				return true;
			}
		}
		return false;
	}
}
