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
package com.threewks.thundr.proxy.intercept;

import java.util.List;


public interface ProxyInterceptorRegistry extends ProxyInterceptor {
	/**
	 * Register a proxy interceptor.
	 *
	 * @param interceptor the interceptor to register.
	 */
	void register(ProxyInterceptor interceptor);

	/**
	 * Check if registry contains the given {@link ProxyInterceptor} object.
	 *
	 * @param interceptor the <code>ProxyInterceptor</code> instance to check
	 * @return true if registry contains object, otherwise false
	 */
	boolean contains(ProxyInterceptor interceptor);

	/**
	 * Return a list of registered interceptors.
	 *
	 * @return a list of <code>ProxyInterceptor</code>s
	 */
	List<ProxyInterceptor> list();

	/**
	 * Returns the number of registered {@link ProxyInterceptor}s
	 *
	 * @return the number of registered interceptors
	 */
	int size();
}
