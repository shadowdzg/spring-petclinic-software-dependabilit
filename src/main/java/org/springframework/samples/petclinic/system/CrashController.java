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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller used to showcase what happens when an exception is thrown
 *
 * @author Michael Isvy
 * <p/>
 * Also see how a view that resolves to "error" has been added ("error.html").
 */
@Controller
class CrashController {

	@GetMapping("/oups")
	public String triggerException() {
		throw new RuntimeException(
				"Expected: controller used to showcase what " + "happens when an exception is thrown");
	}

	/**
	 * SECURE ENDPOINT - Validated Redirects (OWASP A10 Fixed)
	 *
	 * This endpoint now validates redirect URLs against an allowlist to prevent phishing.
	 * @param url user-provided URL to redirect to
	 * @return redirect view or error if URL is not allowed
	 */
	@GetMapping("/redirect")
	public RedirectView vulnerableRedirect(@RequestParam("url") String url) {
		// SECURE: Validate redirect URL against allowlist
		if (isAllowedRedirectUrl(url)) {
			return new RedirectView(url);
		}
		// Default to safe location if URL is not allowed
		return new RedirectView("/");
	}

	private boolean isAllowedRedirectUrl(String url) {
		// Only allow relative URLs or URLs from trusted domains
		return url.startsWith("/") || url.startsWith("./")
				|| url.matches("^https?://(localhost|127\\.0\\.0\\.1)(:[0-9]+)?/.*$");
	}

	/**
	 * VULNERABLE ENDPOINT - Sensitive Data Exposure (OWASP A6)
	 *
	 * This endpoint exposes sensitive information in error messages.
	 * @param ownerId owner ID to look up
	 * @param model Spring model
	 * @return error view with sensitive information
	 */
	@GetMapping("/vulnerable/error")
	public String vulnerableError(@RequestParam(required = false) Integer ownerId, Model model) {
		try {
			// Simulate database error
			if (ownerId == null) {
				throw new RuntimeException("Database connection failed");
			}
		}
		catch (Exception e) {
			// SECURE: Generic error message without sensitive information
			model.addAttribute("error", "An error occurred. Please contact support.");
			// Do not expose stack traces, database URLs, or credentials
		}
		return "error";
	}

	/**
	 * VULNERABLE ENDPOINT - Missing Function Level Access Control (OWASP A7)
	 *
	 * This endpoint should require admin privileges but doesn't check authorization.
	 * @param model Spring model
	 * @return admin panel view
	 */
	@GetMapping("/admin/panel")
	public String adminPanel(Model model) {
		// VULNERABLE: No authorization check - anyone can access admin panel
		model.addAttribute("message", "Welcome to Admin Panel");
		model.addAttribute("users", "All users data here...");
		model.addAttribute("sensitiveData", "Database credentials, API keys, etc.");
		return "error"; // Using error template for simplicity
	}

	/**
	 * VULNERABLE ENDPOINT - Missing Function Level Access Control (OWASP A7)
	 *
	 * This endpoint allows deletion without proper authorization.
	 * @param ownerId owner ID to delete
	 * @param redirectAttributes redirect attributes
	 * @return redirect view
	 */
	@GetMapping("/admin/delete")
	public String adminDelete(@RequestParam("ownerId") Integer ownerId, RedirectAttributes redirectAttributes) {
		// VULNERABLE: No authorization check - anyone can delete owners
		// In real application, this should check if user is admin
		redirectAttributes.addFlashAttribute("message", "Owner " + ownerId + " deleted (vulnerable endpoint)");
		return "redirect:/owners";
	}

}
