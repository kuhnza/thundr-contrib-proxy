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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ClientTest {
	private static String TestServerHost = "localhost";
	private static int TestServerPort = 9999;

	private HttpServer httpServer;
	private Client httpClient;

	@Before
	public void before() throws IOException {
		httpServer = HttpServer.create(new InetSocketAddress(TestServerPort), 0);
		httpServer.createContext("/get", new HttpHandler() {
			public void handle(HttpExchange exchange) throws IOException {
				if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
				} else {
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
				}
				exchange.close();
			}
		});
		httpServer.createContext("/302", new HttpHandler() {
			public void handle(HttpExchange exchange) throws IOException {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, 0);
			exchange.getResponseHeaders().add("Location", String.format("http://%s:%s/get", TestServerHost, TestServerPort));
			exchange.close();
			}
		});
		httpServer.start();

		httpClient = new Client();
	}

	@After
	public void after() {
		httpServer.stop(0);
	}

	@Test
	public void shouldDefaultToSendingGetRequest() {
		String url = String.format("http://%s:%s/get", TestServerHost, TestServerPort);
		Response response = httpClient.send(Request.url(url));
		assertThat(response.status(), is(HttpURLConnection.HTTP_OK));
		assertThat(response.body(), is(notNullValue()));
		assertThat(response.body().length, is(0));
	}

	@Test
	public void shouldHandle302ResponseFromTarget() {
		String url = String.format("http://%s:%s/302", TestServerHost, TestServerPort);
		Response response = httpClient.send(Request.url(url));
		assertThat(response.status(), is(HttpURLConnection.HTTP_MOVED_TEMP));
		assertThat(response.body(), is(notNullValue()));
		assertThat(response.body().length, is(0));
	}
}
