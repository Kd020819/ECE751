import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

class Server {
    private static final ExecutorService pool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2
    );

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("usage: java Server port");
            System.exit(-1);
        }
        int port = Integer.parseInt(args[0]);

        try (ServerSocket ssock = new ServerSocket(port)) {
            System.out.println("listening on port " + port);

            while (true) {
                Socket clientSocket = ssock.accept();
                pool.execute(new ClientHandler(clientSocket));
            }
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        private static final ThreadLocal<Set<Integer>> localSet =
                ThreadLocal.withInitial(HashSet::new);

        @Override
        public void run() {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

                while (true) {
                    int len;
                    try {
                        len = in.readInt();
                    } catch (EOFException e) {
                        break;
                    }

                    if (len < 0) break;

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
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private String countDistinctVertices(String req) {
            Set<Integer> vertices = localSet.get();
            vertices.clear();

            int len = req.length();
            int num = 0, sign = 1, count = 0;
            boolean inNumber = false;

            for (int i = 0; i < len; i++) {
                char c = req.charAt(i);

                if (c == '-' && !inNumber) {
                    sign = -1; inNumber = true; num = 0;
                } else if (Character.isDigit(c)) {
                    num = num * 10 + (c - '0');
                    inNumber = true;
                } else if (inNumber) {
                    vertices.add(sign * num);
                    count++;
                    if (count == 2) count = 0;
                    num = 0; sign = 1; inNumber = false;
                }
            }
            if (inNumber) vertices.add(sign * num);

            return Integer.toString(vertices.size());
        }
    }
}
