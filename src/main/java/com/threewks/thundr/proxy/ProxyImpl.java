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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.proxy.http.Client;
import com.threewks.thundr.proxy.http.Request;
import com.threewks.thundr.proxy.http.Response;
import com.threewks.thundr.proxy.intercept.ProxyInterceptorRegistry;
import com.threewks.thundr.proxy.rule.BuildProxyRequest;
import com.threewks.thundr.proxy.rule.ProxyRule;

public class ProxyImpl implements Proxy {
	private final BuildProxyRequest buildProxyRequest = new BuildProxyRequest();
	private Client httpClient;

	public ProxyImpl() {
		httpClient = new Client();
	}

	@Override
	public void proxy(ProxyRule rule, HttpServletRequest request, HttpServletResponse response) {
		ProxyInterceptorRegistry interceptorRegistry = rule.getInterceptorRegistry();

		try {
			Request inboundRequest = Request.from(request);
			Logger.info("Received request to proxy: %s", inboundRequest);

			Logger.info("Applying proxy rule: %s", rule);
			Request proxyRequest = buildProxyRequest.from(rule, inboundRequest);
			Logger.info("Resulting proxy request: %s", proxyRequest);

			Logger.debug("Invoking before proxy interceptors...");
			Response alternateResponse = interceptorRegistry.before(inboundRequest, proxyRequest);
			if (alternateResponse != null) {
				Logger.info("Before interceptor cancelled request. Sending alternate response: %s", alternateResponse);
				sendResponse(alternateResponse, response);
				return;
			}

			Logger.debug("Sending proxy request: %s", proxyRequest);
			Response proxiedResponse = sendRequest(proxyRequest);
			Logger.info("Received response from target: %s", proxiedResponse);

			Logger.debug("Invoking after proxy interceptors...");
			alternateResponse = interceptorRegistry.after(inboundRequest, proxyRequest, proxiedResponse);
			if (alternateResponse != null) {
				Logger.info("After interceptor is overriding the proxied response.");
				proxiedResponse = alternateResponse;
			}

			Logger.info("Sending response: %s", proxiedResponse);
			sendResponse(proxiedResponse, response);
		} catch (Throwable t) {
			Logger.error("An unexpected error occurred while proxying: %s", ExceptionUtils.getStackTrace(t));
			Logger.debug("Invoking exception interceptors...");
			interceptorRegistry.exception(t, request, response);
		}
	}

	protected Response sendRequest(Request proxyRequest) {
		Logger.info("Proxying %s request to target: %s", proxyRequest.method(), proxyRequest.url());
		return httpClient.send(proxyRequest);
	}

	protected void sendResponse(Response proxiedResponse, HttpServletResponse outboundResponse) throws IOException {
		outboundResponse.setStatus(proxiedResponse.status());

		Map<String, String> headers = proxiedResponse.headers();
		for (String name : headers.keySet()) {
			outboundResponse.addHeader(name, headers.get(name));
		}

		byte[] body = proxiedResponse.body();
		if (body != null) {
			OutputStream os = null;
			try {
				os = outboundResponse.getOutputStream();
				os.write(body);
			} finally {
				if (os != null) {
					os.flush();
					os.close();
				}
			}
		}
	}
}
