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

import com.threewks.thundr.proxy.intercept.ProxyInterceptor;
import com.threewks.thundr.proxy.intercept.ProxyInterceptorRegistry;

public interface ProxyRule  {
	/**
	 * Checks if this rule applies to a given URL.
	 *
	 * @param url the url to match
	 * @return true if the rule applies, otherwise false
	 */
	boolean appliesTo(String url);

	/**
	 * Convenience method to add an interceptor to the interceptor registry. Equivalent to
	 * {@link com.threewks.thundr.proxy.intercept.ProxyInterceptorRegistryImpl#register(ProxyInterceptor)}.
	 *
	 * @param interceptor the interceptor to register
	 */
	void addInterceptor(ProxyInterceptor interceptor);

	/**
	 * Returns the {@link com.threewks.thundr.proxy.intercept.ProxyInterceptorRegistryImpl} instance for this rule.
	 *
	 * @return a <code>ProxyInterceptorRegistry</code> instance
	 */
	ProxyInterceptorRegistry getInterceptorRegistry();

	/**
	 * Transforms the given URL to the target URL to proxy to.
	 *
	 * @param url the url to transform.
	 * @return the URL to proxy to
	 */
	String transform(String url);
}
