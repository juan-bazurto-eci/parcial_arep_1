package org.example;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase principal servidor HTTP
 */
public class HttpServer {

    public static Map<String, String> cache = new HashMap<String, String>();

    public static void main(String[] args) {
        try {
            HttpServer.start();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método principal para iniciar el servidor HTTP.
     *
     * @throws IOException Si ocurre un error al configurar o aceptar conexiones.
     */
    public static void start() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        Socket clientSocket = null;
        while (!serverSocket.isClosed()) {
            try {
                System.out.println("Operando Reflective ChatGPT...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;
            boolean firstLine = true;
            String uriString = "";
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                if (firstLine) {
                    firstLine = false;
                    uriString = inputLine.split(" ")[1];

                }
                if (!in.ready()) {
                    break;
                }
            }
            System.out.println("URI: " + uriString);
            String responseBody = "";
            if (uriString != null && uriString.equals("/")) {
                outputLine = getForm();
            } else {
                if (uriString.split("\\?")[0].equals("/consulta")) {
                    String line = uriString.split("=")[1];
                    String method = line.split("\\(")[0];
                    if(method.equals("Class")){
                        responseBody = classMethod(line);
                    }else if(method.equals("invoke")){
                        responseBody = invokeMethod(line);
                    }
                    else if(method.equals("unaryInvoke")){
                        responseBody = unaryInvokeMethod(line);
                    }else{
                        responseBody = "Método invalido";
                    }
                    outputLine = getLine(responseBody);
                } else {
                    outputLine = getIndexResponse();
                }
            }
            out.println(outputLine);
            out.close();
            in.close();
        }
        clientSocket.close();
        serverSocket.close();
    }

    /**
     * Retorna una página HTML.
     *
     * @return Respuesta HTTP con la página de inicio.
     */
    public static String getIndexResponse() {
        String response = "HTTP/1.1 200 OK"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Reflective ChatGPT</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Reflective ChatGPT</h1>\n" +
                "    </body>\n" +
                "</html>";
        return response;
    }


    public static String getForm() {
        return "HTTP/1.1 200 OK \r\n"
                + "Content-Type: text/html \r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Reflective ChatGPT</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Reflective ChatGPT</h1>\n" +
                "        <form action=\"/hello\">\n" +
                "            <label for=\"name\">Ingrese Método:</label><br>\n" +
                "            <input type=\"text\" id=\"name\" name=\"name\" value=\"Class(java.lang.String)\"><br><br>\n" +
                "            <input type=\"button\" value=\"Enviar\" onclick=\"loadGetMsg()\">\n" +
                "        </form> \n" +
                "        <div id=\"getrespmsg\"></div>\n" +
                "\n" +
                "        <script>\n" +
                "            function loadGetMsg() {\n" +
                "                let nameVar = document.getElementById(\"name\").value;\n" +
                "                const xhttp = new XMLHttpRequest();\n" +
                "                xhttp.onload = function() {\n" +
                "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "                }\n" +
                "                xhttp.open(\"GET\", \"/consulta?comando=\"+nameVar);\n" +
                "                xhttp.send();\n" +
                "            }\n" +
                "            document.getElementById(\"name\").addEventListener(\"keydown\", function(event) {\n" +
                "                if (event.key === \"Enter\") {\n" +
                "                    event.preventDefault();\n" +
                "                    loadGetMsg();\n" +
                "                }\n" +
                "            });\n" +
                "        </script>\n" +
                "\n" +
                "    </body>\n" +
                "</html>";
    }


    private static String classMethod(String line) throws ClassNotFoundException {
        StringBuilder response = new StringBuilder();
        Class c = Class.forName(line.split("\\(")[1].replaceAll("\\)",""));
        for (Method m:
        c.getMethods()) {
            response.append(m.toString()).append("\r\n").append("\n");
        }
        return response.toString();
    }

    private static String invokeMethod(String line) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class c = Class.forName(line.split("\\(")[1].split(",")[0]);
        return c.getMethod(line.split("\\(")[1].split(",")[1].replace("%20","").replaceAll("\\)","")).invoke(c.getClass()).toString();
    }

    private static String unaryInvokeMethod(String line) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        Class c = Class.forName(line.split("\\(")[1].split(",")[0]);
        if(line.split("\\(")[1].split(",")[2].replace("%20","").equals("int")){
            Integer n = Integer.parseInt(line.split("\\(")[1].split(",")[3].replace("%20","").replaceAll("\\)",""));
            System.out.println(n);
            System.out.println(c.getMethod(line.split("\\(")[1].split(",")[1].replace("%20",""), n.getClass()));
        }else if(line.split("\\(")[1].split(",")[2].replace("%20","").equals("float")){
            System.out.println();
        }
        return "";
    }


    public static String getLine(String responseBody) {
        return "HTTP/1.1 200 OK \r\n"
                + "Content-Type: text/html \r\n"
                + "\r\n"
                + "\n"
                + responseBody;
    }

    public static String getJSON(String responseJson) {
        return "HTTP/1.1 200 OK \r\n"
                + "Content-Type: application/json \r\n"
                + "\r\n"
                + responseJson;
    }
}
