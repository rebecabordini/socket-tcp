import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

class Server {

	public static String host;
	private static ServerSocket welcomeSocket;
	private static ArrayList<DataOutputStream> outToClients;
	private static ArrayList<Connection> connections;

	public static void main(String argv[]) throws Exception {
		connections = new ArrayList<Connection>();
		Integer count = 0;

		host = InetAddress.getLocalHost().getHostAddress();
		System.out.println(host);

		welcomeSocket = new ServerSocket(6789);
		outToClients = new ArrayList<DataOutputStream>();

		while (true) {

			Socket connectionSocket = welcomeSocket.accept();
			DataOutputStream outToClient = new DataOutputStream(
					connectionSocket.getOutputStream());
			outToClients.add(outToClient);
			connections.add(new Connection(connectionSocket, count++,
					outToClients, connections));
			connections.get(connections.size() - 1).start();
		}
	}
}

class Connection extends Thread {
	Integer id;
	Boolean active = false;
	Socket connectionSocket;
	String nome, host, inMessage , outMessage;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	ArrayList<DataOutputStream> outToClients;
	ArrayList<Connection> connections;

	public Connection(Socket c, Integer i, ArrayList<DataOutputStream> otc,
			ArrayList<Connection> coms) {
		connectionSocket = c;
		id = i;
		outToClients = otc;
		connections = coms;
		nome=id.toString();
	}

	public void run() {
		System.out.println("Connected with:" + id);
		active = true;
		try {
			inFromClient = new BufferedReader(new InputStreamReader(
					connectionSocket.getInputStream()));

			while (true) {
				inMessage = inFromClient.readLine();
				System.out.println("Message recieved: \"" + inMessage + "\"- from:" + id);

				if (inMessage.equals("quit")) {
					outMessage = "BYE";
					outToClients.get(id).writeBytes(outMessage);
					System.out.println("Client disconnected");
					break;

				} else if (inMessage.startsWith("@")) {
					System.out.println("send a private message to: " + inMessage.substring(1, 2));
					Integer destination = Integer.valueOf(inMessage.substring(
							1, 2));
					if (connections.size() < destination) {
						outMessage = "User:"+destination+" not found\n";
						outToClients.get(id).writeBytes(outMessage);
					}
					else if(connections.get(destination).active) {
						outMessage = "Private from:" + id + ":" + inMessage.substring(2) + "\n";
						outToClients.get(destination).writeBytes(outMessage);
					}else {
						outMessage = "User:" + destination + " not active\n";
						outToClients.get(id).writeBytes(outMessage);
					}
				} else  if (inMessage.startsWith("name:")) {
					nome = inMessage.replace("name:","");
					
					System.out.println("user "+ id +" changed name to:"+nome);
					
					Integer destination = id;
					outMessage = "Your new username is: "+nome+"\n";
					outToClients.get(destination).writeBytes(outMessage);

				} else {
					outMessage = id + ":" + inMessage + '\n';
					for (int i = 0; i < outToClients.size(); i++) {
						if (connections.get(i).active)
							outToClients.get(i).writeBytes(outMessage);
					}
				}

			}
			active = false;
			connectionSocket.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}