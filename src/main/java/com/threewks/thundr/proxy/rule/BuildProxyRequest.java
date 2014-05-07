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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.proxy.http.Request;

public class BuildProxyRequest {
	public static final List<String> IgnoredRequestHeaders = Expressive.list(
		"host",
		"x-forwarded-for",
		"x-forwarded-port",
		"x-forwarded-proto",
		"x-forwarded-host",
		"x-forwarded-server");

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
		Map<String, String> filtered = new HashMap<String, String>();

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
