import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

class Server {
    private static final int THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final ExecutorService pool = Executors.newFixedThreadPool(THREADS);


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("usage: java Server port");
            System.exit(-1);
        }
        int port = Integer.parseInt(args[0]);

        ServerSocket ssock = new ServerSocket(port);
        System.out.println("listening on port " + port);

        while (true) {
            try {
                Socket clientSocket = ssock.accept();
                clientSocket.setTcpNoDelay(true);
                pool.submit(new ClientHandler(clientSocket));
            }catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        private static final ThreadLocal<Set<Integer>> locSet =
        ThreadLocal.withInitial(() -> new HashSet<>(1 << 20));

        private static final ThreadLocal<byte[]> locBuf =
        ThreadLocal.withInitial(() -> new byte[1 << 20]);

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream(),1 << 16));
                 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(),1 << 16))) {

                while (true) {
                    int len;
                    len = in.readInt();
    
                    if (len < 0) break;

                    byte[] buf = locBuf.get();
                    if (buf.length < len) buf = new byte[len];
                    in.readFully(buf, 0, len);

                    int res = count(buf, len);

                    String to_str = Integer.toString(res);
                    out.writeInt(to_str.length());
                    out.writeBytes(to_str);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private int count(byte[] data, int len) {
            Set<Integer> vertices = locSet.get();
            vertices.clear();

            int num = 0;
            boolean inNum = false;

            for (int i = 0; i < len; i++) {
                byte b = data[i];
                if (b >= '0' && b <= '9') {
                    num = num * 10 + (b - '0');
                    inNum = true;
                } else if (inNum) {
                    vertices.add(num);
                    num = 0;
                    inNum = false;
                }
            }
            if (inNum) vertices.add(num);

            return vertices.size();
        }
    }
}
