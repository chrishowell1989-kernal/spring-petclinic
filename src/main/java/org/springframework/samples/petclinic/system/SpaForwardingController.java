/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards client-side (React Router) routes to {@code index.html} so the SPA can take
 * over. Path segments containing a dot (hashed asset filenames like
 * {@code assets/index-abc123.js}) fall through to normal static-resource handling instead
 * of matching here. {@code /api/**} and {@code /actuator/**} are excluded so a genuine
 * 404 there isn't masked by the SPA shell.
 */
@Controller
class SpaForwardingController {

	@GetMapping({ "/", "/{path:^(?!api|actuator)[^\\.]*$}", "/**/{path:^(?!api|actuator)[^\\.]*$}" })
	public String forward() {
		return "forward:/index.html";
	}

}
