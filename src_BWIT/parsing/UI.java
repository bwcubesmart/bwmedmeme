package parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class UI {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader("heartFailure2008.txt"));
		br.readLine();//first line 
		while(br.ready()){
			String line = br.readLine();
			String[] splits = line.split("\t");
			System.out.println(splits.toString());
		}
		br.close();

	}
	
	/*
	 * authors - 8 - "Rietz, Anne; Davies, Anthony; Hennessy, Martina; Volkov, Yuri; Spiers, Paul"
	 */

}
