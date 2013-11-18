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

import java.net.MalformedURLException;
import java.net.URL;

public class SimpleProxyRule extends BaseProxyRule {
	private final String path;
	private final String target;

	public SimpleProxyRule(String path, String target) {
		this.path = path;
		this.target = target;
	}

	@Override
	public boolean appliesTo(String url) {
		String path = toURL(url).getPath();
		return path.startsWith(this.path);
	}

	@Override
	public String transform(String url) {
		URL parsedUrl = toURL(url);
		String path = parsedUrl.getPath();
		String queryString = parsedUrl.getQuery();

		StringBuffer sb = new StringBuffer()
				.append(target)
				.append(path.replace(this.path, ""));
		if (queryString != null && queryString.length() > 0) {
			sb = sb.append("?").append(queryString);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return String.format("SimpleProxyRule{ '%s' -> '%s' }", path, target);
	}

	private URL toURL(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
