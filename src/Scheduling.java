import java.io.FileReader;
import java.util.Scanner;

public class Scheduling {
/*
 * Scheduling Java
 * 
 * parameters on eclipse:
 * copy paste these parameters in to make your life easier!
 ************************************************
 * "Data_JobTimes.txt" "Data_DependencyPairs.txt"
 ************************************************
 *
 * @author Steve Jun
*/

public static class jobNode{
	int id;
	jobNode next;
	
	jobNode(){			id=-1;		next=null;}
	jobNode(int i){		id=i;		next=null;}
}
//*******************************
public static void main(String[] args){
	
	if (args.length==0) System.out.println("No file specified.");
	
	try{ 
		
		//step 0: prepare and initialize everything
		Scanner jobTimeFile;
		Scanner jobDependFile;
		
		jobNode openList = new jobNode();
		jobNode job;
		jobNode pointer;
		jobNode LH;
		
		int[][] table;//schedule table
		jobNode[] dependencyGraph;
		int[] processJob;	//current job on process;
		int[] processTime;	//less than or equal to 0 means available;
		int[] parentCount;	//tracks num of parents for job; -1 deleted
		int[] jobTime;		//job"s time requirement;
		int[] jobStatus;	//jobDone; 1 deleted from graph; 0 still in graph; -1 processing
		
	
		jobTimeFile = new Scanner(new FileReader(args[0]));
		jobDependFile = new Scanner(new FileReader(args[1]));
		
		System.out.println("Reading file 1");
		
		int numOfJobs=jobTimeFile.nextInt();
		int maxTime=0;
		jobTime =new int[(numOfJobs+1)];
		while(jobTimeFile.hasNextInt()){
			int j=jobTimeFile.nextInt();
			int t=jobTimeFile.nextInt();
			jobTime[j]=t;
			maxTime+=t;
			System.out.println("job: "+j+" time: "+t);
		}
		
		//table[numOfJobs][maxTime] //worse case
		table=new int[(numOfJobs+1)][(maxTime+1)];
		
		for(int p=1; p<=numOfJobs; ++p)
			for(int t=0; t<=maxTime; ++t)
				table[p][t]=0;	
		
		dependencyGraph=new jobNode[(numOfJobs+1)];
		parentCount=new int[(numOfJobs+1)];
		jobStatus=new int[(numOfJobs+1)];
		processJob=new int[(numOfJobs+1)];
		processTime=new int[(numOfJobs+1)];
		for(int i=0; i<=numOfJobs; ++i){
			parentCount[i]=0;
			dependencyGraph[i]=new jobNode();
			jobStatus[i]=0;
			processJob[i]=0;
			processTime[i]=0;
		}
		
		System.out.println("\n"+"Reading file 2");
		jobDependFile.nextInt();
		while(jobDependFile.hasNextInt()){
			int j=jobDependFile.nextInt();
			int d=jobDependFile.nextInt();
			job=new jobNode(j);
			pointer=dependencyGraph[d];
			while(pointer.next!=null)
				pointer=pointer.next;
			pointer.next=job;
			++parentCount[d];
			System.out.println("job: "+d+" depends on job: "+j);
		}
	//*******************************
		int time = 0;
		boolean graphNotEmpty = true;
		while(graphNotEmpty){
			openList = new jobNode();
			LH = openList;
			graphNotEmpty=false;
		/*
		step 1: find jobs that do not have any parent 
			(ie., check parentCount[i] == 0)
			and place them, one-by-one, onto OPEN list
		*/
			for(int i=1; i<=numOfJobs; ++i){
				if (parentCount[i]==0 && jobStatus[i]==0){
					job=new jobNode(i);
					LH.next=job;
					LH=LH.next;
					parentCount[i]=-1;
					jobStatus[i]=-1;
				}
			}
			LH=openList;
		/*
		step 2: 2.1: newJob <-- remove from OPEN 
		        2.2: availProc <-- the next available processTime (looking
			     into processjob[i] <= 0)
		        2.3: place newJob on the processJob[availProc], 
			     place newJob"s time on processTime[availProc]
			     update the scheduling table under availProc,
				(with respect to TIME status and job"s time requiement).
		        2.4: repeat 2.1 and 2.3 until OPEN is empty
		*/
			int newJob;
			int availProc=0;
			while(LH.next!=null){
				//2.1
				newJob=LH.next.id;
				//2.2
				boolean foundProc=false;
				for(int i=1; i<=numOfJobs; ++i){
					if (processTime[i]<=0){
						availProc=i;
						foundProc=true;
					}
					if (foundProc) break;
				}
				//2.3
				processJob[availProc]=newJob;
				processTime[availProc]=jobTime[newJob];
				jobStatus[newJob]=-1;
				LH=LH.next;
			}//2.4 repeat	
		//update scheduling table
			for(int np=1; np<=numOfJobs; ++np)
				for(int mt=0; mt<=maxTime; ++mt)
					table[np][time]=processJob[np];
	
		//step 3: print the scheduling table, TIME, all 1-D arrays with proper heading.
			System.out.println("\n"+"At start of Time: "+time);
			
			System.out.println("|Processor|"+"\t"+"processJob"+"\t"+"processTime"+"\t"
					+"|Job|"+"\t"+"parentCount"+"\t"+"jobTime"+"\t"+"\t"+"jobStatus");
				for (int i=1; i<numOfJobs; ++i){
						System.out.println(" "+"|"+i+"|"+"\t"+"\t"
						+processJob[i]+"\t"+"\t"
						+processTime[i]+"\t"+"\t"
						+" "+"|"+i+"|"+"\t"
						+parentCount[i]+"\t"+"\t"
						+jobTime[i]+"\t"+"\t"
						+jobStatus[i]);
				}
				System.out.println("\n"+"Scheduling Table: ");
				for(int p=1; p<=numOfJobs; ++p){
					for(int mt=0; mt<=maxTime; ++mt){
						if (table[p][mt]==0)	System.out.print("  ");
						else {
							if (table[p][mt]<10)
								System.out.print(" "+table[p][mt]);
							else System.out.print(table[p][mt]);
						}
						System.out.print(" ");
					}
					System.out.println();
				}
				
		//step 4: track the TIME (ie, decrease the processTime[i] by 1 and Time++)
			++time;
			for(int i=1; i<=numOfJobs; ++i){
					if (processTime[i]>0) 
						--processTime[i];
			}
		/*
		step 5: job <-- find a job that is done, ie., processTIME [i] == 0 ;
		        5.1: delete the job from the processJob[i]
		        5.2: delete the job from the graph (update jobDone[job])
		        5.3: delete all it"s outgoing arcs (decrease by 1, the paraentCount[job] of its dependents)
				5.4: jobDone[job] <-- 1
				5.5: repeat 5.1 to 5.4 until no more finished job
		*/
			for(int i=1; i<=numOfJobs; ++i){
				//job is done if
				//time on proc==0 && there exists a job on proc && jobStatus: isProcessing (-1)
				int jobID=processJob[i];
				if (processTime[i]==0 && processJob[i]!=0 && jobStatus[jobID]==-1){
					//5.1
					processJob[i]=0;
					//5.2
					jobStatus[jobID]=1;
					//5.3
					for(int k=1; k<=numOfJobs; ++k){
						pointer=dependencyGraph[k];
						while(pointer.next!=null){
							if (pointer.next.id==jobID){
								//remove dependency on hash table
								if (pointer.next.next!=null)
									pointer.next=pointer.next.next;
								--parentCount[k];		
							}
							pointer=pointer.next;	
						}
					}
				}
			}//5.5 repeat
		//step 6: print the scheduling table, TIME, all 1-D arrays with proper heading.
			System.out.println("\n"+"After 1 time, time is now: "+time);
			
			System.out.println("|Processor|"+"\t"+"processJob"+"\t"+"processTime"+"\t"
					+"|Job|"+"\t"+"parentCount"+"\t"+"jobTime"+"\t"+"\t"+"jobStatus");
				for (int i=1; i<numOfJobs; ++i){
						System.out.println(" "+"|"+i+"|"+"\t"+"\t"
						+processJob[i]+"\t"+"\t"
						+processTime[i]+"\t"+"\t"
						+" "+"|"+i+"|"+"\t"
						+parentCount[i]+"\t"+"\t"
						+jobTime[i]+"\t"+"\t"
						+jobStatus[i]);
				}
				System.out.println("\n"+"Scheduling Table: ");
				for(int p=1; p<=numOfJobs; ++p){
					for(int mt=0; mt<=maxTime; ++mt){
						if (table[p][mt]==0)	System.out.print("  ");
						else {
							if (table[p][mt]<10)
								System.out.print(" "+table[p][mt]);
							else System.out.print(table[p][mt]);
						}
						System.out.print(" ");
					}
					System.out.println();
				}
			
			for (int i=1; i<=numOfJobs; ++i)
				if (jobStatus[i]!=1) graphNotEmpty=true;
	}
	//step 7: repeat step 1 to step 6 until graph is empty (looking into the 1-D array of jobs"status)
		
		System.out.println("\n"+"Total number of jobs: "+numOfJobs);
		System.out.println("Total time taken to finish: "+time);
		/***END***/
		jobTimeFile.close();
		jobDependFile.close();
		System.out.println("Assignment Complete!");
	}catch (Exception e){System.out.println("We have a problem.");}
}
}

