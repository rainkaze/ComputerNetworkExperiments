import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MultiWebServer {
    private static final int PORT = 8080;
//    private static final String WEB_ROOT = Paths.get(System.getProperty("user.dir"), "..", "webroot").normalize().toString();

    private static final String WEB_ROOT = resolveWebRoot();

    //这里是为了适配在命令行和IDEA编译器两种模式下运行该项目所产生的找不到HTML文件的错误
    private static String resolveWebRoot() {
        Path current = Paths.get(System.getProperty("user.dir"), "webroot");
        if (Files.exists(current)) {
            return current.toAbsolutePath().toString();
        }
        Path parent = Paths.get(System.getProperty("user.dir"), "..", "webroot").normalize();
        if (Files.exists(parent)) {
            return parent.toAbsolutePath().toString();
        }

        System.err.println("⚠️ 警告：未找到 webroot 目录，请检查路径设置！");
        return current.toAbsolutePath().toString();
    }


    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("服务器已启动，端口：" + PORT);
            System.out.println("网站根目录：" + WEB_ROOT);

            while (true) {
                try (
                        Socket clientSocket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        OutputStream out = clientSocket.getOutputStream()
                ) {
                    // 1. 读取HTTP请求行
                    String requestLine = in.readLine();
                    if (requestLine == null) continue;

                    System.out.println("收到请求：" + requestLine);
                    String[] requestParts = requestLine.split(" ");
                    if (requestParts.length < 2) continue;

                    String method = requestParts[0];
                    String path = requestParts[1];

                    // 2. 仅处理GET请求
                    if (method.equals("GET")) {
                        Path filePath = Paths.get(WEB_ROOT, path.replace("/", File.separator));
                        if (Files.exists(filePath)) {
                            // 3. 找到文件，返回内容
                            byte[] fileBytes = Files.readAllBytes(filePath);
                            sendResponse(out, "200 OK", "text/html", fileBytes);
                        } else {
                            // 4. 没找到文件，返回404.html内容
                            Path notFoundPath = Paths.get(WEB_ROOT, "404.html");
                            byte[] notFoundBytes = Files.exists(notFoundPath)
                                    ? Files.readAllBytes(notFoundPath)
                                    : "<h1>404 页面未找到</h1>".getBytes();
                            sendResponse(out, "404 Not Found", "text/html", notFoundBytes);
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

    // 构造HTTP响应并发送
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
