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
package com.threewks.thundr.proxy.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.action.ActionException;
import com.threewks.thundr.action.ActionResolver;
import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.proxy.Proxy;
import com.threewks.thundr.proxy.exception.ProxyResolverException;
import com.threewks.thundr.proxy.rule.ProxyRule;
import com.threewks.thundr.route.RouteType;

public class ProxyActionResolver implements ActionResolver<ProxyAction> {
	public static final String ActionName = "proxy";

	private final Proxy proxy;
	private final List<ProxyRule> rules = new ArrayList<ProxyRule>();

	public ProxyActionResolver(Proxy proxy) {
		this.proxy = proxy;
	}

	public void registerProxyRule(ProxyRule rule) {
		rules.add(rule);
	}

	@Override
	public ProxyAction createActionIfPossible(String actionName) {
		if (ActionName.equalsIgnoreCase(actionName)) {
			return new ProxyAction();
		}
		return null;
	}

	@Override
	public void initialise(ProxyAction action) {
	}

	@Override
	public Object resolve(ProxyAction action, RouteType routeType, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVars) throws ActionException {
		try {
			String url = getFullUrl(req);
			ProxyRule rule = resolveProxyRule(url);
			proxy.proxy(rule, req, resp);
		} catch (Exception e) {
			Throwable original = e.getCause() == null ? e : e.getCause();
			throw new BaseException(original, "Failed to proxy request for %s: %s", req.getRequestURI(), original.getMessage());
		}
		return null;
	}

	private String getFullUrl(HttpServletRequest req) {
		String url = req.getRequestURL().toString();
		String queryString = req.getQueryString();
		if (queryString != null && queryString.length() > 0) {
			url += "?" + queryString;
		}
		return url;
	}

	private ProxyRule resolveProxyRule(String uri) {
		for (ProxyRule rule : rules) {
			if (rule.appliesTo(uri)) {
				return rule;
			}
		}
		throw new ProxyResolverException("No matching rule for URI: %s", uri);
	}
}
