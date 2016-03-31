package parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		
		Dictionaries dictionaries = new Dictionaries();
		Affiliation aff1= new Affiliation("MEMORIAL SLOAN-KETTERING CANCER CENTER, NEW YORK, NY", dictionaries);
		Affiliation aff2= new Affiliation("MEMORIAL SLOAN KETTERING CANCER CENTER, NEW YORK, NY", dictionaries);

		//System.out.println(aff1.equals(aff2));
		System.out.println(aff1.strictEquals(aff2));
	}
	
	/*
	 * authors - 8 - "Rietz, Anne; Davies, Anthony; Hennessy, Martina; Volkov, Yuri; Spiers, Paul"
	 */

}
