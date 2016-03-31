package production;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.Levenstein;
import com.wcohen.ss.SoftTFIDF;
import com.wcohen.ss.api.Tokenizer;
import com.wcohen.ss.tokens.SimpleTokenizer;

import opennlp.DetailedNLPer;

import neobio.alignment.NeoBio;
import normalization.SocietyNormalizer;
import parsing.Affiliation;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;

public class AllAffiliationsCorrector {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HashMap<String, ArrayList<String>> masterNorm2Matches = new HashMap<String, ArrayList<String>>();
		HashMap<String, String> orgNameTable = new HashMap<String, String>();
		HashMap<String, String> orgRowTable = new HashMap<String, String>();
		
		DetailedNLPer nlp = new DetailedNLPer();
		
		BufferedReader br = new BufferedReader(new FileReader("../NLP Process/Organization Master.tsv.txt"));
		
		br.readLine();
		int c=0;
		while(br.ready()){
			String line=br.readLine();
			c++; if(c%1000==0) System.err.println("read orgs---"+c);
			String[] splits=line.split("\t");
			if(!splits[5].equals("Main")) continue;
			
			String org=splits[2];
			String normorg=nlp.removeStopWordsAndStem(org.toLowerCase());
			orgNameTable.put(org, normorg);
			if(!masterNorm2Matches.containsKey(normorg)) masterNorm2Matches.put(normorg, new ArrayList<String>());
			masterNorm2Matches.get(normorg).add(org);
			orgRowTable.put(org, line);
			//masterSocietyNorms.add(normSoc);
		}
		br.close();
		
		//SoftTFIDF sTFIDFdistance = new SoftTFIDF(new SimpleTokenizer(true,true),new JaroWinkler(),0.8);
		//AbstractStringMetric metric = new uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch();
		/*Levenstein ls = new Levenstein();
		ls.main(argv)*/
		
		c=0;long time = System.currentTimeMillis();
		PrintWriter pwr = new PrintWriter("../NLP Process/Organization Master_corrections.tsv");
		for (String org : orgNameTable.keySet()) {
			c++; if(c%1000==0) {
				System.err.println("processed orgs---"+c);
				System.err.println("time taken in ms = "
						+ (System.currentTimeMillis() - time));
			}
			for (String org2 : orgNameTable.keySet()) {
				if(org.compareTo(org2)>=0||orgNameTable.get(org).length()==0||orgNameTable.get(org2).length()==0) continue;
				
				if(orgNameTable.get(org).equals(orgNameTable.get(org2))){
					//System.err.println(orgRowTable.get(org)+"\t|||\t"+orgRowTable.get(org2));
					pwr.println(orgRowTable.get(org)+"\t|||\t"+orgRowTable.get(org2));
				}
				else if(NeoBio.editDistance(orgNameTable.get(org), orgNameTable.get(org2))<=1){
					System.err.println(orgRowTable.get(org)+"\t|||\t"+orgRowTable.get(org2));
				}
				
				//double score = 1-NeoBio.getScore1(soc, soc2);
				/*double score = NeoBio.getNeedlemanwunschScore(orgNameTable.get(org), orgNameTable.get(org2));
				
				if(score>Affiliation.SW_THRESHOLD){
					System.err.println(org+"\t"+org2+"\t"+score);
					//pwr.println(soc+"\t"+soc2+"\t"+score);
				}*/
			}
		}
		
		pwr.close();
	}

	
	
}
