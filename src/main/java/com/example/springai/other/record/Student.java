package com.example.springai.other.record;

public record Student(Integer id, String name) {
	public static void main(String[] args) {
		Student student = new Student(1, "John Doe");
		System.out.println(student.id());
		System.out.println(student.name());
	}
}
