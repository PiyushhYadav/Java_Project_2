package server;

import common.Request;
import common.Response;
import db.StudentDAO;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final StudentDAO dao = new StudentDAO();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        System.out.println("[Server] Client connected: " + clientSocket.getInetAddress());
        try (ObjectInputStream in  = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            Request request = (Request) in.readObject();
            Response response = handleRequest(request);
            out.writeObject(response);
            out.flush();

        } catch (Exception e) {
            System.err.println("[Server] Error handling client: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
            System.out.println("[Server] Client disconnected: " + clientSocket.getInetAddress());
        }
    }

    private Response handleRequest(Request request) {
        switch (request.getAction()) {
            case ADD: {
                boolean ok = dao.addStudent(request.getStudent());
                return new Response(ok, ok ? "Student added successfully." : "Failed to add student.");
            }
            case UPDATE: {
                boolean ok = dao.updateStudent(request.getStudent());
                return new Response(ok, ok ? "Student updated successfully." : "Student not found.");
            }
            case DELETE: {
                boolean ok = dao.deleteStudent(request.getStudentId());
                return new Response(ok, ok ? "Student deleted successfully." : "Student not found.");
            }
            case GET_BY_ID: {
                var s = dao.getStudentById(request.getStudentId());
                Response r = new Response(s != null, s != null ? "Found." : "Student not found.");
                r.setStudent(s);
                return r;
            }
            case GET_ALL: {
                var list = dao.getAllStudents();
                Response r = new Response(true, "Fetched " + list.size() + " records.");
                r.setStudents(list);
                return r;
            }
            default:
                return new Response(false, "Unknown action.");
        }
    }
}