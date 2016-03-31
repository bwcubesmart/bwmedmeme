package parsing;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Author {

	public ArrayList<String> authorAffiliationIDs;
	public String mention;
	public String authorGivenName;
	public String authorLastName;
	
	public Author(String mention, String authorGivenName, String authorLastName) {
		this.mention=mention;
		this.authorGivenName=authorGivenName;
		this.authorLastName=authorLastName;
	}

	public Author() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Author a = new Author();
		a.createAuthors("Jain, D.(1);Shrivastava, P.(1);Vadnerkar, G.(1);Jain, S.(2)");
	}
	
	//examples
	//1. Jain, D.(1);Shrivastava, P.(1);Vadnerkar, G.(1);Jain, S.(2)
	//2. Jatto, A.;Elkordy, A. A.
	//3. Sachmechi, Issac, MD, FACP, FACE;Parikh, Grishma, MD;Reich, David, MD, FACE;Sebastian, Shirly, NP;Arena, Richard, PhD;Payne, Hildegarde, RN, CDE;Kim, Paul, MD
	//4. VV. Ramadan, M. Petitjean, N. Loos, S. Delanaud, G. Vardon, A. Geloen, F. Gros, G. Dewasmes
	//5. Yagi, Takahito;Iwamoto, Takayuki;Yoshida, Ryuichi;Sato, Daisuke;Umeda, Yuzo;Shinoura, Susumu;Matsuda, Hiroaki;Matsukawa, Hiriyoshi;Sadamori, Hiroshi;Yamada, Gotarou
	//6. DA SILVA LIMA, NATALIA;Franco, JG;Oliveira, E;Santana, AC;Nascimento-Saba, CCA;Moura, EG;Lisboa, PC
	//7. McAllister, Thomas W.;Flashman, Laura A.;Shaw, Patricia K.;Ferrell, Richard B.;Wishart, Heather A.;McDonald, Brenna C.;Mamourian, Alexander C.;Ramirez, Jennifer S.;Saykin, Andrew J.
	//8. Dy Uy, Jean, MD;Sero Gomez, Maria Honolina, MD, FPCP, FPSEM,
	//9. Kar??da?, Ça?atay;Buket Tomruk, Nesrin;Alpay, Nihat
	//10. REES, H(1);HATHAWAY, M(2);LUCAS, G(3);WITCHER, J(4);MASSEY, E(4);GREEN, A(1)
	//11. RUSLI, M;ADI, S;TJOKROPRAWIRO, A;SOETJAHJO, A;WIBISONO, S;MURTIWI, S
	//12. Reid, J.(1)(2);Lawrence, J.M.(1)(3);Taylor, G.J.(1);Stirling, C.A.(2);Reckless, J.P.D.(1)(2)
	//13. 2 3 P. C. Deedwania', H. Krimi , E. Horton , E. PechW, L. S. Khemlanl', R. E. Pratley',
	//14. 2 2 2 G. Roman', M. Opread, 1. Groza , A. GaleanU , C. Bala , G. Guseta,
	//15. T. P. Ciaraldi 1,2, V. Aroda 1,2, A. Chang' 2, M. ChandraW,2, S. Baxi 2 , S. R. Mudaliar 1,2 , R. J. Chan@, R. R. Henry 1,2
	//16. 2 3 3 R. R. Holm an', S. E. Kahn, M. A. HeiSe , L. E. Porter3, M. 1. Freed and the ADOPT Study Group,
	//17. J. Wainstein', M. Shargorodsk y2 , E. Leibovitz3, D. Gavisw, Z. MataS4 , R. Zimlichmad,
	//18. Perdikis, DA(4)(1)(2)(3);Siddique, MI(1)(3);Bowers-Pepe, J(4)(3)
	//19. Chu, P.
	//20. Nussmeier, Nancy A.
	//21. Anca, Diana
	//22. Rocha, Ricardo, MD
	//23. Bath, Philip(2)(1)
	//24. De Guzman, Arnold
	//25. Qing Wang
	//26. Khoo Kah Lin, MD, FRCP,FACC
	//27. MOUHAMAD EL HAYEK
	//28. Kukreja, Rakesh C., Ph.D.
	
	
	Pattern p0 = Pattern.compile("([^,]+),\\s([^,]+?)((\\(\\d+\\))+)"); //covers examples 1, 10, 12, 18
	
	//Pattern p1 = Pattern.compile("([^,]+),\\s([^,]+)\\((\\d+)\\)"); //covers examples 1, 10
	
	Pattern p2 = Pattern.compile("([^,]+),\\s([^,]+)"); //covers examples 2, 3, 5, 6, 7, 8, 9, 11
	
	public ArrayList<Author> createAuthors(String str){
		int count=0;
		ArrayList<Author> authors = new ArrayList<Author>();
		
		if(str.contains(";")){
			String[] splits = str.split(";");
			for (String split : splits) {
				Matcher m = p0.matcher(split);
				if(m.find()){
					System.out.println(m.group()+"\t"+ ++count+"\t"+m.group(2)+"\t"+m.group(1)+"\t"+m.group(3));
					Author a = new Author(m.group(),m.group(2),m.group(1));
					a.authorAffiliationIDs=new ArrayList<String>(); a.authorAffiliationIDs.add(m.group(3));//TODO: convert (1)(2)(3) to arraylist
					authors.add(a);
				}
				
				else{
					m = p2.matcher(split);
					if(m.find()){
						System.out.println(++count+"\t"+m.group(2)+"\t"+m.group(1));
						Author a = new Author(m.group(),m.group(2),m.group(1));
						authors.add(a);
					}
				}
			}
		}
		
		
		return authors;
	}

}
