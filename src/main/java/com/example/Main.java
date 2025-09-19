package com.example;
import com.example.dao.UserDAO;
import com.example.entity.User;
import com.example.util.HibernateUtil;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final UserDAO userDao = new UserDAO();
    private static final Scanner scanner = new Scanner(System.in);
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        try {
            logger.info(MAGENTA + "User Service приложение запущено" + RESET);
            showMenu();
        } catch (Exception e) {
            //System.err.println("Application error: " + e.getMessage());
            logger.error(RED + "Произошла ошибка: {}", e.getMessage() + RESET);
        } finally {
            HibernateUtil.shutdown();
            scanner.close();
            logger.debug("Завершение работы приложения....");
        }
    }

    private static void showMenu() {
        while (true) {
            System.out.println("\n   [Доступный функционал при работе с бд] ");
            System.out.println("пресс --> {CREATE} чтобы создать пользователя");
            System.out.println("пресс --> {FIND_ID} чтобы найти пользователя по ID");
            System.out.println("пресс --> {SHOW} чтобы показать всех пользователей");
            System.out.println("пресс --> {UPDATE} чтобы обновить пользователя");
            System.out.println("пресс --> {DELETE} чтобы Удалить пользователя");
            System.out.println("пресс --> {FIND_E} чтобы найти пользователя по email");
            System.out.println("пресс --> {EXIT} чтобы выйти");
            System.out.print("пресс --> ");
            try {
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "CREATE":
                        createUser();
                        break;
                    case "FIND_ID":
                        getUserById();
                        break;
                    case "SHOW":
                        getAllUsers();
                        break;
                    case "UPDATE":
                        updateUser();
                        break;
                    case "DELETE":
                        deleteUser();
                        break;
                    case "FIND_E":
                        getUserByEmail();
                        break;
                    case "EXIT":
                        return;
                    default:
                        logger.error(RED + "[ERROR] выбрана недоступная опция" + RESET);
                        break;
                }
            } catch (Exception e) {
                logger.error(RED + "Произошла ошибка: {}", e.getMessage() + RESET);
            }
        }
    }

    private static void createUser() {
        logger.info(CYAN + "... Create New User" + RESET);

        try {
            System.out.print("Enter user name /or nickname --> ");
            String name = scanner.nextLine().trim();

            String email = "";
            do {
                System.out.print("Enter email: ");
                email = scanner.nextLine().trim();

                if (EmailValidator.isValid(email)) {
                    logger.info(GREEN + "✓ Email корректен!" + RESET);
                    break;
                } else {
                    logger.info(RED + "✗ Неверный формат email! Пример: user@example.com" + RESET);
                }
            } while (true);

            System.out.print("Enter age: ");
            int age = Integer.parseInt(scanner.nextLine().trim());

            if (name.isEmpty() || email.isEmpty()) {
                logger.error(RED + "Name and email cannot be empty!" + RESET);
                return;
            }

            User user = new User(name, email, age);
            Optional<Long> userId = userDao.createUser(user);

            if (userId.isPresent()) {
                logger.info(GREEN + "User created successfully with ID: {}", userId.get() + RESET);
            } else {
                logger.error(RED + "Failed to create user. Email might already exist." + RESET);
            }
        } catch (NumberFormatException e) {
            logger.error(RED + "Invalid age format! Please enter a number." + RESET);
        }
    }

    private static void getUserById() {
        logger.info(CYAN + "--> Get User by ID" + RESET);

        try {
            System.out.print("Enter User ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<User> user = userDao.getUserById(id);
            if (user.isPresent()) {
                logger.info(GREEN + "User found: {}", user.get() + RESET);
            } else {
                logger.info(RED + "User not found with ID: {}", id + RESET);
            }
        } catch (NumberFormatException e) {
            logger.error(RED + "Invalid ID format!" + RESET);
        }
    }

    private static void getAllUsers() {
        logger.info(CYAN + "--> show All Users" + RESET);
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            logger.info(YELLOW + "No users found." + RESET);
        } else {
            logger.info(MAGENTA + "Total users = {}", users.size() + RESET);
            users.forEach(System.out::println);

        }
    }

    private static void updateUser() {
        logger.info("\n--> Update User");

        try {
            System.out.print("Enter User ID to update: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<User> userOpt = userDao.getUserById(id);
            if (userOpt.isEmpty()) {
                logger.info(RED + "User not found with ID: {}", id + RESET);
                return;
            }

            User user = userOpt.get();
            logger.info("Current user: {}", user);

            System.out.print("Enter new name (press Enter to keep current): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
               name = null;
            }

            System.out.print("Enter new email (press Enter to keep current): ");
            String email = "";
            do {
                System.out.print("Enter email: ");
                email = scanner.nextLine().trim();
                if (email.isEmpty()) {
                    email = null;
                }
                else if (EmailValidator.isValid(email)) {
                    logger.info(GREEN + "✓ Email корректен!" + RESET);
                    break;
                } else {
                    logger.info(RED + "✗ Неверный формат email! Пример: user@example.com" + RESET);
                }
            } while (true);

            System.out.print("Enter new age (press Enter to keep current): ");
            String ageInput = scanner.nextLine().trim();
            Integer ageInt = null;
            if (!ageInput.isEmpty()) {
                ageInt = Integer.parseInt(ageInput);
            }

            boolean success = userDao.updateUser(id, name, email, ageInt);
            if (success) {
                logger.info(GREEN + "User updated successfully." + RESET);
            } else {
                logger.error("Failed to update user.");
            }
        } catch (NumberFormatException e) {
            logger.error(RED + "Invalid number format!" + RESET);
        }
    }



    private static void deleteUser() {
        logger.info("\n --> Delete User");

        try {
            System.out.print("Enter User ID to delete: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            System.out.print("Are you sure you want to delete user with ID " + id + "? (y/N): ");
            String confirmation = scanner.nextLine().trim();

            if (confirmation.equalsIgnoreCase("y")) {
                boolean success = userDao.deleteUser(id);
                if (success) {
                    logger.info(GREEN + "User deleted successfully." + RESET);
                } else {
                    logger.error(RED + "Failed to delete user or user not found." + RESET);
                }
            } else {
                logger.info("Deletion cancelled.");
            }
        } catch (NumberFormatException e) {
            logger.info(RED + "Invalid ID format!" + RESET);
        }
    }

    private static void getUserByEmail() {
        logger.info("\n[] Find User by Email");

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        Optional<User> user = userDao.getUserByEmail(email);
        if (user.isPresent()) {
            System.out.println(BLUE + "User found:" + RESET);
            System.out.println(user.get());
        } else {
            logger.info(RED + "User not found with email: {}", email + RESET);
        }
    }
    // ANSI escape codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

}