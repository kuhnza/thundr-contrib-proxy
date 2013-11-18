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

import com.google.common.collect.Maps;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;

import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import static com.atomicleopard.expressive.Expressive.list;

public class TestUtil {
	public static MockHttpServletRequest newMockHttpServletRequest() {
		return new MockHttpServletRequest() {
			private URL url;
			private String content;
			private Map<String, String[]> headers = Maps.newHashMap();

			@Override
			public MockHttpServletRequest url(String url) {
				try {
					this.url = new URL(url);
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
				return this;
			}

			@Override
			public StringBuffer getRequestURL() {
				String url = this.url.toString();
				int index = url.indexOf('?');
				if (index != -1) {
					url = url.substring(0, index);
				}
				return new StringBuffer(url);
			}

			@Override
			public String getRequestURI() {
				return url.getPath();
			}

			@Override
			public String getPathInfo() {
				return url.getPath();
			}

			@Override
			public String getPathTranslated() {
				return url.getPath();
			}

			@Override
			public String getQueryString() {
				return url.getQuery();
			}

			@Override
			public MockHttpServletRequest header(String name, String value) {
				headers.put(name, new String[] { value });
				return this;
			}

			@Override
			public MockHttpServletRequest header(String name, String... values) {
				headers.put(name, values);
				return this;
			}

			@Override
			public MockHttpServletRequest header(Map<String, String[]> headers) {
				this.headers.putAll(headers);
				return this;
			}

			@Override
			public String getHeader(String name) {
				String[] values = headers.get(name);
				return values == null ? null : values[0];
			}

			@Override
			public Enumeration getHeaders(String name) {
				return Collections.enumeration(list(headers.get(name)));
			}

			@Override
			public Enumeration getHeaderNames() {
				return Collections.enumeration(headers.keySet());
			}

			@Override
			public MockHttpServletRequest content(String content) {
				this.content = content;
				return this;
			}

			@Override
			public ServletInputStream getInputStream() throws IOException {
				String body = (content == null) ? "" : content;
				final ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(this.getCharacterEncoding()));
				return new ServletInputStream() {
					@Override
					public int read() throws IOException {
						return inputStream.read();
					}
				};
			}
		};
	}
}
