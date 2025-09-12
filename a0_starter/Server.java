import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

class Server {
	public static void main(String args[]) throws Exception {
		if (args.length != 1) {
			System.out.println("usage: java Server port");
			System.exit(-1);
		}
		int port = Integer.parseInt(args[0]);

		ServerSocket ssock = new ServerSocket(port);
		System.out.println("listening on port " + port);
		while(true) {
			try {
				//accept a connection from the server socket
				Socket clientSocket = ssock.accept();
                System.out.println("Accepted connection from " + clientSocket);

				new Thread(new ClientHandler(clientSocket)).start();
				/*
				  YOUR CODE GOES HERE
				  - accept a connection from the server socket
				  - for each connection, read and process requests from this
				    connection repeatedly until the client closes it
				    (do this in a dedicated thread so that multiple connections
				    can be handled in parallel)
				  - for each request, compute an output and send a response
				    back to the client over the same connection
				  - each message has a 4-byte header followed by a payload
				  - the header is the length of the payload
				    (signed, two's complement, big-endian)
				  - the payload is an ASCII string
				*/
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


    static class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream in = new DataInputStream(socket.getInputStream());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                while (true) {
                    int len;
                    try {
                        len = in.readInt(); // 4-byte big-endian header
                    } catch (EOFException e) {
                        System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                        break;
                    }

                    if (len < 0) {
                        System.out.println("Invalid length: " + len);
                        break;
                    }

                    byte[] payload = new byte[len];
                    in.readFully(payload);
                    String request = new String(payload, StandardCharsets.UTF_8);

                    String response = countDistinctVertices(request);

                    byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
                    out.writeInt(respBytes.length);
                    out.write(respBytes);
                    out.flush();
                }

            } catch (IOException e) {
                System.err.println("Connection error with " + socket.getRemoteSocketAddress());
                e.printStackTrace();
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private String countDistinctVertices(String req) {
            Set<Integer> vertices = new HashSet<>();
            try (BufferedReader br = new BufferedReader(new StringReader(req))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\s+");
                    if (parts.length != 2) continue; // ignore malformed lines

                    try {
                        int u = Integer.parseInt(parts[0]);
                        int v = Integer.parseInt(parts[1]);
                        vertices.add(u);
                        vertices.add(v);
                    } catch (NumberFormatException ignored) {}
                }
            } catch (IOException ignored) {}

            return Integer.toString(vertices.size());
        }
    }
}

