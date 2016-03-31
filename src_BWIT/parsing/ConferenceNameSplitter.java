package parsing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.DetailedNLPer;

public class ConferenceNameSplitter {

	public static void main(String[] args) throws IOException {
		DetailedNLPer nlp = new DetailedNLPer();
		Dictionaries dict = new Dictionaries();

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
				//"DistinctMeetingNamesAll_43071.txt"
				"PeteJointMeetingNames.txt"
				), "UTF-8"));int meetingColumn=0;
		
		while(br.ready()){
			String meetingString = br.readLine();
			Meeting aMeeting = new Meeting(meetingString,nlp,new ConferenceNameSplitter(), dict);
			aMeeting.print();
			System.out.println("");
			//ArrayList<String> confs=new ConferenceNameSplitter().splitConfs(aMeeting.normalizedConfName);
			if(aMeeting.conferenceNames.size()>2){
				System.err.println();
			}
		}
		br.close();
		
		Meeting aMeeting = new Meeting("3rd International Conference on Chemical , Biological and Environmental Engineering (ICECS) held jointly with 4th International Conference on Environmental and Computer Science (ICECS) and the International Conference on Biotechnology and Environment Management (ICBEM)",nlp,new ConferenceNameSplitter(), dict);
		aMeeting.print();
		System.out.println("");
		
	}

	/*Pattern p1=Pattern.compile("(.*Kongress.*) und (Jahrestagung.*)");
	Pattern p2=Pattern.compile("(.*Meeting.*) and (Course.*)");
	Pattern p3=Pattern.compile("(.*Meeting.*) and (CME.*)");
	Pattern p4=Pattern.compile("(.*Meeting.*) and (Congress.*)");
	Pattern p5=Pattern.compile("(.*Meeting.*) and (Clinical Congress.*)");*/
	
	List<Pattern> patterns= Arrays.asList(new Pattern[]{Pattern.compile("(.*Kongress.*) und (Jahrestagung.*)")
			, Pattern.compile("(.*Meeting.*) and (Course.*)")
			, Pattern.compile("(.*Meeting.*) and (CME.*)")
			, Pattern.compile("(.*Meeting.*) and (Congress.*)")
			, Pattern.compile("(.*Meeting.*) and (Clinical Congress.*)")
			, Pattern.compile("(.*Meeting.*) and (Clinical Congress.*)")
			, Pattern.compile("(.*Meeting.*) and (Clinical Congress.*)")
			, Pattern.compile("(.*Meeting) and (.*Meeting.*)")
			, Pattern.compile("(.*Meeting.*) and (Meeting.*)")
			, Pattern.compile("(.*Congress) and (.*Meeting.*)")
			, Pattern.compile("(.*Congress.*) and ((Quadrennial )?Meeting.*)")
			, Pattern.compile("(.*Congress.*) and (Congress.*)")
			, Pattern.compile("(.*Congress.*)- (Congress.*)")
			, Pattern.compile("(.*Congress.*)- (Joint Congress.*)")
			, Pattern.compile("(.*Congress.*): (Congress.*)")
			, Pattern.compile("(.*Congress.*), (Congress.*)")
			, Pattern.compile("(.*Meeting.*)/ (Meeting.*)")
			, Pattern.compile("(.*Meeting) / (.*Meeting.*)")
			
			,Pattern.compile("(.*) together with (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) will be celebrated along with (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) held jointly with (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) held joitnly with (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) held joinly with (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) held in conjunction with (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) in conjunction with (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) jointly held with (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) jointly with (.*)", Pattern.CASE_INSENSITIVE)
//			,Pattern.compile("(.*) incorporating (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) / (gemeinsame .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) / (annual .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) / (meeting .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) / (congress .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) / (kongress .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) / (wissenschaftliche .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) / (jahrestagung .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) / (jahreskonferenz.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) / (jahresversammlung.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*\\)) / (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) and (annual .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) and (biennial .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) and (meeting .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) with (meeting .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) and (conference .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) and (international .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) and (national .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) and (european .*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*) together with (.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*,) (meeting.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*,) (annual.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*-) (annual.*)", Pattern.CASE_INSENSITIVE)
			,Pattern.compile("(.*); (sponsored.*)", Pattern.CASE_INSENSITIVE)
			
	});
	
	ArrayList<String> splitConfs(String confName) {
		ArrayList<String> ret = new ArrayList<String>();
		
		ArrayList<String> breakPatterns = findBreakPatterns(confName);
		if(breakPatterns.size()==0){
			ret.add(confName);
			return ret;
		}
		
		for (Pattern pattern : this.patterns) {
			Matcher m=pattern.matcher(confName);
			if(m.find()){
				if(m.group(1).split("\\s+").length<3 || m.group(2).split("\\s+").length<3)
					continue;
				ret.addAll(splitConfs(m.group(1).trim()));
				ret.addAll(splitConfs(m.group(2).trim()));
				//System.out.println(ret);
				return ret;
			}
		}
		
		ret.add(confName);
		return ret;
	}

	HashSet<String> jointsocKeywords = new HashSet<String>(Arrays.asList(new String[]{"together with","will be celebrated along with","held jointly with","held joitnly with","held joinlty with","held joinly with","held in conjunction with", "in conjunction with", "jointly held with", "jointly with", //"incorporating", 
			"/ gemeinsame","/ annual","/ meeting", 
			//"/ soc", 
			"/ congress", "/ kongress","/ wissenschaftliche", "/ jahrestagung", "/ jahreskonferenz", "/ jahresversammlung", ") /", "and annual", "and biennial", "and meeting", "and conference", 
			//"and soc meeting",  "and soc annual",  
			"and international","and national","and european", "together with", ", meeting",", annual", "- annual", "; sponsored", "with meeting"
			//, "joint meeting"
			}));
	
	private ArrayList<String> findBreakPatterns(String conf) {
		ArrayList<String> breakPatterns = new ArrayList<String>();
		
		if(conf.matches(".*Kongress.* und Jahrestagung.*")) 
			breakPatterns.add(".*Kongress.* und Jahrestagung.*");
		else if(conf.matches(".*Meeting.* and Course.*")) 
			breakPatterns.add(".*Meeting.* and Course.*");
		else if(conf.matches(".*Meeting.* and CME.*")) 
			breakPatterns.add(".*Meeting.* and CME.*");
		else if(conf.matches(".*Meeting.* and Congress.*")) 
			breakPatterns.add(".*Meeting.* and Congress.*");
		else if(conf.matches(".*Meeting.* and Clinical Congress.*")) 
			breakPatterns.add(".*Meeting.* and Clinical Congress.*");
		else if(conf.matches(".*Meeting and .*Meeting.*")) 
			breakPatterns.add(".*Meeting and .*Meeting.*");
		else if(conf.matches(".*Meeting.* and Meeting.*")) 
			breakPatterns.add(".*Meeting.* and Meeting.*");
		else if(conf.matches(".*Congress and .*Meeting.*")) 
			breakPatterns.add(".*Congress and .*Meeting.*");
		else if(conf.matches(".*Congress.* and (Quadrennial )?Meeting.*")) 
			breakPatterns.add(".*Congress.* and (Quadrennial )?Meeting.*");
		else if(conf.matches(".*Congress.* and Congress.*")) 
			breakPatterns.add(".*Congress.* and Congress.*");
		else if(conf.matches(".*Congress.*- Congress.*")) 
			breakPatterns.add(".*Congress.*- Congress.*");
		else if(conf.matches(".*Congress.*- Joint Congress.*")) 
			breakPatterns.add(".*Congress.*- Joint Congress.*");
		else if(conf.matches(".*Congress.*: Congress.*")) 
			breakPatterns.add(".*Congress.*: Congress.*");
		else if(conf.matches(".*Congress.*, Congress.*")) 
			breakPatterns.add(".*Congress.*, Congress.*");
		else if(conf.matches(".*Meeting.*/ Meeting.*")) 
			breakPatterns.add(".*Meeting.*/ Meeting.*");
		else if(conf.matches(".*Meeting / .*Meeting.*")) 
			breakPatterns.add(".*Meeting / .*Meeting.*");
		
		else{
			for (String key : jointsocKeywords) {
				if(conf.toLowerCase().contains(key))
					breakPatterns.add(key);
			}}
		
		return breakPatterns;
	}

}
