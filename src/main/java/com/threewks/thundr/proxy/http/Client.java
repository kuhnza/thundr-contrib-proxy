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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.threewks.thundr.proxy.ThundrProxyException;
import com.threewks.thundr.util.Streams;

public class Client {
	private static HttpTransport DefaultHttpTransport = new NetHttpTransport();
	private HttpRequestFactory requestFactory;

	public Client() {
		this(DefaultHttpTransport);
	}

	public Client(HttpTransport httpTransport) {
		this.requestFactory = httpTransport.createRequestFactory();
	}

	public Response send(Request request) {
		try {
			HttpRequest clientRequest = convertToGoogleHttpClientRequest(request);
			HttpResponse clientResponse = clientRequest.execute();
			return convertToInternalHttpResponse(clientResponse);
		} catch (HttpResponseException e) {
			return convertToInternalHttpResponse(e);
		} catch (IOException e) {
			throw new ThundrProxyException(e, "An unexpected error occurred while sending the request: %s", e.getMessage());
		}
	}

	private HttpRequest convertToGoogleHttpClientRequest(Request request) throws IOException {
		String method = request.method();
		GenericUrl url = new GenericUrl(request.url());
		HttpRequest internalRequest;
		if (method.equalsIgnoreCase("GET")) {
			internalRequest = requestFactory.buildGetRequest(url);
		} else {
			byte[] body = (request.body() == null) ? new byte[]{} : request.body();
			internalRequest = requestFactory.buildRequest(method, url, new ByteArrayContent(null, body));
		}

		Map<String, String> headers = request.headers();
		HttpHeaders internalHeaders = internalRequest.getHeaders();
		for (String name : request.headerNames()) {
			internalHeaders.put(name, Arrays.asList(headers.get(name)));
		}
		return internalRequest;
	}

	private Response convertToInternalHttpResponse(HttpResponse response) throws IOException {
		return new Response().status(response.getStatusCode())
				.headers(readHeaders(response.getHeaders()))
				.body(readContent(response.getContent()));
	}

	private Response convertToInternalHttpResponse(HttpResponseException e) {
		return new Response().status(e.getStatusCode())
				.headers(readHeaders(e.getHeaders()))
				.body((e.getContent() == null) ? "" : e.getContent());
	}

	private Map<String, String> readHeaders(HttpHeaders headers) {
		Map<String, String> map = new HashMap<String, String>();
		for (String name : headers.keySet()) {
			map.put(name, StringUtils.join(headers.getHeaderStringValues(name), ","));
		}
		return map;
	}

	private byte[] readContent(InputStream content) throws IOException {
		return Streams.readBytes(content);
	}
}
