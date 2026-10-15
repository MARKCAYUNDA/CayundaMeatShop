package com.scc.dao;

import com.scc.model.User;

import java.sql.*;

public class UserDao {

    public boolean registarUser(String full_name, String username, String password, String role){

        String sql = "INSERT INTO users(full_name, username, password, role, status)VALUE(?, ?, ?, 'PENDING')";

         try(Connection conn = Databaseconnection.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1,full_name);
             pstmt.setString(2,username);
             pstmt.setString(3, password);
             pstmt.setString(4,role.toUpperCase());
             pstmt.executeUpdate();
             System.out.println("->Successful registration!, Waiting For Approval!");
             return true;

         } catch (SQLException e) {
             System.err.println("Failed: "+ e.getMessage());
             return false;
         }

    }
    public UserDao(){

        try(Connection conn = Databaseconnection.getConnection();
            Statement stmt = conn.createStatement()){

            defaultAdminGeneret(conn);

        }catch(SQLException e){
            System.err.println("Error seeding default admin: " + e.getMessage());
        }

    }

    public void defaultAdminGeneret(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        String insertSql = "INSERT INTO users(full_name, username, password, role, status) VALUES('Admin','admin@gmail.com', 'admin123', 'ADMIN','APPROVED')";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.err.println("---Defult Admin Created (Username: admin | Password: admin123)");

            }
        }catch(SQLException e){
            System.err.println("Error Seeding Default: " + e.getMessage());
        }
    }
    public boolean usernameExists(String username){
        String sql ="SELECT username FROM users WHERE username = ?";

        try(Connection conn = Databaseconnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            try(ResultSet rs = pstmt.executeQuery()){
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
            return true;
        }
    }
    public User login(String username, String password){
        String sql = "SELECT user_id, full_name, username, password, role, status FROM users WHERE username = ? AND password = ?";

        try(Connection conn = Databaseconnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    String status = rs.getString("status");
                    if("PENDING".equalsIgnoreCase(status)){
                        System.out.println("-> Account is pending, Waiting for Approval");
                        return null;
                    }
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("status")
                    );
                }else {
                    System.out.println(" -> Invalid username or password");
                }
            }
        }catch (SQLException e){
            System.err.println("Error during login " + e.getMessage());
        }
        return null;
    }



}
