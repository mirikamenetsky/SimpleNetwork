package networkSimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkClient {

	/**
	 * client representation of a network connection
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		args = new String[] { "127.0.0.1", "40000" };

		if (args.length != 2) {
			System.err.println("Usage: java NetworkCliet <host name> <port number>");
			System.exit(1);
		}

		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		try (Socket clientSocket = new Socket(hostName, portNumber);
				
				//writes request to server
				PrintWriter requestWriter = new PrintWriter(clientSocket.getOutputStream(), true);
				
				//reads in response from server
				BufferedReader responseReader = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				
				//reads input(request) from user
				BufferedReader requestReader = new BufferedReader(new InputStreamReader(System.in))) {

			String userInput;
			
			//initialize an array to hold incoming packets
			String[] array = null;
			
			//initialize string builder to append missing packet to a string
			StringBuilder sb = new StringBuilder();

			//while user is interacting with the client
			while ((userInput = requestReader.readLine()) != null) {
				
				// send request to server
				requestWriter.println(userInput);
				int totalPackets = 0;
				String serverResponse;
				
				//variable to hold the last index of the substring
				int lastIndex;

				// initial read from server
				while (!(serverResponse = responseReader.readLine()).equals("Done.")) {
					
					//seperation of meta data and data
					int index = serverResponse.indexOf("/");
					lastIndex = serverResponse.indexOf(":");
					int packetNum = Integer.parseInt(serverResponse.substring(0, index));
					
					//if packet is first to be received, set total packets with meta data info
					if (totalPackets == 0) {
						totalPackets = Integer.parseInt(serverResponse.substring((index + 1), lastIndex));
						array = new String[totalPackets - 1];
					}
					
					//accept the data segment of the packet
					String message = serverResponse.substring((lastIndex + 1), serverResponse.length());
					
					//put the packet in the right index of the packet array
					array[packetNum - 1] = message;
				}

				//check to see if packets were dropped
				boolean hasNull = checkForNulls(array);
				
				//while packets are missing
				while (hasNull) {
					sb = new StringBuilder();
					
					//add the indexes of missing packets to string 
					for (int i = 0; i < array.length; i++) {
						if (array[i] == null) {
							sb.append(i + " ");

						}
					}
					
					//send string of missing packets to server
					requestWriter.println(sb.toString());
					
					//while server is not done sending packets
					while (!(serverResponse = responseReader.readLine()).equals("Done.")) {
						
						// Separate meta data from data
						int index = serverResponse.indexOf("/");
						lastIndex = serverResponse.indexOf(":");
						int packetNum = Integer.parseInt(serverResponse.substring(0, index));
						array[packetNum - 1] = serverResponse.substring(lastIndex + 1, serverResponse.length());
					}
					
					//check again for missing packets
					hasNull = checkForNulls(array);
				}
				
				//print out the final message
				for (int i = 0; i < array.length; i++) {
					System.out.print(array[i]);
				}
				
				//close the connection
				clientSocket.close();
			}

		} catch (UnknownHostException e) {
			System.err.println("Host " + hostName + " is unknown");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Counldn't get I/O for the connection to " + hostName);
			System.exit(1);
		}

	}

	/**
	 * check to see if there are any missing packets indicated by null value
	 * @param array
	 * @return
	 */
	public static boolean checkForNulls(String[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				return true;
			}
		}
		return false;
	}

}
