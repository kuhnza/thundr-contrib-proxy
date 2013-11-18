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
import com.threewks.thundr.http.service.HttpResponse;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Response {
	private int status = 200;
	private Map<String, String> headers = Maps.newHashMap();
	private byte[] body = null;

	public static Response from(HttpResponse response) {
		int status = response.getStatus();

		Map<String, String> headers = Maps.newHashMap();
		Map<String, List<String>> responseHeaders = response.getHeaders();
		for (String name : responseHeaders.keySet()) {
			if (name == null) {
				continue;
			}
			String value = StringUtils.join(responseHeaders.get(name), ",");
			headers.put(name, value);
		}

		byte[] body = response.getBodyAsBytes();
		if (body != null && body.length == 0) {
			body = null;
		}

		return new Response()
				.status(status)
				.headers(headers)
				.body(body);
	}

	public Response() {}

	public int status() {
		return status;
	}

	public Response status(int status) {
		this.status = status;
		return this;
	}

	public Map<String, String> headers() {
		return headers;
	}

	public Set<String> headerNames() {
		return headers.keySet();
	}

	public Response header(String name, String value) {
		headers.put(name, value);
		return this;
	}

	public Response headers(Map<String, String> headers) {
		this.headers.putAll(headers);
		return this;
	}

	public byte[] body() {
		return body;
	}

	public Response body(byte[] body) {
		this.body = body;
		return this;
	}

	public Response body(String body) {
		return body(body.getBytes());
	}

	public Response body(String body, Charset charset) {
		return body(body.getBytes(charset));
	}

	public String bodyAsString() {
		return new String(body);
	}

	@Override
	public String toString() {
		return "Response{" +
				"status=" + status +
				", headers=" + headers +
				", body=" + Arrays.toString(body) +
				'}';
	}
}
