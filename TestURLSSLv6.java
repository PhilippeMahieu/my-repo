/*
	Author: P Mahieu
	Date:	May 2017
	Goal:	Injector multithreads
                This application takes 3 mandatory arguments:
		Argument 1: the hpps url you wanna test
		Argument 2: the number of loop (example 100)
               		Argument 3: the number of threads (example 20)
		Argument 4: debug (Y or N)
*/
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestURLSSLv6
{
 private static int MYTHREADS = 100;
 private static int nbcountOK = 0;
 private static int nbcountKO = 0;
 private static int nbloop = 0;
 private static String vdebug = "N";

 public static void main(String[] args) throws Exception  
    {
      if (args.length < 3) 
      {
               System.err.println("Error while trying to execute the aplication.....");
               System.err.println("This application takes 3 arguments:"+"\n");
               System.err.println("Argument 1: the hpps url you wanna test");
               System.err.println("Argument 2: the number of loop (example 100)");
               System.err.println("Argument 3: the number of threads (example 20)");
               System.err.println("Argument 4 optional : debug mode (should be equal to Y or N)"+"\n");
               System.err.println("Usage is: java TestURLSSLv5 https://support1.XXX.com 150 150");
               System.exit(1);
     }
     else
     {
        try {
              nbloop = Integer.parseInt(args[1]);
            } 
        catch (NumberFormatException e) 
            {
               System.err.println("Error while trying to execute the aplication.....");
               System.err.println("Argument (loop) " + args[1] + " must be an integer.");
               System.exit(1);
            }
        try {
              MYTHREADS = Integer.parseInt(args[2]);
            } 
        catch (NumberFormatException e) 
            {
               System.err.println("Error while trying to execute the aplication.....");
               System.err.println("Argument (threads) " + args[2] + " must be an integer.");
               System.exit(1);
            }
        if (args.length == 4)
            {
              vdebug = args[3];
              if ( !vdebug.equals("Y") && !vdebug.equals("N") )
               {
               System.err.println("Error while trying to execute the aplication.....");
               System.err.println("Argument (debug) " + args[3] + " must be equal to Y or N");
               System.exit(1);
               }
            }
 
      }

      System.err.println("Application running....");
      System.out.print("Number of Threads :\t\t " +MYTHREADS + "\n" );
      System.out.print("Number of loop :\t\t " + nbloop + "\n" );
      System.out.print("Debug mode :\t\t\t " + vdebug + "\n" );

      ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
      int x = 0;

      while( x < nbloop) 
      {
         System.out.print("value of x : " + x );
         x++;
         System.out.print("\r");
         String urls = args[0];
         Runnable worker = new MyRunnable(urls,x);
         executor.execute(worker);
      }   

      executor.shutdown();
      // Wait until all threads are finish
      while (!executor.isTerminated()) 
      {
      }
      System.out.println("\nFinished all threads:\tOK="+nbcountOK +"\t\tKO="+nbcountKO );
    }

    public static class MyRunnable implements Runnable {
        private final String urls;
        private final int xx;
	    long startTime;
        long duration;
 
        MyRunnable(String urls, int xx) {
            this.urls = urls;
            this.xx = xx;
        }
 
        public void run() {
 
            String result = "";
            int code = 200;
            try 
            {
			startTime = System.nanoTime();
			URL hp = new URL(urls);
			if (urls.toUpperCase().indexOf("HTTPS:") == -1)
				{
					HttpURLConnection hpCon = (HttpURLConnection)hp.openConnection();
	                hpCon.setRequestMethod("GET");
                	hpCon.connect();
	                code= hpCon.getResponseCode();
				}
			else
				{
					HttpsURLConnection hpCon = (HttpsURLConnection) hp.openConnection();
	                hpCon.setRequestMethod("GET");
                	hpCon.connect();
	                code= hpCon.getResponseCode();
				}

			duration = System.nanoTime() - startTime;
			if (code == 200) 
				{
					result = "Green\t";
					nbcountOK++;
				}
			else if (code == 404)
				{
					result = "->Orange<-\n" +"\t(Invalid URL)";
					nbcountKO++;
				}
			else
				{
					result = "->Orange<-\n" +"\t(HTTP Code:"+code+")";
					nbcountKO++;
				}
				
			// Now read the file
			// open the stream and put it into BufferedReader
			try {
				BufferedReader br = null;
				if (urls.toUpperCase().indexOf("HTTPS:") == -1)
					{
						HttpURLConnection hpCon = (HttpURLConnection)hp.openConnection();
						br = new BufferedReader(new InputStreamReader(hpCon.getInputStream()));
					}
				else
					{
						HttpsURLConnection hpCon = (HttpsURLConnection) hp.openConnection();
						br = new BufferedReader(new InputStreamReader(hpCon.getInputStream()));
					}

				String inputLine;
				//save to this filename
				String fileName = "result.html";
				File file = new File(fileName);

				if (!file.exists()) {
					file.createNewFile();
				}

				//use FileWriter to write file
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);

				while ((inputLine = br.readLine()) != null) {
					bw.write(inputLine);
				}

				bw.close();
				br.close();

				// System.out.println("Done");

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


			}   
            catch (Exception e) 
            {
                result = "->Red<-\t" ;
                if ( vdebug.equals("Y") ) 
					e.printStackTrace();
				nbcountKO++;
            }
            System.out.println(xx + "\t\t " + urls + "\t\tStatus:" + result + "\t" + duration/1000000000 + " sec");
        }
    }
}
