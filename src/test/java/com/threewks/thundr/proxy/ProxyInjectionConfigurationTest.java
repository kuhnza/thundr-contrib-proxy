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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.action.ActionResolver;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.proxy.action.ProxyAction;
import com.threewks.thundr.proxy.action.ProxyActionResolver;
import com.threewks.thundr.route.Routes;

public class ProxyInjectionConfigurationTest {

	private ProxyModule injectionConfiguration = new ProxyModule();
	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();
	private Routes routes = new Routes();

	@Before
	public void before() {
		injectionContext.inject(routes).as(Routes.class);
	}

	@Test
	public void shouldInjectReverseProxyActionResolver() {
		injectionConfiguration.configure(injectionContext);

		ProxyActionResolver resolver = injectionContext.get(ProxyActionResolver.class);
		assertThat(resolver, is(notNullValue()));
	}

	@Test
	public void shouldAddActionResolverToRoutes() {
		injectionConfiguration.configure(injectionContext);
		injectionConfiguration.configure(injectionContext);

		ProxyActionResolver resolver = injectionContext.get(ProxyActionResolver.class);
		ActionResolver<ProxyAction> resolverInterface = resolver;
		assertThat(routes.getActionResolver(ProxyAction.class), is(resolverInterface));
	}
}
