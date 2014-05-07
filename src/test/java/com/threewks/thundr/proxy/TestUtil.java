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

import java.util.Map;

import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;

public class TestUtil {
	public static MockHttpServletRequest newMockHttpServletRequest() {
		return new MockHttpServletRequest() {
			/*
			 * There is a bug in the base class, this overrides the behaviour to fix it.
			 * 
			 * @see com.threewks.thundr.test.mock.servlet.MockHttpServletRequest#header(java.util.Map)
			 */
			@Override
			public MockHttpServletRequest header(Map<String, String[]> headers) {
				for (Map.Entry<String, String[]> header : headers.entrySet()) {
					header(header.getKey(), header.getValue());
				}
				return this;
			}
		};
	}
}
