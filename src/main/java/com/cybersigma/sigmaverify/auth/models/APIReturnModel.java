package com.cybersigma.sigmaverify.auth.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Vector;

@Data
@NoArgsConstructor
public class APIReturnModel {
	private String status = "fail";
	private String message = "something went wrong";
	private Vector<?> data;
	private int count = 0;
}
