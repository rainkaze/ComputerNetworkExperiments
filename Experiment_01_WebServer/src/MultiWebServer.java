import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MultiWebServer {
    private static final int PORT = 8080;
    private static final String WEB_ROOT = System.getProperty("user.dir") +File.separator + "Experiment_01_WebServer" + File.separator + "src" + File.separator + "webroot";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("WebServer started on port " + PORT);
            System.out.println("Web root: " + WEB_ROOT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     OutputStream out = clientSocket.getOutputStream()) {

                    // 1. 解析HTTP请求
                    String requestLine = in.readLine();
                    if (requestLine == null) continue;

                    System.out.println("Request: " + requestLine);
                    String[] requestParts = requestLine.split(" ");
                    if (requestParts.length < 2) continue;

                    String method = requestParts[0];
                    String path = requestParts[1];

                    // 2. 处理GET请求
                    if (method.equals("GET")) {
                        Path filePath = Paths.get(WEB_ROOT, path.replace("/", File.separator));
                        if (Files.exists(filePath)) {
                            // 3. 发送文件内容
                            byte[] fileBytes = Files.readAllBytes(filePath);
                            sendResponse(out, "200 OK", "text/html", fileBytes);
                        }
                        else {
                            // 4. 文件不存在时发送404
                            String errorMsg = "<h1>404 Not Found</h1><p>File " + path + " not found.</p>";
                            sendResponse(out, "404 Not Found", "text/html", errorMsg.getBytes());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendResponse(OutputStream out, String status, String contentType, byte[] content) throws IOException {
        String response = "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "\r\n";
        out.write(response.getBytes());
        out.write(content);
        out.flush();
    }
}