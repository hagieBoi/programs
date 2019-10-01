/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection.
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring
* the fact that the entirety of the webserver execution might be handling
* other clients, too.
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format).
*
* Author: Seth Hager
* Last Modified: September 19th, 2019
*
* Modifications made: Made it so the if a file path is requested the programs serves the file requested
* If the file doesn't exist then is shows and ERROR 404 page.
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.util.Arrays;

public class WebWorker implements Runnable
{
private static File file;
private static String contentType = "";
private static String filepath = "";
private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      readHTTPRequest(is);
      writeHTTPHeader(os,contentType);
      writeContent(os);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private void readHTTPRequest(InputStream is)
{
   String line;
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) {
      try {
        while (!r.ready()) Thread.sleep(1);
        line = r.readLine();
        System.err.println("Request line: (" + line + ")");
        if (line.length()==0) break;
        if(line.contains("GET"))handleGet(line);
      } catch (Exception e) {
         System.err.println("Request error: "+e);
         break;
      }
   }
   return;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
   System.err.println("\n" + filepath + " " + contentType +"\n");
   file = new File(filepath);
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   if(file.exists()) os.write("HTTP/1.1 200 OK\n".getBytes());
   else {os.write("HTTP/1.1 404 OK\n".getBytes());} //If file path doesn't exist then it sets the HTML code to 404
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes());
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os) throws Exception
{
   if(file.exists()){
   byte[] bFile = fileToByte(file);
   os.write(bFile);
   }else{
   //My Error 404 page
   os.write("<html><head></head><body>\n".getBytes());
   os.write("<h1>ERROR 404! Page not found!</h1>\n".getBytes());
   os.write("</body></html>\n".getBytes());
   }
}
public static byte[] fileToByte(File f) throws Exception
//OutputStreams only take binary and the file doesn't have a getByte function like String so I have to create my own.
{
	  FileInputStream fis = null;
	  byte[] bStream = new byte[(int) f.length()];
    fis = new FileInputStream(file);
    fis.read(bStream);
    fis.close();

    return bStream;
}
public static void handleGet(String request){
  String[] path = request.split(" ");
  filepath = path[1].substring(1,path[1].length());
  if(filepath.contains("gif")){contentType = "image/gif";}
  if(filepath.contains("jpeg")){contentType = "image/jpeg";}
  if(filepath.contains("png")){contentType = "image/png";}
  if(filepath.contains("html")){contentType = "text/html";}
  if(filepath.contains("css")){contentType = "text/css";}


}


} // end class
