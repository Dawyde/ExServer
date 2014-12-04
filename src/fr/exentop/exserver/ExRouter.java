package fr.exentop.exserver;

import java.util.ArrayList;

import fr.exentop.exserver.actions.ExAction;
import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.requests.HTTPRequest;

public class ExRouter {
	
	private class Entry{
		
		private String[] mPath;
		private int mParameters = 0;
		private ExAction mAction;
		
		public Entry(String route, ExAction action){
			//On traite la route pour qu'elle soit traitée plus vite au moment du match
			String str = "";
			mAction = action;
			ArrayList<String> path = new ArrayList<String>();
			for(int i=0;i<route.length();i++){
				char c = route.charAt(i);
				if(c == '*' && !str.isEmpty()){
					path.add(str);
					str = "";
					mParameters++;
				}
				else{
					str += c;
				}
			}
			if(!str.isEmpty()) path.add(str);
			mPath = new String[path.size()];
			for(int i=0;i<path.size();i++) mPath[i] = path.get(i);
			
		
		}
		
		public boolean match(HTTPRequest request, String route) throws ExConnectionClosed{
			int path = 0;
			int parameters = 0;
			int mode = 0;
			StringBuffer tmp = new StringBuffer();
			char stopchar = '0';
			int j=0;
			boolean match = true;
			boolean endpath = false;
			String[] values = null;
			for(int i=0;i<route.length();i++){
				char c = route.charAt(i);
				//if(mode == 0 && 
				if(mode == 1 && c == stopchar){
					values[parameters] = tmp.toString();
					tmp.setLength(0);
					mode = 0;
					j=0;
					parameters++;
					if(path >= mPath.length){
						endpath = true;
						break;
					}
				}
				
				
				if(mode == 0){
					char c2 = mPath[path].charAt(j);
					if(c == c2){
						j++;
						if(j >= mPath[path].length()){
							if(parameters >= mParameters){
								endpath = i==route.length()-1;
								break;
							}
							path++;
							j=0;
							mode = 1;
							if(parameters == 0) values = new String[mParameters];
							if(path >= mPath.length){
								values[parameters] = route.substring(i+1);
								endpath = true;
								break;
							}
							else{
								stopchar = mPath[path].charAt(0);
							}
						}
					}
					else{
						match = false;
						break;
					}
				}
				else{
					
					tmp.append(c);
				}
			}
			if(!endpath) match = false;
			if(match){
				mAction.runAction(request, values);
				return true;
			}
			else return false;
		}
		
	}
	
	ArrayList<Entry> mRoutes = new ArrayList<Entry>();
	
	public void addRoute(String route, ExAction action){
		mRoutes.add(new Entry(route, action));	
	}
	
	public boolean handleRequest(HTTPRequest request) throws ExConnectionClosed{
		for(Entry e : mRoutes){
			if(e.match(request, request.getURI())) return true;
		}
		return false;
	}
}
