package de.featjar.feature.model.transformer;

public class CardinalityObject {

	String name;
	int number;
	
	public CardinalityObject(String name, int number) {
		
		this.name = name;
		this.number = number;
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumber() {
		return number;
	}
}
