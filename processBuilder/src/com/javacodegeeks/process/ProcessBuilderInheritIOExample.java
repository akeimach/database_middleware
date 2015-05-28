package com.javacodegeeks.process;

import java.io.IOException;

public class ProcessBuilderInheritIOExample {
	public static void main(String[] args) throws InterruptedException,
			IOException {
		ProcessBuilder pb = new ProcessBuilder("echo", "Hello JCG\nThis is ProcessBuilder Example");
		//inherit IO
		pb.inheritIO();
		System.out.println("Run Echo command with inheritIO set");
		Process process = pb.start();
		process.waitFor();
	}
}
