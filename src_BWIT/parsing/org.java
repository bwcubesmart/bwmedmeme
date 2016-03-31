import java.io.*;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap.*;
import java.util.Iterator;
import java.util.TreeMap;
import java.io.IOException;
import org.apache.commons.lang3.RandomStringUtils;

public class org{
public static void loadfile()
{
        try{

                BufferedReader in = new BufferedReader(new FileReader("/home/solr/ConfParser/NLP Process/Organization Master.tsv.txt"));
                BufferedWriter out = new BufferedWriter(new FileWriter("/home/solr/ConfParser/NLP Process/Org_formatted.txt"));
                String line ;
                int count = 0;
             while ((line = in.readLine()) != null) {
		out.write (line);
		out.newLine();
             }

        }
        catch(Exception e){
                System.out.println("load firstname is an issue");
        }
}
 public static void main(String[] args) throws Exception {

	loadfile();

}
}
