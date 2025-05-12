package com.cybersigma.sigmaverify.utils;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestTempController {

	@GetMapping("/testemail")
	public String emailTemp(Model model) {
		model.addAttribute("message", "Mayank Jyoti Verma");
		return "email-template";
	}
}
