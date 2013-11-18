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
package com.threewks.thundr.proxy.http;

import com.google.common.collect.Maps;
import jodd.util.Base64;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

public class Request {
	private String method = "GET";
	private URL url;
	private Map<String, String> parameters = Maps.newHashMap();
	private Map<String, String> headers = Maps.newHashMap();
	private byte[] body = null;

	public static Request from(HttpServletRequest request) throws IOException {
		StringBuffer url = request.getRequestURL();
		String queryString = request.getQueryString();
		Map<String, String> parameters = Maps.newHashMap();
		if (queryString != null && queryString.length() > 0) {
			url.append('?').append(queryString);

			String[] params = queryString.split("&");
			for (String param : params) {
				String[] parts = param.split("=");
				if (parts == null || parts.length == 0) {
					continue;
				}
				String name = parts[0];
				String value = (parts.length > 1) ? parts[1] : null;
				parameters.put(name, value);
			}
		}

		String method = request.getMethod();

		Map<String, String> headers = Maps.newHashMap();
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = (String) headerNames.nextElement();
			if (name == null) {
				continue;
			}
			headers.put(name, request.getHeader(name));
		}

		byte[] body = null;
		InputStream inputStream = request.getInputStream();
		if (inputStream != null) {
			body = IOUtils.toByteArray(inputStream);
			if (body.length == 0) {
				body = null;
			}
		}

		return Request.url(url.toString())
			.method(method)
			.parameters(parameters)
			.headers(headers)
			.body(body);
	}

	public static Request url(String url) {
		try {
			return new Request(new URL(url));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public String method() {
		return method;
	}

	public Request method(String method) {
		this.method = method;
		return this;
	}

	public String url() {
		return url.toString();
	}

	public Map<String, String> parameters() {
		return parameters;
	}

	public Request parameter(String name, String value) {
		this.parameters.put(name, value);
		return this;
	}

	public Request parameters(Map<String, String> parameters) {
		this.parameters.putAll(parameters);
		return this;
	}

	public Map<String, String> headers() {
		return headers;
	}

	public Set<String> headerNames() {
		return headers.keySet();
	}

	public Request header(String name, String value) {
		headers.put(name, value);
		return this;
	}

	public Request headers(Map<String, String> headers) {
		this.headers.putAll(headers);
		return this;
	}

	public Request authorize(String username, String password) {
		String encoded = Base64.encodeToString(username + ":" + password);
		headers.put("authorization", "Basic " + encoded);
		return this;
	}

	public byte[] body() {
		return body;
	}

	public Request body(byte[] body) {
		this.body = body;
		return this;
	}

	public String path() {
		return url.getPath();
	}

	@Override
	public String toString() {
		return "Request{" +
				"method='" + method + '\'' +
				", url=" + url +
				", headers=" + headers +
				", body=" + Arrays.toString(body) +
				'}';
	}

	private Request(URL url) {
		this.url = url;
	}
}
