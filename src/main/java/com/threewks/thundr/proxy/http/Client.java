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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Client {
	public Response send(Request request) {
		HttpURLConnection connection = null;

		try {
			// Send request
			connection = open(request.url());
			setMethod(connection, request.method());
			addHeaders(connection, request.headers());
			writeBody(connection, request.body());

			// Read response
			int status = connection.getResponseCode();
			Map<String, String> headers = flattenHeaders(connection);
			byte[] body = readBody(connection);
			return new Response().status(status)
					.headers(headers)
					.body(body);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private HttpURLConnection open(String url) throws IOException {
		return (HttpURLConnection) new URL(url).openConnection();
	}

	private void setMethod(HttpURLConnection connection, String method) throws ProtocolException {
		connection.setRequestMethod(method);
		if (method.equals("POST") || method.equals("PUT")) {
			connection.setDoOutput(true);
		}
	}

	private void addHeaders(HttpURLConnection connection, Map<String, String> headers) {
		for (String name : headers.keySet()) {
			connection.setRequestProperty(name, headers.get(name));
		}
	}

	private void writeBody(HttpURLConnection connection, byte[] body) throws IOException {
		if (body == null) {
			return;
		}

		OutputStream os = null;
		try {
			if (connection.getDoOutput()) {
				os = connection.getOutputStream();
				os.write(body);
			}
		} finally {
			if (os != null) {
				os.flush();
				os.close();
			}
		}
	}

	private Map<String, String> flattenHeaders(HttpURLConnection connection) {
		Map<String, List<String>> headers = connection.getHeaderFields();
		Map<String, String> flattened = Maps.newHashMap();
		for (String name : headers.keySet()) {
			String value = StringUtils.join(headers.get(name), ",");
			flattened.put(name, value);
		}
		return flattened;
	}

	private byte[] readBody(HttpURLConnection connection) throws IOException {
		int status = connection.getResponseCode();
		InputStream inputStream = (status >= 200 && status <= 299) ?
				connection.getInputStream() : connection.getErrorStream();
		return IOUtils.toByteArray(inputStream);
	}
}
