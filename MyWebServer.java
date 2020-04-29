/*--------------------------------------------------------

1. Name / Date: Ang Li/ 2/8/2020

2. Java version used, if not the official version for the class:

1.8.0_222

3. Precise command-line compilation examples / instructions:

-- javac MyWebServer.java
-- java  MyWebServer




4. Notes:
my parent directory function not really working well.

*/

import java.io.*; 
import java.net.*;


class Listen_Worker extends Thread { 
	Socket sock; 
	Listen_Worker(Socket s) {
		sock = s;
	} 	
	
	public void run() {

		PrintStream out = null;
		BufferedReader in = null;
		String file_name = null;

		try {

			System.out.println("Thread id is : " + Thread.currentThread().getId());
			// write back HTTP string to client
			out = new PrintStream(sock.getOutputStream());
			// read HTTP string from client
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// find first line of HTTP string
			String first_line = in.readLine();
			if (first_line != null) {
				// we want first token GET or HEAD, parse first line
				String[] arr_input = first_line.split(" ");
				// grab HTTP command, GET or HEAD
				String command = arr_input[0];
				// grab file path
				file_name = arr_input[1];
				System.out.println("token file name is: " + file_name);

				// check get or post
				if (!command.equals("GET")) {
					System.out.println("error reported : only process GET");
				} else {
					// get ride of first "/" and get full file name
					
					System.out.println("full file name is :" + file_name);
					if (!file_name.contains(".")) {
						file_name = file_name + "/";
						file_name = file_name.substring(1);
						System.out.println("**it's a directory and it's :" + file_name);
					}
					
					// here use to avoid error caused by .facivon.ico file
					if (file_name.endsWith(".ico")) {
						System.out.println("facivon.ico file shows up, you can keep" + "going do your next request");
					}
					// back-end process to add up user input's number and print back to client
					else if (file_name.contains("addnums")) {
						System.out.println("add up game shows up");
						do_add_up(out, file_name);
					}
					// user type a directory and end with "/"
					else if (file_name.equals("/")) {
						System.out.println("**input file is root");
						show_directory_root(out, file_name);
					
					} else if (file_name.endsWith("/")) {
						System.out.println("**input file is subdirectory and it's : " + file_name);
						show_directory(out, file_name);
					} else { // user type a file end with .txt or .html
						// get file_length
						file_name = file_name.substring(1);
						File file = new File(file_name);
						System.out.println("file is here ? :" + file.exists());

						// send HTTP header
						out.println("HTTP/1.1 200 OK");
						out.println("Content-Length: " + file.length());
						out.println("Content-type: " + coten_type(file_name));
						out.println();
						out.flush();

						// print out to check
						System.out.println("HTTP/1.1 200 OK");
						System.out.println("Content-Length: " + file.length());
						System.out.println("Content-type: " + coten_type(file_name));
						System.out.println();

						InputStream file_stream = new FileInputStream(file);
						// now, we send the tile
						send_file(file_stream, out);
						System.out.println("file sucessfully send");

					}
				}
			} // if end
			// sock.close(); 
		} catch (IOException x) {
			System.out.println("reste conn");
		}
		
	}
	


	/**
	 * back-end process to add up user input and print back to client
	 * @param out
	 * @param file_name
	 */
	private void do_add_up(PrintStream out, String file_name) {
		int num_1 = 0;
		int num_2 = 0;
		String user_name = null;
		int res = 0 ;
		//play with the string: /cgi/addnums.fake-cgi?person=Matilda&num1=4&num2=5 HTTP/1.1
		
		//since it's a dynamic HTML, we need to know the size, we use stringBuffer
		StringBuffer html_info = new StringBuffer();
		StringBuffer http_header = new StringBuffer();
		
		//grab integer value of user inputs
		num_1 = Integer.parseInt(file_name.substring(file_name.indexOf("num1")+5,file_name.indexOf("num1")+6));
		num_2 = Integer.parseInt(file_name.substring(file_name.indexOf("num2")+5,file_name.indexOf("num2")+6));
		res = num_1 + num_2;
		
		//grab user name from HTTP string
		user_name = file_name.substring(file_name.indexOf("person")+7,file_name.indexOf("&num1"));
		html_info.append("<html>");
		html_info.append("<p>Dear user: " + user_name +" -> " + num_1+ " add " +num_2 +" is "+ res+"</p>");
		html_info.append("</html>");
		System.out.println("num_1 is :" +num_1 + ", num_2 is: " + num_2+ ", and user name is : " + user_name);
		
		byte[] html_arr = html_info.toString().getBytes();
		
		//write HTTP header based on HTML file size
		http_header.append("HTTP/1.1 200 OK\r\n" + "Content-Length: " 
	    	        + html_arr.length + "\r\n" + "Content-Type: text/html \r\n" + "\r\n\r\n");
		
		out.println(http_header);
		out.println(html_info);
	}
	/**
	 * display directory when user send a root directory request
	 * @param out
	 * @param file_name
	 */
	private void show_directory_root(PrintStream out, String file_name) {
		StringBuffer html_info = new StringBuffer();
		StringBuffer http_header = new StringBuffer();
		//create root directory in windows
		File file = new File("./");
		//list all the files and directories under root
		File[] arr_files = file.listFiles();
		//get the length and we use it later for cutting string
		int len = file_name.length(); 
		
		//HTML information send to client
		html_info.append("<html>");
		html_info.append("<pre>");
		html_info.append("<h1> Index of " + file + "</h1>");
	
		html_info.append("<a href= "+file.getParent()+">"
				+"Parent Directory"+"/ </a><br>");
		
		for (int i = 0; i< arr_files.length-1;i++) {
			//if file is directory 
			if (arr_files[i].isDirectory()) {
				System.out.println("directory :" + arr_files[i]);
				System.out.println("displayed as: " + arr_files[i].getPath().substring(len)+ "/");
				//append hot link with directory name and send back to client
				html_info.append("<a href= "+arr_files[i].getPath().substring(len)+">"
				+arr_files[i].getPath().substring(len)+"/"+ "</a><br>");
			}
			else if (arr_files[i].isFile()) { //if file is file
				System.out.println("File: " + arr_files[i] + ", length: " + arr_files[i].length());
				System.out.println("displayed as: " + arr_files[i].getPath().substring(len));
				//append hot link with files name and send back to client
				html_info.append("<a href= "+arr_files[i].getPath().substring(len)+">"
						+arr_files[i].getPath().substring(len)+"</a><br>");
			}
		}
		
		//HTML end line
		html_info.append("</pre>");
		html_info.append("</html>");
		
        //to string function to find the file size of HTML file
        String directoryString = html_info.toString();
        //we need to know the file_size of 
        byte[] html_arr = html_info.toString().getBytes(); 
        
        //HTML header and send back to client
        http_header.append("HTTP/1.1 200 OK\r\n" + "Content-Length: " 
        + html_arr.length + "\r\n" + "Content-Type: text/html \r\n" + "\r\n\r\n");
        
        out.println(http_header);
        out.println(directoryString);
        out.println(); //new line 
	}
	
	/**
	 * send dynamic HTML sub_directory
	 * @param out
	 * @param file_name
	 */
	private void show_directory(PrintStream out, String file_name) {
		//Since we send HTML file, in order to find out the file size of 
		//HTML file, we need use stringBuffer here and find the file size
		//when we finish our dynamic HTML file 
		StringBuffer html_info = new StringBuffer();
		StringBuffer http_header = new StringBuffer();

		// get file based on current file path
		File file = new File("./" + file_name);
		file_name = "/" + file_name;

		System.out.println("show_dir file is : " + file_name);
		
		//list all the files and directories under sub_directory
		File[] arr_files = file.listFiles();
		//grab the length for file path and use cutting string later
		int len = file_name.length(); 
		
	
		
		html_info.append("<html>");
		html_info.append("<pre>");
		html_info.append("<h1> Index of " + file + "</h1>");
		html_info.append("<a href= "+file_name.substring(0, file_name.lastIndexOf("/"))+">"
				+"Parent Directory"+"/ </a><br>");
		
		//if this directory is not empty
		if (arr_files.length != 0) {
			for (int i = 0; i < arr_files.length; i++) {
				//if file is a directory
				if (arr_files[i].isDirectory()) {
					System.out.println("directory :" + arr_files[i]);
					System.out.println("displayed as: " + arr_files[i].getPath().substring(len) + "/");
					String add_dir = arr_files[i].getPath().substring(1);
					// append hot link with directory name and send back to client
					html_info.append("<a href= " + add_dir +   ">" + arr_files[i].getPath().substring(1) + "/"
							+ "</a><br>");
				} else if (arr_files[i].isFile()) { //if file is a file
					System.out.println("File: " + arr_files[i] + ", length: " + arr_files[i].length());
					System.out.println("displayed as: " + arr_files[i].getPath().substring(len));
					// append hot link with files name and send back to client
					html_info.append("<a href= " + arr_files[i].getPath().substring(1) + ">"
							+ arr_files[i].getPath().substring(len) + "</a><br>");
				}
			}
		}
		// HTML end line
		html_info.append("</pre>");
		html_info.append("</html>");

		// to string function to find the file size of HTML file
		String directoryString = html_info.toString();
		// we need to know the file_size of
		byte[] html_arr = html_info.toString().getBytes();

		http_header.append("HTTP/1.1 200 OK\r\n" + "Content-Length: " + html_arr.length + "\r\n" + "Content-Type: text/html \r\n" + "\r\n\r\n");
        
        out.println(http_header);
        out.println(directoryString);
        out.println();
	}
	
	/**
	 * send back file to client
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void send_file(InputStream file, OutputStream out) throws IOException {
		byte[] buf = new byte[1024];
		while (file.available() > 0) {
			out.write(buf, 0, file.read(buf));
		}
	}

	/**
	 * get file content type based on .txt or .html
	 * 
	 * @param file_name
	 * @return
	 */
	private String coten_type(String file_name) {
		if (file_name.endsWith(".html")) {
			return "text/html";
		} else
			return "text/plain";
	}
}

public class MyWebServer {

	public static boolean controlSwitch = true;

	public static void main(String a[]) throws IOException {
		int q_len = 6; /* Number of requests for OpSys to queue */
		int port = 2540;
		Socket sock;

		ServerSocket servsock = new ServerSocket(port, q_len);

		System.out.println("Ang Li's Port listener running at 2540.\n");
		while (controlSwitch) {
			// wait for the next client connection:
			sock = servsock.accept();
			new Listen_Worker(sock).start();
		}
	}
}