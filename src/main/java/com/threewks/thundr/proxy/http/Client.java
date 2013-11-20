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

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.collect.Maps;
import com.threewks.thundr.proxy.ThundrProxyException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class Client {
	public static class DontThrowExceptionsOnUnsuccessfulResponseInitializer
			implements HttpRequestInitializer, HttpUnsuccessfulResponseHandler {
		@Override
		public boolean handleResponse(HttpRequest request, HttpResponse response, boolean retrySupported)
				throws IOException {
			return false;
		}

		@Override
		public void initialize(HttpRequest request) throws IOException {
			request.setUnsuccessfulResponseHandler(this);
		}
	}

	private static HttpTransport DefaultHttpTransport = new NetHttpTransport();
	private HttpRequestFactory requestFactory;

	public Client() {
		this(DefaultHttpTransport);
	}

	public Client(HttpTransport httpTransport) {
		this.requestFactory = httpTransport.createRequestFactory(new DontThrowExceptionsOnUnsuccessfulResponseInitializer());
	}

	public Response send(Request request) {
		try {
			HttpRequest internalRequest = convertToGoogleHttpClientRequest(request);
			HttpResponse internalResponse = internalRequest.execute();
			Response response = convertToInternalHttpResponse(internalResponse);
			return response;
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

	private Map<String, String> readHeaders(HttpHeaders headers) {
		Map<String, String> map = Maps.newHashMap();
		for (String name : headers.keySet()) {
			map.put(name, String.valueOf(headers.get(name)));
		}
		return map;
	}

	private byte[] readContent(InputStream content) throws IOException {
		return IOUtils.toByteArray(content);
	}
}
