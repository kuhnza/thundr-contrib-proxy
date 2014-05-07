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

import com.threewks.thundr.injection.InjectionContext;
import com.threewks.thundr.injection.Module;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.proxy.action.ProxyAction;
import com.threewks.thundr.proxy.action.ProxyActionResolver;
import com.threewks.thundr.route.Routes;

public class ProxyModule implements Module {
	@Override
	public void requires(DependencyRegistry dependencyRegistry) {
	}

	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
	}

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		ProxyActionResolver resolver = new ProxyActionResolver(new ProxyImpl());
		injectionContext.inject(resolver).as(ProxyActionResolver.class);

		Routes routes = injectionContext.get(Routes.class);
		routes.addActionResolver(ProxyAction.class, resolver);
	}

	@Override
	public void start(UpdatableInjectionContext injectionContext) {
	}

	@Override
	public void stop(InjectionContext injectionContext) {
	}
}
