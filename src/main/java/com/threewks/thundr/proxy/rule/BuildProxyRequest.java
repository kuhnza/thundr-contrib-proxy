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


import com.google.common.collect.Maps;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.proxy.http.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildProxyRequest {
	public static final List<String> IgnoredRequestHeaders = new ArrayList<String>(){{
		add("host");
		add("x-forwarded-for");
		add("x-forwarded-port");
		add("x-forwarded-proto");
		add("x-forwarded-host");
		add("x-forwarded-server");
	}};

	public Request from(ProxyRule rule, Request request) {
		String url = rule.transform(request.url());
		String method = request.method();
		Map<String, String> parameters = request.parameters();
		Map<String, String> headers = filterHeaders(request.headers());
		byte[] body = request.body();

		return Request.url(url)
				.method(method)
				.parameters(parameters)
				.headers(headers)
				.body(body);
	}

	private Map<String, String> filterHeaders(Map<String, String> headers) {
		Map<String, String> filtered = Maps.newHashMap();

		for (String name : headers.keySet()) {
			if (IgnoredRequestHeaders.contains(name.toLowerCase())) {
				Logger.debug("Filtered out header: %s", name);
				continue;
			}

			String value = headers.get(name);
			filtered.put(name, value);
			Logger.debug("Adding header: %s: %s", name, name.contains("authorization") ? "[redacted]" : value);
		}
		return filtered;
	}
}
