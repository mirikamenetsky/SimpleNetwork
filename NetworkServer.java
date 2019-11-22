package networkSimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class NetworkServer {

	/**
	 * server representation of a network connection
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		args = new String[] { "40000" };

		if (args.length != 1) {
			System.err.println("Usage: java NetworkServer <port number>");
			System.exit(1);
		}

		// establishes connection with I/O port
		int portNumber = Integer.parseInt(args[0]);

		try (ServerSocket serverSocket = new ServerSocket(portNumber);

				// connects client to the server
				Socket clientSocket = serverSocket.accept();

				// write response to client
				PrintWriter responseWriter = new PrintWriter(clientSocket.getOutputStream(), true);

				// reads from client
				BufferedReader requestReader = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));) {

			// initialize necessary variables
			String usersRequest;
			boolean requestDataType;
			
			//initializing an Array list that will hold a deep of created packets
			ArrayList<String> completePackets = null;
			Random random = new Random();

			// while connection is maintained with the client
			while ((usersRequest = requestReader.readLine()) != null) {

				//data type by default represents a digit
				requestDataType = false;
				String copy = usersRequest;
				
				//replace all spaces in the copy to enable data type checking
				copy = copy.replaceAll(" ", "");
				
				//create an array of copy's characters 
				char[] request = copy.toCharArray();

				// if the input includes a non-digit, the data type becomes
				// true and represents a string
				for (Character temp : request) {
					if (!Character.isDigit(temp)) {

						// the data type becomes true and represents a String
						requestDataType = true;
					}
				}

				// if the data type is not numeric
				if (requestDataType) {

					// server displays user request
					System.out.println("\"" + usersRequest + "\" received");

					// Instantiate lists to hold packet data
					ArrayList<String> packets = new ArrayList<String>();
					completePackets = new ArrayList<String>();
					
					//fill the packets array with message packets
					createPackets(random, packets);
					
					//copy packets to array
					copyPackets(packets, completePackets);

					// remove the last packet from the packets array list so it will not be
					// sent until later
					packets.remove(packets.size() - 1);

					// shuffle the packets to randomize the order in which they will be sent
					Collections.shuffle(packets);

					// attempt to send each packet
					for (int i = 0; i < packets.size(); i++) {

						// send with an 80% probability
						double send = random.nextDouble();
						if (send > .20) {
							responseWriter.println(packets.get(i));
						}

					}
					// send the last packet to indicate all previous packets were sent
					responseWriter.println(completePackets.remove(completePackets.size() - 1));
				}

				else {
					//if data is entirely numeric, separate users request by space
					String[] missingPacketsArray = usersRequest.split(" ");
					for (int i = 0; i < missingPacketsArray.length; i++) {
						int index = Integer.parseInt(missingPacketsArray[i]);
						double send = random.nextDouble();
						if (send > .20) {
							
							//send appropriate packet to user with %80 probability 
							responseWriter.println(completePackets.get(index));
						}
					}
					//send final message when attempt is complete
					responseWriter.println("Done.");

				}

			}

		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * copy packets to a new array list before shuffling
	 * 
	 * @param packets
	 * @param completePackets2
	 */
	private static void copyPackets(ArrayList<String> packets, ArrayList<String> completePackets) {
		for (int i = 0; i < packets.size(); i++) {
			completePackets.add(packets.get(i));
		}

	}

	/**
	 * creates packets to be sent to the client with the message provided
	 * 
	 * @param random
	 * @param packets
	 */
	private static void createPackets(Random random, ArrayList<String> packets) {
		// create a random number of packets ranging from 20 to 30
		int numPackets = random.nextInt((10) + 1) + 20;

		String message = "Hello there.This message should be received in full.";

		// initialize numPackets to be divisible by message length
		int remainder = 0;

		// without including the last packet, if the number of packets is not divisible,
		// update the remainder
		if (message.length() % (numPackets - 1) != 0) {
			remainder = message.length() % (numPackets - 1);
		}

		// the length of each packet will be determined by subLength
		int subLength = message.length() / (numPackets - 1);

		// start splitting the message at the string's first character
		int beginIndex = 0;

		// iterate according to the numPackets(less one)
		for (int i = 1; i < numPackets; i++) {

			// create a string builder to combine the packets metadata and data
			StringBuilder sb = new StringBuilder();

			// append meta data to the string builder
			sb.append(i + "/" + numPackets + ":");

			// if i is not the second to last packet, add data to the string builder
			if (i != numPackets - 1) {
				sb.append(message.substring(beginIndex, beginIndex + subLength));
			} else {
				switch (remainder) {
				// if the remainder is 0, add the regular quantity to the string builder
				case 0:
					sb.append(message.substring(beginIndex, beginIndex + subLength));
					break;
				// otherwise add the remainder of the message as well to the string builder
				default:
					sb.append(message.substring(beginIndex, beginIndex + subLength + remainder));
				}
			}

			// add the contents of the string builder to the list of packets
			packets.add(sb.toString());

			// increment beginIndex
			beginIndex += subLength;
		}

		// add the last packet to the list
		packets.add("Done.");

	}
}
