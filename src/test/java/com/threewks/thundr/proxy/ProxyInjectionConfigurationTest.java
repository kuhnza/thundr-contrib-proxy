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

import com.threewks.thundr.action.Action;
import com.threewks.thundr.action.ActionResolver;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.proxy.action.ProxyActionResolver;
import com.threewks.thundr.route.Routes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ProxyInjectionConfigurationTest {

	private ProxyInjectionConfiguration injectionConfiguration;
	private UpdatableInjectionContext injectionContext;
	private Routes routes;

	@Before
	public void before() {
		routes = mock(Routes.class);

		injectionContext = new InjectionContextImpl();
		injectionContext.inject(routes).as(Routes.class);

		injectionConfiguration = new ProxyInjectionConfiguration();
		injectionConfiguration.configure(injectionContext);
	}

	@Test
	public void shouldInjectReverseProxyActionResolver() {
		ProxyActionResolver resolver = injectionContext.get(ProxyActionResolver.class);
		assertThat(resolver, is(notNullValue()));
	}

	@Test
	public void shouldAddActionResolverToRoutes() {
		ArgumentCaptor<ActionResolver<Action>> resolverCaptor = (ArgumentCaptor<ActionResolver<Action>>) (Object) ArgumentCaptor.forClass(ActionResolver.class);
		verify(routes, times(1)).addActionResolver(any(Class.class), resolverCaptor.capture());

		ActionResolver resolver = injectionContext.get(ProxyActionResolver.class);
		assertThat(resolverCaptor.getValue(), is(resolver));
	}
}
